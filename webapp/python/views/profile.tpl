% rebase("layout")
<h2>{{owner["nick_name"]}}さんのプロフィール</h2>

<div class="row" id="prof">
  <dl class="panel panel-primary">
    <dt>アカウント名</dt><dd id="prof-account-name">{{owner["account_name"]}}</dd>
    % if private:
      <dt>メールアドレス</dt><dd id="prof-email">{{owner["email"]}}</dd>
    % end
    <dt>姓</dt><dd id="prof-last-name">{{profile.get("last_name") or "未入力"}}</dd>
    <dt>名</dt><dd id="prof-first-name">{{profile.get("first_name") or "未入力"}}</dd>
    % if private:
      <dt>性別</dt><dd id="prof-sex">{{profile.get("sex") or "未入力"}}</dd>
      <dt>誕生日</dt><dd id="prof-birthday">{{profile.get("birthday") or "未入力"}}</dd>
      <dt>住んでいる県</dt><dd id="prof-pref">{{profile.get("pref") or "未入力"}}</dd>
    % end
  </dl>
</div>

<h2>{{owner["nick_name"]}}さんの日記</h2>
<div class="row" id="prof-entries">
  % for entry in entries:
    % if not entry["is_private"] or private:
      <div class="panel panel-primary entry">
        <div class="entry-title">タイトル: <a href="/diary/entry/{{entry["id"]}}">{{entry["title"]}}</a></div>
        <div class="entry-content">
          % for line in entry["content"][:60].split("\n"):
            {{line}}<br />
          % end
        </div>
        <div class="entry-created-at">更新日時: {{entry["created_at"]}}</div>
      </div>
    % end
  % end
</div>

% if current_user()["id"] == owner["id"]:
<div id="profile-post-form">
  <h2>プロフィール更新</h2>
  <form method="POST" action="/profile/{{owner["account_name"]}}">
    <div>名字: <input type="text" name="last_name" placeholder="みょうじ" value="{{profile.get("last_name", "")}}" /></div>
    <div>名前: <input type="text" name="first_name" placeholder="なまえ" value="{{profile.get("first_name", "")}}" /></div>
    <div>性別:
      <select name="sex">
        <option>未指定</option>
        <option {{"selected" if profile.get("sex") == "男性" else ""}}>男性</option>
        <option {{"selected" if profile.get("sex") == "女性" else ""}}>女性</option>
        <option {{"selected" if profile.get("sex") == "その他" else ""}}>その他</option>
      </select>
    </div>
    <div>誕生日:
      <input type="date" name="birthday" min="1915-01-01" max="2014-12-31" value="{{profile.get("birthday").strftime("%Y-%m-%d") if profile.get("birthday") else "2000-01-01"}}" />
    </div>
    <div>住んでいる県:
      <select name="pref">
        % for pref in prefectures:
          <option {{"selected" if profile.get("pref") == pref else ""}}>{{pref}}</option>
        % end
      </select>
    </div>
    <div><input type="submit" value="更新" /></div>
  </form>
</div>
% elif not is_friend(owner["id"]):
<div id="profile-friend-form">
  <form method="POST" action="/friends/{{owner["account_name"]}}">
    <input type="submit" value="このユーザと友だちになる" />
  </form>
</div>
% end
