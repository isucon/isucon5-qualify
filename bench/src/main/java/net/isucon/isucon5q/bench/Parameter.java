package net.isucon.isucon5q.bench;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class Parameter {
  public abstract String[] keys();

  public abstract void put(String name, String value);

  public static List<Parameter> generate(String className, String json) throws ClassNotFoundException {
    if (json.equals("dummy")) {
      return dummyParameters(className);
    }
    Class paramClass = Class.forName(className);

    List<Parameter> list = new ArrayList<Parameter>();

    JsonNode root = null;
    try {
      root = (new ObjectMapper()).readTree(json);
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse parameter json");
    }

    if (root.isArray()) {
      for (JsonNode n : root) {
        list.add(getParameterFromJsonNode(paramClass, n));
      }
    } else {
      // just single parameter object
      list.add(getParameterFromJsonNode(paramClass, root));
    }

    return list;
  }

  private static Parameter getParameterFromJsonNode(Class paramClass, JsonNode n) {
    Parameter p = getInstance(paramClass);
    for (String k : p.keys()) {
      JsonNode j = n.get(k);
      if (j != null)
        p.put(k, j.asText());
    }
    return p;
  }

  private static Parameter getInstance(Class klass) {
    Parameter p = null;
    try {
      p = (Parameter) klass.getConstructor().newInstance();
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(String.format("Cannot create instance of %s", klass.getName()));
    }
    return p;
  }

  public static List<Parameter> dummyParameters(String className) throws ClassNotFoundException {
    String dummy = "[" +
      "{\"account_name\":\"tagomoris\",\"nick_name\":\"モリス\",\"email\":\"moris@tagomor.is\",\"password\":\"moris11\",\"first_name\":\"さとし\",\"last_name\":\"たごもり\",\"birthday\":\"2001-08-18\",\"sex\":\"男性\",\"pref\":\"北海道\"}" + "," +
      "{\"account_name\":\"kamipo\",\"nick_name\":\"かみぽ\",\"email\":\"kamipo@kamipopo.po\",\"password\":\"po1\",\"first_name\":\"りゅうた\",\"last_name\":\"かみぞの\",\"birthday\":\"2011-06-13\",\"sex\":\"その他\",\"pref\":\"山形県\"}" + "," +
      "{\"account_name\":\"bbbbbbb\",\"nick_name\":\"b\",\"email\":\"b@b.net\",\"password\":\"bb\",\"first_name\":\"もう\",\"last_name\":\"つかれてきた\",\"birthday\":\"2008-12-03\",\"sex\":\"その他\",\"pref\":\"北海道\"}" +
      "]";
    return generate(className, dummy);
  }
}
