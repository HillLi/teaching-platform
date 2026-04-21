# Teaching Platform Enhancements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement 8 modifications from doc/实验修改点.docx: fill-in-blank underlines, comprehensive question file upload, exercise renaming, exercise item edit/delete, exam module (full CRUD + timed exam + grading), unified auto-scoring, and cross-module score summary.

**Architecture:** Follows existing Entity → Mapper (MyBatis-Plus BaseMapper) → Service → Controller pattern. New exam module mirrors the exercise module structure. Auto-scoring is a shared utility class used by StudentService (experiments), ExerciseService (exercises), and ExamService (exams).

**Tech Stack:** Spring Boot 2.7.18, MyBatis-Plus 3.5.5, MySQL 8, Lombok, Java 8

---

## Task 1: Database Migration Scripts

**Files:**
- Create: `doc/exam_migration.sql`
- Create: `doc/exam_tables.sql`

- [ ] **Step 1: Create the t_exam ALTER TABLE migration**

Create `doc/exam_migration.sql`:

```sql
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
```

- [ ] **Step 2: Create new exam tables**

Create `doc/exam_tables.sql`:

```sql
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
```

- [ ] **Step 3: Run the migrations against the database**

```bash
mysql -u root -p123456 labex < doc/exam_migration.sql
mysql -u root -p123456 labex < doc/exam_tables.sql
```

- [ ] **Step 4: Commit**

```bash
git add doc/exam_migration.sql doc/exam_tables.sql
git commit -m "feat: add database migration scripts for exam module and file paths"
```

---

## Task 2: Auto-Scoring Utility

**Files:**
- Create: `labex/src/main/java/labex/common/ScoringUtil.java`

- [ ] **Step 1: Create the ScoringUtil class**

Create `labex/src/main/java/labex/common/ScoringUtil.java`:

```java
package labex.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScoringUtil {

    /**
     * Returns true if the question type supports auto-scoring.
     * Types: 1=填空, 2=单选, 3=多选, 4=判断
     */
    public static boolean isAutoScorable(int type) {
        return type == 1 || type == 2 || type == 3 || type == 4;
    }

    /**
     * Auto-score an answer against a reference answer.
     * Returns the earned score out of maxScore, or null if not auto-scorable.
     */
    public static Integer autoScore(int type, String referenceAnswer, String studentAnswer, int maxScore) {
        if (!isAutoScorable(type)) {
            return null;
        }
        if (studentAnswer == null || studentAnswer.trim().isEmpty()) {
            return 0;
        }
        if (referenceAnswer == null || referenceAnswer.trim().isEmpty()) {
            return null;
        }
        switch (type) {
            case 1:
                return scoreFillInBlank(referenceAnswer, studentAnswer, maxScore);
            case 2:
                return scoreExactMatch(referenceAnswer, studentAnswer, maxScore);
            case 3:
                return scoreMultipleChoice(referenceAnswer, studentAnswer, maxScore);
            case 4:
                return scoreExactMatch(referenceAnswer, studentAnswer, maxScore);
            default:
                return null;
        }
    }

    /**
     * Fill-in-blank: answers are pipe-separated (e.g. "A|B|C").
     * Each blank is scored equally. Partial credit supported.
     */
    static int scoreFillInBlank(String reference, String student, int maxScore) {
        String[] refBlanks = reference.split("\\|");
        String[] stuBlanks = student.split("\\|");
        int total = refBlanks.length;
        if (total == 0) return 0;
        int correct = 0;
        for (int i = 0; i < total; i++) {
            String ref = refBlanks[i].trim();
            String stu = i < stuBlanks.length ? stuBlanks[i].trim() : "";
            if (ref.equalsIgnoreCase(stu)) {
                correct++;
            }
        }
        if (correct == total) return maxScore;
        return Math.round((float) correct / total * maxScore);
    }

    static int scoreExactMatch(String reference, String student, int maxScore) {
        return reference.trim().equalsIgnoreCase(student.trim()) ? maxScore : 0;
    }

    static int scoreMultipleChoice(String reference, String student, int maxScore) {
        List<String> refList = sortedList(reference);
        List<String> stuList = sortedList(student);
        return refList.equals(stuList) ? maxScore : 0;
    }

    private static List<String> sortedList(String csv) {
        String[] parts = csv.toUpperCase().split("[,，]");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        list.sort(String::compareTo);
        return list;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add labex/src/main/java/labex/common/ScoringUtil.java
git commit -m "feat: add shared auto-scoring utility for objective question types"
```

---

## Task 3: Experiment Entity Updates for File Paths

**Files:**
- Modify: `labex/src/main/java/labex/entity/ExperimentItem.java`
- Modify: `labex/src/main/java/labex/entity/StudentItem.java`

- [ ] **Step 1: Add filePath to ExperimentItem**

In `labex/src/main/java/labex/entity/ExperimentItem.java`, add after `private Integer experimentItemScore;`:

```java
    private String filePath;
```

The full file becomes:

```java
package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_experiment_item")
public class ExperimentItem {
    @TableId(value = "experiment_item_id", type = IdType.AUTO)
    private Integer experimentItemId;
    private Integer experimentItemNo;
    private String experimentItemName;
    private Integer experimentItemType;
    private String experimentItemContent;
    private Integer experimentId;
    private String experimentItemAnswer;
    private Integer experimentItemScore;
    private String filePath;
    private Integer state;
}
```

- [ ] **Step 2: Add filePath to StudentItem**

In `labex/src/main/java/labex/entity/StudentItem.java`, add after `private Integer scoreFlag;`:

```java
    private String filePath;
```

The full file becomes:

```java
package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_student_item")
public class StudentItem {
    @TableId(value = "student_item_id", type = IdType.AUTO)
    private Integer studentItemId;
    private Integer studentId;
    private Integer itemId;
    private String content;
    private Integer score;
    private LocalDateTime fillTime;
    private Integer scoreFlag;
    private String filePath;
}
```

- [ ] **Step 3: Commit**

```bash
git add labex/src/main/java/labex/entity/ExperimentItem.java labex/src/main/java/labex/entity/StudentItem.java
git commit -m "feat: add filePath field to ExperimentItem and StudentItem entities"
```

---

## Task 4: Experiment File Upload/Download Endpoints

**Files:**
- Modify: `labex/src/main/java/labex/controller/TeacherController.java`
- Modify: `labex/src/main/java/labex/service/TeacherService.java`
- Modify: `labex/src/main/java/labex/controller/StudentController.java`

- [ ] **Step 1: Add file upload method to TeacherService**

Add this import and method to `TeacherService.java`. Add `@Value` field and inject the path:

```java
import org.springframework.beans.factory.annotation.Value;
```

Add field after the existing fields:

```java
    @Value("${labex.upload.experiment-path}")
    private String experimentPath;
```

Add method at the end of the class (before the closing brace):

```java
    public void uploadItemAttachment(Integer itemId, MultipartFile file) throws java.io.IOException {
        ExperimentItem item = experimentItemMapper.selectById(itemId);
        if (item == null) throw new BusinessException("题目不存在");
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".") + 1) : "";
        java.io.File dir = new java.io.File(experimentPath);
        if (!dir.exists()) dir.mkdirs();
        String filename = itemId + "_attachment." + ext;
        file.transferTo(new java.io.File(dir, filename));
        item.setFilePath(filename);
        experimentItemMapper.updateById(item);
    }

    public java.io.File getStudentAnswerFile(Integer studentItemId) {
        StudentItem si = studentItemMapper.selectById(studentItemId);
        if (si == null) throw new BusinessException("学生提交不存在");
        if (si.getFilePath() == null || si.getFilePath().isEmpty()) {
            throw new BusinessException("该提交无附件");
        }
        java.io.File file = new java.io.File(new java.io.File(experimentPath).getParent(), "answers/" + si.getFilePath());
        if (!file.exists()) throw new BusinessException("文件不存在");
        return file;
    }
```

Also add `MultipartFile` import if not present — it is already imported in TeacherService.

- [ ] **Step 2: Add attachment endpoints to TeacherController**

Add `@Value` field and endpoints to `TeacherController.java`:

```java
    @Value("${labex.upload.experiment-path}")
    private String experimentPath;
```

Add these endpoint methods after the `setItemAnswer` endpoint (after line 220):

```java
    @PostMapping("/experiments/items/{itemId}/attachment")
    public Result<Void> uploadItemAttachment(@PathVariable Integer itemId,
                                              @RequestParam MultipartFile file,
                                              HttpSession session) throws IOException {
        verifyTeacher(session);
        teacherService.uploadItemAttachment(itemId, file);
        return Result.ok();
    }

    @GetMapping("/reports/items/{studentItemId}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadStudentAnswer(
            @PathVariable Integer studentItemId, HttpSession session) throws IOException {
        verifyTeacher(session);
        java.io.File file = teacherService.getStudentAnswerFile(studentItemId);
        org.springframework.core.io.FileSystemResource resource =
                new org.springframework.core.io.FileSystemResource(file);
        String contentType = java.nio.file.Files.probeContentType(file.toPath());
        if (contentType == null) contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }
```

Note: `ResponseEntity`, `MediaType`, `HttpHeaders`, `FileSystemResource` are already imported in TeacherController. Add `import org.springframework.core.io.Resource;` if not present — it is NOT currently imported, so add:

```java
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
```

- [ ] **Step 3: Add student file upload endpoint to StudentController**

Add `@Value` fields to `StudentController.java`:

```java
    @Value("${labex.upload.answers-path}")
    private String answersPath;
```

Add this endpoint after the existing `saveAnswer` method (after line 87):

```java
    @PostMapping("/items/{itemId}/upload")
    public Result<Void> uploadAnswer(@PathVariable Integer itemId,
                                      @RequestParam MultipartFile file,
                                      HttpSession session) throws IOException {
        UserTokenVO token = verifyStudent(session);
        studentService.uploadAnswerFile(itemId, token.getUserId(), file);
        return Result.ok();
    }
```

- [ ] **Step 4: Add uploadAnswerFile method to StudentService**

Add this method to `StudentService.java`. Add `@Value` field:

```java
    @Value("${labex.upload.answers-path}")
    private String answersPath;
```

Add import:

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
```

Add method:

```java
    public void uploadAnswerFile(Integer itemId, Integer studentId, MultipartFile file) throws java.io.IOException {
        StudentItem existing = studentItemMapper.selectOne(
                new QueryWrapper<StudentItem>()
                        .eq("item_id", itemId)
                        .eq("student_id", studentId));

        if (existing != null && existing.getScoreFlag() != null && existing.getScoreFlag() == 1) {
            throw new BusinessException("该题目已批改，无法修改");
        }

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".") + 1) : "";
        java.io.File dir = new java.io.File(answersPath);
        if (!dir.exists()) dir.mkdirs();
        String filename = studentId + "_" + itemId + "." + ext;
        file.transferTo(new java.io.File(dir, filename));

        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            StudentItem item = new StudentItem();
            item.setItemId(itemId);
            item.setStudentId(studentId);
            item.setContent("[文件提交: " + filename + "]");
            item.setFilePath(filename);
            item.setFillTime(now);
            studentItemMapper.insert(item);
        } else {
            existing.setContent("[文件提交: " + filename + "]");
            existing.setFilePath(filename);
            existing.setFillTime(now);
            studentItemMapper.updateById(existing);
        }
    }
```

- [ ] **Step 5: Compile to verify**

```bash
cd labex && mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add labex/src/main/java/labex/controller/TeacherController.java labex/src/main/java/labex/service/TeacherService.java labex/src/main/java/labex/controller/StudentController.java labex/src/main/java/labex/service/StudentService.java
git commit -m "feat: add file upload/download for experiment items and student answers"
```

---

## Task 5: Fill-in-Blank Auto-Scoring in Experiments

**Files:**
- Modify: `labex/src/main/java/labex/service/StudentService.java`

- [ ] **Step 1: Modify saveAnswer to auto-score objective questions**

In `StudentService.java`, add import:

```java
import labex.common.ScoringUtil;
```

Replace the existing `saveAnswer` method (lines 103-135) with:

```java
    public void saveAnswer(Integer itemId, Integer studentId, String content) {
        StudentItem existing = studentItemMapper.selectOne(
                new QueryWrapper<StudentItem>()
                        .eq("item_id", itemId)
                        .eq("student_id", studentId));

        if (existing != null && existing.getScoreFlag() != null && existing.getScoreFlag() == 1) {
            throw new BusinessException("该题目已批改，无法修改");
        }

        LocalDateTime now = LocalDateTime.now();

        // Auto-score objective questions
        ExperimentItem experimentItem = experimentItemMapper.selectById(itemId);
        Integer autoScore = null;
        if (experimentItem != null && ScoringUtil.isAutoScorable(experimentItem.getExperimentItemType())) {
            autoScore = ScoringUtil.autoScore(
                    experimentItem.getExperimentItemType(),
                    experimentItem.getExperimentItemAnswer(),
                    content,
                    experimentItem.getExperimentItemScore() != null ? experimentItem.getExperimentItemScore() : 0);
        }

        if (existing == null) {
            StudentItem item = new StudentItem();
            item.setItemId(itemId);
            item.setStudentId(studentId);
            item.setContent(content);
            item.setFillTime(now);
            if (autoScore != null) {
                item.setScore(autoScore);
                item.setScoreFlag(1);
            }
            studentItemMapper.insert(item);
            saveLog(item.getStudentItemId(), content, now);
        } else {
            existing.setContent(content);
            existing.setFillTime(now);
            if (autoScore != null) {
                existing.setScore(autoScore);
                existing.setScoreFlag(1);
            }
            studentItemMapper.updateById(existing);
            saveLog(existing.getStudentItemId(), content, now);
        }
    }
```

- [ ] **Step 2: Compile to verify**

```bash
cd labex && mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add labex/src/main/java/labex/service/StudentService.java
git commit -m "feat: auto-score objective questions in experiment answers"
```

---

## Task 6: Exercise Item Edit and Delete

**Files:**
- Modify: `labex/src/main/java/labex/service/ExerciseService.java`
- Modify: `labex/src/main/java/labex/controller/ExerciseController.java`

- [ ] **Step 1: Add updateItem and deleteItem methods to ExerciseService**

Add these methods to `ExerciseService.java` after the `addExerciseItem` method (after line 93):

```java
    public void updateExerciseItem(Ex3Item item) {
        ex3ItemMapper.updateById(item);
    }

    public void deleteExerciseItem(Integer itemId) {
        // Delete student answers for this item first
        studentExerciseMapper.delete(
                new QueryWrapper<StudentExercise>().eq("item_id", itemId));
        ex3ItemMapper.deleteById(itemId);
    }
```

- [ ] **Step 2: Add PUT and DELETE item endpoints to ExerciseController**

Add these endpoint methods to `ExerciseController.java` after the `addItem` method (after line 62):

```java
    @PutMapping("/{exerciseId}/items/{itemId}")
    public Result<Void> updateItem(@PathVariable Integer exerciseId,
                                    @PathVariable Integer itemId,
                                    @RequestBody Ex3Item item, HttpSession session) {
        item.setExcerciseItemId(itemId);
        item.setExcerciseId(exerciseId);
        exerciseService.updateExerciseItem(item);
        return Result.ok();
    }

    @DeleteMapping("/{exerciseId}/items/{itemId}")
    public Result<Void> deleteItem(@PathVariable Integer exerciseId,
                                    @PathVariable Integer itemId, HttpSession session) {
        exerciseService.deleteExerciseItem(itemId);
        return Result.ok();
    }
```

- [ ] **Step 3: Compile to verify**

```bash
cd labex && mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add labex/src/main/java/labex/service/ExerciseService.java labex/src/main/java/labex/controller/ExerciseController.java
git commit -m "feat: add exercise item update and delete endpoints"
```

---

## Task 7: Exercise Grading and Auto-Scoring

**Files:**
- Modify: `labex/src/main/java/labex/service/ExerciseService.java`
- Modify: `labex/src/main/java/labex/controller/ExerciseController.java`
- Modify: `labex/src/main/java/labex/controller/StudentController.java`

- [ ] **Step 1: Modify answerQuestion to auto-score objective questions**

In `ExerciseService.java`, add import:

```java
import labex.common.ScoringUtil;
```

Replace the `answerQuestion` method (lines 95-122) with:

```java
    public void answerQuestion(Integer studentId, Integer itemId, Integer type, String answer) {
        Ex3Item item = ex3ItemMapper.selectById(itemId);
        if (item == null) throw new labex.common.BusinessException("题目不存在");

        // Auto-score objective questions
        Integer autoScore = null;
        if (ScoringUtil.isAutoScorable(type)) {
            autoScore = ScoringUtil.autoScore(type, item.getAnswer(), answer, 100);
        }

        StudentExercise existing = studentExerciseMapper.selectOne(
                new QueryWrapper<StudentExercise>()
                        .eq("student_id", studentId)
                        .eq("item_id", itemId));

        if (existing == null) {
            StudentExercise se = new StudentExercise();
            se.setStudentId(studentId);
            se.setItemId(itemId);
            se.setFillTime(LocalDateTime.now());
            if (type == 1 || type == 2) {
                se.setAnswer(answer);
            } else {
                se.setContent(answer);
            }
            if (autoScore != null) {
                se.setScore(autoScore);
            }
            studentExerciseMapper.insert(se);
        } else {
            existing.setFillTime(LocalDateTime.now());
            if (type == 1 || type == 2) {
                existing.setAnswer(answer);
            } else {
                existing.setContent(answer);
            }
            if (autoScore != null) {
                existing.setScore(autoScore);
            }
            studentExerciseMapper.updateById(existing);
        }
    }
```

- [ ] **Step 2: Add grading methods to ExerciseService**

Add these methods to `ExerciseService.java` after `deleteExerciseItem`:

```java
    public List<Map<String, Object>> getExerciseSubmissions(Integer exerciseId) {
        List<Ex3Item> items = getExerciseItems(exerciseId);
        if (items.isEmpty()) return new java.util.ArrayList<>();

        List<Integer> itemIds = items.stream().map(Ex3Item::getExcerciseItemId).collect(Collectors.toList());
        List<StudentExercise> answers = studentExerciseMapper.selectList(
                new QueryWrapper<StudentExercise>().in("item_id", itemIds));

        // Group by studentId
        Map<Integer, List<StudentExercise>> byStudent = answers.stream()
                .collect(Collectors.groupingBy(StudentExercise::getStudentId));

        return byStudent.entrySet().stream().map(entry -> {
            Map<String, Object> m = new HashMap<>();
            m.put("studentId", entry.getKey());
            m.put("submittedItemCount", entry.getValue().size());
            m.put("totalItemCount", items.size());
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getExerciseSubmissionDetail(Integer exerciseId, Integer studentId) {
        return getExerciseItemsWithAnswer(exerciseId, studentId);
    }

    public void submitExerciseScore(Long studentExerciseId, Integer score) {
        StudentExercise se = studentExerciseMapper.selectById(studentExerciseId);
        if (se == null) throw new labex.common.BusinessException("学生提交不存在");
        se.setScore(score);
        studentExerciseMapper.updateById(se);
    }

    public List<Map<String, Object>> getExerciseScores(Integer exerciseId) {
        List<Ex3Item> items = getExerciseItems(exerciseId);
        if (items.isEmpty()) return new java.util.ArrayList<>();

        List<Integer> itemIds = items.stream().map(Ex3Item::getExcerciseItemId).collect(Collectors.toList());
        List<StudentExercise> answers = studentExerciseMapper.selectList(
                new QueryWrapper<StudentExercise>().in("item_id", itemIds));

        // Calculate total score per student
        Map<Integer, Integer> scoreByStudent = new HashMap<>();
        Map<Integer, Integer> countByStudent = new HashMap<>();
        int maxTotal = items.size() * 100; // Each item max 100 points (from auto-scoring)
        for (StudentExercise se : answers) {
            int s = se.getScore() != null ? se.getScore() : 0;
            scoreByStudent.merge(se.getStudentId(), s, Integer::sum);
            countByStudent.merge(se.getStudentId(), 1, Integer::sum);
        }

        return scoreByStudent.entrySet().stream().map(entry -> {
            Map<String, Object> m = new HashMap<>();
            m.put("studentId", entry.getKey());
            m.put("totalScore", entry.getValue());
            m.put("answeredCount", countByStudent.getOrDefault(entry.getKey(), 0));
            m.put("totalCount", items.size());
            return m;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getExerciseStudentScore(Integer exerciseId, Integer studentId) {
        List<Ex3Item> items = getExerciseItems(exerciseId);
        List<StudentExercise> answers = studentExerciseMapper.selectList(
                new QueryWrapper<StudentExercise>()
                        .eq("student_id", studentId)
                        .in("item_id", items.stream().map(Ex3Item::getExcerciseItemId).collect(Collectors.toList())));

        int totalScore = answers.stream().mapToInt(a -> a.getScore() != null ? a.getScore() : 0).sum();
        int answeredCount = answers.size();

        Map<String, Object> result = new HashMap<>();
        result.put("totalScore", totalScore);
        result.put("answeredCount", answeredCount);
        result.put("totalCount", items.size());
        return result;
    }
```

- [ ] **Step 3: Add grading endpoints to ExerciseController**

Add these endpoints to `ExerciseController.java` after the `deleteItem` method:

```java
    @GetMapping("/{id}/submissions")
    public Result<List<Map<String, Object>>> getSubmissions(@PathVariable Integer id, HttpSession session) {
        return Result.ok(exerciseService.getExerciseSubmissions(id));
    }

    @GetMapping("/{id}/submissions/{studentId}")
    public Result<List<Map<String, Object>>> getSubmissionDetail(
            @PathVariable Integer id, @PathVariable Integer studentId, HttpSession session) {
        return Result.ok(exerciseService.getExerciseSubmissionDetail(id, studentId));
    }

    @PostMapping("/scores")
    public Result<Void> submitScore(@RequestBody Map<String, Object> body, HttpSession session) {
        Long studentExerciseId = ((Number) body.get("studentExerciseId")).longValue();
        Integer score = (Integer) body.get("score");
        exerciseService.submitExerciseScore(studentExerciseId, score);
        return Result.ok();
    }

    @GetMapping("/{id}/scores")
    public Result<List<Map<String, Object>>> getScores(@PathVariable Integer id, HttpSession session) {
        return Result.ok(exerciseService.getExerciseScores(id));
    }
```

Also add `import java.util.Map;` — it's already imported.

- [ ] **Step 4: Add student exercise score endpoint to StudentController**

Add this endpoint to `StudentController.java` after `answerExercise`:

```java
    @GetMapping("/exercises/{id}/score")
    public Result<Map<String, Object>> getExerciseScore(@PathVariable Integer id, HttpSession session) {
        UserTokenVO token = verifyStudent(session);
        return Result.ok(exerciseService.getExerciseStudentScore(id, token.getUserId()));
    }
```

- [ ] **Step 5: Compile to verify**

```bash
cd labex && mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add labex/src/main/java/labex/service/ExerciseService.java labex/src/main/java/labex/controller/ExerciseController.java labex/src/main/java/labex/controller/StudentController.java
git commit -m "feat: add exercise grading, auto-scoring, and score summary"
```

---

## Task 8: Exam Entities

**Files:**
- Create: `labex/src/main/java/labex/entity/Exam.java`
- Create: `labex/src/main/java/labex/entity/ExamItem.java`
- Create: `labex/src/main/java/labex/entity/StudentExamAnswer.java`
- Create: `labex/src/main/java/labex/entity/ExamSubmission.java`

- [ ] **Step 1: Create Exam entity**

Create `labex/src/main/java/labex/entity/Exam.java`:

```java
package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_exam")
public class Exam {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    private String description;
    private Integer duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer createdBy;
    private LocalDateTime time;
    private Boolean flag;
}
```

- [ ] **Step 2: Create ExamItem entity**

Create `labex/src/main/java/labex/entity/ExamItem.java`:

```java
package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_exam_item")
public class ExamItem {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer examId;
    private Integer type;
    private String content;
    private String options;
    private String answer;
    private Integer score;
}
```

- [ ] **Step 3: Create StudentExamAnswer entity**

Create `labex/src/main/java/labex/entity/StudentExamAnswer.java`:

```java
package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("t_student_exam_answer")
public class StudentExamAnswer {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer examItemId;
    private Integer studentId;
    private String answer;
    private String content;
    private String filePath;
    private Integer score;
    private Integer autoScored;
    private LocalDateTime submitTime;
}
```

- [ ] **Step 4: Create ExamSubmission entity**

Create `labex/src/main/java/labex/entity/ExamSubmission.java`:

```java
package labex.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_exam_submission")
public class ExamSubmission {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer examId;
    private Integer studentId;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private BigDecimal totalScore;
    private Integer status;
}
```

- [ ] **Step 5: Commit**

```bash
git add labex/src/main/java/labex/entity/Exam.java labex/src/main/java/labex/entity/ExamItem.java labex/src/main/java/labex/entity/StudentExamAnswer.java labex/src/main/java/labex/entity/ExamSubmission.java
git commit -m "feat: add exam entities (Exam, ExamItem, StudentExamAnswer, ExamSubmission)"
```

---

## Task 9: Exam Mappers and DTOs

**Files:**
- Create: `labex/src/main/java/labex/mapper/ExamMapper.java`
- Create: `labex/src/main/java/labex/mapper/ExamItemMapper.java`
- Create: `labex/src/main/java/labex/mapper/StudentExamAnswerMapper.java`
- Create: `labex/src/main/java/labex/mapper/ExamSubmissionMapper.java`
- Create: `labex/src/main/java/labex/dto/ExamSubmitDTO.java`
- Create: `labex/src/main/java/labex/dto/ExamSubmitItemDTO.java`

- [ ] **Step 1: Create mapper interfaces**

Create `labex/src/main/java/labex/mapper/ExamMapper.java`:

```java
package labex.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import labex.entity.Exam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExamMapper extends BaseMapper<Exam> {
}
```

Create `labex/src/main/java/labex/mapper/ExamItemMapper.java`:

```java
package labex.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import labex.entity.ExamItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExamItemMapper extends BaseMapper<ExamItem> {
}
```

Create `labex/src/main/java/labex/mapper/StudentExamAnswerMapper.java`:

```java
package labex.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import labex.entity.StudentExamAnswer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentExamAnswerMapper extends BaseMapper<StudentExamAnswer> {
}
```

Create `labex/src/main/java/labex/mapper/ExamSubmissionMapper.java`:

```java
package labex.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import labex.entity.ExamSubmission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ExamSubmissionMapper extends BaseMapper<ExamSubmission> {
}
```

- [ ] **Step 2: Create DTOs**

Create `labex/src/main/java/labex/dto/ExamSubmitItemDTO.java`:

```java
package labex.dto;

import lombok.Data;

@Data
public class ExamSubmitItemDTO {
    private Integer examItemId;
    private String answer;
}
```

Create `labex/src/main/java/labex/dto/ExamSubmitDTO.java`:

```java
package labex.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamSubmitDTO {
    private List<ExamSubmitItemDTO> answers;
}
```

- [ ] **Step 3: Commit**

```bash
git add labex/src/main/java/labex/mapper/ExamMapper.java labex/src/main/java/labex/mapper/ExamItemMapper.java labex/src/main/java/labex/mapper/StudentExamAnswerMapper.java labex/src/main/java/labex/mapper/ExamSubmissionMapper.java labex/src/main/java/labex/dto/ExamSubmitDTO.java labex/src/main/java/labex/dto/ExamSubmitItemDTO.java
git commit -m "feat: add exam mappers and submission DTOs"
```

---

## Task 10: ExamService

**Files:**
- Create: `labex/src/main/java/labex/service/ExamService.java`

- [ ] **Step 1: Create ExamService with full teacher and student logic**

Create `labex/src/main/java/labex/service/ExamService.java`:

```java
package labex.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import labex.common.BusinessException;
import labex.common.ScoringUtil;
import labex.dto.ExamSubmitDTO;
import labex.dto.ExamSubmitItemDTO;
import labex.entity.*;
import labex.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private final ExamMapper examMapper;
    private final ExamItemMapper examItemMapper;
    private final StudentExamAnswerMapper studentExamAnswerMapper;
    private final ExamSubmissionMapper examSubmissionMapper;

    public ExamService(ExamMapper examMapper, ExamItemMapper examItemMapper,
                       StudentExamAnswerMapper studentExamAnswerMapper,
                       ExamSubmissionMapper examSubmissionMapper) {
        this.examMapper = examMapper;
        this.examItemMapper = examItemMapper;
        this.studentExamAnswerMapper = studentExamAnswerMapper;
        this.examSubmissionMapper = examSubmissionMapper;
    }

    // ===== Teacher: Exam CRUD =====

    public List<Exam> listExams() {
        return examMapper.selectList(new QueryWrapper<Exam>().orderByDesc("id"));
    }

    public Exam getExam(Integer id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) throw new BusinessException("考试不存在");
        return exam;
    }

    public void addExam(Exam exam) {
        examMapper.insert(exam);
    }

    public void updateExam(Exam exam) {
        examMapper.updateById(exam);
    }

    public void deleteExam(Integer id) {
        // Delete student answers
        List<ExamItem> items = examItemMapper.selectList(
                new QueryWrapper<ExamItem>().eq("exam_id", id));
        for (ExamItem item : items) {
            studentExamAnswerMapper.delete(
                    new QueryWrapper<StudentExamAnswer>().eq("exam_item_id", item.getId()));
        }
        examItemMapper.delete(new QueryWrapper<ExamItem>().eq("exam_id", id));
        examSubmissionMapper.delete(new QueryWrapper<ExamSubmission>().eq("exam_id", id));
        examMapper.deleteById(id);
    }

    // ===== Teacher: Exam Items =====

    public List<ExamItem> getExamItems(Integer examId) {
        return examItemMapper.selectList(
                new QueryWrapper<ExamItem>().eq("exam_id", examId));
    }

    public void addExamItem(ExamItem item) {
        examItemMapper.insert(item);
    }

    public void updateExamItem(ExamItem item) {
        examItemMapper.updateById(item);
    }

    public void deleteExamItem(Integer itemId) {
        studentExamAnswerMapper.delete(
                new QueryWrapper<StudentExamAnswer>().eq("exam_item_id", itemId));
        examItemMapper.deleteById(itemId);
    }

    // ===== Teacher: Grading =====

    public List<Map<String, Object>> getExamSubmissions(Integer examId) {
        List<ExamSubmission> submissions = examSubmissionMapper.selectList(
                new QueryWrapper<ExamSubmission>().eq("exam_id", examId));
        return submissions.stream().map(sub -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", sub.getId());
            m.put("studentId", sub.getStudentId());
            m.put("startTime", sub.getStartTime());
            m.put("submitTime", sub.getSubmitTime());
            m.put("totalScore", sub.getTotalScore());
            m.put("status", sub.getStatus());
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getExamSubmissionDetail(Integer examId, Integer studentId) {
        List<ExamItem> items = getExamItems(examId);
        List<StudentExamAnswer> answers = studentExamAnswerMapper.selectList(
                new QueryWrapper<StudentExamAnswer>()
                        .in("exam_item_id", items.stream().map(ExamItem::getId).collect(Collectors.toList()))
                        .eq("student_id", studentId));

        Map<Integer, StudentExamAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(StudentExamAnswer::getExamItemId, a -> a));

        return items.stream().map(item -> {
            Map<String, Object> m = new HashMap<>();
            m.put("itemId", item.getId());
            m.put("type", item.getType());
            m.put("content", item.getContent());
            m.put("options", item.getOptions());
            m.put("maxScore", item.getScore());
            m.put("referenceAnswer", item.getAnswer());

            StudentExamAnswer ans = answerMap.get(item.getId());
            m.put("studentAnswer", ans != null ? ans.getAnswer() : null);
            m.put("studentContent", ans != null ? ans.getContent() : null);
            m.put("studentFilePath", ans != null ? ans.getFilePath() : null);
            m.put("score", ans != null ? ans.getScore() : null);
            m.put("autoScored", ans != null ? ans.getAutoScored() : null);
            m.put("answerId", ans != null ? ans.getId() : null);
            return m;
        }).collect(Collectors.toList());
    }

    public void submitExamScore(Integer answerId, Integer score) {
        StudentExamAnswer ans = studentExamAnswerMapper.selectById(answerId);
        if (ans == null) throw new BusinessException("学生答案不存在");
        ans.setScore(score);
        ans.setAutoScored(0);
        studentExamAnswerMapper.updateById(ans);

        // Check if all questions are graded for this exam
        ExamItem item = examItemMapper.selectById(ans.getExamItemId());
        checkAndFinalizeSubmission(item.getExamId(), ans.getStudentId());
    }

    private void checkAndFinalizeSubmission(Integer examId, Integer studentId) {
        List<ExamItem> items = getExamItems(examId);
        List<StudentExamAnswer> answers = studentExamAnswerMapper.selectList(
                new QueryWrapper<StudentExamAnswer>()
                        .in("exam_item_id", items.stream().map(ExamItem::getId).collect(Collectors.toList()))
                        .eq("student_id", studentId));

        boolean allGraded = true;
        BigDecimal total = BigDecimal.ZERO;
        for (ExamItem item : items) {
            StudentExamAnswer ans = answers.stream()
                    .filter(a -> a.getExamItemId().equals(item.getId()))
                    .findFirst().orElse(null);
            if (ans == null || ans.getScore() == null) {
                allGraded = false;
                break;
            }
            total = total.add(BigDecimal.valueOf(ans.getScore()));
        }

        ExamSubmission submission = examSubmissionMapper.selectOne(
                new QueryWrapper<ExamSubmission>()
                        .eq("exam_id", examId)
                        .eq("student_id", studentId));
        if (submission != null) {
            submission.setTotalScore(total);
            if (allGraded) {
                submission.setStatus(2);
            }
            examSubmissionMapper.updateById(submission);
        }
    }

    // ===== Teacher: Score Summary =====

    public List<Map<String, Object>> getExamScores(Integer examId) {
        List<ExamSubmission> submissions = examSubmissionMapper.selectList(
                new QueryWrapper<ExamSubmission>().eq("exam_id", examId));
        return submissions.stream().map(sub -> {
            Map<String, Object> m = new HashMap<>();
            m.put("studentId", sub.getStudentId());
            m.put("totalScore", sub.getTotalScore());
            m.put("status", sub.getStatus());
            m.put("submitTime", sub.getSubmitTime());
            return m;
        }).collect(Collectors.toList());
    }

    // ===== Student: Exam =====

    public List<Exam> listAvailableExams() {
        LocalDateTime now = LocalDateTime.now();
        return examMapper.selectList(
                new QueryWrapper<Exam>()
                        .le("start_time", now)
                        .ge("end_time", now)
                        .orderByAsc("start_time"));
    }

    public void startExam(Integer examId, Integer studentId) {
        Exam exam = getExam(examId);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime())) {
            throw new BusinessException("考试尚未开始");
        }
        if (now.isAfter(exam.getEndTime())) {
            throw new BusinessException("考试已结束");
        }

        // Check if already submitted
        ExamSubmission existing = examSubmissionMapper.selectOne(
                new QueryWrapper<ExamSubmission>()
                        .eq("exam_id", examId)
                        .eq("student_id", studentId));
        if (existing != null && existing.getSubmitTime() != null) {
            throw new BusinessException("已提交过考试");
        }

        if (existing == null) {
            ExamSubmission sub = new ExamSubmission();
            sub.setExamId(examId);
            sub.setStudentId(studentId);
            sub.setStartTime(now);
            sub.setStatus(0);
            examSubmissionMapper.insert(sub);
        } else {
            // Re-entering - update is fine, keep original start_time
        }
    }

    public List<Map<String, Object>> getStudentExamItems(Integer examId, Integer studentId) {
        Exam exam = getExam(examId);
        List<ExamItem> items = getExamItems(examId);

        // Check submission status
        ExamSubmission sub = examSubmissionMapper.selectOne(
                new QueryWrapper<ExamSubmission>()
                        .eq("exam_id", examId)
                        .eq("student_id", studentId));
        if (sub == null) {
            throw new BusinessException("请先开始考试");
        }
        if (sub.getSubmitTime() != null) {
            throw new BusinessException("已提交过考试");
        }

        // Check time limits
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(exam.getEndTime())) {
            throw new BusinessException("考试已结束");
        }
        if (exam.getDuration() != null && sub.getStartTime() != null) {
            LocalDateTime deadline = sub.getStartTime().plusMinutes(exam.getDuration());
            if (now.isAfter(deadline)) {
                throw new BusinessException("考试时长已到");
            }
        }

        // Return items without answers (exam in progress)
        return items.stream().map(item -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", item.getId());
            m.put("type", item.getType());
            m.put("content", item.getContent());
            m.put("options", item.getOptions());
            m.put("score", item.getScore());
            return m;
        }).collect(Collectors.toList());
    }

    public void submitExam(Integer examId, Integer studentId, ExamSubmitDTO dto) {
        Exam exam = getExam(examId);

        ExamSubmission sub = examSubmissionMapper.selectOne(
                new QueryWrapper<ExamSubmission>()
                        .eq("exam_id", examId)
                        .eq("student_id", studentId));
        if (sub == null) throw new BusinessException("请先开始考试");
        if (sub.getSubmitTime() != null) throw new BusinessException("已提交过考试");

        // Time checks
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(exam.getEndTime())) {
            throw new BusinessException("考试已结束，无法提交");
        }
        if (exam.getDuration() != null && sub.getStartTime() != null) {
            LocalDateTime deadline = sub.getStartTime().plusMinutes(exam.getDuration());
            if (now.isAfter(deadline)) {
                throw new BusinessException("考试时长已到，无法提交");
            }
        }

        // Save answers
        for (ExamSubmitItemDTO item : dto.getAnswers()) {
            ExamItem examItem = examItemMapper.selectById(item.getExamItemId());
            if (examItem == null || !examItem.getExamId().equals(examId)) continue;

            StudentExamAnswer ans = new StudentExamAnswer();
            ans.setExamItemId(item.getExamItemId());
            ans.setStudentId(studentId);
            ans.setSubmitTime(now);

            // Store answer based on type
            int type = examItem.getType();
            if (type == 1 || type == 2 || type == 3 || type == 4) {
                ans.setAnswer(item.getAnswer());
            } else {
                ans.setContent(item.getAnswer());
            }

            // Auto-score objective questions
            if (ScoringUtil.isAutoScorable(type)) {
                Integer autoScore = ScoringUtil.autoScore(type, examItem.getAnswer(),
                        item.getAnswer(), examItem.getScore());
                if (autoScore != null) {
                    ans.setScore(autoScore);
                    ans.setAutoScored(1);
                }
            }

            studentExamAnswerMapper.insert(ans);
        }

        // Update submission
        sub.setSubmitTime(now);
        sub.setStatus(1);
        examSubmissionMapper.updateById(sub);

        // Check if all auto-scored (no subjective questions)
        checkAndFinalizeSubmission(examId, studentId);
    }

    public Map<String, Object> getStudentExamScore(Integer examId, Integer studentId) {
        ExamSubmission sub = examSubmissionMapper.selectOne(
                new QueryWrapper<ExamSubmission>()
                        .eq("exam_id", examId)
                        .eq("student_id", studentId));
        if (sub == null) throw new BusinessException("未参加该考试");
        if (sub.getStatus() == null || sub.getStatus() < 2) {
            throw new BusinessException("成绩尚未公布");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalScore", sub.getTotalScore());
        result.put("status", sub.getStatus());
        return result;
    }
}
```

- [ ] **Step 2: Compile to verify**

```bash
cd labex && mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add labex/src/main/java/labex/service/ExamService.java
git commit -m "feat: add ExamService with full teacher/student exam logic"
```

---

## Task 11: ExamController (Teacher)

**Files:**
- Create: `labex/src/main/java/labex/controller/ExamController.java`

- [ ] **Step 1: Create ExamController**

Create `labex/src/main/java/labex/controller/ExamController.java`:

```java
package labex.controller;

import labex.common.Result;
import labex.dto.SessionUtil;
import labex.dto.UserTokenVO;
import labex.entity.Exam;
import labex.entity.ExamItem;
import labex.service.ExamService;
import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    private void verifyTeacher(HttpSession session) {
        UserTokenVO token = SessionUtil.getUserToken(session);
        if (token.getUserType() != 0) {
            throw new labex.common.BusinessException("无权限访问");
        }
    }

    @GetMapping
    public Result<List<Exam>> list(HttpSession session) {
        verifyTeacher(session);
        return Result.ok(examService.listExams());
    }

    @PostMapping
    public Result<Void> add(@RequestBody Exam exam, HttpSession session) {
        verifyTeacher(session);
        UserTokenVO token = SessionUtil.getUserToken(session);
        exam.setCreatedBy(token.getUserId());
        examService.addExam(exam);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Exam exam, HttpSession session) {
        verifyTeacher(session);
        exam.setId(id);
        examService.updateExam(exam);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        examService.deleteExam(id);
        return Result.ok();
    }

    @GetMapping("/{id}/items")
    public Result<List<ExamItem>> getItems(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(examService.getExamItems(id));
    }

    @PostMapping("/{id}/items")
    public Result<Void> addItem(@PathVariable Integer id, @RequestBody ExamItem item, HttpSession session) {
        verifyTeacher(session);
        item.setExamId(id);
        examService.addExamItem(item);
        return Result.ok();
    }

    @PutMapping("/items/{itemId}")
    public Result<Void> updateItem(@PathVariable Integer itemId, @RequestBody ExamItem item, HttpSession session) {
        verifyTeacher(session);
        item.setId(itemId);
        examService.updateExamItem(item);
        return Result.ok();
    }

    @DeleteMapping("/items/{itemId}")
    public Result<Void> deleteItem(@PathVariable Integer itemId, HttpSession session) {
        verifyTeacher(session);
        examService.deleteExamItem(itemId);
        return Result.ok();
    }

    @GetMapping("/{id}/submissions")
    public Result<List<Map<String, Object>>> getSubmissions(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(examService.getExamSubmissions(id));
    }

    @GetMapping("/{id}/submissions/{studentId}")
    public Result<List<Map<String, Object>>> getSubmissionDetail(
            @PathVariable Integer id, @PathVariable Integer studentId, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(examService.getExamSubmissionDetail(id, studentId));
    }

    @PostMapping("/scores")
    public Result<Void> submitScore(@RequestBody Map<String, Object> body, HttpSession session) {
        verifyTeacher(session);
        Integer answerId = (Integer) body.get("answerId");
        Integer score = (Integer) body.get("score");
        examService.submitExamScore(answerId, score);
        return Result.ok();
    }

    @GetMapping("/{id}/scores")
    public Result<List<Map<String, Object>>> getScores(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(examService.getExamScores(id));
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add labex/src/main/java/labex/controller/ExamController.java
git commit -m "feat: add ExamController with teacher exam management endpoints"
```

---

## Task 12: Student Exam Endpoints

**Files:**
- Modify: `labex/src/main/java/labex/controller/StudentController.java`

- [ ] **Step 1: Add exam service injection to StudentController**

In `StudentController.java`, add import:

```java
import labex.service.ExamService;
import labex.dto.ExamSubmitDTO;
```

Add field:

```java
    private final ExamService examService;
```

Update constructor:

```java
    public StudentController(StudentService studentService, ExerciseService exerciseService, ExamService examService) {
        this.studentService = studentService;
        this.exerciseService = exerciseService;
        this.examService = examService;
    }
```

- [ ] **Step 2: Add student exam endpoints**

Add these endpoints to `StudentController.java` after the existing exercises section (after `getExerciseScore`):

```java
    // ===== Exams (Student) =====

    @GetMapping("/exams")
    public Result<List<Exam>> listExams(HttpSession session) {
        verifyStudent(session);
        return Result.ok(examService.listAvailableExams());
    }

    @PostMapping("/exams/{id}/start")
    public Result<Void> startExam(@PathVariable Integer id, HttpSession session) {
        UserTokenVO token = verifyStudent(session);
        examService.startExam(id, token.getUserId());
        return Result.ok();
    }

    @GetMapping("/exams/{id}/items")
    public Result<List<Map<String, Object>>> getExamItems(@PathVariable Integer id, HttpSession session) {
        UserTokenVO token = verifyStudent(session);
        return Result.ok(examService.getStudentExamItems(id, token.getUserId()));
    }

    @PostMapping("/exams/{id}/submit")
    public Result<Void> submitExam(@PathVariable Integer id,
                                    @RequestBody ExamSubmitDTO dto,
                                    HttpSession session) {
        UserTokenVO token = verifyStudent(session);
        examService.submitExam(id, token.getUserId(), dto);
        return Result.ok();
    }

    @GetMapping("/exams/{id}/score")
    public Result<Map<String, Object>> getExamScore(@PathVariable Integer id, HttpSession session) {
        UserTokenVO token = verifyStudent(session);
        return Result.ok(examService.getStudentExamScore(id, token.getUserId()));
    }
```

Also add `import labex.entity.Exam;` — it's already imported via `import labex.entity.*;`.

- [ ] **Step 3: Compile to verify**

```bash
cd labex && mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add labex/src/main/java/labex/controller/StudentController.java
git commit -m "feat: add student exam endpoints (start, items, submit, score)"
```

---

## Task 13: Final Compile and Verification

- [ ] **Step 1: Full compile check**

```bash
cd labex && mvn compile
```

Expected: BUILD SUCCESS

- [ ] **Step 2: Verify all new files exist**

```bash
find labex/src -name "Exam*" -o -name "ScoringUtil*" | sort
```

Expected output:
```
labex/src/main/java/labex/common/ScoringUtil.java
labex/src/main/java/labex/controller/ExamController.java
labex/src/main/java/labex/dto/ExamSubmitDTO.java
labex/src/main/java/labex/dto/ExamSubmitItemDTO.java
labex/src/main/java/labex/entity/Exam.java
labex/src/main/java/labex/entity/ExamItem.java
labex/src/main/java/labex/entity/ExamSubmission.java
labex/src/main/java/labex/mapper/ExamItemMapper.java
labex/src/main/java/labex/mapper/ExamMapper.java
labex/src/main/java/labex/mapper/ExamSubmissionMapper.java
labex/src/main/java/labex/mapper/StudentExamAnswerMapper.java
labex/src/main/java/labex/entity/StudentExamAnswer.java
labex/src/main/java/labex/service/ExamService.java
```

- [ ] **Step 3: Verify modified files**

```bash
git diff --name-only HEAD~12
```

Should include all modified entity, service, and controller files.

- [ ] **Step 4: Final commit if any fixes were needed**

```bash
git add -A && git commit -m "fix: resolve any compilation issues from implementation"
```
