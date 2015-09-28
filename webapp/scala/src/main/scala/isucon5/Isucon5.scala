package isucon5

import java.time.{ LocalDateTime, LocalDate }

import skinny.micro._
import skinny.micro.contrib.ScalateSupport

case object AuthenticationError extends Exception
case object PermissionDenied extends Exception
case object ContentNotFound extends Exception

/**
 *
 */
object Isucon5 extends WebApp with ScalateSupport {

  implicit object LocalDateTimeOrdering extends Ordering[LocalDateTime] {
    override def compare(x: LocalDateTime, y: LocalDateTime): Int = x.compareTo(y)
  }

  import DB._

  private lazy val prefectures = ("未入力 " +
    "北海道 青森県 岩手県 宮城県 秋田県 山形県 福島県 茨城県 栃木県 群馬県 埼玉県 千葉県 東京都 神奈川県 新潟県 富山県 " +
    "石川県 福井県 山梨県 長野県 岐阜県 静岡県 愛知県 三重県 滋賀県 京都府 大阪府 兵庫県 奈良県 和歌山県 鳥取県 島根県 " +
    "岡山県 広島県 山口県 徳島県 香川県 愛媛県 高知県 福岡県 佐賀県 長崎県 熊本県 大分県 宮崎県 鹿児島県 沖縄県").split(" ").toSeq

  before() {
    contentType = "text/html"
  }

  error {
    case AuthenticationError =>
      status = 401
      ssp("/login.ssp", "message" -> "ログインに失敗しました")
    case PermissionDenied =>
      status = 403
      ssp("/error.ssp", "message" -> "友人のみしかアクセスできません")
    case ContentNotFound =>
      status = 404
      ssp("/error.ssp", "message" -> "要求されたコンテンツは存在しません")
  }

  get("/login") {
    ssp("/login.ssp", "message" -> "高負荷に耐えられるSNSコミュニティサイトへようこそ")
  }

  post("/login") {
    authenticate(params("email"), params("password"))
    redirect303("/")
  }

  get("/logout") {
    session.remove("user_id")
    session.clear()
    redirect("/")
  }

  get("/initialize") {
    execute("DELETE FROM relations WHERE id > 500000")
    execute("DELETE FROM footprints WHERE id > 500000")
    execute("DELETE FROM entries WHERE id > 500000")
    execute("DELETE FROM comments WHERE id > 1500000")
    ssp("/error.ssp", "message" -> "initialized!")
  }

  private def authenticate(email: String, password: String): Unit = {
    executeQuery(
      s"""
         |SELECT u.id AS id, u.account_name AS account_name, u.nick_name AS nick_name, u.email AS email
         |FROM users u
         |JOIN salts s ON u.id = s.user_id
         |WHERE u.email = ? AND u.passhash = SHA2(CONCAT(?, s.salt), 512)
      """.stripMargin, email, password)(rs => rs.getInt("id")).headOption match {
        case Some(id) => session.setAttribute("user_id", id)
        case None => throw AuthenticationError
      }
  }

  private def getCurrentUser(user_id: Int): User = {
    executeQuery("SELECT id, account_name, nick_name, email FROM users WHERE id=?", user_id)(User(_)).headOption match {
      case Some(u) => u
      case None =>
        session.remove("user_id")
        throw AuthenticationError
    }
  }

  private def ensureAuthenticated[U](onSuccess: User => U): Any = {
    session.getAs[Int]("user_id") match {
      case Some(u: Int) => onSuccess(getCurrentUser(u))
      case None =>
        redirect302("/login")
    }
  }

  get("/") {
    ensureAuthenticated { current_user =>

      val profile: Profile =
        executeQuery("SELECT * FROM profiles WHERE user_id = ?", current_user.id)(Profile(_)).head

      val entries: Seq[Entry] =
        executeQuery("SELECT * FROM entries WHERE user_id = ? ORDER BY created_at LIMIT 5", current_user.id)(Entry(_))

      val comments_for_me: Seq[Comment] =
        executeQuery(
          """
            |SELECT c.id AS id, c.entry_id AS entry_id, c.user_id AS user_id, c.comment AS comment, c.created_at AS created_at
            |FROM comments c
            |JOIN entries e ON c.entry_id = e.id
            |WHERE e.user_id = ?
            |ORDER BY c.created_at DESC
            |LIMIT 10
          """.stripMargin, current_user.id)(Comment(_))

      val entries_of_friends: Seq[Entry] =
        executeQuery("SELECT * FROM entries ORDER BY created_at DESC LIMIT 1000")(Entry(_))
          .filter(entry => Friend.isFriend(current_user.id, entry.user_id))
          .take(10)

      val comments_of_friends: Seq[Comment] = (for {
        comment: Comment <- executeQuery("SELECT * FROM comments ORDER BY created_at DESC LIMIT 1000")(Comment(_)) if Friend
          .isFriend(current_user
            .id, comment
            .user_id)
        entry: Entry <- executeQuery("SELECT * FROM entries WHERE id = ?", comment.entry_id)(Entry(_)).headOption if !(entry.isPrivate &&
          !Friend.isPermitted(current_user.id, entry.user_id))
      } yield {
        comment
      }).take(10)

      val friends: Seq[Friend] =
        executeQuery(
          """
            |SELECT * FROM relations
            |WHERE one = ? OR another = ?
            |ORDER BY created_at DESC
          """.stripMargin, current_user.id, current_user.id)(Relation(_))
          .groupBy { rel => if (rel.one == current_user.id) rel.another else rel.one }
          .map { case (key: Int, rels: Seq[Relation]) => Friend(key, rels.map(_.created_at).max) }
          .toSeq

      val footprints = executeQuery(
        """
          |SELECT id, user_id, owner_id, DATE(created_at) AS date, MAX(created_at) AS updated
          |FROM footprints
          |WHERE user_id = ?
          |GROUP BY user_id, owner_id, DATE(created_at)
          |ORDER BY updated DESC
          |LIMIT 10
        """.stripMargin, current_user.id)(Footprint(_))

      ssp("/index.ssp",
        "user" -> current_user,
        "profile" -> profile,
        "entries" -> entries,
        "comments_for_me" -> comments_for_me,
        "entries_of_friends" -> entries_of_friends,
        "comments_of_friends" -> comments_of_friends,
        "friends" -> friends,
        "footprints" -> footprints)
    }
  }

  get("/profile/:account_name") {
    ensureAuthenticated { current_user =>
      val owner: User = User.fromAccountName(params("account_name"))
      val profile: Profile = executeQuery("SELECT * FROM profiles WHERE user_id = ?", owner.id)(Profile(_))
        .headOption
        .getOrElse(Profile.empty)
      val entries: Seq[Entry] = executeQuery(
        if (Friend.isPermitted(current_user.id, owner.id)) {
          "SELECT * FROM entries WHERE user_id = ? ORDER BY created_at LIMIT 5"
        } else {
          "SELECT * FROM entries WHERE user_id = ? AND private=0 ORDER BY created_at LIMIT 5"
        },
        owner.id)(Entry(_))
      Footprint.markFootprint(current_user.id, owner.id)
      ssp("/profile.ssp",
        "current_user" -> current_user,
        "owner" -> owner,
        "profile" -> profile,
        "entries" -> entries,
        "isPrivate" -> Friend.isFriend(current_user.id, owner.id),
        "prefectures" -> prefectures
      )
    }
  }

  post("/profile/:account_name") {
    ensureAuthenticated { current_user =>
      if (params("account_name") != current_user.account_name) {
        throw PermissionDenied
      }

      val args = Seq(params("first_name"), params("last_name"), params("sex"), params("birthday"), params("pref"))
      executeQuery("SELECT * FROM profiles WHERE user_id = ?", current_user.id)(Profile(_))
        .headOption match {
          case Some(profile) =>
            execute(
              """
              |UPDATE profiles
              |SET first_name=?, last_name=?, sex=?, birthday=?, pref=?, updated_at=CURRENT_TIMESTAMP()
              |WHERE user_id = ?
            """.stripMargin, (args :+ current_user.id): _*)
          case None =>
            execute("INSERT INTO profiles (user_id,first_name,last_name,sex,birthday,pref) VALUES (?,?,?,?,?,?)",
              (args :+ current_user.id): _*)
        }
      redirect303(s"/profile/${params("account_name")}")
    }
  }

  get("/diary/entries/:account_name") {
    ensureAuthenticated { current_user =>
      val owner = User.fromAccountName(params("account_name"))
      val entries = executeQuery(
        if (Friend.isPermitted(current_user.id, owner.id)) {
          "SELECT * FROM entries WHERE user_id = ? ORDER BY created_at DESC LIMIT 20"
        } else {
          "SELECT * FROM entries WHERE user_id = ? AND private=0 ORDER BY created_at DESC LIMIT 20"
        },
        owner.id)(Entry(_))
      Footprint.markFootprint(current_user.id, owner.id)
      ssp("/entries.ssp", "owner" -> owner, "entries" -> entries, "myself" -> (current_user.id == owner.id))
    }
  }

  get("/diary/entry/:entry_id") {
    ensureAuthenticated { current_user =>
      val entry: Entry = executeQuery("SELECT * FROM entries WHERE id = ?", params("entry_id"))(Entry(_)).headOption match {
        case None => throw ContentNotFound
        case Some(entry) => entry
      }
      val owner = User(entry.user_id)
      if (entry.isPrivate && !Friend.isPermitted(current_user.id, owner.id)) {
        throw PermissionDenied
      }
      val comments: Seq[Comment] = executeQuery("SELECT * FROM comments WHERE entry_id = ?", entry.id)(Comment(_))
      Footprint.markFootprint(current_user.id, owner.id)
      ssp("/entry.ssp", "owner" -> owner, "entry" -> entry, "comments" -> comments)
    }
  }

  post("/diary/entry") {
    ensureAuthenticated { current_user =>
      val body = Option(params("title")).getOrElse("タイトルなし") + "\n" + params("content")
      val isPrivate: Boolean = params.get("private").isDefined
      execute(
        "INSERT INTO entries (user_id, private, body) VALUES (?,?,?)",
        current_user.id,
        if (isPrivate) 1 else 0,
        body
      )
      redirect303(s"/diary/entries/${current_user.account_name}")
    }
  }

  post("/diary/comment/:entry_id") {
    ensureAuthenticated { current_user =>
      executeQuery("SELECT * FROM entries WHERE id = ?", params("entry_id"))(Entry(_)).headOption match {
        case None => throw ContentNotFound
        case Some(entry) =>
          if (entry.isPrivate && !Friend.isPermitted(current_user.id, entry.user_id)) {
            throw PermissionDenied
          }
          execute("INSERT INTO comments (entry_id, user_id, comment) VALUES (?, ?, ?)", entry.id, current_user.id, params("comment"))
          redirect303(s"/diary/entry/${entry.id}")
      }
    }
  }

  get("/footprints") {
    ensureAuthenticated { current_user =>
      val footprints = executeQuery(
        """
          |SELECT user_id, owner_id, DATE(created_at) AS date, MAX(created_at) as updated
          |FROM footprints
          |WHERE user_id = ?
          |GROUP BY user_id, owner_id, DATE(created_at)
          |ORDER BY updated DESC
          |LIMIT 50
        """.stripMargin, current_user.id)(Footprint(_))
      ssp("/footprints.ssp", "footprints" -> footprints)
    }
  }

  get("/friends") {
    ensureAuthenticated { current_user =>
      val friends: Seq[Friend] =
        executeQuery(
          "SELECT * FROM relations WHERE one = ? OR another = ? ORDER BY created_at DESC",
          current_user.id, current_user.id)(Relation(_))
          .groupBy { rel => if (rel.one == current_user.id) rel.another else rel.one }
          .map { case (key: Int, rels: Seq[Relation]) => Friend(key, rels.map(_.created_at).max) }
          .toSeq

      ssp("/friends.ssp", "friends" -> friends)
    }
  }

  post("/friends/:account_name") {
    ensureAuthenticated { current_user =>
      val user = User.fromAccountName(params("account_name"))
      if (!Friend.isFriend(current_user.id, user.id)) {
        execute("INSERT INTO relations (one, another) VALUES (?,?), (?, ?)", current_user.id, user.id, user.id, current_user.id)
        redirect303("/friends")
      }
    }
  }

}
