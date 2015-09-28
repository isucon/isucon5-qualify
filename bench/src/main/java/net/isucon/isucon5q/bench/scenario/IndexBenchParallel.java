package net.isucon.isucon5q.bench.scenario;

import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import net.isucon.isucon5q.bench.Scenario;
import net.isucon.isucon5q.bench.Step;
import net.isucon.isucon5q.bench.Result;
import net.isucon.isucon5q.bench.Parameter;
import net.isucon.isucon5q.bench.Session;

public class IndexBenchParallel extends Scenario {
  private static long DURATION_MILLIS = 35 * 1000;

  public IndexBenchParallel(Long timeout) {
    super(timeout);
  }

  @Override
  public boolean complex() {
    return true;
  }

  @Override
  public Step[] steps() {
    Step[] steps = new Step[1];
    steps[0] = new Step(
      DURATION_MILLIS,
      IndexBench.class,
      IndexBench.class,
      IndexBench.class
    );

    return steps;
  }
}
