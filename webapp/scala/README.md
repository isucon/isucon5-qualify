isucon5-qualify-scala
===

Scala-based example application for the ISUCON5 qualification round.

ISUCON5予選ラウンドアプリケーションのScalaによる参考実装です。

## Development notes (開発ノート)

To start the development, use `~container:restart` command.
With this command you can automatically reload the web application as you modify the source code:

開発を始めるには、`~container:restart`コマンドを使用します。
コードを修正するたびにウェブアプリケーションがリロードされます。

```
$ ./sbt
> ~container:restart
```

You can access the web application from <http://localhost:8080/>

起動したウェブアプリケーションには<http://localhost:8080/> からアクセスできます。

### Run as a standalone web server　スタンドアローンサーバーとして起動する
```
# Generates a start-up script of your web application　ウェブアプリケーションの起動スクリプトを生成
$ ./sbt launcher

# Launch a jetty-based web server　Jettyウェブサーバーを起動
$ ./target/launcher

# Change the port number 使用するポート番号を変更する
$ ./target/launcher 8081

```

To use your own Java-based web server (e.g., Tomcat), you can create a war (web archive) file with `sbt package` command:

独自のJavaウェブサーバー（Tomcatなど）を利用するために、warファイルを`sbt package`コマンドで生成することもできます。
```
$ ./sbt package
```

