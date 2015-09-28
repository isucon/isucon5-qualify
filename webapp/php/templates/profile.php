<h2><?php h($owner['nick_name']) ?>さんのプロフィール</h2>
<div class="row" id="prof">
  <dl class="panel panel-primary">
    <dt>アカウント名</dt><dd id="prof-account-name"><?php h($owner['account_name']) ?></dd>
    <?php if ($private) { ?>
    <dt>メールアドレス</dt><dd id="prof-email"><?php h($owner['email']) ?></dd>
    <?php } ?>
    <dt>姓</dt><dd id="prof-last-name"><?php h($profile['last_name'] ?: '未入力') ?></dd>
    <dt>名</dt><dd id="prof-first-name"><?php h($profile['first_name'] ?: '未入力') ?></dd>
    <?php if ($private) { ?>
    <dt>性別</dt><dd id="prof-sex"><?php h($profile['sex'] ?: '未入力') ?></dd>
    <dt>誕生日</dt><dd id="prof-birthday"><?php h($profile['birthday'] ?: '未入力') ?></dd>
    <dt>住んでいる県</dt><dd id="prof-pref"><?php h($profile['pref'] ?: '未入力') ?></dd>
    <?php } ?>
  </dl>
</div>

<h2><?php h($owner['nick_name']) ?>さんの日記</h2>
<div class="row" id="prof-entries">
  <?php foreach ($entries as $entry) { ?>
  <?php if (!$entry['is_private'] || $private) { ?>
  <div class="panel panel-primary entry">
    <div class="entry-title">タイトル: <a href="/diary/entry/<?php h($entry['id']) ?>"><?php h($entry['title']) ?></a></div>
    <div class="entry-content">
      <?php foreach (preg_split('/\n/', mb_substr($entry['content'], 0, 60)) as $line) { ?>
      <?php h($line) ?><br />
      <?php } ?>
    </div>
    <div class="entry-created-at">更新日時: <?php h($entry['created_at']) ?></div>
  </div>
  <?php } ?>
  <?php } ?>
</div>

<?php if (current_user()['id'] == $owner['id']) { ?>
<h2>プロフィール更新</h2>
<div id="profile-post-form">
  <form method="POST" action="/profile/<?php h(current_user()['account_name']) ?>">
    <div>名字: <input type="text" name="last_name" placeholder="みょうじ" value="<?php h($profile['last_name']) ?>" /></div>
    <div>名前: <input type="text" name="first_name" placeholder="なまえ" value="<?php h($profile['first_name']) ?>" /></div>
    <div>性別:
      <select name="sex">
        <option>未指定</option>
        <option <?php h($profile['sex'] == "男性" ? 'selected' : '') ?>>男性</option>
        <option <?php h($profile['sex'] == "女性" ? 'selected' : '') ?>>女性</option>
        <option <?php h($profile['sex'] == "その他" ? 'selected' : '') ?>>その他</option>
      </select>
    </div>
    <div>誕生日:
      <input type="date" name="birthday" min="1915-01-01" max="2014-12-31" value="<?php h($profile['birthday'] ? strftime('%Y-%m-%d', strtotime($profile['birthday'])) : "2000-01-01") ?>" />
    </div>
    <div>住んでいる県:
      <select name="pref">
        <?php foreach (prefectures() as $pref) { ?>
        <option <?php h($profile['pref'] == $pref ? 'selected' : '') ?>><?php h($pref) ?></option>
        <?php } ?>
      </select>
    </div>
    <div><input type="submit" value="更新" /></div>
  </form>
</div>
<?php } elseif (!is_friend($owner['id'])) { ?>
<h2>あなたは友だちではありません</h2>
<div id="profile-friend-form">
  <form method="POST" action="/friends/<?php h($owner['account_name']) ?>">
    <input type="submit" value="このユーザと友だちになる" />
  </form>
</div>
<?php } ?>
