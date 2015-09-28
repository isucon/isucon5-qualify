package isucon5

import java.sql._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.{TimeZone, Calendar}

import org.slf4j.LoggerFactory

/**
 *
 */
object DB {

  private val logger = LoggerFactory.getLogger("isucon5.DB")
  val cal = Calendar.getInstance(TimeZone.getDefault)
  val df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  private implicit class RichResultSet(rs:ResultSet) {
    def getLocalDateTime(colName:String) = rs.getTimestamp(colName, cal).toLocalDateTime
  }

  // database configuration
  case class DBConfig(host: String,
                      port: Int,
                      user: String,
                      password: Option[String],
                      name: String,
                      jdbcDriverName: String,
                      jdbcProperties: Map[String, String]
                       ) {
    lazy val jdbcUrl = {
      var props = jdbcProperties + ("user" -> user)
      password.map(p => props += "password" -> p)
      s"jdbc:mysql://${host}:${port}/${name}?${props.map { case (k, v) => s"${k}=${v}" }.mkString("&")}"
    }
  }

  private def env = System.getenv()

  lazy val dbConfig: DBConfig = DBConfig(
    host = env.getOrDefault("ISUCON5_DB_HOST", "localhost"),
    port = env.getOrDefault("ISUCON5_DB_PORT", "3306").toInt,
    user = env.getOrDefault("ISUCON5_DB_USER", "root"),
    password = Option(System.getenv.get("ISUCON5_DB_PASSWORD")),
    name = env.getOrDefault("ISUCON5_DB_HOST", "isucon5q"),
    jdbcDriverName = "com.mysql.jdbc.Driver",
    jdbcProperties = Map(
      "useUnicode" -> "true",
      "characterEncoding" -> "utf8"
    )
  )

  // Model classes
  case class User(id: Int, account_name: String, nick_name: String, email: String)

  object User {
    def apply(rs: ResultSet): User = User(rs.getInt("id"), rs.getString("account_name"), rs.getString("nick_name"), rs.getString("email"))
    def apply(user_id: Int): User = {
      executeQuery("SELECT * FROM users WHERE id = ?", user_id)(User(_)).headOption match {
        case Some(user) => user
        case None => throw ContentNotFound
      }
    }
    def fromAccountName(accountName: String): User = {
      executeQuery("SELECT * FROM users WHERE account_name = ?", accountName)(User(_)).headOption match {
        case Some(user) => user
        case None => throw ContentNotFound
      }
    }
  }

  case class Salt(user_id: Int, salt: String)
  object Salt {
    def apply(rs: ResultSet): Salt = Salt(rs.getInt("id"), rs.getString("salt"))
  }

  case class Relation(id: Int, one: Int, another: Int, created_at: LocalDateTime)
  object Relation {
    def apply(rs: ResultSet): Relation =
      Relation(
        rs.getInt("id"),
        rs.getInt("one"),
        rs.getInt("another"),
        rs.getLocalDateTime("created_at")
      )
  }

  case class Profile(user_id: Int,
                     first_name: Option[String],
                     last_name: Option[String],
                     sex: Option[String],
                     birthday: Option[Date],
                     pref: Option[String],
                     updated_at: LocalDateTime) {
    def isMale: Boolean = sex.isDefined && sex == "男性"
    def isFemale: Boolean = sex.isDefined && sex == "女性"
  }
  object Profile {
    def empty = Profile(0, None, None, None, None, None, LocalDateTime.parse("2000-01-01"))
    def apply(rs: ResultSet): Profile =
      Profile(rs.getInt("user_id"),
        Option(rs.getString("first_name")),
        Option(rs.getString("last_name")),
        Option(rs.getString("sex")),
        Option(rs.getDate("birthday")),
        Option(rs.getString("pref")),
        rs.getLocalDateTime("updated_at")
      )
  }

  case class Entry(id: Int, user_id: Int, `private`: Int, body: String, created_at: LocalDateTime) {
    def summaryLines: Seq[String] = Option(body).map(s => s.substring(0, math.min(60, s.length)).split("\n").toSeq).getOrElse(Seq.empty)
    def headline = Option(body).map(_.split("\n").head).getOrElse("")
    def title = headline
    def isPrivate = `private` == 1
    lazy val content: String = {
      val lines = body.split("\n")
      lines.drop(1).mkString("\n")
    }
    def contentLines = content.split("\n")
    def createdTime = created_at.format(df)
  }
  object Entry {
    def apply(rs: ResultSet): Entry =
      Entry(rs.getInt("id"),
        rs.getInt("user_id"),
        rs.getInt("private"),
        rs.getString("body"),
        rs.getLocalDateTime("created_at")
      )

    def apply(id: Int): Entry = {
      executeQuery("SELECT * FROM entries WHERE id=?", id)(Entry(_)).head
    }
  }

  case class Comment(id: Int, entry_id: Int, user_id: Int, comment: String, created_at: LocalDateTime) {
    def lines: Seq[String] = comment.split("\n").toSeq
    def createdTime = created_at.format(df)
  }
  object Comment {
    def apply(rs: ResultSet): Comment =
      Comment(
        rs.getInt("id"),
        rs.getInt("entry_id"),
        rs.getInt("user_id"),
        rs.getString("comment"),
        rs.getLocalDateTime("created_at")
      )
  }

  case class Footprint(user_id: Int, owner_id: Int, updated: LocalDateTime) {
    def updatedTime = updated.format(df)
  }
  object Footprint {
    def apply(rs: ResultSet): Footprint = Footprint(
      rs.getInt("user_id"),
      rs.getInt("owner_id"),
      rs.getLocalDateTime("updated"))
    def markFootprint(current_user_id: Int, target_id: Int) = {
      if (target_id != current_user_id) {
        execute("INSERT INTO footprints (user_id,owner_id) VALUES(?,?)", target_id, current_user_id)
      }
    }
  }

  case class Friend(user_id: Int, created_at: LocalDateTime) {
    def createdAt = created_at.format(df)
  }

  object Friend {
    def isFriend(user_id: Int, another_id: Int): Boolean = {
      val cnt = executeQuery(
        "SELECT COUNT(1) AS cnt FROM relations WHERE (one = ? AND another = ?) OR (one = ? AND another = ?)",
        user_id, another_id, another_id, user_id
      )(_.getInt("cnt")).head
      cnt > 0
    }
    def isPermitted(current_user_id: Int, another_id: Int): Boolean = {
      another_id == current_user_id || isFriend(current_user_id, another_id)
    }
  }


  // Query execution helper methods
  def withResource[Resource <: AutoCloseable, U](resource: Resource)(body: Resource => U): U = {
    try {
      body(resource)
    }
    finally {
      resource.close()
    }
  }

  def executeQuery[A](sql: String, args: Any*)(resultMapper: ResultSet => A): Seq[A] = {
    executeSQL(sql, args: _*) { st =>
      val rs = st.executeQuery
      val b = Seq.newBuilder[A]
      while (rs.next()) {
        b += resultMapper(rs)
      }
      b.result()
    }
  }

  def execute[A](sql: String, args: Any*): Unit = {
    executeSQL(sql, args: _*) { st =>
      st.execute
    }
  }

  private def executeSQL[A](sql: String, args: Any*)(handler: PreparedStatement => A): A = {
    Class.forName(dbConfig.jdbcDriverName)
    withResource(DriverManager.getConnection(dbConfig.jdbcUrl)) { conn =>
      withResource(conn.prepareStatement(sql)) { st =>
        // populate the placeholders in the prepared statement
        for ((a, i) <- args.zipWithIndex) {
          st.setObject(i + 1, a)
        }
        handler(st)
      }
    }
  }

}
