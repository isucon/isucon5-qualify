package net.isucon.isucon5q.bench.scenario;

import java.util.List;

import net.isucon.isucon5q.bench.Scenario;
import net.isucon.isucon5q.bench.Step;
import net.isucon.isucon5q.bench.Result;
import net.isucon.isucon5q.bench.Parameter;
import net.isucon.isucon5q.bench.Session;

import net.isucon.isucon5q.bench.I5Parameter;

public class Isucon5InitExecutor extends Scenario {
  private static long DURATION_MILLIS = 33 * 1000;

  public Isucon5InitExecutor(Long timeout) {
    super(timeout);
  }

  @Override
  public Result finishHook(Result result) {
    if (result.violations.size() > 0)
      result.fail();
    return result;
  }

  @Override
  public void scenario(List<Session> originalSessions) {
    Session s = new Session(null);

    getAndCheck(s, "/initialize", "INITIALIZE DATA", (check) -> {
        check.isStatus(200);
        check.respondUntil(30L * 1000);
    });
  }
}
