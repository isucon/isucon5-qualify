require 'mysql2'

srand(941) # to create the same data

def db
  return @client if @client
  @client = Mysql2::Client.new(
    host: 'localhost',
    port: nil,
    username: 'root',
    password: nil,
    database: 'isucon5q',
    reconnect: true,
  )
  @client.query_options.merge!(symbolize_keys: true)
  @client
end

# user: { info: [account_name, nick_name, email, password, first_name, last_name],
#         entries: [ [ entry_sym, true/false(private), title, body ], ... ],
#         comments: [ [ entry_sym, comment ], ... ],
#         footprints: [ who, who, who, who ],
#         relations: [ who, who, who, who ],
#       },

def main()
  users = {} # sym => user_id
  entries = {} # sym => entry_id

  tree = {
    u1: {
      info: ['tagomoris', 'モリス', 'moris@tagomor.is', 'moris11', 'さとし', 'たごもり'],
      entries: [
        [ :e1_1, false, 'はじめました', 'これがうわさのアレか!' ],
        [ :e1_2, false, 'つづき', "いやっほおおおおおおおお\nおおおおおおおおおおおおおおおおおおおおおおおおおおおおおう" ],
        [ :e1_3, true,  'ひみつ', 'まじもう仕事がこんなことになっていようとはと思ってたら予選なしにするんだった! まじで!' ],
        [ :e1_4, false, 'ビールのんだ', 'うまああああああああああああああああああああああああああああああああああああああああああああああああああああい!' ],
      ],
      comments: [
        [:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],
        [:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],
        [:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],
        [:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],
        [:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],
        [:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],
        [:e2_9, 'おつ'],[:e2_9, 'おつ'],[:e2_9, 'おつ'],
      ],
      footprints: [:u2, :u2],
      relations: [:u2],
    },
    u2: {
      info: ['kamipo', 'かみぽ', 'kamipo@kamipopo.po', 'po1', 'りゅうた', 'かみぞの'],
      entries: [
        [ :e2_1, false, 'いち', 'いち' * 5 ],
        [ :e2_2, false, 'に', 'にー' * 5 ],
        [ :e2_3, false, 'さん', 'さん' * 5 ],
        [ :e2_4, false, 'しい', 'しいいいいい' * 5 ],
        [ :e2_5, false, 'ごごご', 'ごごごごご' * 5 ],
        [ :e2_6, false, 'ろくう', "ろく\n" * 5 ],
        [ :e2_7, false, 'ななななな', "なな" * 5 ],
        [ :e2_8, false, 'はち', "はち？\n" * 5 ],
        [ :e2_9, false, 'きゅう', "くくくくくく………\n" * 5 ],
      ],
      comments: [ [:e1_1, "1ゲット"], [:e1_3, "ねるねるねるね"], ],
      footprints: [:u1],
      relations: [:u1],
    },
    u3: {
      info: ['aaaaaaa', 'えい', 'aaaa@a.com', 'a1a1', 'えいいち', 'えいわ'],
      entries: [
        [ :e3_1, false, 'はじめました!', 'わくわくしています!!!!' ],
        [ :e3_2, false, 'ともだちがいっぱい', "うれしいです!\nみんなぜひコメントを残してください!" ],
        [ :e3_3, true, 'あれー', "なんでみんな友だちになれたのにコメント残してくれないの？\nいっぱい足あとついてるのに？\nなんで？？？？？？" ]
      ],
      comments: [
        [:e1_1,'よろしくおねがいします!'],[:e1_2,'よろしくおねがいします!'],[:e1_4,'よろしくおねがいします!'],[:e2_1,'よろしくおねがいします!'],
        [:e2_2,'よろしくおねがいします!'],[:e4_1,'よろしくおねがいします!'],[:e4_2,'よろしくおねがいします!'],[:e4_3,'よろしくおねがいします!'],
        [:e4_4,'よろしくおねがいします!'],[:e4_5,'よろしくおねがいします!'],[:e4_6,'よろしくおねがいします!'],[:e3_3,'もうだめだ'],
      ],
      footprints: [:u1,:u2,:u4,:u5,:u6,:u1,:u2,:u4,:u5,:u6,:u1,:u2,:u4,:u5,:u6,:u1,:u2,:u4,:u5,:u6],
      relations: [:u1,:u2,:u4,:u5,:u6],
    },
    u4: {
      info: ['bbbbbbb', 'b', 'b@b.net', 'bb', 'もう', 'つかれてきた'],
      entries: (1..30).map{|i| [ "e4_#{i}".to_sym, (i % 5 == 0), i.to_s, i.to_s * i ] },
      comments: [],
      footprints: [],
      relations: [],
    },
    u5: {
      info: ['ccccc', 'ＣＣＣＣＣＣ', 'ccc@c.net', 'c?cx', 'まじねむい', 'もうだめ'],
      entries: [],
      comments: [],
      footprints: [:u1,:u2,:u2,:u1,:u4,:u6,:u4,:u1,:u6],
      relations: [],
    },
    u6: {
      info: ['ddd666', 'でぃーろく', 'ddddddddd@roku.com', 'ddd666', 'あとちょっと', 'たろう'],
      entries: [],
      comments: [],
      footprints: [],
      relations: [],
    }
  }

  tree.each_pair do |user_sym, data|
    id = create_user(data[:info][0], data[:info][1], data[:info][2], data[:info][3])
    users[user_sym] = id
    create_profile(id, data[:info][4], data[:info][5])
    data[:entries].each do |entry|
      entry_id = create_entry(id, entry[1], entry[2], entry[3])
      entries[entry[0]] = entry_id
    end
  end
  tree.each_pair do |user_sym, data|
    current_id = users[user_sym]
    data[:comments].each do |c|
      add_comment(entries[c[0]], current_id, c[1])
    end
    data[:footprints].each do |f|
      add_footprint(users[f], current_id)
    end
    data[:relations].each do |r|
      make_relation(users[r], current_id)
    end
  end
end

def create_user(account_name, nick_name, email, password)
  c = db()
  begin
    c.query('begin')
    q1 = "INSERT INTO users (account_name,nick_name,email,passhash) VALUES ('#{account_name}','#{nick_name}','#{email}','')"
    c.query(q1)
    user_id = c.last_id

    q2 = "INSERT INTO salts (user_id,salt) VALUES (?,FLOOR(MICROSECOND(NOW(6)) * RAND()))"
    c.prepare(q2).execute(user_id)
    q3 = "UPDATE users u JOIN salts s ON u.id=s.user_id SET passhash=SHA2(CONCAT(?, s.salt), 512) WHERE id=?"
    c.prepare(q3).execute(password, user_id)
    c.query("commit")
    user_id # user_id
  ensure
    c.query("rollback")
  end
end

def create_profile(user_id, first, last)
  q = 'INSERT INTO profiles (user_id,first_name,last_name,sex,birthday,pref) VALUES (?,?,?,?,?,?)'
  db.prepare(q).execute(
    user_id,
    first,
    last,
    choose_sex(),
    create_birthday(),
    choose_pref()
  )
  user_id
end

def create_entry(user_id, private, title, body)
  q = 'INSERT INTO entries (user_id, private, body, created_at) VALUES (?,?,?,?)'
  db.prepare(q).execute(user_id, (private ? '1' : '0'), title + "\n" + body, random_timestamp())
  db.last_id # entry_id
end

def add_comment(entry_id, user_id, comment)
  q = 'INSERT INTO comments (entry_id, user_id, comment, created_at) VALUES (?,?,?,?)'
  db.prepare(q).execute(entry_id, user_id, comment, random_timestamp())
  nil
end

def add_footprint(user_id, owner_id)
  q = 'INSERT INTO footprints (user_id, owner_id, created_at) VALUES (?,?,?)'
  db.prepare(q).execute(user_id, owner_id, random_timestamp())
  nil
end

def make_relation(one_id, another_id)
  if db.prepare('SELECT * FROM relations WHERE one=? AND another=?').execute(one_id,another_id).first
    return
  end
  q = 'INSERT INTO relations (one, another, created_at) VALUES (?,?,?), (?,?,?)'
  at = random_timestamp()
  db.prepare(q).execute(one_id, another_id, at, another_id, one_id, at)
  nil
end

PREFS = %w(
  北海道 青森県 岩手県 宮城県 秋田県 山形県 福島県 茨城県 栃木県 群馬県 埼玉県 千葉県 東京都 神奈川県 新潟県 富山県
  石川県 福井県 山梨県 長野県 岐阜県 静岡県 愛知県 三重県 滋賀県 京都府 大阪府 兵庫県 奈良県 和歌山県 鳥取県 島根県
  岡山県 広島県 山口県 徳島県 香川県 愛媛県 高知県 福岡県 佐賀県 長崎県 熊本県 大分県 宮崎県 鹿児島県 沖縄県
)

def choose_pref
  PREFS[rand(PREFS.size)]
end

SEXES = %w(男性 女性 男性 女性 その他)

def choose_sex
  SEXES[rand(SEXES.size)]
end

require 'time'
BIRTH_TIME_MIN = Time.parse('1970-01-01 00:00:00').to_i
BIRTH_TIME_MAX = Time.parse('2014-12-31 23:59:59').to_i
BIRTH_TIME_RANGE = Range.new(BIRTH_TIME_MIN, BIRTH_TIME_MAX)

def create_birthday
  Time.at(rand(BIRTH_TIME_RANGE)).strftime('%Y-%m-%d')
end

TIMESTAMP_MIN = Time.parse('2013-09-17 10:00:00').to_i
TIMESTAMP_MAX = Time.parse('2015-09-17 01:20:30').to_i
TIMESTAMP_RANGE = Range.new(TIMESTAMP_MIN, TIMESTAMP_MAX)

def random_timestamp
  Time.at(rand(TIMESTAMP_RANGE)).strftime('%Y-%m-%d %H:%M:%S')
end

main()
