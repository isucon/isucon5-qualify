# ISUCON5 予選リポジトリ

ISUCON5 予選マニュアル(含レギュレーション詳細)： https://gist.github.com/tagomoris/1a2df5ab0999f5e64cff

ISUCON5 予選用イメージ(2日目版)

* https://storage.googleapis.com/isucon5-images/isucon5-qualifier-4.image.tar.gz
* gs://isucon5-images/isucon5-qualifier-4.image.tar.gz

予選は参加者、ベンチマークノード共に Google Compute Engine n1-highcpu-4 をインスタンスタイプとして指定。

## 補足事項

* apt-get upgrade すると再起動後に正常動作しなくなります ([詳細はこちら](http://isucon.net/archives/45532743.html))
* 「Java, Golangについては初期状態でベンチマークのチェックを通過しません」とレギュレーションに付記してありますが、おそらく通過します
  * 26日(土曜)の予選時間中、早い段階において参加者から指摘のあったベンチマークシナリオのバグを修正した結果通過するようになったはずです
  * ただし1日目参加者と2日目参加者の公平性のため、ここについては記述を修正しませんでした
* PHPについては、1日目「参加時の使用に耐えません」と記述していたものが、26日にベンチマークを通るよう修正ができたため、2日目にはイメージと記述を修正しています
  * これについては、取捨選択の上で、より多くの参加者への便宜を優先しました
  * 結果的に1日目参加者のPHPユーザに対しては不公平な運営になったかもしれません

## リポジトリ内容

* `eventapp/` 予選ポータル用Webアプリケーション
* `bench/` ベンチマークツール + 実シナリオ + ノード上のagentプロセススクリプト
* `gcp/` GCP上での各サーバのイメージ作成/プロビジョニング用ファイル/ツール類
  * rootユーザでansible provisioningを実行するため、リポジトリに秘密鍵を持ち接続に使用しています
  * 絶対にこれを用いてプロビジョニングしたサーバをそのまま放置しないでください(予選参加者用イメージ等はこれに対応する公開鍵を削除しています)
  * Githubからのアプリケーションのデプロイに用いていたDeploy keyがリポジトリに含まれていますが、これは既に無効です
  * 利用者がそれぞれDeploy keyを作成し、差し替えて用いてください
* `webapp/` 予選用各言語参考実装ファイル等
  * `webapp/ruby/create_users.rb` 開発時用 小容量ダミーデータ生成スクリプト
  * `webapp/script` 予選用本番データ生成スクリプト類
  * `webapp/sql` schema.sql 以外は開発時用の各種SQL

## ベンチマークツール

Java8およびgradleが必要。 `bench/` 以下において `gradle compileJava` でビルド、`gradle run -Pargs="SCENARIO_CLASS TARGET_IP_ADDRESS"` でベンチマーク実行。なおこのツールは標準入力からJSON形式で必要なテストセットデータを読み込みます。
`webapp/script/testsets/testsets.json` が30回分のテストデータを配列形式でまとめたデータのJSON表現となっていますので `parse(jsonText)[0].to_json` 的な変換をすれば1回のベンチマークに必要な入力データが得られます。

結果がJSONで出力されるが、これは直接のスコアを含まない。スコアの計算式は `eventapp/lib/score.rb` に存在するが、単純なのでベンチマークツールの出力を見てもほぼ推定できるはず。

## License

The MIT License (MIT)

Copyright (c) 2015 tagomoris, kamipo, najeira, hkurokawa, making, xerial

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
