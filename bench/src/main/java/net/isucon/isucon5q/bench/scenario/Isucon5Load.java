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

public class Isucon5Load extends Isucon5BaseScenario {
  private static long DURATION_MILLIS = 60 * 1000;

  public Isucon5Load(Long timeout) {
    super(timeout);
  }

  @Override
  public void scenario(List<Session> originalSessions) {
    //  0..3  -> BootstrapChecker
    //  3..10 -> Isucon5Checker
    // 10.... -> Isucon5Load
    System.err.println("Isucon5Load");

    List<Session> sessions = originalSessions.subList(10, originalSessions.size());
    Random random = new Random();

    LocalDateTime stopAt = LocalDateTime.now().plus(DURATION_MILLIS, ChronoUnit.MILLIS);

    while (true) {
      if (LocalDateTime.now().isAfter(stopAt))
        break;
      Session s1 = sessions.get(random.nextInt((int) sessions.size()));
      Session s2 = sessions.get(random.nextInt((int) sessions.size()));
      Session s3 = sessions.get(random.nextInt((int) sessions.size()));

      get(s1, "/logout");
      get(s1, "/login");
      post(s1, "/login", formLogin(s1));
      get(s1, "/");

      if (LocalDateTime.now().isAfter(stopAt))
        break;

      get(s1, String.format("/profile/%s", accountName(s2)));

      String entryPath = getAndRead(s1, String.format("/diary/entries/%s", accountName(s2)), "#entries .entry .entry-title a", 0, e -> e.attr("href"));
      long entryId = 0L;
      if (entryPath != null) {
        URI uri = null;
        try {
          uri = new URI(entryPath);
          if (uri.getPath() != null && uri.getPath().startsWith("/diary/entry/")) {
            entryId = Long.parseLong(uri.getPath().substring("/diary/entry/".length()));
            if (entryId > 0) {
              get(s1, String.format("/diary/entry/%d", entryId));
              postAndCheck(s1, String.format("/diary/comment/%d", entryId), formComment(random), "COMMENT POST", check -> check.respondUntil(3000));
            }
          }
        } catch (URISyntaxException e) {
          // ignore
        }
      }

      if (LocalDateTime.now().isAfter(stopAt))
        break;

      int indexStatus = getStatus(s2, "/");
      if (indexStatus != 200) {
        get(s2, "/login");
        post(s2, "/login", formLogin(s2));
        get(s2, "/");
      }
      
      if (LocalDateTime.now().isAfter(stopAt))
        break;

      get(s2, "/friends");
      get(s2, "/");
      get(s2, "/footprints");
      get(s2, String.format("/diary/entries/%s", accountName(s2)));

      if (LocalDateTime.now().isAfter(stopAt))
        break;

      if (entryId > 0) {
        get(s2, String.format("/diary/entry/%d", entryId));
        get(s1, String.format("/diary/entry/%d", entryId));
      }

      if (LocalDateTime.now().isAfter(stopAt))
        break;

      get(s2, String.format("/profile/%s", accountName(s2)));
      postAndCheck(s2, "/diary/entry", formEntry(random), "DIARY ENTRY POST", check -> check.respondUntil(3000));
      get(s2, String.format("/diary/entries/%s", accountName(s2)));
      get(s2, String.format("/profile/%s", accountName(s3)));
    }
  }
}
