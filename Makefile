MYSQL_LOCALPORT=63306
MYSQL_DATABASE=isucon5q
MYSQL_PASSWORD=isucon5q

all: start create_user

build:
	mkdir -p data
	docker-compose build

start: build
	docker-compose up -d
log:
	docker-compose logs
status:
	docker-compose ps
connect_app:
	docker-compose exec webapp /bin/bash
restart:
	docker-compose run -d --build
stop:
	docker-compose stop
clean: stop
	docker-compose rm db webapp

init_db: start
	mysql -uroot -p$(MYSQL_PASSWORD) -h 127.0.0.1 -P $(MYSQL_LOCALPORT) -e "CREATE DATABASE IF NOT EXISTS isucon5q;"
create_schema: init_db
	mysql -uroot -p$(MYSQL_PASSWORD) $(MYSQL_DATABASE) -h 127.0.0.1 -P $(MYSQL_LOCALPORT) < ./webapp/sql/schema.sql
import_dump: create_schema
	mysql -uroot -p$(MYSQL_PASSWORD) $(MYSQL_DATABASE) -h 127.0.0.1 -P $(MYSQL_LOCALPORT) < ./webapp/sql/isucon5q.dev.sql
create_user: import_dump
	mysql -uroot -p$(MYSQL_PASSWORD) $(MYSQL_DATABASE) -h 127.0.0.1 -P $(MYSQL_LOCALPORT) < ./webapp/sql/create_user.sql
connect_db: create_user
	mysql -uroot -p$(MYSQL_PASSWORD) $(MYSQL_DATABASE) -h 127.0.0.1 -P $(MYSQL_LOCALPORT)
