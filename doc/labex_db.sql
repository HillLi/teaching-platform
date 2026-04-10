-- MySQL dump 10.13  Distrib 8.0.24, for Win64 (x86_64)
--
-- Host: localhost    Database: labex
-- ------------------------------------------------------
-- Server version	8.0.24

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `t_assistant`
--

DROP TABLE IF EXISTS `t_assistant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_assistant` (
  `assistant_account` varchar(255) NOT NULL,
  `assistant_password` varchar(255) DEFAULT NULL,
  `assistant_student_no` varchar(8) DEFAULT NULL,
  `assistant_student_name` varchar(255) DEFAULT NULL,
  `assistant_student_clazz` varchar(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_assistant`
--

LOCK TABLES `t_assistant` WRITE;
/*!40000 ALTER TABLE `t_assistant` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_assistant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_clazz`
--

DROP TABLE IF EXISTS `t_clazz`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_clazz` (
  `no` varchar(6) NOT NULL,
  `memo` text,
  `state` int DEFAULT '0',
  PRIMARY KEY (`no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_clazz`
--

LOCK TABLES `t_clazz` WRITE;
/*!40000 ALTER TABLE `t_clazz` DISABLE KEYS */;
INSERT INTO `t_clazz` VALUES ('182011','胡',1),('182012','安',1),('992011','联系人',1);
/*!40000 ALTER TABLE `t_clazz` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_ex3`
--

DROP TABLE IF EXISTS `t_ex3`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_ex3` (
  `id` int NOT NULL AUTO_INCREMENT,
  `no` int DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `extype` int DEFAULT NULL,
  `type` int DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `begin_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_ex3`
--

LOCK TABLES `t_ex3` WRITE;
/*!40000 ALTER TABLE `t_ex3` DISABLE KEYS */;
INSERT INTO `t_ex3` VALUES (1,1,'HTML练习',1,2,'HTML','2016-07-04 00:00:00','2016-09-13 21:16:27');
/*!40000 ALTER TABLE `t_ex3` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_ex3_item`
--

DROP TABLE IF EXISTS `t_ex3_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_ex3_item` (
  `excercise_item_id` int NOT NULL AUTO_INCREMENT,
  `excercise_id` int NOT NULL,
  `question` varchar(255) DEFAULT NULL,
  `options` varchar(255) DEFAULT NULL,
  `answer` varchar(255) DEFAULT NULL,
  `type` int DEFAULT NULL,
  PRIMARY KEY (`excercise_item_id`),
  KEY `fk_excercise_id` (`excercise_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_ex3_item`
--

LOCK TABLES `t_ex3_item` WRITE;
/*!40000 ALTER TABLE `t_ex3_item` DISABLE KEYS */;
INSERT INTO `t_ex3_item` VALUES (1,1,'下面哪些标签不是超链接?','A,BR,TR,IMG','BCD',2),(2,1,'图片用什么标签?','A,BR,TR,IMG','D',1);
/*!40000 ALTER TABLE `t_ex3_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_exam`
--

DROP TABLE IF EXISTS `t_exam`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_exam` (
  `id` int NOT NULL,
  `description` varchar(30) NOT NULL,
  `duration` int DEFAULT NULL COMMENT '即使单位-分',
  `time` datetime DEFAULT NULL,
  `flag` bit(1) DEFAULT NULL COMMENT '考试开放标记',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_exam`
--

LOCK TABLES `t_exam` WRITE;
/*!40000 ALTER TABLE `t_exam` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_exam` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_experiment`
--

DROP TABLE IF EXISTS `t_experiment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_experiment` (
  `experiment_id` int NOT NULL AUTO_INCREMENT,
  `experiment_no` int NOT NULL,
  `experiment_name` varchar(30) NOT NULL,
  `experiment_type` int DEFAULT NULL,
  `instruction_type` varchar(10) DEFAULT NULL,
  `experiment_requirement` text,
  `experiment_content` text,
  `state` int DEFAULT NULL,
  PRIMARY KEY (`experiment_id`),
  KEY `idx_experiment_no` (`experiment_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_experiment_item`
--

DROP TABLE IF EXISTS `t_experiment_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_experiment_item` (
  `experiment_item_id` int NOT NULL AUTO_INCREMENT,
  `experiment_item_no` int NOT NULL,
  `experiment_item_name` varchar(100) NOT NULL,
  `experiment_item_type` int NOT NULL,
  `experiment_item_content` text,
  `experiment_id` int NOT NULL,
  `experiment_item_answer` varchar(255) DEFAULT NULL,
  `experiment_item_score` tinyint DEFAULT NULL,
  `state` int DEFAULT NULL,
  PRIMARY KEY (`experiment_item_id`),
  KEY `fk_experiment_id` (`experiment_id`),
  KEY `idx_experiment_item_no` (`experiment_item_no`) USING BTREE,
  CONSTRAINT `fk_experiment_id` FOREIGN KEY (`experiment_id`) REFERENCES `t_experiment` (`experiment_id`)
) ENGINE=InnoDB AUTO_INCREMENT=143 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_lecture`
--

DROP TABLE IF EXISTS `t_lecture`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_lecture` (
  `lecture_id` int NOT NULL AUTO_INCREMENT,
  `lecture_name` varchar(50) DEFAULT NULL,
  `lecture_type` int DEFAULT NULL,
  `lecture_filetype` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`lecture_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_lecture`
--

LOCK TABLES `t_lecture` WRITE;
/*!40000 ALTER TABLE `t_lecture` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_lecture` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_paper`
--

DROP TABLE IF EXISTS `t_paper`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_paper` (
  `id` int NOT NULL,
  `no` int NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_paper`
--

LOCK TABLES `t_paper` WRITE;
/*!40000 ALTER TABLE `t_paper` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_paper` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_paper_question`
--

DROP TABLE IF EXISTS `t_paper_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_paper_question` (
  `id` int NOT NULL,
  `paper_id` int DEFAULT NULL,
  `question_id` int DEFAULT NULL,
  `score` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_paper_question`
--

LOCK TABLES `t_paper_question` WRITE;
/*!40000 ALTER TABLE `t_paper_question` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_paper_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_question`
--

DROP TABLE IF EXISTS `t_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_question` (
  `id` int NOT NULL,
  `question` varchar(255) DEFAULT NULL,
  `answer` varchar(255) DEFAULT NULL,
  `type` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_question`
--

LOCK TABLES `t_question` WRITE;
/*!40000 ALTER TABLE `t_question` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_question_type`
--

DROP TABLE IF EXISTS `t_question_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_question_type` (
  `type_id` int NOT NULL,
  `type_name` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_question_type`
--

LOCK TABLES `t_question_type` WRITE;
/*!40000 ALTER TABLE `t_question_type` DISABLE KEYS */;
INSERT INTO `t_question_type` VALUES (1,'填空'),(2,'单选'),(3,'多选'),(4,'判断'),(5,'简答'),(6,'编程'),(7,'综合');
/*!40000 ALTER TABLE `t_question_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_score`
--

DROP TABLE IF EXISTS `t_score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_score` (
  `score_id` int NOT NULL AUTO_INCREMENT,
  `student_id` int DEFAULT NULL,
  `experiment_id` int DEFAULT NULL,
  `score` int DEFAULT NULL,
  PRIMARY KEY (`score_id`),
  KEY `fk_score_student` (`student_id`),
  KEY `fk_score_experiment` (`experiment_id`),
  CONSTRAINT `fk_score_experiment` FOREIGN KEY (`experiment_id`) REFERENCES `t_experiment` (`experiment_id`),
  CONSTRAINT `fk_score_student` FOREIGN KEY (`student_id`) REFERENCES `t_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_score`
--

LOCK TABLES `t_score` WRITE;
/*!40000 ALTER TABLE `t_score` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_score` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_student`
--

DROP TABLE IF EXISTS `t_student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_student` (
  `student_id` int NOT NULL AUTO_INCREMENT,
  `student_no` varchar(8) NOT NULL,
  `student_name` varchar(20) NOT NULL,
  `student_password` char(32) NOT NULL,
  `clazz_no` varchar(6) NOT NULL,
  `memo` text,
  `state` int(10) unsigned zerofill DEFAULT NULL,
  `error` int(10) unsigned zerofill DEFAULT NULL,
  `ip` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `idx_student_no` (`student_no`) USING BTREE,
  KEY `fk_clazz_no` (`clazz_no`),
  CONSTRAINT `fk_clazz_no` FOREIGN KEY (`clazz_no`) REFERENCES `t_clazz` (`no`)
) ENGINE=InnoDB AUTO_INCREMENT=2226 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_student_answer`
--

DROP TABLE IF EXISTS `t_student_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_student_answer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_id` int DEFAULT NULL,
  `fill_no` int DEFAULT NULL,
  `content` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `content_hash` char(32) DEFAULT NULL,
  `count` int DEFAULT NULL,
  `is_correct` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_answer_pk` (`item_id`,`fill_no`,`content_hash`) USING BTREE,
  CONSTRAINT `fk_answer_item_id` FOREIGN KEY (`item_id`) REFERENCES `t_experiment_item` (`experiment_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11811 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_student_excercise`
--

DROP TABLE IF EXISTS `t_student_excercise`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_student_excercise` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `item_id` int DEFAULT NULL,
  `student_id` int DEFAULT NULL,
  `answer` varchar(30) DEFAULT NULL,
  `content` text,
  `score` int DEFAULT NULL,
  `fill_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_excercise_item_id` (`item_id`),
  KEY `fk_student_excercise_id` (`student_id`),
  CONSTRAINT `fk_excercise_item_id` FOREIGN KEY (`item_id`) REFERENCES `t_ex3_item` (`excercise_item_id`),
  CONSTRAINT `fk_student_excercise_id` FOREIGN KEY (`student_id`) REFERENCES `t_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_student_excercise`
--

LOCK TABLES `t_student_excercise` WRITE;
/*!40000 ALTER TABLE `t_student_excercise` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_student_excercise` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_student_item`
--

DROP TABLE IF EXISTS `t_student_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_student_item` (
  `student_item_id` int NOT NULL AUTO_INCREMENT,
  `student_id` int NOT NULL,
  `item_id` int NOT NULL,
  `content` text NOT NULL,
  `score` tinyint DEFAULT NULL,
  `fill_time` datetime NOT NULL,
  `score_flag` int DEFAULT NULL,
  PRIMARY KEY (`student_item_id`),
  UNIQUE KEY `idx_student_item` (`student_id`,`item_id`) USING BTREE,
  KEY `fk_item_id` (`item_id`),
  CONSTRAINT `fk_item_id` FOREIGN KEY (`item_id`) REFERENCES `t_experiment_item` (`experiment_item_id`),
  CONSTRAINT `fk_student_id` FOREIGN KEY (`student_id`) REFERENCES `t_student` (`student_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25112 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_student_item`
--

LOCK TABLES `t_student_item` WRITE;
/*!40000 ALTER TABLE `t_student_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_student_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_student_item_log`
--

DROP TABLE IF EXISTS `t_student_item_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_student_item_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `student_item` int DEFAULT NULL,
  `content` text,
  `fill_time` datetime DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `fk_student_item` (`student_item`,`fill_time`) USING BTREE,
  CONSTRAINT `fk_student_item` FOREIGN KEY (`student_item`) REFERENCES `t_student_item` (`student_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=136670 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_student_item_log`
--

LOCK TABLES `t_student_item_log` WRITE;
/*!40000 ALTER TABLE `t_student_item_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_student_item_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_student_log`
--

DROP TABLE IF EXISTS `t_student_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_student_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `account` varchar(20) DEFAULT NULL,
  `type` int DEFAULT NULL,
  `info` varchar(255) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  `ip` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_time` (`time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=352452 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table `t_student_question`
--

DROP TABLE IF EXISTS `t_student_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_student_question` (
  `id` int NOT NULL AUTO_INCREMENT,
  `student_id` int DEFAULT NULL,
  `question_id` int DEFAULT NULL,
  `answer` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_sq` (`student_id`,`question_id`) USING BTREE,
  KEY `fk_question` (`question_id`),
  CONSTRAINT `fk_question` FOREIGN KEY (`question_id`) REFERENCES `t_question` (`id`),
  CONSTRAINT `fk_student` FOREIGN KEY (`student_id`) REFERENCES `t_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_student_question`
--

LOCK TABLES `t_student_question` WRITE;
/*!40000 ALTER TABLE `t_student_question` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_student_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_sys_config`
--

DROP TABLE IF EXISTS `t_sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_sys_config` (
  `param` varchar(20) NOT NULL,
  `value` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`param`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_sys_config`
--

LOCK TABLES `t_sys_config` WRITE;
/*!40000 ALTER TABLE `t_sys_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_sys_log`
--

DROP TABLE IF EXISTS `t_sys_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_sys_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `account` varchar(20) DEFAULT NULL,
  `type` int DEFAULT NULL,
  `info` varchar(255) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  `ip` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_time` (`time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=231223 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_sys_log`
--

LOCK TABLES `t_sys_log` WRITE;
/*!40000 ALTER TABLE `t_sys_log` DISABLE KEYS */;
INSERT INTO `t_sys_log` VALUES (231213,NULL,2,'登录失败','2021-06-23 22:51:57','0:0:0:0:0:0:0:1'),(231214,'admin',1,'登录成功','2021-06-23 22:53:05','0:0:0:0:0:0:0:1'),(231215,'admin',1,'登录成功','2021-06-23 23:47:05','0:0:0:0:0:0:0:1'),(231216,'admin',2,'登录失败','2021-06-23 23:47:33','0:0:0:0:0:0:0:1'),(231217,'admin',1,'登录成功','2021-06-23 23:47:43','0:0:0:0:0:0:0:1'),(231218,'admin',1,'登录成功','2021-06-24 00:04:07','0:0:0:0:0:0:0:1'),(231219,'admin',1,'登录成功','2021-06-24 00:20:39','0:0:0:0:0:0:0:1'),(231220,'admin',1,'登录成功','2021-06-24 00:22:09','0:0:0:0:0:0:0:1'),(231221,'admin',1,'登录成功','2021-06-24 00:30:44','0:0:0:0:0:0:0:1'),(231222,'admin',1,'登录成功','2021-06-24 00:44:04','0:0:0:0:0:0:0:1');
/*!40000 ALTER TABLE `t_sys_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_teacher`
--

DROP TABLE IF EXISTS `t_teacher`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_teacher` (
  `teacher_id` int NOT NULL AUTO_INCREMENT,
  `teacher_account` varchar(6) NOT NULL,
  `teacher_password` char(32) NOT NULL,
  `teacher_name` varchar(20) NOT NULL,
  PRIMARY KEY (`teacher_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_teacher`
--

LOCK TABLES `t_teacher` WRITE;
/*!40000 ALTER TABLE `t_teacher` DISABLE KEYS */;
INSERT INTO `t_teacher` VALUES (1,'admin','8d83faabcb8805yu7f3039e111576239','Green'),(2,'arga27','8d83fafcebabc5yu7f3039e111576239','Lion');
/*!40000 ALTER TABLE `t_teacher` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_clazz_experiments_score`
--

DROP TABLE IF EXISTS `v_clazz_experiments_score`;
/*!50001 DROP VIEW IF EXISTS `v_clazz_experiments_score`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_clazz_experiments_score` AS SELECT 
 1 AS `student_id`,
 1 AS `student_no`,
 1 AS `student_name`,
 1 AS `clazz_no`,
 1 AS `experiment_id`,
 1 AS `experiment_no`,
 1 AS `score`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_clazz_info`
--

DROP TABLE IF EXISTS `v_clazz_info`;
/*!50001 DROP VIEW IF EXISTS `v_clazz_info`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_clazz_info` AS SELECT 
 1 AS `maxId`,
 1 AS `lastAccess`,
 1 AS `count`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_student_answer_data_info`
--

DROP TABLE IF EXISTS `v_student_answer_data_info`;
/*!50001 DROP VIEW IF EXISTS `v_student_answer_data_info`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_student_answer_data_info` AS SELECT 
 1 AS `maxId`,
 1 AS `lastAccess`,
 1 AS `count`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_student_answer_log_info`
--

DROP TABLE IF EXISTS `v_student_answer_log_info`;
/*!50001 DROP VIEW IF EXISTS `v_student_answer_log_info`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_student_answer_log_info` AS SELECT 
 1 AS `maxId`,
 1 AS `lastAccess`,
 1 AS `count`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_student_expeirment_items`
--

DROP TABLE IF EXISTS `v_student_expeirment_items`;
/*!50001 DROP VIEW IF EXISTS `v_student_expeirment_items`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_student_expeirment_items` AS SELECT 
 1 AS `itemNo`,
 1 AS `itemName`,
 1 AS `itemType`,
 1 AS `score`,
 1 AS `fillTime`,
 1 AS `studentId`,
 1 AS `itemId`,
 1 AS `experimentId`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_student_experiment_score`
--

DROP TABLE IF EXISTS `v_student_experiment_score`;
/*!50001 DROP VIEW IF EXISTS `v_student_experiment_score`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_student_experiment_score` AS SELECT 
 1 AS `score`,
 1 AS `student_id`,
 1 AS `experiment_id`,
 1 AS `experiment_no`,
 1 AS `experiment_name`,
 1 AS `experiment_type`,
 1 AS `instruction_type`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_student_info`
--

DROP TABLE IF EXISTS `v_student_info`;
/*!50001 DROP VIEW IF EXISTS `v_student_info`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_student_info` AS SELECT 
 1 AS `maxId`,
 1 AS `lastAccess`,
 1 AS `count`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_studentexperimentitems`
--

DROP TABLE IF EXISTS `v_studentexperimentitems`;
/*!50001 DROP VIEW IF EXISTS `v_studentexperimentitems`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_studentexperimentitems` AS SELECT 
 1 AS `itemNo`,
 1 AS `itemName`,
 1 AS `itemType`,
 1 AS `score`,
 1 AS `fillTime`,
 1 AS `studentId`,
 1 AS `itemId`,
 1 AS `experimentId`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_sys_log_info`
--

DROP TABLE IF EXISTS `v_sys_log_info`;
/*!50001 DROP VIEW IF EXISTS `v_sys_log_info`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_sys_log_info` AS SELECT 
 1 AS `maxId`,
 1 AS `lastAccess`,
 1 AS `count`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `v_clazz_experiments_score`
--

/*!50001 DROP VIEW IF EXISTS `v_clazz_experiments_score`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_clazz_experiments_score` AS select `v`.`student_id` AS `student_id`,(select `t_student`.`student_no` from `t_student` where (`t_student`.`student_id` = `v`.`student_id`)) AS `student_no`,(select `t_student`.`student_name` from `t_student` where (`t_student`.`student_id` = `v`.`student_id`)) AS `student_name`,(select `t_student`.`clazz_no` from `t_student` where (`t_student`.`student_id` = `v`.`student_id`)) AS `clazz_no`,`v`.`experiment_id` AS `experiment_id`,`v`.`experiment_no` AS `experiment_no`,sum(`v`.`score`) AS `score` from `v_student_experiment_score` `v` group by `v`.`student_id`,`v`.`experiment_id` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_clazz_info`
--

/*!50001 DROP VIEW IF EXISTS `v_clazz_info`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_clazz_info` AS select max(`t_clazz`.`no`) AS `maxId`,'----' AS `lastAccess`,count(`t_clazz`.`no`) AS `count` from `t_clazz` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_student_answer_data_info`
--

/*!50001 DROP VIEW IF EXISTS `v_student_answer_data_info`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_student_answer_data_info` AS select max(`t_student_item`.`student_item_id`) AS `maxId`,max(`t_student_item`.`fill_time`) AS `lastAccess`,count(`t_student_item`.`student_item_id`) AS `count` from `t_student_item` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_student_answer_log_info`
--

/*!50001 DROP VIEW IF EXISTS `v_student_answer_log_info`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_student_answer_log_info` AS select max(`t_student_item_log`.`log_id`) AS `maxId`,max(`t_student_item_log`.`fill_time`) AS `lastAccess`,count(`t_student_item_log`.`log_id`) AS `count` from `t_student_item_log` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_student_expeirment_items`
--

/*!50001 DROP VIEW IF EXISTS `v_student_expeirment_items`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_student_expeirment_items` AS select `t_experiment_item`.`experiment_item_no` AS `itemNo`,`t_experiment_item`.`experiment_item_name` AS `itemName`,`t_experiment_item`.`experiment_item_type` AS `itemType`,`t_student_item`.`score` AS `score`,`t_student_item`.`fill_time` AS `fillTime`,`t_student_item`.`student_id` AS `studentId`,`t_experiment_item`.`experiment_item_id` AS `itemId`,`t_experiment_item`.`experiment_id` AS `experimentId` from (`t_experiment_item` join `t_student_item` on((`t_student_item`.`item_id` = `t_experiment_item`.`experiment_item_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_student_experiment_score`
--

/*!50001 DROP VIEW IF EXISTS `v_student_experiment_score`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_student_experiment_score` AS select `t_student_item`.`score` AS `score`,`t_student_item`.`student_id` AS `student_id`,`t_experiment`.`experiment_id` AS `experiment_id`,`t_experiment`.`experiment_no` AS `experiment_no`,`t_experiment`.`experiment_name` AS `experiment_name`,`t_experiment`.`experiment_type` AS `experiment_type`,`t_experiment`.`instruction_type` AS `instruction_type` from (`t_experiment` left join (`t_student_item` join `t_experiment_item` on((`t_student_item`.`item_id` = `t_experiment_item`.`experiment_item_id`))) on((`t_experiment_item`.`experiment_id` = `t_experiment`.`experiment_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_student_info`
--

/*!50001 DROP VIEW IF EXISTS `v_student_info`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_student_info` AS select max(`t_student`.`student_id`) AS `maxId`,max(`t_student`.`ip`) AS `lastAccess`,count(`t_student`.`student_id`) AS `count` from `t_student` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_studentexperimentitems`
--

/*!50001 DROP VIEW IF EXISTS `v_studentexperimentitems`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_studentexperimentitems` AS select `t_experiment_item`.`experiment_item_no` AS `itemNo`,`t_experiment_item`.`experiment_item_name` AS `itemName`,`t_experiment_item`.`experiment_item_type` AS `itemType`,`t_student_item`.`score` AS `score`,`t_student_item`.`fill_time` AS `fillTime`,`t_student_item`.`student_id` AS `studentId`,`t_experiment_item`.`experiment_item_id` AS `itemId`,`t_experiment_item`.`experiment_id` AS `experimentId` from (`t_experiment_item` join `t_student_item` on((`t_student_item`.`item_id` = `t_experiment_item`.`experiment_item_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_sys_log_info`
--

/*!50001 DROP VIEW IF EXISTS `v_sys_log_info`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8 */;
/*!50001 SET character_set_results     = utf8 */;
/*!50001 SET collation_connection      = utf8_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_sys_log_info` AS select max(`t_sys_log`.`id`) AS `maxId`,max(`t_sys_log`.`time`) AS `lastAccess`,count(`t_sys_log`.`id`) AS `count` from `t_sys_log` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-06-24  2:10:00
