var ISUCON5_UPDATE_MESSAGE_INTERVAL = 120 * 1000;
var ISUCON5_UPDATE_QUEUE_INTERVAL = 20 * 1000;
var ISUCON5_UPDATE_HISTORY_INTERVAL = 10 * 1000;
var ISUCON5_UPDATE_LEADER_BOARD_INTERVAL = 120 * 1000;
// var ISUCON5_UPDATE_PROJECT_CHECK_INTERVAL = 600 * 1000;

// my_team_id MUST be set in html

$(function(){
  updateMessage();
  updateQueue();
  updateHistory();
  updateLeaderBoard();
  updateProjectCheck();

  $("button#enqueue-request").click(requestEnqueue);
  $("button.show-bench-detail").click(showBenchDetail);

  setInterval(updateMessage, ISUCON5_UPDATE_MESSAGE_INTERVAL);
  setInterval(updateQueue,   ISUCON5_UPDATE_QUEUE_INTERVAL);
  setInterval(updateHistory, ISUCON5_UPDATE_HISTORY_INTERVAL);
  setInterval(updateLeaderBoard, ISUCON5_UPDATE_LEADER_BOARD_INTERVAL);
  // setInterval(updateProjectCheck, ISUCON5_UPDATE_PROJECT_CHECK_INTERVAL);
});

function requestEnqueue(event){
  event.preventDefault();
  $.post("/enqueue", {ip_address: $("input#inputIPAddress").val()}, function(data){
    $("#enqueue-request-result .modal-dialog .modal-content .modal-body #result-message").text(data.message);
    $("#enqueue-request-result").modal();
  });
}

function showBenchDetail(event){
  var updateAndShow = function(data){
    var dialog = $("#bench-detail");
    dialog.find("td").text("");
    dialog.find("tr.violation").remove();
    dialog.find("p#score-message").text(data.message);
    var detail = data.detail;
    if (detail !== null && detail !== undefined) {
      dialog.find("td#bench-requests").text(detail.requests);
      dialog.find("td#bench-elapsed").text(detail.elapsed / 1000);
      dialog.find("td#bench-done").text(detail.done);
      dialog.find("td#bench-success").text(detail.responses.success);
      dialog.find("td#bench-redirect").text(detail.responses.redirect);
      dialog.find("td#bench-failure").text(detail.responses.failure);
      dialog.find("td#bench-error").text(detail.responses.error + "/" + detail.responses.exception);
      detail.violations.forEach(function(v){
        var item = $("#item_box table#item-box-violation tr.violation").clone();
        item.find("td.violation-type").text(v.type);
        item.find("td.violation-num").text(v.number);
        item.find("td.violation-message").text(v.description);
        dialog.find("table#table-bench-detail tbody").append(item);
      });
    }
    $("#bench-detail").modal();
  };
  var id = $(event.target).closest("tr").data("id");
  $.get("/bench_detail/" + id, function(data){
    updateAndShow(data);
  });
}

function updateMessage(){
  $.get("/messages", function(data) {
    $("#messages .message").remove();
    data.forEach(function(message){
      var alert = $("#item_box .message").clone();
      alert.text(message.message);
      alert.addClass(message.priority);
      $("#messages").append(alert);
    });
  });
}

function updateQueue(){
  $.get("/queuestat", function(list){
    $("#queue span.queue-entry").remove();
    list.forEach(function(entry){
      var item = $("#item_box span.queue-entry").clone();
      var kls = "label-info";
      if (entry.status === "running" && entry.team_id == my_team_id) {
        kls = "label-danger";
      } else if (entry.status === "running") {
        kls = "label-warning";
      } else if (entry.status === "waiting" && entry.team_id == my_team_id) {
        kls = "label-primary";
      }
      item.text(entry.team_id_s);
      item.addClass(kls);
      $("#queue").append(item);
    });
  });
}

function updateHistory(){
  $.get("/history", function(list){
    $("#history table#history-table tr.bench-history").remove();
    list.forEach(function(row){
      var item = $("#item_box table#item-box-bench tr.bench-history.bench-failed").clone();
      if (row.success)
        item = $("#item_box table#item-box-bench tr.bench-history.bench-successed").clone();
      item.data("id", row.id);
      item.find("td.score").text(row.score);
      item.find("td.timestamp").text(row.submitted_at);
      item.find("button.show-bench-detail").click(showBenchDetail);
      $("#history table#history-table").append(item);
    });
  });
}

function updateLeaderBoard(){
  $.get("/leader_board", function(list){
    $("#leader-board table#leader-board-table tr.bench-leader").remove();
    list.forEach(function(row){
      var item = $("#item_box table#item-box-bench-leader tr.bench-leader").clone();
      item.find("td.bench-team").text(row.shortname);
      item.find("td.bench-latest-at").text(row.latest_at);
      item.find("td.bench-latest").text(row.latest);
      item.find("td.bench-best").text(row.best);
      $("#leader-board table#leader-board-table").append(item);
    });
  });
}

function updateProjectCheck(){
  $.get("/project_check", function(data){
    console.log(data);
    $("#instance-status-check").html("");
    var base = $("#item_box #item-box-instance-check .check-success").clone();
    if (! data.valid) {
      base = $("#item_box #item-box-instance-check .check-fail").clone();
    }
    if (data.messages != null && data.messages.length > 0) {
      data.messages.forEach(function(m){
        var item = base.clone();
        item.find("span").text(m);
        $("#instance-status-check").append(item);
      });
    }
  });
}