package net.isucon.isucon5q.bench.scenario;

import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import net.isucon.isucon5q.bench.Scenario;
import net.isucon.isucon5q.bench.Step;
import net.isucon.isucon5q.bench.Result;
import net.isucon.isucon5q.bench.Parameter;
import net.isucon.isucon5q.bench.Session;

public class Isucon5Qualify extends Scenario {
  private static long DURATION_MILLIS = 120 * 1000;

  public Isucon5Qualify(Long timeout) {
    super(timeout);
  }

  @Override
  public boolean complex() {
    return true;
  }

  @Override
  public Result finishHook(Result result) {
    long requests = result.requests;
    // critical line:  1% for exceptions
    //                 1% for errors
    //                 1% for failures
    if (result.responses.exception * 100.0 / (requests * 1.0) >= 1.0)
      result.addViolation("Too many exceptions", "通信エラー等の失敗が多過ぎます(1%以上)");
    if (result.responses.error * 100.0 / (requests * 1.0) >= 1.0)
      result.addViolation("Too many errors", "ステータス 5xx のレスポンスが多過ぎます(1%以上)");
    if (result.responses.failure * 100.0 / (requests * 1.0) >= 1.0)
      result.addViolation("Too many failures", "ステータス 4xx のレスポンスが多過ぎます(1%以上)");

    if (result.violations.size() > 0) {
      result.fail();
    }
    return result;
  }

  @Override
  public Step[] steps() {
    Step[] steps = new Step[2];
    // TODO: add Step to call /init on app server
    steps[0] = new Step(60000L, BootstrapChecker.class);
    steps[1] = new Step(70000L, Isucon5Load.class, Isucon5Load.class, Isucon5Load.class, Isucon5Load.class, Isucon5Checker.class);

    return steps;
  }
}
