#!/usr/bin/env ruby
require 'bundler/setup'
require 'thor'
require 'mysql2'
require 'gimei'
require 'faker'
require 'time'
require 'securerandom'
require 'json'

srand(941) # to create the same data

class Command < Thor
  desc "gen_users", "generate users"
  option :users, type: :numeric, aliases: '-u'
  def gen_users(users = 10_000/2)
    users = options[:users] if options[:users]

    db = db()
    stmt = {}
    stmt[:user] = db.prepare("INSERT INTO users (id,account_name,nick_name,email,passhash) VALUES (?,?,?,?,SHA2(CONCAT(?, ?), 512))")
    stmt[:salt] = db.prepare("INSERT INTO salts (user_id,salt) VALUES (?,?)")
    stmt[:prof] = db.prepare("INSERT INTO profiles (user_id,first_name,last_name,sex,birthday,pref) VALUES (?,?,?,?,?,?)")

    (1..users).each do |n|
      u = create_user(id: n)
      begin
        db.query("BEGIN".freeze)
        stmt[:user].execute(u[:id],u[:account_name],u[:nick_name],u[:email],u[:password],u[:salt])
        stmt[:salt].execute(u[:id],u[:salt])
        stmt[:prof].execute(u[:id],u[:first_name],u[:last_name],u[:sex],u[:birthday],u[:pref])
        db.query("COMMIT".freeze)
        p u
      rescue
        p $!
        db.query("ROLLBACK".freeze)
      end
    end
  end

  desc "gen_relations", "generate relations"
  option :users, type: :numeric, aliases: '-u'
  option :friends, type: :numeric, aliases: '-f'
  def gen_relations(friends = 50, users = 10_000/2)
    users = options[:users] if options[:users]
    friends = options[:friends] if options[:friends]

    db = db()
    #db.query("CREATE UNIQUE INDEX gen_relations ON relations (one, another)")
    stmt = {}
    stmt[:delete] = db.prepare("DELETE FROM relations WHERE (one=? AND another=?) OR (one=? AND another=?)")
    stmt[:insert] = db.prepare("INSERT INTO relations (id, one, another, created_at) VALUES (?,?,?,?), (?,?,?,?)")
    relation_ids = (1..(friends*users)).to_a.shuffle!
    (1..users).each do |n|
      rel_ids = relation_ids.pop(friends)
      begin
        db.query("BEGIN".freeze)
        rel_ids.each do |id|
          id_1 = 2*id - 1
          id_2 = 2*id
          one = n
          another = create_user_id(1..users, exclude_id: one)
          at = create_timestamp(offset: 2*60*id_1)
          stmt[:delete].execute(one, another, another, one)
          stmt[:insert].execute(id_1, one, another, at, id_2, another, one, at)
        end
        db.query("COMMIT".freeze)
        #p db.query("SELECT one, another FROM relations WHERE one = #{n}").map { |r| [r[:one], r[:another]] }
        puts "user:#{n} done"
      rescue
        p $!
        db.query("ROLLBACK".freeze)
      end
    end
  rescue
    p $!
  ensure
    #db.query("DROP INDEX gen_relations ON relations")
  end

  desc "gen_footprints", "generate footprints"
  option :users, type: :numeric, aliases: '-u'
  option :footprints, type: :numeric, aliases: '-f'
  def gen_footprints(footprints = 100, users = 10_000/2)
    users = options[:users] if options[:users]
    footprints = options[:footprints] if options[:footprints]

    db = db()
    stmt = db.prepare("INSERT INTO footprints (id, user_id, owner_id, created_at) VALUES (?,?,?,?)")
    footprint_ids = (1..(footprints*users)).to_a.shuffle!
    (1..users).each do |n|
      fp_ids = footprint_ids.pop(footprints)
      begin
        db.query("BEGIN".freeze)
        fp_ids.each do |id|
          one = n
          another = create_user_id(1..users, exclude_id: one)
          at = create_timestamp(offset: 2*60*id)
          stmt.execute(id, one, another, at)
        end
        db.query("COMMIT".freeze)
        #p db.query("SELECT user_id, owner_id FROM footprints WHERE user_id = #{n}").map { |r| [r[:user_id], r[:owner_id]] }
        puts "user:#{n} done"
      rescue
        p $!
        db.query("ROLLBACK".freeze)
      end
    end
  end

  desc "gen_entries", "generate entries"
  option :users, type: :numeric, aliases: '-u'
  option :entries, type: :numeric, aliases: '-e'
  def gen_entries(entries = 100, users = 10_000/2)
    users = options[:users] if options[:users]
    entries = options[:entries] if options[:entries]

    db = db()
    stmt = db.prepare("INSERT INTO entries (id, user_id, private, body, created_at) VALUES (?,?,?,?,?)")
    entry_ids = (1..(entries*users)).to_a.shuffle!
    (1..users).each do |n|
      ent_ids = entry_ids.pop(entries)
      begin
        #db.query("BEGIN".freeze)
        ent_ids.each do |id|
          user_id = n
          is_private = rand(10) == 0 ? 1 : 0
          body = create_content
          at = create_timestamp(offset: 2*60*id)
          stmt.execute(id, user_id, is_private, body, at)
        end
        #db.query("COMMIT".freeze)
        #p db.query("SELECT id, user_id FROM entries WHERE user_id = #{n}").map { |r| [r[:user_id], r[:id]] }
        puts "user:#{n} done"
      rescue
        p $!
        #db.query("ROLLBACK".freeze)
      end
    end
  end

  desc "gen_comments", "generate comments"
  option :users, type: :numeric, aliases: '-u'
  option :comments, type: :numeric, aliases: '-c'
  def gen_comments(comments = 300, users = 10_000/2)
    users = options[:users] if options[:users]
    comments = options[:comments] if options[:comments]

    db = db()
    stmt = db.prepare("INSERT INTO comments (id, entry_id, user_id, comment, created_at) VALUES (?,?,?,?,?)")
    comment_ids = (1..(comments*users)).to_a.shuffle!
    (1..users).each do |n|
      comm_ids = comment_ids.pop(comments)
      begin
        entries = db.query("SELECT id, user_id, private, created_at FROM entries WHERE user_id = #{n}").to_a
        #db.query("BEGIN".freeze)
        comm_ids.each do |id|
          user_id = create_user_id(1..users)
          comment = create_comment
          at = create_timestamp(offset: 2*20*id + 3_000_000)
          e_idx = rand(entries.size)
          entry_id = nil
          loop do
            entry = entries[e_idx]
            if at < entry[:created_at].strftime('%Y-%m-%d %H:%M:%S')
              e_idx -= 1
              next if e_idx >= 0
            end
            entry_id = entry[:id]
            break
          end
          stmt.execute(id, entry_id, user_id, comment, at)
        end
        #db.query("COMMIT".freeze)
        #p db.query("SELECT id, entry_id FROM comments WHERE entry_id IN (#{e_ids.join(',')})").map { |r| [r[:id], r[:entry_id]] }
        puts "user:#{n} done"
      rescue
        p $!
        #db.query("ROLLBACK".freeze)
      end
    end
  rescue
    p $!
  end

  desc "gen_testsets", "generate testsets.json"
  option :users, type: :numeric, aliases: '-u'
  option :testsets, type: :numeric, aliases: '-d'
  def gen_testsets(testsets = 20, users = 10_000/2)
    users = options[:users] if options[:users]
    testsets = options[:testsets] if options[:testsets]
    elems = 30

    db = db()
    stmt = {}
    stmt[:user] = db.prepare("SELECT * FROM users WHERE id = ?")
    stmt[:prof] = db.prepare("SELECT * FROM profiles WHERE user_id = ?")
    stmt[:rel]  = db.prepare("SELECT * FROM relations WHERE one = ?")

    outputs = []
    testsets.times do |n|
      testset = []
      user_ids = (1..users).to_a.shuffle!

      user_id1 = user_ids.pop
      user1 = stmt[:user].execute(user_id1).first
      prof1 = stmt[:prof].execute(user_id1).first
      testset << create_dataset(user1, prof1)

      # friend
      user_id2 = nil
      friend_ids = stmt[:rel].execute(user_id1).map { |rel| rel[:another] }
      friend_ids.each do |friend_id|
        user_id2 = user_ids.delete(friend_id)
        next if user_id2.nil?
      end
      p [:user_id2, user_id1, friend_ids, n] unless user_id2
      user2 = stmt[:user].execute(user_id2).first
      prof2 = stmt[:prof].execute(user_id2).first
      testset << create_dataset(user2, prof2)

      # not friend
      user_id3 = nil
      loop do
        user_id3 = user_ids.pop
        break unless friend_ids.include?(user_id3)
      end
      user3 = stmt[:user].execute(user_id3).first
      prof3 = stmt[:prof].execute(user_id3).first
      testset << create_dataset(user3, prof3)

      # friend check
      unless db.query("SELECT * FROM relations WHERE one = #{user_id1} AND another = #{user_id2}").first
        puts "#{user_id1} and #{user_id2} should be friend"
        exit
      end
      # not friend check
      if db.query("SELECT * FROM relations WHERE one = #{user_id1} AND another = #{user_id3}").first
        puts "#{user_id1} and #{user_id3} should not friend"
        exit
      end

      other_ids = user_ids.pop(elems - testset.size)
      other_ids.each do |other_id|
        user = stmt[:user].execute(other_id).first
        prof = stmt[:prof].execute(other_id).first
        testset << create_dataset(user, prof)
      end

      outputs << testset
    end
    puts outputs.to_json
  end

  private

  def create_dataset(user, prof)
    u = {}
    u[:account_name] = user[:account_name]
    u[:nick_name]    = user[:nick_name]
    u[:email]        = user[:email]
    u[:password]     = user[:account_name]
    u[:first_name]   = prof[:first_name]
    u[:last_name]    = prof[:last_name]
    u[:sex]          = prof[:sex]
    u[:birthday]     = prof[:birthday].strftime('%Y-%m-%d')
    u[:pref]         = prof[:pref]
    u
  rescue
    p [user, prof]
    exit
  end

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

  BIRTH_TIME_MIN = Time.parse('1970-01-01 00:00:00').to_i
  BIRTH_TIME_MAX = Time.parse('2010-12-31 23:59:59').to_i
  BIRTH_TIME_RANGE = Range.new(BIRTH_TIME_MIN, BIRTH_TIME_MAX)

  def create_birthday
    Time.at(rand(BIRTH_TIME_RANGE)).strftime('%Y-%m-%d')
  end

  TIMESTAMP_MIN = Time.parse('2013-09-17 10:00:00').to_i
  TIMESTAMP_MAX = Time.parse('2015-09-17 01:20:30').to_i

  # Time.at(Time.parse('2013-09-17 10:00:00').to_i + 30_000_000) # => 2014-08-30 15:20:00 +0900
  # Time.at(Time.parse('2013-09-17 10:00:00').to_i + 40_000_000) # => 2014-12-24 09:06:40 +0900
  # Time.at(Time.parse('2013-09-17 10:00:00').to_i + 50_000_000) # => 2015-04-19 02:53:20 +0900
  # Time.at(Time.parse('2013-09-17 10:00:00').to_i + 60_000_000) # => 2015-08-12 20:40:00 +0900
  def create_timestamp(offset: rand(0..(TIMESTAMP_MAX-TIMESTAMP_MIN)))
    Time.at(TIMESTAMP_MIN+offset).strftime('%Y-%m-%d %H:%M:%S')
  end

  def create_user(id:, **args)
    gimei = Gimei.new
    u = {}
    u[:id] = id
    u[:account_name] = args[:account_name] || Faker::Internet.user_name(nil, %w(_)) + id.to_s
    u[:nick_name]    = args[:nick_name]    || gimei.first.katakana
    u[:email]        = args[:email]        || "#{u[:account_name]}@isucon.net"
    u[:password]     = args[:password]     || u[:account_name]
    u[:salt]         = args[:salt]         || SecureRandom.hex(3)
    u[:first_name]   = args[:first_name]   || gimei.first.hiragana
    u[:last_name]    = args[:last_name]    || gimei.last.hiragana
    u[:sex]          = args[:sex]          || (u[:id] % 10 == 0) ? 'その他' : gimei.male? ? '男性' : '女性'
    u[:birthday]     = args[:birthday]     || create_birthday
    u[:pref]         = args[:pref]         || gimei.prefecture.kanji
    u
  end

  def create_user_id(range, exclude_id: nil)
    loop do
      user_id = rand(range)
      return user_id unless user_id == exclude_id
    end
  end

  def create_content
    CONTENTS[rand(0...CONTENTS.size)]
  end

  def create_comment
    COMMENTS[rand(0...COMMENTS.size)]
  end

  CONTENTS = [
    <<-EOL,
昨日、近所の吉野家行ったんです。吉野家。
そしたらなんか人がめちゃくちゃいっぱいで座れないんです。
で、よく見たらなんか垂れ幕下がってて、１５０円引き、とか書いてあるんです。
もうね、アホかと。馬鹿かと。
お前らな、１５０円引き如きで普段来てない吉野家に来てんじゃねーよ、ボケが。
１５０円だよ、１５０円。
なんか親子連れとかもいるし。一家４人で吉野家か。おめでてーな。
よーしパパ特盛頼んじゃうぞー、とか言ってるの。もう見てらんない。
お前らな、１５０円やるからその席空けろと。
吉野家ってのはな、もっと殺伐としてるべきなんだよ。
Ｕの字テーブルの向かいに座った奴といつ喧嘩が始まってもおかしくない、
刺すか刺されるか、そんな雰囲気がいいんじゃねーか。女子供は、すっこんでろ。
で、やっと座れたかと思ったら、隣の奴が、大盛つゆだくで、とか言ってるんです。
そこでまたぶち切れですよ。
あのな、つゆだくなんてきょうび流行んねーんだよ。ボケが。
得意げな顔して何が、つゆだくで、だ。
お前は本当につゆだくを食いたいのかと問いたい。問い詰めたい。小１時間問い詰めたい。
お前、つゆだくって言いたいだけちゃうんかと。
吉野家通の俺から言わせてもらえば今、吉野家通の間での最新流行はやっぱり、
ねぎだく、これだね。
大盛りねぎだくギョク。これが通の頼み方。
ねぎだくってのはねぎが多めに入ってる。そん代わり肉が少なめ。これ。
で、それに大盛りギョク（玉子）。これ最強。
しかしこれを頼むと次から店員にマークされるという危険も伴う、諸刃の剣。
素人にはお薦め出来ない。
まあお前らド素人は、牛鮭定食でも食ってなさいってこった。
    EOL
    <<-EOL * 5,
あ～よっしゃいくぞ～！！
タイガー・ファイヤー・サイバー・ファイバー・ダイバー・バイバー・ジャージャー！！！！

あ～もういっちょいくぞ～！！
虎・火・人造・繊維・海女・振動・化繊飛除去！！！！

あ～よっしゃいくぞ～！！
チャぺ・アペ・カラ・キラ・ララ・トゥッスケ・ミョーホントゥスケ！！！！
    EOL
  ] + %w(
  ラーメン
  寿司
  ビール
  日本酒
  カレー
  天ぷら
  からあげ
  竜田揚げ
  うどん
  そば
  きょうのもちもち
  ).map { |f|
    "#{f}<3\n" + "#{f}うまああああああああああああああああああああああああああああああいい！！！！\n" * 20
  }

  COMMENTS = %w(
  なるほど？？
  なるほどなるほど！！
  で、誰？
  １ゲット！！
  LGTM(y)
  ) + %w(
  ラーメン
  寿司
  ビール
  日本酒
  カレー
  天ぷら
  からあげ
  竜田揚げ
  うどん
  そば
  きょうのもちもち
  ).map { |f|
    "#{f}うまああああああああああああああああああああああああああああああいい！！！！\n" * 2
  }
end

Command.start
