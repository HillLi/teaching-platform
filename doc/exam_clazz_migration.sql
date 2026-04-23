-- Exam-Class association table (many-to-many)
CREATE TABLE IF NOT EXISTS `t_exam_clazz` (
  `exam_id` INT NOT NULL,
  `clazz_no` VARCHAR(20) NOT NULL,
  PRIMARY KEY (`exam_id`, `clazz_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
