-- Add columns to existing t_exam table
ALTER TABLE t_exam
  ADD COLUMN `name` VARCHAR(100) DEFAULT NULL AFTER `id`,
  ADD COLUMN `start_time` DATETIME DEFAULT NULL AFTER `duration`,
  ADD COLUMN `end_time` DATETIME DEFAULT NULL AFTER `start_time`,
  ADD COLUMN `created_by` INT DEFAULT NULL AFTER `end_time`;

-- Add filePath to t_experiment_item for teacher attachments
ALTER TABLE t_experiment_item
  ADD COLUMN `file_path` VARCHAR(200) DEFAULT NULL AFTER `experiment_item_score`;

-- Add filePath to t_student_item for student file answers
ALTER TABLE t_student_item
  ADD COLUMN `file_path` VARCHAR(200) DEFAULT NULL AFTER `score_flag`;
