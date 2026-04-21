CREATE TABLE IF NOT EXISTS `t_exam_item` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `exam_id` INT NOT NULL,
  `type` TINYINT NOT NULL COMMENT '题目类型 1-7',
  `content` TEXT COMMENT '题干',
  `options` VARCHAR(500) DEFAULT NULL COMMENT '选项(逗号分隔)',
  `answer` TEXT COMMENT '参考答案',
  `score` TINYINT NOT NULL DEFAULT 0 COMMENT '满分',
  PRIMARY KEY (`id`),
  KEY `idx_exam_id` (`exam_id`),
  CONSTRAINT `fk_exam_item_exam` FOREIGN KEY (`exam_id`) REFERENCES `t_exam` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `t_student_exam_answer` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `exam_item_id` INT NOT NULL,
  `student_id` INT NOT NULL,
  `answer` VARCHAR(200) DEFAULT NULL COMMENT '短答案(填空/选择)',
  `content` TEXT COMMENT '长答案(简答/编程)',
  `file_path` VARCHAR(200) DEFAULT NULL COMMENT '附件路径',
  `score` TINYINT DEFAULT NULL COMMENT '得分',
  `auto_scored` TINYINT DEFAULT 0 COMMENT '1=已自动评分',
  `submit_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_item_student` (`exam_item_id`, `student_id`),
  KEY `idx_student_id` (`student_id`),
  CONSTRAINT `fk_exam_answer_item` FOREIGN KEY (`exam_item_id`) REFERENCES `t_exam_item` (`id`),
  CONSTRAINT `fk_exam_answer_student` FOREIGN KEY (`student_id`) REFERENCES `t_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `t_exam_submission` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `exam_id` INT NOT NULL,
  `student_id` INT NOT NULL,
  `start_time` DATETIME DEFAULT NULL COMMENT '开始作答时间',
  `submit_time` DATETIME DEFAULT NULL COMMENT '提交时间',
  `total_score` DECIMAL(5,1) DEFAULT NULL COMMENT '总成绩',
  `status` TINYINT DEFAULT 0 COMMENT '0=未提交 1=已提交 2=已批改',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_student` (`exam_id`, `student_id`),
  KEY `idx_submission_student` (`student_id`),
  CONSTRAINT `fk_submission_exam` FOREIGN KEY (`exam_id`) REFERENCES `t_exam` (`id`),
  CONSTRAINT `fk_submission_student` FOREIGN KEY (`student_id`) REFERENCES `t_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
