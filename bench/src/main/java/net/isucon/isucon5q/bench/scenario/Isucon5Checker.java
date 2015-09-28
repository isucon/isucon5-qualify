package net.isucon.isucon5q.bench.scenario;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import java.net.URI;
import java.net.URISyntaxException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.ChronoField;

import net.isucon.isucon5q.bench.Scenario;
import net.isucon.isucon5q.bench.Step;
import net.isucon.isucon5q.bench.Result;
import net.isucon.isucon5q.bench.Parameter;
import net.isucon.isucon5q.bench.Session;

import net.isucon.isucon5q.bench.I5Parameter;

public class Isucon5Checker extends Isucon5BaseScenario {
  private static long DURATION_MILLIS = 60 * 1000;

  public Isucon5Checker(Long timeout) {
    super(timeout);
  }

  @Override
  public void scenario(List<Session> originalSessions) {
    //  0..3  -> BootstrapChecker
    //  3..10 -> Isucon5Checker
    // 10.... -> Isucon5Load
    System.err.println("Isucon5Checker");

    List<Session> sessions = originalSessions.subList(3, 10);
    Random random = new Random();

    LocalDateTime stopAt = LocalDateTime.now().plus(DURATION_MILLIS, ChronoUnit.MILLIS);

    while (true) {
      if (LocalDateTime.now().isAfter(stopAt))
        break;
      int ssize = (int) sessions.size();
      int r1 = random.nextInt(ssize);
      int r2 = random.nextInt(ssize);
      while (r1 == r2) {
        r2 = random.nextInt(ssize);
      }
      Session s1 = sessions.get(r1);
      Session s2 = sessions.get(r2);

      int i1 = getStatus(s1, "/");
      int i2 = 0;
      if (i1 != 200) {
        getAndCheck(s1, "/login", "LOGIN PAGE BECAUSE NOT LOGGED IN", check -> check.isStatus(200));
        postAndCheck(s1, "/login", formLogin(s1), "LOGIN POST WHEN LOGGED OUT", check -> check.isRedirect("/"));
        getAndCheck(s1, "/", "SHOW INDEX AFTER LOGIN", check -> check.isStatus(200));
        i2 = getStatus(s1, "/");
      }
      if (i1 != 200 && i2 != 200)
        continue;

      if (LocalDateTime.now().isAfter(stopAt))
        break;

      int i3 = getStatus(s2, "/");
      int i4 = 0;
      if (i3 != 200) {
        getAndCheck(s2, "/login", "LOGIN PAGE BECAUSE NOT LOGGED IN", check -> check.isStatus(200));
        postAndCheck(s2, "/login", formLogin(s2), "LOGIN POST WHEN LOGGED OUT", check -> check.isRedirect("/"));
        getAndCheck(s2, "/", "SHOW INDEX AFTER LOGIN", check -> check.isStatus(200));
        i4 = getStatus(s2, "/");
      }
      if (i3 != 200 && i4 != 200)
        continue;

      if (LocalDateTime.now().isAfter(stopAt))
        break;

      sleep(1000);

      if (LocalDateTime.now().isAfter(stopAt))
        break;

      String s2friendPath = String.format("/profile/%s", accountName(s2));
      String s2nickName = getAndRead(s1, "/friends", String.format("#friends dl dd.friend-friend a[href=%s]", s2friendPath), 0, e -> e.text());
      if (s2nickName == null) {
        // s1 and s2 are not friends each other
        postAndCheck(s2, String.format("/friends/%s", accountName(s1)), new HashMap(), "MAKE FRIENDS", check -> {
            check.isRedirect("/friends");
            check.respondUntil(3000);
        });
        if (LocalDateTime.now().isAfter(stopAt))
          break;

        sleep(1000);

        if (LocalDateTime.now().isAfter(stopAt))
          break;

        getAndCheck(s1, "/friends", "FRIEND LIST AFTER MAKING FRIEND", (check) -> {
            check.isStatus(200);
            check.contentCheck(String.format("#friends dl dd.friend-friend a[href=%s]", s2friendPath), "友だちリストになったばかりの友だちが含まれていません", (e) -> {
                return e.attr("href").equals(s2friendPath);
            });
        });
      } else {
        // s1 and s2 are friends each other
        String entryPath = getAndRead(s2, String.format("/diary/entries/%s", accountName(s1)), "#entries .entry .entry-title a", 0, e -> e.attr("href"));
        long entryId = 0L;
        String commentText = null;
        if (entryPath != null) {
          URI uri = null;
          try {
            uri = new URI(entryPath);
            if (uri.getPath() != null && uri.getPath().startsWith("/diary/entry/")) {
              entryId = Long.parseLong(uri.getPath().substring("/diary/entry/".length()));
              if (entryId > 0) {
                Map f = formComment(random);
                postAndCheck(s2, String.format("/diary/comment/%d", entryId), f, "COMMENT POST", check -> check.respondUntil(3000));
                commentText = (String) f.get("comment");
              }
            }
          } catch (URISyntaxException e) {
            // ignore, but in fact, it's bug of application
          }
        }

        if (LocalDateTime.now().isAfter(stopAt))
          break;

        sleep(1000);

        if (LocalDateTime.now().isAfter(stopAt))
          break;

        if (entryId > 0 && commentText != null) {
          final String longText = commentText;
          getAndCheck(s1, String.format("/diary/entry/%d", entryId), "SHOW ENTRY AFTER COMMENT POSTED", (check) -> {
              check.isStatus(200);
              check.contentLongText("#entry-comments .comment .comment-comment", longText);
          });
        }
      }
    }
  }
}
