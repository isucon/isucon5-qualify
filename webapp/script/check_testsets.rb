#!/usr/bin/env ruby
require 'bundler/setup'
require 'mysql2'
require 'json'

def db
  return @client if @client
  @client = Mysql2::Client.new(
    host: 'localhost',
    port: nil,
    username: 'root',
    password: nil,
    database: 'isucon5q',
    encoding: 'utf8mb4',
    reconnect: true,
  )
  @client.query_options.merge!(symbolize_keys: true)
  @client
end

testsets_file = 'testsets/testsets.json'

testsets_json = open(testsets_file) do |io|
  JSON.load(io)
end

puts "### testsets: #{testsets_json.size} sets ###"
testsets_json.each do |testsets|
  #puts "#{testsets.size} users/sets."
  user1, user2, user3 = testsets

  user_id1 = user1['account_name'].match(%r{(\d+)$})[1]
  user_id2 = user2['account_name'].match(%r{(\d+)$})[1]
  user_id3 = user3['account_name'].match(%r{(\d+)$})[1]

  # friend check
  if db.query("SELECT * FROM relations WHERE one = #{user_id1} AND another = #{user_id2}").first
    puts "  OK: #{user_id1} and #{user_id2} are friend"
  else
    puts "  NG: #{user_id1} and #{user_id2} should be friend"
    exit
  end
  # not friend check
  unless db.query("SELECT * FROM relations WHERE one = #{user_id1} AND another = #{user_id3}").first
    puts "  OK: #{user_id1} and #{user_id3} are not friend"
  else
    puts "  NG: #{user_id1} and #{user_id3} should not friend"
    exit
  end
end
