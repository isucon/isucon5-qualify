package net.isucon.isucon5q.bench;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jetty.client.api.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Checker {
  private Result result;
  private String type;
  private Config config;
  private long responseTime;
  private Response response;

  private String contentBodyHtml;
  private Document parsedDocument;

  private Matcher lastMatch;

  public Checker(Result result, String type, Config config, long responseTime, Response response) {
    this.result = result;
    this.type = type;
    this.config = config;
    this.responseTime = responseTime;
    this.response = response;

    this.contentBodyHtml = null;
    this.parsedDocument = null;
  }

  public void setContentBodyHtml(String html) {
    this.contentBodyHtml = html;
  }

  public Response response() {
    return response;
  }

  public Document document() {
    if (contentBodyHtml == null) {
      throw new IllegalStateException();
    }
    if (parsedDocument != null) {
      return parsedDocument;
    }
    parsedDocument = Jsoup.parse(contentBodyHtml);
    return parsedDocument;
  }

  public boolean hasViolations() {
    return result.violations.size() > 0;
  }

  public void addViolation(String description) {
    result.addViolation(type, description);
  }

  public Matcher lastMatch() {
    return lastMatch;
  }

  public void fatal(String message) {
    addViolation(message);
    throw new Driver.ScenarioAbortException();
  }

  public void respondUntil(long millis) {
    if (responseTime > millis) {
      addViolation(String.format("アプリケーションが %d ミリ秒以内に応答しませんでした", millis));
    }
  }

  public void isStatus(int status) {
    if (response.getStatus() != status) {
      addViolation(String.format("パス '%s' へのレスポンスコード %d が期待されていましたが %d でした", response.getRequest().getPath(), status, response.getStatus()));
    }
  }

  public void isRedirect(String path) {
    int status = response.getStatus();
    if (status != 302 && status != 303 && status != 307) {
      addViolation(String.format("レスポンスコードが一時リダイレクトのもの(302, 303, 307)ではなく %d でした", status));
      return;
    }

    String value = response.getHeaders().get("Location");
    if (value == null) {
      addViolation(String.format("Location ヘッダがありません"));
      return;
    } else if (value.equals(config.uri(path)) || value.equals(config.uriDefaultPort(path))) {
      return; // ok
    }

    URI uri = null;
    try {
      uri = new URI(value);
    } catch (URISyntaxException e) {
      // invalid syntax
    }
    if (uri != null) {
      String h = uri.getHost();
      String p = uri.getPath();
      if (p.isEmpty())
        p = "/";
      if ((h == null || h.equals(config.host)) && p.equals(path))
        return; // ok
    }
    addViolation(String.format("リダイレクト先が %s でなければなりませんが %s でした", path, value));
  }

  public void isContentLength(long bytes) {
    String value = response.getHeaders().get("Content-Length");
    if (value == null) {
      addViolation(String.format("リクエストパス %s に対して Content-Length がありませんでした", response.getRequest().getPath()));
    } else if (Long.parseLong(value) == bytes) {
      // ok
    } else {
      addViolation(String.format("パス %s に対するレスポンスのサイズが正しくありません: %s bytes", response.getRequest().getPath(), value));
    }
  }

  public void hasStyleSheet(String path) {
    Elements es = document().head().getElementsByTag("link");
    if (es.stream().noneMatch(e -> e.attr("rel").equals("stylesheet") && e.attr("href").equals(path))) {
      addViolation(String.format("スタイルシートのパス %s への参照がありません", path));
    }
  }

  public void exist(String selector) {
    if (document().select(selector).size() == 0) {
      addViolation(String.format("指定のDOM要素 '%s' が見付かりません", selector));
    }
  }

  public void exist(String selector, int num) {
    if (document().select(selector).size() != num) {
      addViolation(String.format("指定のDOM要素 '%s' が %d 回表示されるはずですが、正しくありません", selector, num));
    }
  }

  public void missing(String selector) {
    if (document().select(selector).size() > 0) {
      addViolation(String.format("DOM要素 '%s' は表示されないはずですが、表示されています", selector));
    }
  }

  public void content(String selector, String text) {
    Elements es = document().select(selector);
    if (es.stream().noneMatch(e -> e.hasText() && e.text().trim().equals(text))) {
      if (es.size() == 1) {
        addViolation(String.format("DOM要素 '%s' に文字列 '%s' がセットされているはずですが '%s' となっています", selector, text, es.first().text()));
      } else {
        addViolation(String.format("DOM要素 '%s' で文字列 '%s' をもつものが見付かりません", selector, text));
      }
    }
  }

  public void contentMissing(String selector, String text) {
    Elements es = document().select(selector);
    if (es.stream().anyMatch(e -> e.hasText() && e.text().trim().equals(text))) {
      addViolation(String.format("DOM要素 '%s' に文字列 '%s' をもつものは表示されないはずですが、表示されています", selector, text));
    }
  }

  public void contentLongText(String selector, String text) {
    String shrinkText = Arrays.stream(text.split("\n")).map(v -> v.trim()).collect(Collectors.joining());
    Elements es = document().select(selector);
    for (Element e : es) {
      String fullText = Arrays.stream(e.html().trim().split("<(br|BR|Br|bR) */?>"))
        .map(v -> Arrays.stream(v.trim().split("\n")).collect(Collectors.joining()))
        .collect(Collectors.joining(""));
      if (fullText.equals(shrinkText))
        return;
    }
    addViolation(String.format("入力されたはずのテキストがDOM要素 '%s' に表示されていません", selector));
  }

  public void contentMatch(String selector, String regexp) {
    Elements es = document().select(selector);
    if (es.size() == 1) {
      lastMatch = (Pattern.compile(regexp)).matcher(es.first().text());
      if (! lastMatch.matches()) {
        addViolation(String.format("DOM要素 '%s' のテキストが正規表現 '%s' にマッチしません", selector, regexp));
      }
    } else {
      Pattern p = Pattern.compile(regexp);
      boolean match = false;
      for (Element e : es) {
        if (p.matcher(e.text()).matches()) {
          match = true;
          break;
        }
      }
      if (! match)
        addViolation(String.format("DOM要素 '%s' の中に、テキストが正規表現 '%s' にマッチするものが見付かりません", selector, regexp));
    }
  }

  public void contentCheck(String selector, String message, Predicate<Element> callback) {
    boolean ok = false;
    for (Element e : document().select(selector)) {
      if (callback.test(e)) {
        ok = true;
        break;
      }
    }
    if (!ok)
      addViolation(message);
  }

  public void attribute(String selector, String attributeName, String text) {
    Elements es = document().select(selector);
    if (es.stream().noneMatch(e -> e.attr(attributeName).equals(text))) {
      addViolation(String.format("DOM要素 '%s' のattribute %s の内容が '%s' になっていません", selector, attributeName, text));
    }
  }
}
