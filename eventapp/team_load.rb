#!/usr/bin/env ruby

require "mysql2-cs-bind"
require "json"

teams_tsv_path, testsets_json_path = ARGV

client = Mysql2::Client.new(
  host: 'localhost',
  port: nil,
  username: 'root',
  password: nil,
  database: 'isucon5portal',
  reconnect: true,
)

File.open(teams_tsv_path) do |file|
  file.readlines.each do |line|
    team, password, email, round = line.split("\t")
    client.xquery('INSERT INTO teams (team,password,email,round) VALUES (?,?,?,?)', team, password, email, round)
  end
end

File.open(testsets_json_path) do |file|
  JSON.parse(file.read).each do |set|
    client.xquery('INSERT INTO testsets (json) VALUES (?)', set.to_json)
  end
end
