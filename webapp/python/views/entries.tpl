% rebase("layout")
<h2>{{owner["nick_name"]}}さんの日記</h2>
% if myself:
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
% end

<div class="row" id="entries">
  % for entry in entries:
    <div class="panel panel-primary entry">
      <div class="entry-title">タイトル: <a href="/diary/entry/{{entry["id"]}}">{{entry["title"]}}</a></div>
      <div class="entry-content">
        % for line in entry["content"].split("\n"):
          {{line}}<br />
        % end
      </div>
      % if entry["is_private"]:
        <div class="text-danger entry-private">範囲: 友だち限定公開</div>
      % end
      <div class="entry-created-at">更新日時: {{entry["created_at"]}}</div>
      <div class="entry-comments">コメント:
        <%
          with db().cursor() as cursor:
              cursor.execute("SELECT COUNT(*) AS c FROM comments WHERE entry_id = %s", entry["id"])
              result = cursor.fetchone()
              comment_count = result["c"] if result else 0
          end
        %>
        {{comment_count}}件</div>
    </div>
  % end
</div>
