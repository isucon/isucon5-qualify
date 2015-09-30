#!/usr/bin/env ruby

require "socket"
MYHOSTNAME = Socket.gethostname

MANAGER_ADDRESS = "104.155.221.244"
MANAGER_USER = "portalsan"
MANAGER_PASSWORD = "tony-morris"

require "timeout"
require "mysql2-cs-bind"
require "json"

client = Mysql2::Client.new(
  host: MANAGER_ADDRESS,
  port: 3306,
  username: MANAGER_USER,
  password: MANAGER_PASSWORD,
  database: 'isucon5portal',
  reconnect: true,
)
client.query_options.merge!(symbolize_keys: true)

def acquire_queue_entry(client)
  queue_entry_query = <<SQL
SELECT id, team_id, status, ip_address, testset_id, acked_at, submitted_at, json FROM queue WHERE status=? LIMIT 1 FOR UPDATE
SQL
  begin
    client.xquery("BEGIN");
    entry = client.xquery(queue_entry_query, "waiting").first
    unless entry
      client.xquery("ROLLBACK")
      return nil
    end
    acquire_query = "UPDATE queue SET status=?, bench_node=?, acked_at=CURRENT_TIMESTAMP() WHERE id=? "
    client.xquery(acquire_query, "running", MYHOSTNAME, entry[:id])
    client.xquery("COMMIT")
    return entry
  rescue => e
    STDERR.puts "Exception #{e.class}: #{e.message}"
    e.backtrace.each{|bt| STDERR.puts "\t" + bt }
  ensure
    client.xquery("ROLLBACK")
  end
end

def get_testset(client, setid)
  client.xquery("SELECT json FROM testsets WHERE id=? ", setid).first[:json]
end

def revert_queue_entry(client, entry_id, result_json)
  client.xquery("UPDATE queue SET status=?, bench_node=NULL, acked_at='0000-00-00 00:00:00' WHERE id=? ", "waiting", entry_id)
end

def release_complete_entry(client, entry_id, result_json)
  client.xquery("UPDATE queue SET status=?, submitted_at=CURRENT_TIMESTAMP(), json=? WHERE id=? ", "submitted", result_json, entry_id)
end

class JVMStuckError < StandardError; end

def run_benchmark(entry_id, ip_address, testset_json)
  result_json = ""

  source_path = "/tmp/testset.#{entry_id}.json"
  File.open(source_path, "w") do |f|
    f.write testset_json
  end
  result_path = "/tmp/result.#{entry_id}.json"
  stderr_path = "/tmp/err.#{entry_id}.log"
  scenario = "net.isucon.isucon5q.bench.scenario.Isucon5Qualification"
  command = "cat #{source_path} | gradle -q run -Pargs='#{scenario} #{ip_address}' > #{result_path} 2>#{stderr_path}"

  begin
    timeout(60*5) { system(command) }
    obj = JSON.parse(File.open("/tmp/result.#{entry_id}.json"){|f| f.read }) rescue nil
    if obj
      obj["bench"] = File.open("/tmp/err.#{entry_id}.log"){|f| f.read }
      return obj.to_json
    end
    result_json
  rescue Timeout::Error => e
    # JVM stuck! remove related files and make it to be retried (on other host?)
    File.unlink source_path, result_path, stderr_path
    raise JVMStuckError
  end
end

while true
  begin
    entry = acquire_queue_entry(client)
    if entry
      testset = get_testset(client, entry[:testset_id])

      STDERR.puts "starting benchmark id #{entry[:id]}, address: #{entry[:ip_address]}"

      begin
        result_text = run_benchmark(entry[:id], entry[:ip_address], testset)

        STDERR.puts "done, reporting: benchmark id #{entry[:id]}"

        release_complete_entry(client, entry[:id], result_text)

        STDERR.puts "complete: benchmark id #{entry[:id]}"
      rescue JVMStuckError => e
        STDERR.puts "timeout for benchmark: #{entry[:id]}"

        revert_queue_entry(client, entry[:id])

        STDERR.puts "reverted to waiting: benchmark id: #{entry[:id]}"
      end
    end

    sleep 10
  rescue => e
    STDERR.puts "Exception #{e.class}: #{e.message}"
    e.backtrace.each{|bt| STDERR.puts "\t" + bt }
  end
end
