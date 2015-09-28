-- MySQL dump 10.13  Distrib 5.6.23, for osx10.10 (x86_64)
--
-- Host: localhost    Database: isucon5q
-- ------------------------------------------------------
-- Server version	5.6.23

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `comments`
--

DROP TABLE IF EXISTS `comments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `entry_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `comment` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comments`
--

LOCK TABLES `comments` WRITE;
/*!40000 ALTER TABLE `comments` DISABLE KEYS */;
INSERT INTO `comments` VALUES (1,13,1,'おつ','2014-03-06 00:02:17'),(2,13,1,'おつ','2015-08-04 22:07:57'),(3,13,1,'おつ','2014-02-10 18:35:54'),(4,13,1,'おつ','2015-01-06 19:11:54'),(5,13,1,'おつ','2014-11-17 04:33:03'),(6,13,1,'おつ','2014-02-01 06:15:50'),(7,13,1,'おつ','2015-03-01 18:01:51'),(8,13,1,'おつ','2015-06-02 10:32:24'),(9,13,1,'おつ','2014-09-24 12:00:15'),(10,13,1,'おつ','2014-06-09 22:50:43'),(11,13,1,'おつ','2014-09-21 03:38:11'),(12,13,1,'おつ','2014-03-24 19:26:24'),(13,13,1,'おつ','2015-01-08 15:55:03'),(14,13,1,'おつ','2013-10-10 02:26:45'),(15,13,1,'おつ','2014-11-24 11:59:47'),(16,13,1,'おつ','2014-11-27 16:18:45'),(17,13,1,'おつ','2014-08-28 05:28:56'),(18,13,1,'おつ','2014-06-28 06:35:41'),(19,13,1,'おつ','2015-03-22 15:29:19'),(20,13,1,'おつ','2014-05-23 21:15:17'),(21,13,1,'おつ','2015-07-08 19:21:03'),(22,13,1,'おつ','2013-11-08 00:38:23'),(23,13,1,'おつ','2015-01-14 02:45:40'),(24,13,1,'おつ','2015-03-29 00:26:08'),(25,13,1,'おつ','2015-06-03 05:57:55'),(26,13,1,'おつ','2014-01-06 22:11:45'),(27,13,1,'おつ','2015-08-25 00:30:07'),(28,13,1,'おつ','2014-03-20 10:04:52'),(29,13,1,'おつ','2014-01-25 10:37:27'),(30,13,1,'おつ','2014-03-01 12:50:11'),(31,13,1,'おつ','2013-10-31 14:32:58'),(32,13,1,'おつ','2015-08-27 17:32:20'),(33,13,1,'おつ','2015-01-29 23:15:43'),(34,13,1,'おつ','2014-11-30 07:49:30'),(35,13,1,'おつ','2014-03-03 14:02:33'),(36,13,1,'おつ','2014-12-21 11:33:45'),(37,13,1,'おつ','2015-08-16 20:17:01'),(38,13,1,'おつ','2014-02-19 17:48:12'),(39,13,1,'おつ','2015-07-16 20:31:33'),(40,13,1,'おつ','2015-06-19 12:10:59'),(41,13,1,'おつ','2015-03-01 02:13:53'),(42,13,1,'おつ','2015-03-15 18:54:01'),(43,13,1,'おつ','2015-05-25 07:40:55'),(44,13,1,'おつ','2014-01-03 15:15:17'),(45,13,1,'おつ','2014-05-01 08:59:14'),(46,1,2,'1ゲット','2015-03-10 18:21:35'),(47,3,2,'ねるねるねるね','2014-11-02 14:32:56'),(48,1,3,'よろしくおねがいします!','2015-03-16 07:20:21'),(49,2,3,'よろしくおねがいします!','2014-06-17 15:27:45'),(50,4,3,'よろしくおねがいします!','2014-08-10 22:15:47'),(51,5,3,'よろしくおねがいします!','2014-09-11 07:14:24'),(52,6,3,'よろしくおねがいします!','2014-02-01 04:59:56'),(53,17,3,'よろしくおねがいします!','2015-05-02 02:38:17'),(54,18,3,'よろしくおねがいします!','2014-02-06 21:42:37'),(55,19,3,'よろしくおねがいします!','2015-04-05 17:11:29'),(56,20,3,'よろしくおねがいします!','2014-03-17 13:30:50'),(57,21,3,'よろしくおねがいします!','2014-04-14 01:31:56'),(58,22,3,'よろしくおねがいします!','2015-06-27 20:57:53'),(59,16,3,'もうだめだ','2014-11-02 23:57:35');
/*!40000 ALTER TABLE `comments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `entries`
--

DROP TABLE IF EXISTS `entries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `entries` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `private` tinyint(4) NOT NULL,
  `body` text,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `entries`
--

LOCK TABLES `entries` WRITE;
/*!40000 ALTER TABLE `entries` DISABLE KEYS */;
INSERT INTO `entries` VALUES (1,1,0,'はじめました\nこれがうわさのアレか!','2013-10-12 16:44:50'),(2,1,0,'つづき\nいやっほおおおおおおおお\nおおおおおおおおおおおおおおおおおおおおおおおおおおおおおう','2014-05-30 10:30:02'),(3,1,1,'ひみつ\nまじもう仕事がこんなことになっていようとはと思ってたら予選なしにするんだった! まじで!','2014-10-05 05:24:12'),(4,1,0,'ビールのんだ\nうまああああああああああああああああああああああああああああああああああああああああああああああああああああい!','2013-09-21 20:19:44'),(5,2,0,'いち\nいちいちいちいちいち','2015-08-17 00:06:48'),(6,2,0,'に\nにーにーにーにーにー','2014-10-25 01:41:19'),(7,2,0,'さん\nさんさんさんさんさん','2014-02-14 06:52:47'),(8,2,0,'しい\nしいいいいいしいいいいいしいいいいいしいいいいいしいいいいい','2014-07-19 09:10:45'),(9,2,0,'ごごご\nごごごごごごごごごごごごごごごごごごごごごごごごご','2013-11-01 16:31:23'),(10,2,0,'ろくう\nろく\nろく\nろく\nろく\nろく\n','2013-12-08 17:54:49'),(11,2,0,'ななななな\nなななななななななな','2015-07-12 06:43:31'),(12,2,0,'はち\nはち？\nはち？\nはち？\nはち？\nはち？\n','2014-03-24 01:31:25'),(13,2,0,'きゅう\nくくくくくく………\nくくくくくく………\nくくくくくく………\nくくくくくく………\nくくくくくく………\n','2015-04-15 06:49:11'),(14,3,0,'はじめました!\nわくわくしています!!!!','2014-08-23 07:08:04'),(15,3,0,'ともだちがいっぱい\nうれしいです!\nみんなぜひコメントを残してください!','2014-10-22 14:18:12'),(16,3,1,'あれー\nなんでみんな友だちになれたのにコメント残してくれないの？\nいっぱい足あとついてるのに？\nなんで？？？？？？','2014-05-12 20:50:31'),(17,4,0,'1\n1','2014-12-18 11:40:15'),(18,4,0,'2\n22','2015-01-08 22:48:01'),(19,4,0,'3\n333','2014-12-26 14:32:27'),(20,4,0,'4\n4444','2014-10-04 00:19:54'),(21,4,1,'5\n55555','2014-12-13 13:08:30'),(22,4,0,'6\n666666','2015-03-18 16:14:59'),(23,4,0,'7\n7777777','2013-11-09 14:10:13'),(24,4,0,'8\n88888888','2014-01-01 23:53:28'),(25,4,0,'9\n999999999','2015-02-09 17:21:38'),(26,4,1,'10\n10101010101010101010','2015-01-02 18:20:46'),(27,4,0,'11\n1111111111111111111111','2014-07-18 05:55:59'),(28,4,0,'12\n121212121212121212121212','2015-08-06 23:23:45'),(29,4,0,'13\n13131313131313131313131313','2014-08-15 14:57:22'),(30,4,0,'14\n1414141414141414141414141414','2014-09-26 23:49:49'),(31,4,1,'15\n151515151515151515151515151515','2014-06-20 00:12:09'),(32,4,0,'16\n16161616161616161616161616161616','2013-09-29 03:22:36'),(33,4,0,'17\n1717171717171717171717171717171717','2014-03-07 10:40:05'),(34,4,0,'18\n181818181818181818181818181818181818','2014-02-03 00:47:27'),(35,4,0,'19\n19191919191919191919191919191919191919','2014-06-12 05:35:11'),(36,4,1,'20\n2020202020202020202020202020202020202020','2014-04-27 15:20:27'),(37,4,0,'21\n212121212121212121212121212121212121212121','2015-04-08 21:14:49'),(38,4,0,'22\n22222222222222222222222222222222222222222222','2015-09-01 00:59:23'),(39,4,0,'23\n2323232323232323232323232323232323232323232323','2014-06-08 05:55:12'),(40,4,0,'24\n242424242424242424242424242424242424242424242424','2015-08-12 15:45:55'),(41,4,1,'25\n25252525252525252525252525252525252525252525252525','2015-09-08 11:13:59'),(42,4,0,'26\n2626262626262626262626262626262626262626262626262626','2014-01-24 01:41:27'),(43,4,0,'27\n272727272727272727272727272727272727272727272727272727','2015-05-03 09:03:30'),(44,4,0,'28\n28282828282828282828282828282828282828282828282828282828','2015-06-27 19:38:48'),(45,4,0,'29\n2929292929292929292929292929292929292929292929292929292929','2015-09-04 17:51:19'),(46,4,1,'30\n303030303030303030303030303030303030303030303030303030303030','2015-06-22 21:16:28');
/*!40000 ALTER TABLE `entries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `footprints`
--

DROP TABLE IF EXISTS `footprints`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `footprints` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `owner_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `footprints`
--

LOCK TABLES `footprints` WRITE;
/*!40000 ALTER TABLE `footprints` DISABLE KEYS */;
INSERT INTO `footprints` VALUES (1,2,1,'2015-05-14 19:32:59'),(2,2,1,'2015-05-19 22:44:37'),(3,1,2,'2014-11-27 13:11:34'),(4,1,3,'2015-03-16 08:42:46'),(5,2,3,'2013-10-03 17:58:10'),(6,4,3,'2014-01-03 14:41:31'),(7,5,3,'2013-11-17 20:24:44'),(8,6,3,'2015-06-06 20:01:48'),(9,1,3,'2013-10-03 01:29:21'),(10,2,3,'2015-06-21 06:53:51'),(11,4,3,'2015-02-27 18:52:08'),(12,5,3,'2014-10-16 07:51:19'),(13,6,3,'2014-08-28 10:27:08'),(14,1,3,'2014-12-11 22:05:13'),(15,2,3,'2014-12-07 21:41:44'),(16,4,3,'2015-07-02 10:07:47'),(17,5,3,'2015-02-21 10:24:39'),(18,6,3,'2014-12-05 14:48:35'),(19,1,3,'2014-11-01 04:04:21'),(20,2,3,'2014-07-21 13:47:48'),(21,4,3,'2014-03-11 22:58:16'),(22,5,3,'2014-03-14 03:53:47'),(23,6,3,'2014-08-18 13:44:14'),(24,1,5,'2014-12-16 06:13:50'),(25,2,5,'2014-02-24 00:56:51'),(26,2,5,'2014-11-01 09:22:03'),(27,1,5,'2014-11-05 04:52:47'),(28,4,5,'2014-08-10 07:11:07'),(29,6,5,'2013-10-09 06:57:33'),(30,4,5,'2013-11-24 07:55:56'),(31,1,5,'2014-04-28 18:53:38'),(32,6,5,'2014-07-09 17:05:23');
/*!40000 ALTER TABLE `footprints` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `profiles`
--

DROP TABLE IF EXISTS `profiles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `profiles` (
  `user_id` int(11) NOT NULL,
  `first_name` varchar(64) NOT NULL,
  `last_name` varchar(64) NOT NULL,
  `sex` varchar(4) NOT NULL,
  `birthday` date NOT NULL,
  `pref` varchar(4) NOT NULL,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `profiles`
--

LOCK TABLES `profiles` WRITE;
/*!40000 ALTER TABLE `profiles` DISABLE KEYS */;
INSERT INTO `profiles` VALUES (1,'さとし','たごもり','男性','2001-08-18','北海道','2015-09-16 17:45:57'),(2,'りゅうた','かみぞの','その他','2011-06-13','山形県','2015-09-16 17:45:57'),(3,'えいいち','えいわ','その他','2010-01-23','岐阜県','2015-09-16 17:45:57'),(4,'もう','つかれてきた','その他','2008-12-03','北海道','2015-09-16 17:45:57'),(5,'まじねむい','もうだめ','男性','1991-12-31','愛知県','2015-09-16 17:45:57'),(6,'あとちょっと','たろう','女性','2000-11-19','京都府','2015-09-16 17:45:57');
/*!40000 ALTER TABLE `profiles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `relations`
--

DROP TABLE IF EXISTS `relations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `relations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `one` int(11) NOT NULL,
  `another` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `relations`
--

LOCK TABLES `relations` WRITE;
/*!40000 ALTER TABLE `relations` DISABLE KEYS */;
INSERT INTO `relations` VALUES (1,2,1,'2014-09-07 06:13:44'),(2,1,2,'2014-09-07 06:13:44'),(3,1,3,'2015-08-31 00:55:53'),(4,3,1,'2015-08-31 00:55:53'),(5,2,3,'2015-03-26 08:45:06'),(6,3,2,'2015-03-26 08:45:06'),(7,4,3,'2013-10-15 18:47:48'),(8,3,4,'2013-10-15 18:47:48'),(9,5,3,'2013-10-05 21:58:16'),(10,3,5,'2013-10-05 21:58:16'),(11,6,3,'2015-08-25 15:59:25'),(12,3,6,'2015-08-25 15:59:25');
/*!40000 ALTER TABLE `relations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `salts`
--

DROP TABLE IF EXISTS `salts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `salts` (
  `user_id` int(11) NOT NULL,
  `salt` varchar(6) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `salts`
--

LOCK TABLES `salts` WRITE;
/*!40000 ALTER TABLE `salts` DISABLE KEYS */;
INSERT INTO `salts` VALUES (1,'133481'),(2,'636582'),(3,'113228'),(4,'435427'),(5,'44009'),(6,'713465');
/*!40000 ALTER TABLE `salts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `account_name` varchar(64) NOT NULL,
  `nick_name` varchar(32) NOT NULL,
  `email` varchar(255) CHARACTER SET utf8 NOT NULL,
  `passhash` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `account_name` (`account_name`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'tagomoris','モリス','moris@tagomor.is','9d177faed19307aced8b6f4fda2ad346a46651598fca0a6e8bc90d37d8d763e4fafe4703a15e1bc9605729664107acb1bb957cc1d04558b0cdbeb88e50a8ed3a'),(2,'kamipo','かみぽ','kamipo@kamipopo.po','16ee0940c4352462e64c4d5a6e6a04e353bed46ea15983d85ab9949335c60e1deebba66196acf0006bb79667985566834a5d971055d2d2a65d69b17e6de0b2df'),(3,'aaaaaaa','えい','aaaa@a.com','b46d63d8e17b38da8e02d612b627350dcebcedb9f0afdeae601e0852c666ae758c797dae383658a38094a0e6f98ec05922da2624d1a5ef998be9c603186a6dd9'),(4,'bbbbbbb','b','b@b.net','9a42a0cc5cafb51c1bb0bbaab1657e669a2d1f2e6bea081641238e7569b6a6542e56fdff135d7c9cedca4438e46ef1b46c1fd4c4c4fbfc356667b13e9dae57d6'),(5,'ccccc','ＣＣＣＣＣＣ','ccc@c.net','b5c1752216af2cdbff0283acd985d6e289791dd07aa8ce2802759be1cf999e8a79b20f9def5241184dc13f3d59e5c71574fbe2596f5c9c4b879a142ea3b7f0f6'),(6,'ddd666','でぃーろく','ddddddddd@roku.com','f7e1e99ec7e11b53fee4e685efcd8741c81b53a3b31f01e5299b9c7f4a4ebc71b3383f7ff8606ff3bbdc5dcfc2f2c67ac5927c6546cc923940a0220c7a6c5524');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-09-17  2:50:41
