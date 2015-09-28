#!/usr/bin/env ruby

TEAM_ID_LIST = ARGV.map{|i| i.to_i}

require "mysql2-cs-bind"
require "json"

MANAGER_ADDRESS = "104.155.221.244"
MANAGER_USER = "portalsan"
MANAGER_PASSWORD = "tony-morris"

client = Mysql2::Client.new(
  host: MANAGER_ADDRESS,
  port: 3306,
  username: MANAGER_USER,
  password: MANAGER_PASSWORD,
  database: 'isucon5portal',
  reconnect: true,
)
client.query_options.merge!(symbolize_keys: true)

def get_team(client, team_id)
  client.xquery("SELECT id, team AS name, project_id, zone_name, instance_name FROM teams WHERE id=? ", team_id).first
end

def command(team, cmd)
  "gcloud compute --project #{team[:project_id]} instances #{cmd} --zone #{team[:zone_name]} --format json"
end

def call_gcloud(command)
  p command
  jsonText = IO.popen(command) do |io|
    io.read()
  end
  JSON.parse(jsonText) rescue nil
end

def get_server(team)
  call_gcloud(command(team, "describe #{team[:instance_name]}"))
end

def is_running?(server)
  server["status"] == "RUNNING"
end

def ip_address(server)
  ifaces = server["networkInterfaces"].select{|iface| iface["accessConfigs"].any?{|c| c["natIP"] } }
  ifaces.first["accessConfigs"].select{|c| c["natIP"] }.first["natIP"]
end

def start_server(team, server)
  list = call_gcloud(command(team, "start #{team[:instance_name]}"))
  list.first
end

def stop_server(team, server)
  list = call_gcloud(command(team, "stop #{team[:instance_name]}"))
  list.first
end

def get_testset(client)
  client.xquery("SELECT json FROM testsets WHERE id=? ", 1).first[:json]
end

def run_benchmark(team_id, ip_address, testset, times)
  source_path = "/tmp/testset.#{team_id}.#{times}.json"
  File.open(source_path, "w") do |f|
    f.write testset
  end
  result_path = "/tmp/result.#{team_id}.#{times}.json"
  stderr_path = "/tmp/err.#{team_id}.#{times}.log"
  scenario = "net.isucon.isucon5q.bench.scenario.Isucon5Qualification"
  command = "cat #{source_path} | gradle -q run -Pargs='#{scenario} #{ip_address}' > #{result_path} 2>#{stderr_path}"

  pid = spawn(command)
  timeout = Time.now + 60 * 3
  begin
    while Time.now < timeout
      Process.waitpid(pid, Process::WNOHANG)
      sleep 2
    end
  rescue Errno::ECHILD
    # bench end
  end
  obj = JSON.parse(File.open(result_path){|f| f.read }) rescue nil
  if obj.nil?
    return {"result" => "result is not produced (timeout?)"}
  end
  obj
end

def team_turn_around(client, team_id)
  team = get_team(client, team_id)
  header = "[#{sprintf('% 3d', team_id)}]"
  puts "#{header} team name: #{team[:name]}"
  puts "#{header} project:#{team[:project_id]}, zone:#{team[:zone_name]}, instance:#{team[:instance_name]}"
  server = get_server(team)

  if !is_running?(server)
    while !is_running?(server)
      puts "#{header} instance isn't running. starting..."
      server = start_server(team, server)
      puts "#{header} done. wait for 10 seconds for ssh"
    end
  end

  pretty_print = JSON::State.new(indent:"  ", space:" ", object_nl:"\n", array_nl:"\n")

  testset = get_testset(client)

  puts "#{header} IP address: #{ip_address(server)}"

  puts "#{header} running benchmark 1st time"
  r1 = run_benchmark(team[:id], ip_address(server), testset, 1)
  puts r1.to_json(pretty_print)

  puts "#{header} running benchmark 2nd time"
  r2 = run_benchmark(team[:id], ip_address(server), testset, 1)
  puts r2.to_json(pretty_print)

  puts "#{header} access http://#{ip_address(server)}/ with user: edwardo3657@isucon.net pass: edwardo3657"
  print "#{header} Enter after write comment:"
  STDIN.gets

  puts "#{header} going to restart #{team[:instance_name]}"
  puts "#{header} stopping"

  server = stop_server(team, server)
  puts "#{header} status: #{server["status"]}"
  puts "#{header} starting"
  server = start_server(team, server)

  puts "#{header} ok, access application again to check result http://#{ip_address(server)}/ with user: edwardo3657@isucon.net pass: edwardo3657"
end

TEAM_ID_LIST.each do |id|
  team_turn_around(client, id)
end
