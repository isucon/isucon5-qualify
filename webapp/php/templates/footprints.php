<h2>あしあとリスト</h2>
<div class="row panel panel-primary" id="footprints">
  <ul class="list-group">
    <?php foreach ($footprints as $fp) { ?>
    <?php $owner = get_user($fp['owner_id']) ?>
    <li class="list-group-item footprints-footprint"><?php h($fp['updated']) ?>: <a href="/profile/<?php h($owner['account_name']) ?>"><?php h($owner['nick_name']) ?>さん</a>
    <?php } ?>
  </ul>
</div>
