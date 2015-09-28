package net.isucon.isucon5q.bench;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Result {
  public boolean valid;
  public long requests;
  public long elapsed; // milliseconds, to be updated at the root senario
  public String done;

  public Responses responses;
  public ArrayList<Violation> violations; // content check errors, response time errors, ...

  public enum MergeType {
    PARALLEL,
    SEQUENTIAL,
  }

  public Result() {
    valid = true;
    requests = 0;
    elapsed = 0;
    done = "";
    responses = new Responses(0, 0, 0, 0, 0);
    violations = new ArrayList<Violation>();
  }

  public Result(boolean v, long e) {
    valid = v;
    elapsed = e;

    requests = 0;
    done = "";
    responses = new Responses(0, 0, 0, 0, 0);
    violations = new ArrayList<Violation>();
  }

  public Result(boolean v, long r, long e, String d, Responses res, ArrayList<Violation> vio) {
    valid = v;
    requests = r;
    elapsed = e;
    done = d;
    responses = res;
    violations = vio;
  }

  public Result clone() {
    return new Result(valid, requests, elapsed, done, responses.clone(), (ArrayList<Violation>) violations.clone());
  }

  public void update(Result r) {
    synchronized(this) {
      this.valid = this.valid && r.valid;
      this.requests += r.requests;
      this.elapsed = r.elapsed; // updated
      this.done = r.done;
      this.responses  = mergeResponses(this.responses, r.responses);
      updateViolations(this.violations, r.violations);
    }
  }

  public static Result merge(String name, MergeType t, Result r1, Result r2) {
    ArrayList<Result> r = new ArrayList<Result>();
    r.add(r1);
    r.add(r2);
    return merge(name, t, r);
  }

  public static Result merge(String name, MergeType t, List<Result> results) {
    boolean valid = true;
    long requests = 0;
    long elapsed = 0;
    Responses res = new Responses();
    ArrayList<Result.Violation> vlist = new ArrayList<Violation>();
    for (Result r : results) {
      valid = valid && r.valid;
      requests += r.requests;
      if (t == MergeType.SEQUENTIAL) {
        elapsed += r.elapsed;
      } else { // PARALLEL
        elapsed = (elapsed > r.elapsed ? elapsed : r.elapsed);
      }
      res = mergeResponses(res, r.responses);
      updateViolations(vlist, r.violations);
    }
    return new Result(valid, requests, elapsed, name, res, vlist);
  }

  public void fail() {
    valid = false;
  }

  public void addResponse(ResponseType t) {
    synchronized(this) {
      this.requests += 1;
      switch (t) {
      case SUCCESS:   responses.success += 1; break;
      case REDIRECT:  responses.redirect += 1; break;
      case FAILURE:   responses.failure += 1; break;
      case ERROR:     responses.error += 1; break;
      case EXCEPTION: responses.exception += 1; break;
      }
    }
  }

  public void addViolation(String type, String description) {
    synchronized(this) {
      boolean notfound = true;
      for (Violation v : violations) {
        if (v.isSame(type, description)) {
          v.number += 1;
          notfound = false;
          return;
        }
      }
      if (notfound) {
        violations.add(new Violation(type, description, 1));
      }
    }
  }

  public static class Responses {
    public long success;
    public long redirect;
    public long failure; // 4xx
    public long error; // 5xx
    public long exception; // request exceptions (ex: timeout) 

    public Responses() {
      success = 0;
      redirect = 0;
      failure = 0;
      error = 0;
      exception = 0;
    }

    public Responses(long s, long r, long f, long e1, long e2) {
      success = s;
      redirect = r;
      failure = f;
      error = e1;
      exception = e2;
    }

    public Responses clone() {
      return new Responses(success, redirect, failure, error, exception);
    }
  }

  public static Responses mergeResponses(Responses r1, Responses r2) {
    return new Responses(
      r1.success + r2.success,
      r1.redirect + r2.redirect,
      r1.failure + r2.failure,
      r1.error + r2.error,
      r1.exception + r2.exception
    );
  }

  public static class Violation {
    public String type;
    public String description;
    public long number;

    public Violation(String t, String d, long n) {
      type = t;
      description = d;
      number = n;
    }

    public boolean isSame(String t, String d) {
      return type.equals(t) && description.equals(d);
    }

    public boolean isSame(Violation other) {
      return isSame(other.type, other.description);
    }
  }

  public static void updateViolations(List<Violation> a, List<Violation> b) {
    for (Violation vitem : b) {
      boolean notfound = true;
      for (Violation v : a) {
        if (v.isSame(vitem)) {
          v.number += vitem.number;
          notfound = false;
          break;
        }
      }
      if (notfound) {
        a.add(vitem);
      }
    }
  }

  public String toJSON() {
    try {
      return (new ObjectMapper()).writerWithDefaultPrettyPrinter().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      return String.format("{\"message\":\"Failed to create json: %s\"}", this.getClass().getName());
    }
  }
}
