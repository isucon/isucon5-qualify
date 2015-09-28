# GCE image の作り方

## インスタンスを立てる

事前に gcloud コマンドが使えるよう設定しておくこと。

```sh
./pack.rb run_instance --name [key_name]
```

このとき `tmp/instance_id` に起動したインスタンスの情報がJSONで吐かれる。以降のコマンドはデフォルトではこれが対象インスタンスとして使われる。

## セットアップ

```sh
./pack.rb provision
```
Ansible で `ansible/` 以下のplaybookがプロビジョンされる。

## インスタンスに SSH で入りたいとき

```sh
./pack.rb ssh
```

## イメージ作成

以下の方法でがんばって作る。

    # https://cloud.google.com/compute/docs/images#export_an_image_to_google_cloud_storage

    gcloud compute disks create temporary-disk --zone ZONE
    gcloud compute instances attach-disk example-instance --disk temporary-disk \
        --device-name temporary-disk \
        --zone ZONE
    ./pack.rb ssh
    $ sudo mkdir /mnt/tmp
    $ sudo /usr/share/google/safe_format_and_mount -m "mkfs.ext4 -F" /dev/sdb /mnt/tmp
    $ sudo gcimagebundle -d /dev/sda -o /mnt/tmp/ --log_file=/tmp/abc.log
    $ gsutil mb gs://BUCKET_NAM
    $ gsutil cp /mnt/tmp/IMAGE_NAME.image.tar.gz gs://BUCKET_NAME

なお予選用イメージの最終版を作るときには `ansible/_cleanup.yml` を完成させ実行してからイメージを作成すること。

## インスタンスを消す

```sh
./pack.rb terminate
```

`tmp/instance_id` も消える。

