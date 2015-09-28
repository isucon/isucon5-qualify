package net.isucon.isucon5q.bench.scenario;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import net.isucon.isucon5q.bench.Scenario;
import net.isucon.isucon5q.bench.Step;
import net.isucon.isucon5q.bench.Result;
import net.isucon.isucon5q.bench.Parameter;
import net.isucon.isucon5q.bench.Session;

import net.isucon.isucon5q.bench.I5Parameter;

public class BootstrapChecker extends Scenario {
  public BootstrapChecker(Long timeout) {
    super(timeout);
  }

  @Override
  public Result finishHook(Result result) {
    if (result.responses.exception > 0 || result.violations.size() > 0) {
      result.fail();
    }
    return result;
  }

  @Override
  public boolean verbose() {
    return true;
  }

  private class CheckingStatus {
    public int friends;

    public Long existingEntryId;
    public String commentText;

    public Long postedEntryId;
    public String postedTitle;
    public String postedContent;

    public String newBirthday;
  }

  @Override
  public void scenario(List<Session> originalSessions) {
    //  0..3  -> BootstrapChecker
    //  3..10 -> Isucon5Checker
    // 10.... -> Isucon5Load
    List<Session> sessions = originalSessions.subList(0, 3);

    Random random = new Random();
    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // This scenario requires 3 I5Parameter instances:
    // 1st: main user to be checked
    // 2nd: user who is a friend of 1st
    // 3rd: user who isn't a friend of 1st
    Session session = sessions.get(0);
    I5Parameter param = (I5Parameter) session.param();

    Session session2 = sessions.get(1);
    I5Parameter param2 = (I5Parameter) session2.param();

    Session session3 = sessions.get(2);
    I5Parameter param3 = (I5Parameter) session3.param();

    {
      getAndCheck(session2, "/login", "LOGIN GET 2ND USER", (check) -> {
          check.isStatus(200);
      });

      Map loginForm = new HashMap();
      loginForm.put("email", param2.email);
      loginForm.put("password", param2.password);

      postAndCheck(session2, "/login", loginForm, "LOGIN POST 2ND USER", (check) -> {
          check.isRedirect("/");
          if (check.hasViolations())
            check.fatal("ログイン操作に対して正しいレスポンスが返りませんでした");
      });
    }
    {
      getAndCheck(session3, "/login", "LOGIN GET 3RD USER", (check) -> {
          check.isStatus(200);
      });

      Map loginForm = new HashMap();
      loginForm.put("email", param3.email);
      loginForm.put("password", param3.password);

      postAndCheck(session3, "/login", loginForm, "LOGIN POST 3RD USER", (check) -> {
          check.isRedirect("/");
          if (check.hasViolations())
            check.fatal("ログイン操作に対して正しいレスポンスが返りませんでした");
      });
    }

    CheckingStatus checking = new CheckingStatus();

    getAndCheck(session, "/", "SHOULD LOGIN AT FIRST", (check) -> {
        check.isRedirect("/login");
        if (check.hasViolations())
          check.fatal("未ログインでトップページへのアクセスが正しいリダイレクトになっていません");
    });

    getAndCheck(session, "/login", "LOGIN PAGE", (check) -> {
        check.isStatus(200);
        // to check form exists or not
        check.exist("form input[type=text]", 1);
        check.exist("form input[type=password]", 1);
        check.exist("form input[type=submit]", 1);
        if (check.hasViolations()) {
          check.fatal("ログインフォームが正常に表示されていません");
        }
    });

    {
      Map loginForm = new HashMap();
    
      loginForm.put("email", param.email);
      loginForm.put("password", param.password);

      postAndCheck(session, "/login", loginForm, "LOGIN POST", (check) -> {
          check.isRedirect("/");
          if (check.hasViolations())
            check.fatal("ログイン処理に対して正しいレスポンスが返りませんでした");
        });
    }

    getAndCheck(session, "/", "INDEX AFTER LOGIN", (check) -> {
        check.isStatus(200);

        check.hasStyleSheet("/css/bootstrap.min.css");

        check.content("dd#prof-account-name", param.accountName);
        check.content("dd#prof-email", param.email);

        check.exist("dd#prof-last-name");
        check.exist("dd#prof-first-name");
        check.exist("dd#prof-sex");
        check.exist("dd#prof-pref");

        check.exist("dd#prof-friends a", 1);
        check.attribute("dd#prof-friends a", "href", "/friends");
        check.contentMatch("dd#prof-friends a", "(\\d+)人");
        if (! check.hasViolations()) {
          try {
            String friendsText = check.lastMatch().group(1);
            checking.friends = Integer.parseInt(friendsText);
          } catch (IndexOutOfBoundsException e) {
            // ignore: violations already added
          }
        }

        check.exist("#entries-title a", 1);
        check.attribute("#entries-title a", "href", String.format("/diary/entries/%s", param.accountName));

        if (check.document().select("#entries ul li").size() > 0) {
          String entryPath = check.document().select("#entries ul li").first().getElementsByTag("a").attr("href");
          Matcher m = Pattern.compile("/diary/entry/(\\d+)").matcher(entryPath);
          if (! m.matches()) {
            check.addViolation(String.format("日記ページへのリンクが正しくありません: %s", entryPath));
          } else {
            checking.existingEntryId = Long.valueOf(m.group(1));
          }
        }

        if (check.hasViolations())
          check.fatal("トップページが正しく表示されていません");
    });

    getAndCheck(session, "/css/bootstrap.min.css", "STYLE SHEET CHECK", false, (check) -> {
        System.err.println("style sheet check");
        check.isStatus(200);
        check.isContentLength(122540);
        if (check.hasViolations())
          check.fatal("スタイルシートが取得できません");
    });

    getAndCheck(session2, "/", "INDEX AFTER LOGIN 2ND USER", (check) -> {
        check.isStatus(200);
        check.hasStyleSheet("/css/bootstrap.min.css");
        check.content("dd#prof-account-name", param2.accountName);

        check.exist("dd#prof-email");
        check.exist("dd#prof-last-name");
        check.exist("dd#prof-first-name");
    });

    getAndCheck(session3, "/", "INDEX AFTER LOGIN 3RD USER", (check) -> {
        check.isStatus(200);
        check.hasStyleSheet("/css/bootstrap.min.css");
        check.content("dd#prof-account-name", param3.accountName);

        check.exist("dd#prof-email");
        check.exist("dd#prof-last-name");
        check.exist("dd#prof-first-name");
    });

    LocalDateTime beforeUpdates = LocalDateTime.now().withNano(0);

    // null means this user doesn't have any entries
    if (checking.existingEntryId != null) {
      String postCommentPath = String.format("/diary/comment/%d", checking.existingEntryId);
      Map form = new HashMap();
      checking.commentText = String.valueOf(random.nextInt(Integer.MAX_VALUE));
      form.put("comment", checking.commentText);
      postAndCheck(session, postCommentPath, form, "POST COMMENT ON MY ENTRY", (check) -> {
          check.isRedirect(String.format("/diary/entry/%d", checking.existingEntryId));
      });
    }

    {
      Map form = new HashMap();
      checking.postedTitle = String.valueOf(random.nextInt(Integer.MAX_VALUE));
      checking.postedContent = String.valueOf(random.nextInt(Integer.MAX_VALUE)) + "\n" +
        String.valueOf(random.nextInt(Integer.MAX_VALUE)) + "\n" +
        String.valueOf(random.nextInt(Integer.MAX_VALUE)) + "\n";
      form.put("title", checking.postedTitle);
      form.put("content", checking.postedContent);
      form.put("private", "1");
      postAndCheck(session, "/diary/entry", form, "POST NEW DIARY ENTRY", (check) -> {
          check.isRedirect(String.format("/diary/entries/%s", param.accountName));
      });
    }

    String profPath = String.format("/profile/%s", param.accountName);

    {
      getAndCheck(session2, profPath, "PROFILE FROM FRIEND USER", (check) -> {
          check.content("dd#prof-account-name", param.accountName);
          check.exist("dd#prof-last-name");
          check.exist("dd#prof-first-name");
          // 2nd is a friend
          check.content("dd#prof-email", param.email);
          check.exist("dd#prof-sex");
          check.exist("dd#prof-pref");

          check.missing("#profile-friend-form");
      });
      getAndCheck(session3, profPath, "PROFILE FROM NON-FRIEND USER", (check) -> {
          check.content("dd#prof-account-name", param.accountName);
          check.exist("dd#prof-last-name");
          check.exist("dd#prof-first-name");

          check.missing("dd#prof-email");
          check.missing("dd#prof-sex");
          check.missing("dd#prof-pref");

          check.exist("#profile-friend-form");
      });
    }
    {
      Map profForm = new HashMap();
      profForm.put("first_name", param.firstName);
      profForm.put("last_name", param.lastName);
      profForm.put("sex", param.sex);
      profForm.put("pref", param.pref);

      LocalDateTime lower = LocalDateTime.parse("1970-01-01T00:00:00");
      LocalDateTime higher = LocalDateTime.parse("2014-12-31T15:00:00");
      LocalDateTime newBirthday = lower.plus(
        new Double(random.nextDouble() * lower.until(higher, ChronoUnit.SECONDS)).longValue(),
        ChronoUnit.SECONDS
      );
      checking.newBirthday = newBirthday.format(DateTimeFormatter.ISO_LOCAL_DATE);

      profForm.put("birthday", checking.newBirthday);

      postAndCheck(session, profPath, profForm, "UPDATE PROFILE", (check) -> {
          check.isRedirect(profPath);
      });
    }

    LocalDateTime afterUpdates = LocalDateTime.now().withNano(0);

    sleep(1000);

    {
      String footprintDate = afterUpdates.format(DateTimeFormatter.ISO_LOCAL_DATE); // yyyy-MM-dd
      String user2Path = String.format("/profile/%s", param2.accountName);
      String user3Path = String.format("/profile/%s", param3.accountName);

      getAndCheck(session, "/footprints", "CHECK LEFT FOOTPRINTS OF OTHERS", (check) -> {
          check.isStatus(200);
          if (check.document().select("#footprints ul li.footprints-footprint").stream()
              .filter((e) -> e.text().startsWith(footprintDate))
              .filter((e) -> {
                  String p = e.getElementsByTag("a").first().attr("href");
                  return p.equals(user2Path) || p.equals(user3Path);
                })
              .count() != 2) {
            check.addViolation("友だちリストに友だちになったばかりのユーザが表示されていません");
          }
      });
    }
    if (checking.existingEntryId != null) {
      getAndCheck(session2, "/", "SHOW INDEX TO FIND FRIEND'S COMMENT", (check) -> {
          check.isStatus(200);
          check.contentCheck("#friend-comments .friend-comment ul", "友だちになったユーザのコメントがトップページに正しく表示されていません", (e) -> {
              if (e.select("li.comment-from-to a").size() == 2 &&
                  e.select("li.comment-from-to a").get(0).text().trim().equals(param.nickName + "さん") &&
                  e.select("li.comment-from-to a").get(1).text().trim().equals(param.nickName + "さん")) {
                // ok
              } else {
                System.err.println("\t> from-to a missing");
                return false;
              }
              String checkingCommentInIndex = checking.commentText;
              if (checking.commentText.length() > 30) {
                checkingCommentInIndex = checking.commentText.substring(0, 27) + "...";
              }
              if (e.select("li.comment-comment").size() == 1 &&
                  e.select("li.comment-comment").get(0).text().trim().equals(checkingCommentInIndex)) {
                // ok
              } else {
                System.err.println("\t> comment-comment missing");
                return false;
              }
              if (e.select("li.comment-created-at").size() != 1) {
                System.err.println("\t> comment-created-at missing");
                return false;
              }
              String atText = e.select("li.comment-created-at").get(0).text().trim();
              Matcher m = Pattern.compile("投稿時刻:(\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d)").matcher(atText);
              if (! m.matches()) {
                System.err.println("\t> comment-created-at not match");
                return false;
              }
              return true;
          });
      });
    }
    {
      String entriesPath = String.format("/diary/entries/%s", param.accountName);
      checking.postedEntryId = null;
      getAndCheck(session2, entriesPath, "FRIEND'S ENTRY LIST MUST HAVE RECENT PRIVATE ONE", (check) -> {
          check.isStatus(200);
          check.content("#entries .entry .entry-title a", checking.postedTitle);
          if (! check.hasViolations()) {
            String entryPath = check.document().select("#entries .entry .entry-title a").stream()
              .filter(i -> i.text().equals(checking.postedTitle))
              .findFirst().get()
              .attr("href");
            checking.postedEntryId = Long.valueOf(entryPath.substring("/diary/entry/".length()));
          }
      });

      if (checking.postedEntryId != null) {
        String entryPath = String.format("/diary/entry/%d", checking.postedEntryId);
        getAndCheck(session2, entryPath, "FRIEND CAN SEE PRIVATE ENTRY", (check) -> {
            check.isStatus(200);
            check.contentLongText("#entry-entry .entry-content", checking.postedContent);
          });

        getAndCheck(session3, entriesPath, "NON-FRIEND'S ENTRY LIST MUST NOT HAVE PRIVATE ONE", (check) -> {
            check.isStatus(200);
            check.contentMissing("#entries .entry .entry-title a", checking.postedTitle);
          });
        getAndCheck(session3, entryPath, "NON-FRIND IS PROHIBITED FOR PRIVATE ENTRY", (check) -> {
            check.isStatus(403);
          });
      }
    }

    getAndCheck(session, "/friends", "LIST OF FRIENDS", (check) -> {
        check.isStatus(200);
        check.content("#friends dd.friend-friend a", param2.nickName);
        check.contentMissing("#friends dd.friend-friend a", param3.nickName);
    });

    String makeFriendPath = String.format("/friends/%s", param3.accountName);
    postAndCheck(session, makeFriendPath, (new HashMap()), "GET FRIEND", (check) -> {
        check.isRedirect("/friends");
    });

    sleep(1000);

    getAndCheck(session, "/friends", "LIST OF FRIENDS AGAIN", (check) -> {
        check.isStatus(200);
        check.content("#friends dd.friend-friend a", param3.nickName);
    });

    {
      if (checking.postedTitle != null) {
        String entriesPath = String.format("/diary/entries/%s", param.accountName);
        getAndCheck(session3, entriesPath, "NEW-FRIEND'S ENTRY LIST MUST HAVE RECENT PRIVATE ONE", (check) -> {
            check.isStatus(200);
            check.content("#entries .entry .entry-title a", checking.postedTitle);
        });
      }
      if (checking.postedEntryId != null) {
        String entryPath = String.format("/diary/entry/%d", checking.postedEntryId);
        getAndCheck(session3, entryPath, "NEW FRIEND CAN SEE PRIVATE ENTRY", (check) -> {
            check.isStatus(200);
            check.contentLongText("#entry-entry .entry-content", checking.postedContent);
        });
      }
    }
  }
}
