## 環境

go version go1.5 linux/amd64

Goの実行環境は `/home/isucon/.local/go/` にインストールされています。

イメージ起動時点では

```
GOROOT=/home/isucon/.local/go
GOPATH=/home/isucon/webapp/go
```

となっています。

`/home/isucon/webapp/go` 以下のディレクトリ構成は変則ですが、`app.go` はディレクトリ直下にあります。
各種ライブラリは通常通り `src` 以下にあります。


## ビルド

systemd の初期設定では `/home/isucon/webapp/go/app` を起動するようになっています。
このため、`app` という名前の実行ファイルをビルドする必要があります。

```
go build -o app
```

もちろん、systemd側の設定を変更して、好きな名前の実行ファイルを使うことも可能です。



## 実行

Goアプリはsystemdに登録されており、systemdのコマンドで起動や停止が出来ます。

```
sudo systemctl start isuxi.go
sudo systemctl stop isuxi.go
```

パラメータ等はsystemdのファイル `/etc/systemd/system/isuxi.go.service` を参照してください。

> イメージ起動時点ではRubyが起動しているので、先にRubyの停止をしないとGoが起動しません
