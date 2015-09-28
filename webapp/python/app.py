# -*- coding: utf-8 -*-

import os
import bottle
import pymysql


app = bottle.default_app()
app.config.load_dict({
    "db": {
        "host": os.environ.get("ISUCON5_DB_HOST") or "localhost",
        "port": int(os.environ.get("ISUCON5_DB_PORT") or 3306),
        "username": os.environ.get("ISUCON5_DB_USER") or "root",
        "password": os.environ.get("ISUCON5_DB_PASSWORD"),
        "database": os.environ.get("ISUCON5_DB_NAME") or "isucon5q",
    },
    "session_secret": os.environ.get("ISUCON5_SESSION_SECRET") or "beermoris",
})


PREFECTURES = [
    "未入力",
    "北海道", "青森県", "岩手県", "宮城県", "秋田県", "山形県", "福島県", "茨城県", "栃木県", "群馬県", "埼玉県", "千葉県", "東京都", "神奈川県", "新潟県", "富山県",
    "石川県", "福井県", "山梨県", "長野県", "岐阜県", "静岡県", "愛知県", "三重県", "滋賀県", "京都府", "大阪府", "兵庫県", "奈良県", "和歌山県", "鳥取県", "島根県",
    "岡山県", "広島県", "山口県", "徳島県", "香川県", "愛媛県", "高知県", "福岡県", "佐賀県", "長崎県", "熊本県", "大分県", "宮崎県", "鹿児島県", "沖縄県",
]


def abort_authentication_error():
    set_session_user_id(None)
    response = bottle.HTTPResponse(status=401, body=bottle.template("login", {"message": "ログインに失敗しました"}))
    response.add_header("WWW-Authenticate", 'Login realm="hi"')
    raise response


def abort_permission_denied():
    raise bottle.HTTPResponse(status=403, body=bottle.template("error", {"message": "友人のみしかアクセスできません"}))


def abort_content_not_found():
    raise bottle.HTTPResponse(status=404, body=bottle.template("error", {"message": "要求されたコンテンツは存在しません"}))


def get_session_user_id():
    try:
        return bottle.request.get_cookie("user_id", secret=app.config["session_secret"])
    except ValueError:
        set_session_user_id(None)
        return None


def set_session_user_id(user_id):
    bottle.response.set_cookie("user_id", user_id, secret=app.config["session_secret"])


def db():
    try:
        return bottle.local.db
    except AttributeError:
        bottle.local.db = pymysql.connect(
            host=app.config["db.host"],
            port=app.config["db.port"],
            user=app.config["db.username"],
            password=app.config["db.password"],
            db=app.config["db.database"],
            charset="utf8mb4",
            autocommit=True,
            cursorclass=pymysql.cursors.DictCursor)
        return bottle.local.db


def db_fetchone(query, *args):
    args = args if args else None
    with db().cursor() as cursor:
        cursor.execute(query, args)
        return cursor.fetchone()


def db_fetchall(query, *args):
    args = args if args else None
    with db().cursor() as cursor:
        cursor.execute(query, args)
        return cursor.fetchall()


def db_execute(query, *args):
    args = args if args else None
    with db().cursor() as cursor:
        cursor.execute(query, args)
    db().commit()


def authenticate(email, password):
    query = "SELECT u.id AS id, u.account_name AS account_name, " \
            " u.nick_name AS nick_name, u.email AS email " \
            "FROM users u JOIN salts s ON u.id = s.user_id " \
            "WHERE u.email = %s AND u.passhash = SHA2(CONCAT(%s, s.salt), 512)"
    result = db_fetchone(query, email, password)
    if not result:
        abort_authentication_error()
    set_session_user_id(result["id"])


def current_user():
    try:
        return bottle.request.user
    except AttributeError:
        user_id = get_session_user_id()
        if user_id:
            query = "SELECT id, account_name, nick_name, email FROM users WHERE id = %s"
            bottle.request.user = db_fetchone(query, get_session_user_id())
            if not bottle.request.user:
                set_session_user_id(None)
                abort_authentication_error()
        else:
            bottle.request.user = None
        return bottle.request.user


def authenticated():
    if not current_user():
        bottle.redirect("/login", 302)


def get_user(user_id):
    query = "SELECT * FROM users WHERE id = %s"
    result = db_fetchone(query, user_id)
    if not result:
        abort_content_not_found()
    return result


def user_from_account(account_name):
    query = "SELECT id FROM users WHERE account_name = %s"
    result = db_fetchone(query, account_name)
    if not result:
        abort_content_not_found()
    return result


def is_friend(another_id):
    user_id = get_session_user_id()
    if not user_id:
        return False
    query = "SELECT COUNT(1) AS cnt FROM relations WHERE (one = %s AND another = %s) OR (one = %s AND another = %s)"
    result = db_fetchone(query, user_id, another_id, another_id, user_id)
    return result and result["cnt"] > 0


def is_friend_account(account_name):
    return is_friend(user_from_account(account_name)["id"])


def permitted(another_id):
    return another_id == current_user()["id"] or is_friend(another_id)


def mark_footprint(user_id):
    if user_id != current_user()["id"]:
        query = "INSERT INTO footprints (user_id, owner_id) VALUES (%s, %s)"
        db_execute(query, user_id, current_user()["id"])


@app.get("/login")
def get_login():
    set_session_user_id(None)
    return bottle.template("login", {"message": "高負荷に耐えられるSNSコミュニティサイトへようこそ!"})


@app.post("/login")
def post_login():
    email = bottle.request.forms.getunicode("email")
    password = bottle.request.forms.getunicode("password")
    authenticate(email, password)
    bottle.redirect("/", 303)


@app.get("/logout")
def get_logout():
    set_session_user_id(None)
    bottle.redirect("/login", 303)


@app.get("/")
def get_index():
    authenticated()
    
    profile = db_fetchone("SELECT * FROM profiles WHERE user_id = %s", current_user()["id"])
    
    query = "SELECT * FROM entries WHERE user_id = %s ORDER BY created_at LIMIT 5"
    entries = db_fetchall(query, current_user()["id"])
    for entry in entries:
        entry["is_private"] = entry["private"] == 1
        entry["title"], entry["content"] = entry["body"].split("\n", 1)
    
    comments_for_me_query = "SELECT c.id AS id, c.entry_id AS entry_id, c.user_id AS user_id, " \
                            " c.comment AS comment, c.created_at AS created_at " \
                            "FROM comments c JOIN entries e ON c.entry_id = e.id " \
                            "WHERE e.user_id = %s ORDER BY c.created_at DESC LIMIT 10"
    comments_for_me = db_fetchall(comments_for_me_query, current_user()["id"])
    
    entries_of_friends = []
    with db().cursor() as cursor:
        cursor.execute("SELECT * FROM entries ORDER BY created_at DESC LIMIT 1000")
        for entry in cursor:
            if not is_friend(entry["user_id"]):
                continue
            entry["title"] = entry["body"].split("\n")[0]
            entries_of_friends.append(entry)
            if len(entries_of_friends) >= 10:
                break
    
    comments_of_friends = []
    with db().cursor() as cursor:
        cursor.execute("SELECT * FROM comments ORDER BY created_at DESC LIMIT 1000")
        for comment in cursor:
            if not is_friend(comment["user_id"]):
                continue
            entry = db_fetchone("SELECT * FROM entries WHERE id = %s", comment["entry_id"])
            entry["is_private"] = (entry["private"] == 1)
            if entry["is_private"] and not permitted(entry["user_id"]):
                continue
            comments_of_friends.append(comment)
            if len(comments_of_friends) >= 10:
                break
    
    friends_map = {}
    with db().cursor() as cursor:
        cursor.execute("SELECT * FROM relations WHERE one = %s OR another = %s ORDER BY created_at DESC",
                       args=(current_user()["id"], current_user()["id"]))
        for relation in cursor:
            key = "another" if relation["one"] == current_user()["id"] else "one"
            friends_map.setdefault(relation[key], relation["created_at"])
    friends = list(friends_map.items())
    
    query = "SELECT user_id, owner_id, DATE(created_at) AS date, MAX(created_at) AS updated " \
            "FROM footprints " \
            "WHERE user_id = %s " \
            "GROUP BY user_id, owner_id, DATE(created_at) " \
            "ORDER BY updated DESC " \
            "LIMIT 10"
    footprints = db_fetchall(query, current_user()["id"])
    
    return bottle.template("index", {
      "owner": current_user(),
      "profile": profile or {},
      "entries": entries,
      "comments_for_me": comments_for_me,
      "entries_of_friends": entries_of_friends,
      "comments_of_friends": comments_of_friends,
      "friends": friends,
      "footprints": footprints,
    })


@app.get("/profile/<account_name>")
def get_profile(account_name):
    authenticated()
    owner = user_from_account(account_name)
    prof = db_fetchone("SELECT * FROM profiles WHERE user_id = %s", owner["id"])
    prof = prof or {}
    if permitted(owner["id"]):
        query = "SELECT * FROM entries WHERE user_id = %s ORDER BY created_at LIMIT 5"
    else:
        query = "SELECT * FROM entries WHERE user_id = %s AND private = 0 ORDER BY created_at LIMIT 5"
    entries = db_fetchall(query, owner["id"])
    for entry in entries:
        entry["is_private"] = entry["private"] == 1
        entry["title"], entry["content"] = entry["body"].split("\n", 1)
    mark_footprint(owner["id"])
    return bottle.template("profile", {
        "owner": get_user(owner["id"]),
        "profile": prof,
        "entries": entries,
        "private": permitted(owner["id"]),
    })


@app.post("/profile/<account_name>")
def post_profile(account_name):
    authenticated()
    if account_name != current_user()["account_name"]:
        abort_permission_denied()
    args = [bottle.request.forms.getunicode("first_name"),
            bottle.request.forms.getunicode("last_name"),
            bottle.request.forms.getunicode("sex"),
            bottle.request.forms.getunicode("birthday"),
            bottle.request.forms.getunicode("pref")]
    prof = db_fetchone("SELECT * FROM profiles WHERE user_id = %s", current_user()["id"])
    if prof:
        query = "UPDATE profiles " \
                "SET first_name = %s, last_name = %s, sex = %s, birthday = %s, pref = %s, updated_at = CURRENT_TIMESTAMP() " \
                "WHERE user_id = %s"
        args.append(current_user()["id"])
    else:
        query = "INSERT INTO profiles (user_id, first_name, last_name, sex, birthday, pref) VALUES (%s, %s, %s, %s, %s, %s)"
        args.insert(0, current_user()["id"])
    db_execute(query, *args)
    bottle.redirect("/profile/%s" % account_name, 303)


@app.get("/diary/entries/<account_name>")
def get_entries(account_name):
    authenticated()
    owner = user_from_account(account_name)
    if permitted(owner["id"]):
      query = "SELECT * FROM entries WHERE user_id = %s ORDER BY created_at DESC LIMIT 20"
    else:
      query = "SELECT * FROM entries WHERE user_id = %s AND private = 0 ORDER BY created_at DESC LIMIT 20"
    entries = db_fetchall(query, owner["id"])
    for entry in entries:
        entry["is_private"] = entry["private"] == 1
        entry["title"], entry["content"] = entry["body"].split("\n", 1)
    mark_footprint(owner["id"])
    return bottle.template("entries", {
        "owner": get_user(owner["id"]),
        "entries": entries, 
        "myself": current_user()["id"] == owner["id"],
    })


@app.get("/diary/entry/<entry_id>")
def get_entry(entry_id):
    authenticated()
    entry = db_fetchone("SELECT * FROM entries WHERE id = %s", int(entry_id))
    if not entry:
        abort_content_not_found()
    entry["is_private"] = (entry["private"] == 1)
    entry["title"], entry["content"] = entry["body"].split("\n", 1)
    owner = get_user(entry["user_id"])
    if entry["is_private"] and not permitted(owner["id"]):
        abort_permission_denied()
    comments = db_fetchall("SELECT * FROM comments WHERE entry_id = %s", entry["id"])
    mark_footprint(owner["id"])
    return bottle.template("entry", {"owner": owner, "entry": entry, "comments": comments})


@app.post("/diary/entry")
def post_entry():
    authenticated()
    query = "INSERT INTO entries (user_id, private, body) VALUES (%s, %s, %s)"
    body = (bottle.request.forms.getunicode("title", "") or "タイトルなし") + "\n" + bottle.request.forms.getunicode("content", "")
    db_execute(query,
               current_user()["id"],
               1 if bottle.request.forms.getunicode("private") else 0,
               body)
    bottle.redirect("/diary/entries/%s" % current_user()["account_name"], 303)


@app.post("/diary/comment/<entry_id>")
def post_comment(entry_id):
    authenticated()
    entry = db_fetchone("SELECT * FROM entries WHERE id = %s", int(entry_id))
    if not entry:
        abort_content_not_found()
    entry["is_private"] = (entry["private"] == 1)
    if entry["is_private"] and not permitted(entry["user_id"]):
        abort_permission_denied()
    query = "INSERT INTO comments (entry_id, user_id, comment) VALUES (%s, %s, %s)"
    db_execute(query, entry["id"], current_user()["id"], bottle.request.forms.getunicode("comment"))
    bottle.redirect("/diary/entry/%s" % entry["id"], 303)


@app.get("/footprints")
def get_footprints():
    authenticated()
    query = "SELECT user_id, owner_id, DATE(created_at) AS date, MAX(created_at) AS updated " \
            "FROM footprints " \
            "WHERE user_id = %s " \
            "GROUP BY user_id, owner_id, DATE(created_at) " \
            "ORDER BY updated DESC " \
            "LIMIT 50"
    footprints = db_fetchall(query, current_user()["id"])
    return bottle.template("footprints", {"footprints": footprints})


@app.get("/friends")
def get_friends():
    authenticated()
    query = "SELECT * FROM relations WHERE one = %s OR another = %s ORDER BY created_at DESC"
    relations = db_fetchall(query, current_user()["id"], current_user()["id"])
    friends_map = {}
    for relation in relations:
        key = "another" if relation["one"] == current_user()["id"] else "one"
        friends_map.setdefault(relation[key], relation["created_at"])
    friends = list(friends_map.items())
    return bottle.template("friends", {"friends": friends})


@app.post("/friends/<account_name>")
def post_friends(account_name):
    authenticated()
    if not is_friend_account(account_name):
        user = user_from_account(account_name)
        db_execute("INSERT INTO relations (one, another) VALUES (%s, %s), (%s, %s)",
                   current_user()["id"], user["id"], user["id"], current_user()["id"])
    bottle.redirect("/friends", 303)


@app.get("/css/<filename:path>")
def get_css(filename):
    return get_static("css", filename)


@app.get("/fonts/<filename:path>")
def get_fonts(filename):
    return get_static("fonts", filename)


@app.get("/js/<filename:path>")
def get_js(filename):
    return get_static("js", filename)


def get_static(dirname, filename):
    basedir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    staticdir = os.path.join(basedir, "static", dirname)
    return bottle.static_file(filename, root=staticdir)


@app.get("/initialize")
def get_initialize():
    db_execute("DELETE FROM relations WHERE id > 500000")
    db_execute("DELETE FROM footprints WHERE id > 500000")
    db_execute("DELETE FROM entries WHERE id > 500000")
    db_execute("DELETE FROM comments WHERE id > 1500000")
    return ""


bottle.BaseTemplate.defaults = {
    "db": db,
    "db_fetchone": db_fetchone,
    "get_user": get_user,
    "is_friend": is_friend,
    "current_user": current_user,
    "prefectures": PREFECTURES,
}

if __name__ == "__main__":
    app.run(server="wsgiref",
            host="127.0.0.1",
            port=8080,
            reloader=True,
            quiet=False,
            debug=True)
