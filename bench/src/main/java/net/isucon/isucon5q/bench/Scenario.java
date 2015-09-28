package net.isucon.isucon5q.bench;

import java.util.List;
import java.util.ArrayList;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.eclipse.jetty.client.HttpClient;

// Session is independent from Request
// Checker is independent from Request/Session

// Session has:
//  * cookie list for requests
//  * user data for checks

// Request:
//  * get client/session, request and return response
//  * but do it async

// Scenario: Runner get senario name and run it
// 1. execute simple 1-pass behavior check
// 2. handle 2 or more HTTP sessions/cookies
//   * request by 1 sesison, and then execute check request by the other...
// 3. execute requests by 1 session simply

// step: list of sub-senarios which runs parallely
//   scenario will stop and don't execute next step (before timeout) if any scenario returns invalid status

public abstract class Scenario extends Driver {
  private long timeout;

  private State state;

  // override this method for complex scenario class
  protected boolean complex() {
    return false;
  }

  // override this method for complex scenario class
  protected Step[] steps() {
    return new Step[]{};
  }

  // override this method for simple leaf scenario class
  @Override
  protected void scenario(List<Session> sessions) {
    throw new AbstractMethodError();
  }

  // override to mark "fail()" with some violations or others along scenario specific rule
  @Override
  public Result finishHook(Result result) {
    return result;
  }

  public Scenario(Long timeout) {
    this.timeout = timeout.longValue();
    this.state = new State();
  }

  private List<Session> createSessions(List<Parameter> params) {
    List<Session> sessions = new ArrayList<Session>();
    for (Parameter param : params) {
      sessions.add(new Session(param));
    }
    return sessions;
  }

  public Result run(HttpClient client, Config config, List<Parameter> params) {
    if (complex()) {
      return executeRecursively(client, config, params);
    }
    return executeSingle(client, config, params);
  }

  private Result executeSingle(HttpClient client, Config config, List<Parameter> params) {
    setHttpClient(client);

    LocalDateTime started = LocalDateTime.now();
    LocalDateTime stopAt = started.plus(timeout, ChronoUnit.MILLIS);
    
    List<Session> sessions = createSessions(params);
    Result result = new Result();

    Runnable singleTask = () -> {
      try {
        Result r = execute(config, sessions);
        result.update(r);
      } catch (Throwable e) {
        e.printStackTrace();
        Result re = generateIncompleteResult("EXCEPTION", started.until(LocalDateTime.now(), ChronoUnit.MILLIS));
        result.update(re);
      }
      state.finish();
    };
    Thread t = new Thread(singleTask);
    t.start();

    boolean onError = false;

    LocalDateTime now = LocalDateTime.now();

    while (state.isRunning() && stopAt.isAfter(now)) {
      long sleepTime = now.until(stopAt, ChronoUnit.MILLIS);
      try {
        t.join(sleepTime);
      } catch (InterruptedException e) {
        // just re-join if not finished
      }
      now = LocalDateTime.now();
    }
    if (onError) {
      Result timeoutResult = generateIncompleteResult("INTERRUPTED", started.until(LocalDateTime.now(), ChronoUnit.MILLIS));
      return Result.merge(name() + ":INTERRUPTED", Result.MergeType.SEQUENTIAL, result, timeoutResult);
    } else if (t.isAlive()) {
      t.interrupt();
      Result timeoutResult = generateIncompleteResult("TIMEOUT", started.until(LocalDateTime.now(), ChronoUnit.MILLIS));
      return Result.merge(name() + ":TIMEOUT", Result.MergeType.SEQUENTIAL, result, timeoutResult);
    }
    result.done = name();
    return result;
  }

  private Result executeRecursively(HttpClient client, Config config, List<Parameter> params) {
    List<Session> sessions = createSessions(params);

    ArrayList<Result> rlist = new ArrayList<Result>();
    ArrayList<String> doneList = new ArrayList<String>();

    LocalDateTime started = LocalDateTime.now();
    LocalDateTime shutdownAt = started.plus(timeout, ChronoUnit.MILLIS);

    for (Step s : steps()) {
      long stepTimeout = LocalDateTime.now().until(shutdownAt, ChronoUnit.MILLIS);
      s.execute(client, config, sessions);
      s.join(stepTimeout);

      if (s.isFinished()) {
        doneList.add(s.name());
        Result r = finishHook(s.mergedResult());
        rlist.add(r);
        if (! r.valid)
          break;
      } else {
        s.kill();
        // timeout at the one of steps
        rlist.add(generateIncompleteResult("TIMEOUT", started.until(LocalDateTime.now(), ChronoUnit.MILLIS)));
        doneList.add(s.name() + ":TIMEOUT");
        break;
      }
    }
    String resultName = "[" + String.join(",", doneList) + "]";
    return Result.merge(resultName, Result.MergeType.SEQUENTIAL, rlist);
  }

  public String name() {
    return this.getClass().getSimpleName();
  }

  private Result generateIncompleteResult(String label, long timeoutElapsed) {
    Result r = new Result(false, timeoutElapsed);
    r.addViolation(label, "Blocking bench step didn't finish");
    return r;
  }
}
