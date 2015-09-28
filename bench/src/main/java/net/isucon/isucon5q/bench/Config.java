package net.isucon.isucon5q.bench;

public class Config {
  public static final long MAX_RUNNING_TIME = 3 * 60 * 1000;

  public static final long GET_TIMEOUT = 30 * 1000;
  public static final long POST_TIMEOUT = 30 * 1000;

  public static final String DEFAULT_USER_AGENT = "Isucon5q bench";

  public String scheme;
  public String host;
  public int port;
  public String agent;
  public long runningTime;

  public Config() {
    this.scheme = "http";
    this.host = null;
    this.port = 0;
    this.agent = DEFAULT_USER_AGENT;
    this.runningTime = MAX_RUNNING_TIME;
  }

  public String uri(String path) {
    if (port == 0) {
      return uriDefaultPort(path);
    } else {
      return String.format("%s://%s:%d%s", scheme, host, port, path);
    }
  }

  public String uriDefaultPort(String path) {
    return String.format("%s://%s%s", scheme, host, path);
  }
}
