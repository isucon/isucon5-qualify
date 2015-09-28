<h2>ISUxi index</h2>
<div class="row panel panel-primary" id="prof">
  <div class="col-md-12 panel-title" id="prof-nickname"><?php h($user['nick_name']) ?></div>
  <div class="col-md-12"><a href="/profile/<?php h($user['account_name']) ?>">プロフィール</a></div>
  <div class="col-md-4">
    <dl>
      <dt>アカウント名</dt><dd id="prof-account-name"><?php h($user['account_name']) ?></dd>
      <dt>メールアドレス</dt><dd id="prof-email"><?php h($user['email']) ?></dd>
      <dt>姓</dt><dd id="prof-last-name"><?php h($profile['last_name']) ?: '未入力' ?></dd>
      <dt>名</dt><dd id="prof-first-name"><?php h($profile['first_name']) ?: '未入力' ?></dd>
      <dt>性別</dt><dd id="prof-sex"><?php h($profile['sex']) ?: '未入力' ?></dd>
      <dt>誕生日</dt><dd id="prof-birthday"><?php h($profile['birthday']) ?: '未入力' ?></dd>
      <dt>住んでいる県</dt><dd id="prof-pref"><?php h($profile['pref']) ?: '未入力' ?></dd>
      <dt>友だちの人数</dt><dd id="prof-friends"><a href="/friends"><?php h(sizeof($friends)) ?>人</a></dd>
    </dl>
  </div>
  <div class="col-md-4">
    <div id="entries-title"><a href="/diary/entries/<?php h($user['account_name']) ?>">あなたの日記エントリ</a></div>
    <div id="entries">
      <ul class="list-group">
        <?php foreach ($entries as $entry) { ?>
        <li class="list-group-item entries-entry"><a href="/diary/entry/<?php h($entry['id']) ?>"><?php h(preg_split('/\n/', $entry['body'])[0]) ?></a></li>
        <?php } ?>
      </ul>
    </div>
  </div>

  <div class="col-md-4">
    <div><a href="/footprints">あなたのページへの足あと</a></div>
    <div id="footprints">
      <ul class="list-group">
        <?php foreach ($footprints as $fp) { ?>
        <?php $owner = get_user($fp['owner_id']) ?>
        <li class="list-group-item footprints-footprint"><?php h($fp['updated']) ?>: <a href="/profile/<?php h($owner['account_name']) ?>"><?php h($owner['nick_name']) ?>さん</a>
        <?php } ?>
      </ul>
    </div>
  </div>
</div>

<div class="row panel panel-primary">
<div class="col-md-4">
  <div>あなたへのコメント</div>
  <div id="comments">
    <?php foreach ($comments_for_me as $comment) { ?>
    <div class="comments-comment">
      <ul class="list-group">
        <?php $comment_user = get_user($comment['user_id']) ?>
        <li class="list-group-item comment-owner"><a href="/profile/<?php h($comment_user['account_name']) ?>"><?php h($comment_user['nick_name']) ?>さん</a>:
        <li class="list-group-item comment-comment"><?php h(mb_strlen($comment['comment']) > 30 ? mb_substr($comment['comment'], 0, 27) . '...' : $comment['comment']) ?>
        <li class="list-group-item comment-created-at">投稿時刻:<?php h($comment['created_at']) ?>
      </ul>
    </div>
    <?php } ?>
  </div>
</div>

<div class="col-md-4">
  <div>あなたの友だちの日記エントリ</div>
  <div id="friend-entries">
    <?php foreach ($entries_of_friends as $entry) { ?>
    <div class="friend-entry">
      <ul class="list-group">
        <?php $entry_owner = get_user($entry['user_id']) ?>
        <li class="list-group-item entry-owner"><a href="/diary/entries/<?php h($entry_owner['account_name']) ?>"><?php h($entry_owner['nick_name']) ?>さん</a>:
        <li class="list-group-item entry-title"><a href="/diary/entry/<?php h($entry['id']) ?>"><?php h(preg_split('/\n/', $entry['body'])[0]) ?></a>
        <li class="list-group-item entry-created-at">投稿時刻:<?php h($entry['created_at']) ?>
      </ul>
    </div>
    <?php } ?>
  </div>
</div>

<div class="col-md-4">
  <div>あなたの友だちのコメント</div>
  <div id="friend-comments">
    <?php foreach ($comments_of_friends as $comment) { ?>
    <div class="friend-comment">
      <ul class="list-group">
        <?php $comment_owner = get_user($comment['user_id']) ?>
        <?php $entry = db_execute('SELECT * FROM entries WHERE id=?', array($comment['entry_id']))->fetch() ?>
        <?php $entry_owner = get_user($entry['user_id']) ?>
        <li class="list-group-item comment-from-to">
          <a href="/profile/<?php h($comment_owner['account_name']) ?>"><?php h($comment_owner['nick_name']) ?>さん</a>から
          <a href="/profile/<?php h($entry_owner['account_name']) ?>"><?php h($entry_owner['nick_name']) ?>さん</a>へのコメント:
        <li class="list-group-item comment-comment"><?php h(mb_strlen($comment['comment']) > 30 ? mb_substr($comment['comment'], 0, 27) . '...' : $comment['comment']) ?>
        <li class="list-group-item comment-created-at">投稿時刻:<?php h($comment['created_at']) ?>
      </ul>
    </div>
    <?php } ?>
  </div>
</div>
</div>
