require_relative "lib/score"

require "mysql2-cs-bind"
require "json"

client = Mysql2::Client.new(
  host: 'localhost',
  port: nil,
  username: 'root',
  password: nil,
  database: 'isucon5portal',
  reconnect: true,
)
client.query_options.merge!(symbolize_keys: true)

while true
  sleep 5

  begin
    queue_entry_query = "SELECT id, team_id, status, acked_at, submitted_at, json FROM queue WHERE status=? "

    # fetch submitted queries, and post score
    client.xquery(queue_entry_query, "submitted").each do |row|
      result = JSON.parse(row[:json]) rescue nil
      summary = "fail"
      score = 0
      if result
        summary, score = Isucon5Portal::Score.calculate(result)
      end
      insert_score_query = "INSERT INTO scores (team_id,summary,score,submitted_at,json) VALUES (?,?,?,?,?)"
      client.xquery(insert_score_query, row[:team_id], summary, score, row[:submitted_at], row[:json])
      # ignore duplicated insert ...
      if summary == "success" && score > 0
        highscore = client.xquery("SELECT score FROM highscores WHERE team_id=?", row[:team_id]).first()
        if highscore.nil?
          client.xquery("INSERT INTO highscores (team_id, score, submitted_at) VALUES (?,?,?)", row[:team_id], score, row[:submitted_at])
        elsif highscore[:score] < score
          client.xquery("UPDATE highscores SET score=?, submitted_at=? WHERE team_id=?", score, row[:submitted_at], row[:team_id])
        end
      end
      client.xquery("UPDATE queue SET status='done' WHERE id=? ", row[:id])
    end

    # fetch queue entries stays in 'running' too long to retry...
    client.xquery(queue_entry_query, "running").each do |row|
      if row[:acked_at] + 300 < Time.now
        # 5min is too long to wait for bench: does something go wrong? -> retry
        client.xquery("UPDATE queue SET status='waiting' WHERE id=? ", row[:id])
      end
    end
  rescue => e
    STDERR.puts "Exception #{e.class}: #{e.message}"
    e.backtrace.each do |t|
      STDERR.puts "\t" + t
    end
  end
end
