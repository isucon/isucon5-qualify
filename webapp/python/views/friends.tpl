% rebase("layout")
<h2>友だちリスト</h2>
<div class="row panel panel-primary" id="friends">
  <dl>
    % for user_id, created_at in friends:
      % friend = get_user(user_id)
      <dt class="friend-date">{{created_at}}</dt><dd class="friend-friend"><a href="/profile/{{friend["account_name"]}}">{{friend["nick_name"]}}</a></dd>
    % end
  </dl>
</div>
