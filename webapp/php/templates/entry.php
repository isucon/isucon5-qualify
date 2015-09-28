<h2><?php h($owner['nick_name']) ?>さんの日記</h2>
<div class="row panel panel-primary" id="entry-entry">
  <div class="entry-title">タイトル: <a href="/diary/entry/<?php h($entry['id']) ?>"><?php h($entry['title']) ?></a></div>
  <div class="entry-content">
    <?php foreach (preg_split('/\n/', $entry['content']) as $line) { ?>
    <?php h($line) ?><br />
    <?php } ?>
  </div>
  <?php if ($entry['is_private']) { ?><div class="text-danger entry-private">範囲: 友だち限定公開</div><?php } ?>
  <div class="entry-created-at">更新日時: <?php h($entry['created_at']) ?></div>
</div>

<h3>この日記へのコメント</h3>
<div class="row panel panel-primary" id="entry-comments">
  <?php foreach ($comments as $comment) { ?>
  <div class="comment">
    <?php $comment_user = get_user($comment['user_id']) ?>
    <div class="comment-owner"><a href="/profile/<?php h($comment_user['account_name']) ?>"><?php h($comment_user['nick_name']) ?>さん</a></div>
    <div class="comment-comment">
      <?php foreach (preg_split('/\n/', $comment['comment']) as $line) { ?>
      <?php h($line) ?><br />
      <?php } ?>
    </div>
    <div class="comment-created-at">投稿時刻:<?php h($comment['created_at']) ?></div>
  </div>
  <?php } ?>
</div>

<h3>コメントを投稿</h3>
<div id="entry-comment-form">
  <form method="POST" action="/diary/comment/<?php h($entry['id']) ?>">
    <div>コメント: <textarea name="comment" ></textarea></div>
    <div><input type="submit" value="送信" /></div>
  </form>
</div>
