## 環境

Python 3.4.3

Pythonは `/home/isucon/.local/python3/` にインストールされています。
イメージ起動時点ではこのPython実行環境が使用されます(OS側のPythonではありません)。

参考実装が使用している各種ライブラリも、ここにインストール済みです。
`pip` や `easy_install` も上記の `bin` 以下にあります。

もちろん、自前でPythonの環境をセットアップし、そちらを使うようにするのは可能です。


## 実行

Pythonアプリはsystemdに登録されており、systemdのコマンドで起動や停止が出来ます。

```
sudo systemctl start isuxi.python
sudo systemctl stop isuxi.python
```

パラメータ等はsystemdのファイル `/etc/systemd/system/isuxi.python.service` を参照してください。

> イメージ起動時点ではRubyが起動しているので、先にRubyの停止をしないとPythonが起動しません
