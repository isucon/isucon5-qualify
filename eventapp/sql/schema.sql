DROP DATABASE IF EXISTS isucon5portal;

CREATE DATABASE IF NOT EXISTS isucon5portal;

use isucon5portal;

CREATE TABLE IF NOT EXISTS teams (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `team` varchar(128) NOT NULL UNIQUE,
  `password` varchar(32) NOT NULL,
  `email` varchar(128) NOT NULL UNIQUE,
  `round` int NOT NULL, -- 1 is Saturday, 2 is Sunday, 0 is Both(only for organizer)
  `project_id` varchar(128),
  `zone_name` varchar(128),
  `instance_name` varchar(128)
) DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS queue (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `team_id` int NOT NULL,
  `status` varchar(16) NOT NULL, -- waiting, running, submitted, done
  `ip_address` varchar(32) NOT NULL,
  `testset_id` int NOT NULL,
  `acked_at` timestamp DEFAULT '0000-00-00 00:00:00',
  `bench_node` varchar(64) DEFAULT NULL, 
  `submitted_at` timestamp DEFAULT '0000-00-00 00:00:00',
  `json` text
) DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS scores (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `team_id` int NOT NULL,
  `summary` varchar(32) NOT NULL, -- success, fail
  `score` int NOT NULL,
  `submitted_at` timestamp,
  `json` text
) DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS highscores (
  `team_id` int NOT NULL PRIMARY KEY,
  `score` int NOT NULL,
  `submitted_at` timestamp
) DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS testsets (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `json` text
) DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS messages (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  -- http://getbootstrap.com/components/#alerts
  `priority` varchar(16) DEFAULT 'alert-info', -- 'alert-success', 'alert-info', 'alert-warning', 'alert-danger'
  `content` TEXT NOT NULL,
  `show_at` timestamp NOT NULL,
  `hide_at` timestamp NOT NULL
) DEFAULT CHARSET=utf8mb4;
