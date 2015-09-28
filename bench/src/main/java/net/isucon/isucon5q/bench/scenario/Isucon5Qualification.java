package net.isucon.isucon5q.bench.scenario;

import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import net.isucon.isucon5q.bench.Scenario;
import net.isucon.isucon5q.bench.Step;
import net.isucon.isucon5q.bench.Result;
import net.isucon.isucon5q.bench.Parameter;
import net.isucon.isucon5q.bench.Session;

public class Isucon5Qualification extends Scenario {
  public Isucon5Qualification(Long timeout) {
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
    if (result.responses.failure * 100.0 / (requests * 1.0) >= 10.0)
      result.addViolation("Too many failures", "ステータス 4xx のレスポンスが多過ぎます(10%以上)");

    if (result.violations.size() > 0) {
      int num = 0;
      for (Result.Violation v : result.violations) {
        if (! v.description.equals("アプリケーションが 3000 ミリ秒以内に応答しませんでした"))
          num += 1;
      }
      if (num > 0)
        result.fail();
    }
    return result;
  }

  @Override
  public Step[] steps() {
    Step[] steps = new Step[3];
    steps[0] = new Step(35000L, Isucon5InitExecutor.class);
    steps[1] = new Step(60000L, BootstrapChecker.class);
    steps[2] = new Step(70000L, Isucon5Load.class, Isucon5Load.class, Isucon5Load.class, Isucon5Load.class, Isucon5Checker.class);

    return steps;
  }
}
