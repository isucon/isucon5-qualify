package net.isucon.isucon5q.bench;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.concurrent.TimeUnit;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.util.FormContentProvider;

import org.eclipse.jetty.util.Fields;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.isucon.isucon5q.bench.Result;
import net.isucon.isucon5q.bench.Session;

public class Driver {
  private HttpClient client;
  private LocalDateTime started;
  private Result storedResult;

  private Config config;

  private State state;

  private long DRIVER_WATCH_BLOCK_INTERVAL = 3;

  private int DRIVER_CONTENT_BUFFER_SIZE = 1 * 1024 * 1024;

  public static class ScenarioAbortException extends RuntimeException {
  }

  public void setHttpClient(HttpClient client) {
    this.client = client;
  }

  public HttpClient getHttpClient() {
    return this.client;
  }

  protected void scenario(List<Session> sessions) {
    throw new AbstractMethodError();
  }

  public Result finishHook(Result result) {
    return result;
  }

  protected boolean verbose() {
    return false;
  }

  protected Result execute(Config config, List<Session> sessions) {
    this.config = config;
    start();
    try {
      scenario(sessions);
    } catch (ScenarioAbortException e) {
      // stop immediately
    }
    return finishHook(storedResult.clone());
  }

  protected void start() {
    started = LocalDateTime.now();
    Result r = new Result();
    r.done = this.getClass().getSimpleName();
    storedResult = r;
    state = new State();
  }

  protected Result result() {
    storedResult.elapsed = started.until(LocalDateTime.now(), ChronoUnit.MILLIS);
    return storedResult;
  }

  private void block() {
    while (state.isRunning()) {
      try {
        Thread.sleep(DRIVER_WATCH_BLOCK_INTERVAL);
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }

  protected void sleep(long timeoutMs) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime dt = now.plus(timeoutMs, ChronoUnit.MILLIS);
    while (dt.isAfter(now)) {
      try {
        Thread.sleep(now.until(dt, ChronoUnit.MILLIS));
      } catch (InterruptedException e) {
        // ignore
      }
      now = LocalDateTime.now();
    }
  }

  protected void get(Session session, String path) {
    getAndCheck(session, path, null, false, null, null);
  }

  private class IntValueHolter {
    private int value;
    public void set(int value) {
      this.value = value;
    }
    public int get() {
      return this.value;
    }
  }

  protected int getStatus(Session session, String path) {
    IntValueHolter holder = new IntValueHolter();
    getAndCheck(session, path, "TO READ STATUS", false, (check) -> {
        holder.set(check.response().getStatus());
    });
    return holder.get();
  }

  protected void get(Session session, String path, String type) {
    getAndCheck(session, path, type, false, null, null);
  }

  private class StringValueHolder {
    private String value;
    public void set(String value) {
      this.value = value;
    }
    public String get() {
      return this.value;
    }
  }

  protected String getAndRead(Session session, String path, String selector, int index, Function<Element, String> getter) {
    StringValueHolder holder = new StringValueHolder();
    getAndCheck(session, path, "TO READ NODE", (check) -> {
        Elements es = check.document().select(selector);
        if (es.size() > index)
          holder.set(getter.apply(es.get(index)));
    });
    return holder.get();
  }

  protected void getAndCheck(Session session, String path, String type, Consumer<Checker> checkerCallback) {
    requestAndCheck(createGetRequest(session, path), session, type, checkerCallback);
  }

  protected void getAndCheck(Session session, String path, String type, boolean contentBodyCheck, Consumer<Checker> checkerCallback) {
    // mainly for contentBodyCheck is false (check for HTTP response header only)
    getAndCheck(session, path, type, contentBodyCheck, null, checkerCallback);
  }
  
  protected void getAndCheck(
    Session session,
    String path,
    String type,
    boolean contentBodyCheck,
    BiConsumer<Response, ByteBuffer> contentCheckerCallback,
    Consumer<Checker> checkerCallback)
  {
    requestAndCheck(
      createGetRequest(session, path),
      session,
      type,
      contentBodyCheck,
      contentCheckerCallback,
      checkerCallback
    );
  }

  protected void post(Session session, String path, Map params) {
    postAndCheck(session, path, params, null, false, null, null);
  }

  protected void postAndCheck(Session session, String path, Map params, String type, Consumer<Checker> checkerCallback) {
    requestAndCheck(createPostRequest(session, path, formContent(params)), session, type, checkerCallback);
  }

  protected void postAndCheck(
    Session session,
    String path,
    Map params,
    String type,
    boolean contentBodyCheck,
    BiConsumer<Response, ByteBuffer> contentCheckerCallback,
    Consumer<Checker> checkerCallback)
  {
    requestAndCheck(
      createPostRequest(session, path, formContent(params)),
      session,
      type,
      contentBodyCheck,
      contentCheckerCallback,
      checkerCallback
    );
  }

  protected void requestAndCheck(
    Request req,
    Session session,
    String type,
    Consumer<Checker> checkerCallback)
  {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();

    BiConsumer<Response, ByteBuffer> hookCallback = (request, buffer) -> {
      while (buffer.hasRemaining()) {
        byte[] bytes = new byte[DRIVER_CONTENT_BUFFER_SIZE];
        int readLength = buffer.remaining() < DRIVER_CONTENT_BUFFER_SIZE ? buffer.remaining() : DRIVER_CONTENT_BUFFER_SIZE;
        buffer.get(bytes, 0, readLength);
        stream.write(bytes, 0, readLength);
      }
    };

    Consumer<Checker> wrapperCallback = (check) -> {
      check.setContentBodyHtml(stream.toString()); // system default encoding
      checkerCallback.accept(check);
    };

    requestAndCheck(req, session, type, true, hookCallback, wrapperCallback);
  }

  protected void requestAndCheck(
    Request req,
    Session session,
    String type,
    boolean contentBodyCheck,
    BiConsumer<Response, ByteBuffer> contentCheckerCallback,
    Consumer<Checker> checkerCallback)
  {
    if (type == null && checkerCallback != null || contentBodyCheck && checkerCallback == null) {
      throw new IllegalArgumentException();
    }

    if (verbose()) {
      System.err.println(type);
    }

    state.init();

    req.onResponseFailure((res, failure) -> {
        storedResult.addResponse(ResponseType.EXCEPTION);
        storedResult.addViolation(failure.getClass().getSimpleName(), failure.getMessage());
        System.err.println(String.format("Exception: %s: %s", failure.getClass().getName(), failure.getMessage()));
        failure.printStackTrace();
        state.finish();
    });

    if (contentCheckerCallback != null) {
      req.onResponseContent((r,b) -> contentCheckerCallback.accept(r,b));
    }

    LocalDateTime requestAt = LocalDateTime.now();

    SuccessLambdaArgs args = new SuccessLambdaArgs();

    req.onResponseSuccess((res) -> {
        args.set(requestAt.until(LocalDateTime.now(), ChronoUnit.MILLIS), res);
        session.readCookie(res);
        state.finish();
    });

    req.send(result -> { });

    block();

    if (args.isSuccess()) {
      Result r = result();
      Response res = args.response();
      int s = res.getStatus();
      if (s >= 200 && s < 300) {
        r.addResponse(ResponseType.SUCCESS);
      } else if (s >= 300 && s < 400) {
        r.addResponse(ResponseType.REDIRECT);
      } else if (s >= 400 && s < 500) {
        r.addResponse(ResponseType.FAILURE);
      } else {
        r.addResponse(ResponseType.ERROR);
      }
      if (checkerCallback != null) {
        Checker check = new Checker(r, type, config, args.responseTime(), res);
        checkerCallback.accept(check);
      }
    }

    state.init();
  }

  private ContentProvider formContent(Map params) {
    Fields fields = new Fields();
    params.forEach(
      (key, value) -> fields.add((String)key, (String)value)
    );
    return new FormContentProvider(fields);
  }

  private Request createGetRequest(Session session, String path) {
    Request req = client.newRequest(config.uri(path))
      .method("GET")
      .timeout(Config.GET_TIMEOUT, TimeUnit.MILLISECONDS);

    session.writeCookie(req);

    return req;
  }

  private Request createPostRequest(Session session, String path, ContentProvider content) {
    Request req = client.newRequest(config.uri(path))
      .method("POST")
      .timeout(Config.POST_TIMEOUT, TimeUnit.MILLISECONDS)
      .content(content)
      .header("Content-Length", String.valueOf(content.getLength()));

    session.writeCookie(req);

    return req;
  }

  // "onResponseSuccess" callback will be evaluated in jetty-client thread.
  // We can't catch exceptions (ex: ScenarioAbortException) from jetty-client thread, so
  // this class receives arguments of onResponseSuccess, and then evaluate checkerCallback
  // in scenario running thread later.
  private class SuccessLambdaArgs {
    private boolean success;
    private long responseTime;
    private Response response;

    public SuccessLambdaArgs() {
      this.success = false;
    }

    public void set(long responseTime, Response response) {
      this.success = true;
      this.responseTime = responseTime;
      this.response = response;
    }

    public boolean isSuccess() {
      return success;
    }

    public long responseTime() {
      return responseTime;
    }

    public Response response() {
      return response;
    }
  }
}
