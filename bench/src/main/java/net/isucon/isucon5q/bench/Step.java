package net.isucon.isucon5q.bench;

import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.eclipse.jetty.client.HttpClient;

public class Step {
  private long timeout;
  private ArrayList<Scenario> list;
  private ArrayList<Result> results;
  private ArrayList<Thread> threads;

  private long STEP_WATCHER_INTERVAL_MS = 200;

  public Step(long timeout) {
    this.timeout = timeout;
    this.list = new ArrayList<Scenario>();
    this.results = new ArrayList<Result>();
  }

  public Step(Long timeout, Class<? extends Scenario>... klasses) {
    this.timeout = timeout.longValue();
    this.list = new ArrayList<Scenario>();
    for (Class<? extends Scenario> klass : klasses) {
      try {
        Scenario item = klass.getConstructor(Long.class).newInstance(timeout);
        list.add(item);
      } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
        System.err.format("Failed to create instance of Scenario: %s%n", klass);
        System.exit(1);
      }
    }
    this.results = new ArrayList<Result>();
  }

  public String name() {
    ArrayList<String> names = new ArrayList<String>();
    for (Scenario s : list) {
      names.add(s.name());
    }
    return "{" + String.join(",", names) + "}";
  }

  public void addStep(Scenario s) {
    list.add(s);
  }

  public void execute(HttpClient client, Config config, List<Session> sessions) {
    LocalDateTime started = LocalDateTime.now();
    threads = new ArrayList<Thread>();
    for (Scenario sc : list) {
      Runnable task = () -> {
        try {
          sc.setHttpClient(client);
          Result r = sc.execute(config, sessions);
          synchronized(results){
            results.add(r);
          }
        } catch (Throwable e) {
          e.printStackTrace();
          Result re = generateIncompleteResult("EXCEPTION", started.until(LocalDateTime.now(), ChronoUnit.MILLIS));
          results.add(re);
        }
      };

      Thread t = new Thread(task);
      threads.add(t);
      t.start();
    }
  }

  public void join(long waitTimeout) { // ms
    LocalDateTime started = LocalDateTime.now();
    LocalDateTime timeoutAt = started.plus(waitTimeout, ChronoUnit.MILLIS);
    while (timeoutAt.isAfter(LocalDateTime.now())) {
      boolean completed = true;
      boolean interrupted = false;
      for (Thread t : threads) {
        try {
          t.join(STEP_WATCHER_INTERVAL_MS);
        } catch (InterruptedException e) {
          // just ignore: next loop will come if thread is still alive
        }
        if (t.isAlive()) {
          completed = false;
        }
      }
      if (completed)
        break;
    }
  }

  public void kill() {
    for (Thread t : threads) {
      try {
        t.join(1); // ms
      } catch (InterruptedException e) {
        // just ignore
      }
      t.interrupt();
    }
  }

  public boolean isFinished() {
    synchronized(results) {
      return list.size() == results.size();
    }
  }

  public Result mergedResult() {
    synchronized(results) {
      return Result.merge(name(), Result.MergeType.PARALLEL, results);
    }
  }

  private Result generateIncompleteResult(String label, long timeoutElapsed) {
    Result r = new Result(false, timeoutElapsed);
    r.addViolation(label, "A scenario in parallel didn't finish");
    return r;
  }
}
