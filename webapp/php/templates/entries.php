<h2><?php h($owner['nick_name']) ?>さんの日記</h2>
<?php if ($myself) { ?>
<div class="row" id="entry-post-form">
  <form method="POST" action="/diary/entry">
    <div class="col-md-4 input-group">
      <span class="input-group-addon">タイトル</span>
      <input type="text" name="title" />
    </div>
    <div class="col-md-4 input-group">
      <span class="input-group-addon">本文</span>
      <textarea name="content" ></textarea>
    </div>
    <div class="col-md-2 input-group">
      <span class="input-group-addon">
        友だちのみに限定<input type="checkbox" name="private" />
      </span>
    </div>
    <div class="col-md-1 input-group">
      <input class="btn btn-default" type="submit" value="送信" />
    </div>
  </form>
</div>
<?php } ?>

<div class="row" id="entries">
  <?php foreach ($entries as $entry) { ?>
  <div class="panel panel-primary entry">
    <div class="entry-title">タイトル: <a href="/diary/entry/<?php h($entry['id']) ?>"><?php h($entry['title']) ?></a></div>
    <div class="entry-content">
      <?php foreach (preg_split('/\n/', $entry['content']) as $line) { ?>
      <?php h($line) ?><br />
      <?php } ?>
    </div>
    <?php if ($entry['is_private']) { ?><div class="text-danger entry-private">範囲: 友だち限定公開</div><?php } ?>
    <div class="entry-created-at">更新日時: <?php h($entry['created_at']) ?></div>
    <div class="entry-comments">コメント: <?php h(db_execute('SELECT COUNT(*) AS c FROM comments WHERE entry_id = ?', array($entry['id']))->fetch()['c']) ?>件</div>
  </div>
  <?php } ?>
</div>
