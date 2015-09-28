package net.isucon.isucon5q.bench;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.util.HttpCookieStore;

import java.util.List;
import java.util.ArrayList;
import java.net.HttpCookie;
import java.lang.reflect.InvocationTargetException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Runner {
  private static final int MAX_CONNECTIONS_PER_DEST = 2048;
  private static final int MAX_QUEUED_REQUESTS_PER_DEST = 512;

  private static final String DEFAULT_PARAMETER_CLASS = "net.isucon.isucon5q.bench.I5Parameter";

  private Class rootClass;

  private boolean running;

  public Config config;

  public static Options commandLineOptions() {
    Options options = new Options();
    options.addOption("a", true, "User-Agent string");
    options.addOption("p", true, "Port number");
    options.addOption("P", true, "Parameter class specification");
    options.addOption("t", true, "Timeout seconds for benchmark (default: 120)");
    options.addOption("h", "help", false, "Show this message");

    return options;
  }

  public static void main(String[] args) throws ClassNotFoundException {
    Options options = commandLineOptions();
    CommandLine cmd;
    try {
	    cmd = (new DefaultParser()).parse(options, args);
    } catch (ParseException e) {
	    showHelpAndExit(options);
	    return;
    }
    if (cmd.hasOption("h")) {
      showHelpAndExit(options);
    }

    if (cmd.getArgs().length != 2) {
      throw new IllegalArgumentException(String.format("Runner requires 2 arguments: %d", cmd.getArgs().length));
    }

    String rootClassName = cmd.getArgs()[0];

    String host = cmd.getArgs()[1];

    Class root = Class.forName(rootClassName);
    Runner runner = new Runner(root, host);

    if (cmd.hasOption("p")) {
      runner.config.port = Integer.parseInt(cmd.getOptionValue("p"));
    }

    if (cmd.hasOption("a")) {
      runner.config.agent = cmd.getOptionValue("a");
    }
    if (cmd.hasOption("t")) {
      runner.config.runningTime = Long.parseLong(cmd.getOptionValue("t"));
    }

    String paramClassName = DEFAULT_PARAMETER_CLASS;
    if (cmd.hasOption("P")) {
      paramClassName = cmd.getOptionValue("P");
    }

    System.err.println("reading stdin");

    String jsonInput = readFromStdIn();

    System.err.println("got data");

    List<Parameter> params = Parameter.generate(paramClassName, jsonInput);

    System.err.format("data number: %d%n", params.size());

    System.err.println("start");
    runner.execute(params);
    System.err.println("done");
  }

  public Runner(Class root, String host) {
    this.rootClass = root;
    config = new Config();
    config.host = host;
  }

  private static void showHelpAndExit(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("gradle run -Pargs='[options] ScenarioClass HOST'", options );
    System.exit(1);
  }

  private static String readFromStdIn() {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    StringBuffer buf = new StringBuffer();
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        if (! line.equals(""))
          buf.append(line);
      }
    } catch (IOException e) {
      System.err.println("Read error from STDIN");
      System.exit(1);
    }
    return buf.toString();
  }

  private HttpClient client() {
    HttpField agent = new HttpField("User-Agent", config.agent);

    HttpClient httpClient = new HttpClient();
    httpClient.setFollowRedirects(false);
    httpClient.setMaxConnectionsPerDestination(MAX_CONNECTIONS_PER_DEST);
    httpClient.setMaxRequestsQueuedPerDestination(MAX_QUEUED_REQUESTS_PER_DEST);
    httpClient.setUserAgentField(agent);
    httpClient.setCookieStore(new HttpCookieStore.Empty());
    return httpClient;
  }

  private Scenario getRootInstance(Class root) {
    Long timeout = new Long(config.runningTime);
    Scenario sc = null;
    try {
      sc = (Scenario) rootClass
        .getConstructor(Long.class)
        .newInstance(timeout);
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      System.err.format("Failed to create instance of Scenario: %s%n", rootClass);
      System.err.format("Error %s: %s", e.getClass(), e.getMessage());
    }
    return sc;
  }

  public void execute(List<Parameter> params) {
    Scenario root = getRootInstance(rootClass);
    if (root == null) {
      System.exit(1);
    }

    HttpClient client = client();

    try {
      client.start();
    } catch (Exception e) {
      throw new RuntimeException(String.format("failed to start httpClient, %s: %s", e.getClass().getName(), e.getMessage()));
    }

    Result r = root.run(client, config, params);

    try {
      client.stop();
    } catch (Exception e) {
      throw new RuntimeException(String.format("failed to stop httpClient, %s: %s", e.getClass().getName(), e.getMessage()));
    }
    System.out.println(r.toJSON());
  }
}
