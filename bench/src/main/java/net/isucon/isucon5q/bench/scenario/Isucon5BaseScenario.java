package net.isucon.isucon5q.bench.scenario;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import java.net.URI;
import java.net.URISyntaxException;

import net.isucon.isucon5q.bench.Scenario;
import net.isucon.isucon5q.bench.Parameter;
import net.isucon.isucon5q.bench.Session;

import net.isucon.isucon5q.bench.I5Parameter;

public class Isucon5BaseScenario extends Scenario {
  public Isucon5BaseScenario(Long timeout) {
    super(timeout);
  }

  protected String accountName(Session session) {
    return ((I5Parameter) session.param()).accountName;
  }

  protected Map formLogin(Session session) {
    I5Parameter p = (I5Parameter) session.param();
    Map form = new HashMap();

    form.put("email", p.email);
    form.put("password", p.password);
    return form;
  }

  protected Map formComment(Random random) {
    Map form = new HashMap();
    form.put("comment", String.valueOf(random.nextInt(Integer.MAX_VALUE)));
    return form;
  }

  protected Map formEntry(Random random) {
    Map form = new HashMap();
    form.put("title", String.valueOf(random.nextInt(Integer.MAX_VALUE)));
    int lines = random.nextInt(4);
    StringBuffer buf = new StringBuffer();
    for (int i = 0 ; i < lines ; i++) {
      buf.append(String.valueOf(random.nextInt(Integer.MAX_VALUE)));
      buf.append("\n");
    }
    form.put("content", buf.toString());
    if (random.nextInt(2) == 0)
      form.put("private", "1");
    return form;
  }
}

