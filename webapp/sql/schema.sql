-- DROP TABLE IF EXISTS users;
CREATE TABLE IF NOT EXISTS users (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `account_name` varchar(64) NOT NULL UNIQUE,
  `nick_name` varchar(32) NOT NULL,
  `email` varchar(255) CHARACTER SET utf8 NOT NULL UNIQUE,
  `passhash` varchar(128) NOT NULL -- SHA2 512 non-binary (hex)
) DEFAULT CHARSET=utf8mb4;

-- DROP TABLE IF EXISTS salts;
CREATE TABLE IF NOT EXISTS salts (
  `user_id` int NOT NULL PRIMARY KEY,
  `salt` varchar(6)
) DEFAULT CHARSET=utf8;

-- DROP TABLE IF EXISTS relations;
CREATE TABLE IF NOT EXISTS relations (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `one` int NOT NULL,
  `another` int NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `friendship` (`one`,`another`)
) DEFAULT CHARSET=utf8;

-- DROP TABLE IF EXISTS profiles;
CREATE TABLE IF NOT EXISTS profiles (
  `user_id` int NOT NULL PRIMARY KEY,
  `first_name` varchar(64) NOT NULL,
  `last_name` varchar(64) NOT NULL,
  `sex` varchar(4) NOT NULL,
  `birthday` date NOT NULL, -- yyyy-mm-dd
  `pref` varchar(4) NOT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) DEFAULT CHARSET=utf8mb4;

-- DROP TABLE IF EXISTS entries;
CREATE TABLE IF NOT EXISTS entries (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` int NOT NULL,
  `private` tinyint NOT NULL,
  `body` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `user_id` (`user_id`,`created_at`),
  KEY `created_at` (`created_at`)
) DEFAULT CHARSET=utf8mb4;

-- DROP TABLE IF EXISTS comments;
CREATE TABLE IF NOT EXISTS comments (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `entry_id` int NOT NULL,
  `user_id` int NOT NULL,
  `comment` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `entry_id` (`entry_id`),
  KEY `created_at` (`created_at`)
) DEFAULT CHARSET=utf8mb4;

-- DROP TABLE IF EXISTS footprints;
CREATE TABLE IF NOT EXISTS footprints (
  `id` int NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` int NOT NULL,
  `owner_id` int NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) DEFAULT CHARSET=utf8;
