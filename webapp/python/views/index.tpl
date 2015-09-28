% rebase("layout")
<h2>ISUxi index</h2>
<div class="row panel panel-primary" id="prof">
  <div class="col-md-12 panel-title" id="prof-nickname">{{owner["nick_name"]}}</div>
  <div class="col-md-12"><a href="/profile/{{owner["account_name"]}}">プロフィール</a></div>
  <div class="col-md-4">
    <dl>
      <dt>アカウント名</dt><dd id="prof-account-name">{{owner["account_name"]}}</dd>
      <dt>メールアドレス</dt><dd id="prof-email">{{owner["email"]}}</dd>
      <dt>姓</dt><dd id="prof-last-name">{{profile.get("last_name") or "未入力"}}</dd>
      <dt>名</dt><dd id="prof-first-name">{{profile.get("first_name") or "未入力"}}</dd>
      <dt>性別</dt><dd id="prof-sex">{{profile.get("sex") or "未入力"}}</dd>
      <dt>誕生日</dt><dd id="prof-birthday">{{profile.get("birthday") or "未入力"}}</dd>
      <dt>住んでいる県</dt><dd id="prof-pref">{{profile.get("pref") or "未入力"}}</dd>
      <dt>友だちの人数</dt><dd id="prof-friends"><a href="/friends">{{len(friends)}}人</a></dd>
    </dl>
  </div>
  <div class="col-md-4">
    <div id="entries-title"><a href="/diary/entries/{{owner["account_name"]}}">あなたの日記エントリ</a></div>
    <div id="entries">
      <ul class="list-group">
        % for entry in entries:
          <li class="list-group-item entries-entry"><a href="/diary/entry/{{entry["id"]}}">{{entry["body"].split("\n")[0]}}</a></li>
        % end
      </ul>
    </div>
  </div>
  <div class="col-md-4">
    <div><a href="/footprints">あなたのページへの足あと</a></div>
    <div id="footprints">
      <ul class="list-group">
        % for fp in footprints:
          % fp_owner = get_user(fp["owner_id"])
          <li class="list-group-item footprints-footprint">{{fp["updated"]}}: <a href="/profile/{{fp_owner["account_name"]}}">{{fp_owner["nick_name"]}}さん</a>
        % end
      </ul>
    </div>
  </div>
</div>

<div class="row panel panel-primary">
<div class="col-md-4">
  <div>あなたへのコメント</div>
  <div id="comments">
    % for comment in comments_for_me:
      <div class="comments-comment">
        <ul class="list-group">
          % comment_user = get_user(comment["user_id"])
          <li class="list-group-item comment-owner"><a href="/profile/{{comment_user["account_name"]}}">{{comment_user["nick_name"]}}さん</a>:
          <li class="list-group-item comment-comment">{{comment["comment"][:27] + '...' if len(comment["comment"]) > 30 else comment["comment"]}}
          <li class="list-group-item comment-created-at">投稿時刻:{{comment["created_at"]}}
        </ul>
      </div>
    % end
  </div>
</div>

<div class="col-md-4">
  <div>あなたの友だちの日記エントリ</div>
  <div id="friend-entries">
    % for entry in entries_of_friends:
    <div class="friend-entry">
      <ul class="list-group">
        % entry_owner = get_user(entry["user_id"])
        <li class="list-group-item entry-owner"><a href="/diary/entries/{{entry_owner["account_name"]}}">{{entry_owner["nick_name"]}}さん</a>:
        <li class="list-group-item entry-title"><a href="/diary/entry/{{entry["id"]}}">{{entry["body"].split("\n")[0]}}</a>
        <li class="list-group-item entry-created-at">投稿時刻:{{entry["created_at"]}}
      </ul>
    </div>
    % end
  </div>
</div>

<div class="col-md-4">
  <div>あなたの友だちのコメント</div>
  <div id="friend-comments">
    % for comment in comments_of_friends:
    <div class="friend-comment">
      <ul class="list-group">
        % comment_owner = get_user(comment["user_id"])
        % entry = db_fetchone("SELECT * FROM entries WHERE id = %s", comment["entry_id"])
        % entry_owner = get_user(entry["user_id"])
        <li class="list-group-item comment-from-to">
          <a href="/profile/{{comment_owner["account_name"]}}">{{comment_owner["nick_name"]}}さん</a>から
          <a href="/profile/{{entry_owner["account_name"]}}">{{entry_owner["nick_name"]}}さん</a>へのコメント:
        <li class="list-group-item comment-comment">{{comment["comment"][:27] + '...' if len(comment["comment"]) > 30 else comment["comment"]}}
        <li class="list-group-item comment-created-at">投稿時刻:{{comment["created_at"]}}
      </ul>
    </div>
    % end
  </div>
</div>
