package net.isucon.isucon5q.bench;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;

import java.net.HttpCookie;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;

public class Session {
  private Parameter param;

  private List<HttpCookie> cookies;

  public Session(Parameter param) {
    this.param = param;
    this.cookies = new ArrayList<HttpCookie>();
  }

  public Parameter param() {
    return param;
  }

  public void writeCookie(Request req) {
    List<HttpCookie> existing = req.getCookies();
    synchronized(cookies) {
      for (HttpCookie cookie : cookies) {
        boolean exists = false;
        for (HttpCookie e : existing) {
          if (e.getName().equals(cookie.getName())) {
            e.setValue(cookie.getValue());
            exists = true;
            break;
          }
        }
        if (! exists) {
          req.cookie(cookie);
        }
      }
    }
  }

  public void readCookie(Response res) {
    synchronized(cookies) {
      for (Enumeration<String> i = res.getHeaders().getValues("Set-Cookie"); i.hasMoreElements();) {
        for (HttpCookie cookie : HttpCookie.parse(i.nextElement())) {
          boolean exists = false;
          for (HttpCookie e : cookies) {
            if (e.getName().equals(cookie.getName())) {
              e.setValue(cookie.getValue());
              exists = true;
              break;
            }
          }
          if (! exists)
            cookies.add(cookie);
        }
      }
    }
  }
}
