package net.isucon.isucon5q.bench.scenario;

import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.ChronoField;

import net.isucon.isucon5q.bench.Scenario;
import net.isucon.isucon5q.bench.Step;
import net.isucon.isucon5q.bench.Result;
import net.isucon.isucon5q.bench.Parameter;
import net.isucon.isucon5q.bench.Session;

public class IndexBench extends Scenario {
  // private static long DURATION_MILLIS = 30 * 1000;
  private static long DURATION_MILLIS = 5 * 1000;

  public IndexBench(Long timeout) {
    super(timeout);
  }

  @Override
  public boolean verbose() {
    return false;
  }

  @Override
  public void scenario(List<Session> sessions) {
    Session s = sessions.get(0);

    LocalDateTime stopAt = LocalDateTime.now().plus(DURATION_MILLIS, ChronoUnit.MILLIS);
    while (LocalDateTime.now().isBefore(stopAt)) {
      for (int i = 0 ; i < 100 ; i++) {
        get(s, "/", String.format("GET / %d", LocalDateTime.now().get(ChronoField.SECOND_OF_MINUTE)));
      }
    }
  }
}
