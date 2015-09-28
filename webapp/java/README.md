
## データベースの準備(開発用)

アプリケーションを起動する前にMySQLを起動してください。

Dockerを使う場合は以下のDockerイメージを使えば良いです。

    $ docker run -d --name mysql -p 3306:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=true -e MYSQL_USER=isucon5q -e MYSQL_PASSWORD=isucon5q -e MYSQL_DATABASE=isucon5q making/mysql


以下のコマンドでデータベースのスキーマを生成してください。

    $ cd ../sql
    $ echo "drop database isucon5q; create database isucon5q" | mysql -u root isucon5q; mysql -u root isucon5q < ../sql/schema.sql;

用意されたGCEイメージではMySQLは設定済みです。

開発用のテストデータは以下のコマンドで投入してください。テストユーザー情報は`create_users.rb`を確認してください。

    $ cd ../ruby
    $ bundle exec ruby create_users.rb

`../sql/isucon5q.dev.sql`を実行すればスキーマ生成+初期データ投入を同時に行えます。


## アプリケーションの実行方法(開発用)

MySQLが起動した状態で

    $ ./mvnw spring-boot:run

を実行すればアプリケーションが起動します(初回の`mvnw`実行時には10分ほど時間がかかります)。[http://localhost:8080](http://localhost:8080)にアクセスしてください。


MySQLの接続情報を変える場合は`src/main/resources/application.properties`を編集するか、以下の環境変数を設定してください。

* `ISUCON5_DB_HOST`
* `ISUCON5_DB_PORT`
* `ISUCON5_DB_USER`
* `ISUCON5_DB_PASSWORD`
* `ISUCON5_DB_NAME`


## IDEにインポート(開発用)

インポートする前に必ず以下を実行してください。静的ファイルを`../static`からクラスパス(`target`)にコピーするためです。

    $ ./mvnw compile

`./mvnw spring-boot:run`を実行済みの場合は、上記コマンドを実行する必要はありません。

開発中は`isucon5.App`クラスの`main`メソッドを実行すれば良いです。

`../sql`のファイルもクラスパス(`target`)にコピーされるので、`src/main/resources/application.properties`を以下のように変更すると、
アプリケーション起動のたびにデータベースが初期化されます。必要に応じて開発中に利用してください。

``` diff
- spring.datasource.initialize=false
- #spring.datasource.schema=classpath:isucon5q.dev.sql
+ spring.datasource.initialize=true
+ spring.datasource.schema=classpath:isucon5q.dev.sql
```

## 実行可能jarの作り方

    $ ./mvnw clean package

作成したjarファイルは以下のコマンドで実行可能です。

    $ java -jar target/isuxi-0.0.1-SNAPSHOT.jar

## systemdにサービス登録

この実行可能jarファイルはそのままsystemdのスクリプトから実行できます。

    $ cat <<'EOF' | sudo tee /etc/systemd/system/isuxi.java.service
    [Unit]
    Description=isuxi-java
    After=syslog.target
    
    [Service]
    Environment="_JAVA_OPTIONS=-Djava.security.egd=file:/dev/urandom" 
    ExecStart=/home/isucon/webapp/java/target/isuxi-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
    
    [Install]
    WantedBy=multi-user.target
    
    $ sudo systemctl daemon-reload
    $ sudo systemctl enable isuxi.java.service
    $ sudo systemctl start isuxi.java.service
    $ sudo systemctl status isuxi.java.service

**ここまでは用意されたGCEイメージでは設定済みです。**

実行時に[Spring Bootのプロパティ](http://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)を変えたい場合は
`ExecStart=/home/isucon/webapp/java/target/isuxi-0.0.1-SNAPSHOT.jar --server.port=80`のように`.jar`の後ろにパラメータを指定してください。

`/etc/systemd/system/isuxi.java.service`を変更したら

    $ sudo systemctl daemon-reload
    $ sudo systemctl restart isuxi.java.service

で反映&再起動してください。

JVMの起動オプションは`/etc/systemd/system/isuxi.java.service`内の`_JAVA_OPTIONS`に設定してください。

ログは`/var/log/syslog`に吐かれますが、特定のファイルに出力したい場合は、
`--logging.file=/tmp/isuxi.log`のようにパラメータを指定してください。

サーバー側でアプリケーションを修正した場合は以下の手順で反映してください。

* `./mvnw clean package`
* `sudo systemctl restart isuxi.java.service`

クライアント(開発環境)でアプリケーションを修正した場合は、jarをビルドしてサーバーに再配置してください

* クライアント側
    * `export GCE_IP=<GCEのIP>`
    * `./mvnw clean package`
    * `cat target/isuxi-0.0.1-SNAPSHOT.jar | ssh -i ../../gcp/image/keys/root_id_rsa  root@$GCE_IP "sudo -u isucon cat - > /home/isucon/webapp/java/target/isuxi-0.0.1-SNAPSHOT.jar"`
* サーバー側
    * `sudo systemctl restart isuxi.java.service`

`--spring.profiles.active=prod`を指定して`prod`プロファイルを有効にしているので、
本番のみ適用したいプロパティを`src/main/resources/application-prod.properties`に定義しておくことも可能です。

## おまけ(APサーバーの変更)

デフォルトではTomcatが使用されますが、変更することもできます。
pom.xmlを変更した後は`./mvnw clean package`を実行し、再起動してください。

### Jettyに変更

``` diff
         <dependency>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-starter-web</artifactId>
+            <exclusions>
+                <exclusion>
+                    <groupId>org.springframework.boot</groupId>
+                    <artifactId>spring-boot-starter-tomcat</artifactId>
+                </exclusion>
+            </exclusions>
+        </dependency>
+        <dependency>
+            <groupId>org.springframework.boot</groupId>
+            <artifactId>spring-boot-starter-jetty</artifactId>
         </dependency>
```

### Undertowに変更

``` diff
         <dependency>
             <groupId>org.springframework.boot</groupId>
             <artifactId>spring-boot-starter-web</artifactId>
+            <exclusions>
+                <exclusion>
+                    <groupId>org.springframework.boot</groupId>
+                    <artifactId>spring-boot-starter-tomcat</artifactId>
+                </exclusion>
+            </exclusions>
+        </dependency>
+        <dependency>
+            <groupId>org.springframework.boot</groupId>
+            <artifactId>spring-boot-starter-undertow</artifactId>
         </dependency>
```
