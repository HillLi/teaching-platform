-- MySQL dump 10.13  Distrib 8.0.18, for Linux (x86_64)
--
-- Host: localhost    Database: labex
-- ------------------------------------------------------
-- Server version	8.0.18

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
-- Dumping routines for database 'labex'
--
/*!50003 DROP PROCEDURE IF EXISTS `answerQuestion` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `answerQuestion`(IN studentId int, IN itemId int, IN type int, IN studentAnswer VARCHAR(500))
BEGIN
	DECLARE n INT;
	SELECT count(*) INTO n FROM t_student_excercise WHERE student_id=studentId AND item_id=itemId;

	IF n=0 THEN
		IF type=1 OR type=2	THEN
			INSERT INTO t_student_excercise(student_id,item_id,answer,fill_time) VALUES(studentId,itemId,studentAnswer,now());
		ELSE
			INSERT INTO t_student_excercise(student_id,item_id,content,fill_time) VALUES(studentId,itemId,studentAnswer,now());
		END IF;
	ELSE
		IF type=1 OR type=2	THEN
			UPDATE t_student_excercise SET answer=studentAnswer,fill_time=now()
				WHERE student_id=studentId AND item_id=itemId;
		ELSE
			UPDATE t_student_excercise SET content=studentAnswer,fill_time=now()
				WHERE student_id=studentId AND item_id=itemId;
		END IF;
	END IF;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `p_clazz_experiment_answers` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `p_clazz_experiment_answers`(IN `expid` int,IN `cno` varchar(6))
BEGIN
	
select student_item_id,item_id,b.experiment_item_score,a.content 
from t_student_item as a,t_experiment_item as b,t_student as c 
where  a.item_id=b.experiment_item_id 
			and a.student_id = c.student_id 
			and b.experiment_id = expid 
			and c.clazz_no = cno;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `p_clazz_experiment_score` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `p_clazz_experiment_score`(IN `cno` varchar(6), IN `expid` int)
BEGIN
	SELECT
		student_id,student_no,student_name,clazz_no,memo, 
		(select sum(score) from t_student_item,t_experiment_item 
				where t_student_item.item_id = t_experiment_item.experiment_item_id 							
							and t_experiment_item.experiment_id = expid 
							and t_student_item.student_id = t_student.student_id
		) as score
	FROM
		t_student	
  WHERE
		clazz_no = cno
	ORDER BY
		student_no;		
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `p_student_experiment_item_score` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `p_student_experiment_item_score`(IN `sid` int, IN `expid` int)
BEGIN
	SELECT
		experiment_item_id,
		experiment_item_no,
		experiment_item_name,
		experiment_item_type,
		experiment_item_content,
		experiment_item_score,
		(select student_item_id from t_student_item 
				where t_student_item.item_id = experiment_item_id 							
							and student_id = sid				
		) as student_item_id,
		(select content from t_student_item 
				where t_student_item.item_id = experiment_item_id 							
							and student_id = sid				
		) as student_answer,
		(select score from t_student_item 
				where t_student_item.item_id = experiment_item_id 							
							and student_id = sid				
		) as score,
		state
	FROM
		t_experiment_item
	WHERE 
		t_experiment_item.experiment_id = expid 
  ORDER BY
		experiment_item_no;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `p_student_experiment_score` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'NO_AUTO_VALUE_ON_ZERO' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `p_student_experiment_score`(IN `sid` int)
BEGIN
	SELECT
		t_experiment.experiment_id,
		t_experiment.experiment_no,
		t_experiment.experiment_name,
		t_experiment.experiment_type,
		t_experiment.instruction_type,
		(select sum(score) from t_student_item,t_experiment_item
				where t_student_item.item_id = t_experiment_item.experiment_item_id 
							and t_experiment_item.experiment_id = t_experiment.experiment_id
							and student_id = sid				
		) as score,
		state
	FROM
		t_experiment
	ORDER BY
		experiment_no;


END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-06-23 16:49:30
