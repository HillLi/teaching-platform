# 教学一体化平台功能完善设计文档

日期: 2026-04-21
来源: doc/实验修改点.docx

## 概述

基于修改建议文档，完善教学一体化平台的实验、作业、考试、批改和成绩汇总功能。共涉及 8 项修改，涵盖 5 个功能域。

## 方案选型

**选定方案 A：复用练习模式扩展考试**
- 新建 Exam/ExamItem/StudentExamAnswer 实体
- 沿用 Entity → Mapper → Service → Controller 代码模式
- 数据库新增 t_exam_item 和 t_student_exam_answer 表
- t_exam 表新增 start_time/end_time 列

## 设计详情

### 1. 实验模块改造

#### 1.1 填空题下划线机制

**教师端**：题干中用连续下划线 `_____`（>=3个）标记填空位。answer 字段按序号用 `|` 分隔填写正确答案。

示例题干：`数据库的三大范式是_____、_____和_____。`
参考答案：`第一范式|第二范式|第三范式`

**学生端**：前端解析题干，将 `_____` 替换为 `<input>` 输入框。学生答案按 `|` 拼接存入 `t_student_item.content`。

**自动评分**：提交时逐空比对（忽略首尾空格），按空位数平均分配满分。

**改动文件**：
- `StudentService.java` — answer 方法增加填空题自动评分
- `TeacherService.java` — 保存题目时验证填空位数量与答案数量一致性
- 前端 — 学生答题页解析下划线渲染输入框

#### 1.2 综合题文档上传

**教师端**：创建综合题(type=7)时可上传附件文档。
**学生端**：综合题支持上传文档作为答案。
**存储路径**：
- 教师附件：`{experiment-path}/{itemId}_attachment.{ext}`
- 学生答案：`{answers-path}/{studentId}_{itemId}.{ext}`

**改动文件**：
- `ExperimentItem` 实体增加 `filePath` 字段（ALTER TABLE 增加列）
- `TeacherController` 增加 `POST /experiments/items/{itemId}/attachment`
- `StudentItem` 实体增加 `filePath` 字段
- `StudentController` 增加 `POST /items/{itemId}/upload`
- `TeacherController` 增加 `GET /reports/items/{studentItemId}/download`

### 2. 作业模块改造

#### 2.1 改名

"题库管理" → "作业管理"，纯前端文案修改。

#### 2.2 题目编辑/删除

新增 API：
- `PUT /api/teacher/exercises/{exerciseId}/items/{itemId}` — 修改单个题目
- `DELETE /api/teacher/exercises/{exerciseId}/items/{itemId}` — 删除单个题目（级联删除 t_student_excercise 中该题的学生答案）

**改动文件**：
- `ExerciseController.java` — 新增两个端点
- `ExerciseService.java` — 新增 updateItem / deleteItem 方法
- `Ex3ItemMapper.java` — MyBatis-Plus BaseMapper 自带 updateById/deleteById

#### 2.3 作业批改

新增 API：
- `GET /api/teacher/exercises/{id}/submissions` — 查看某练习所有学生提交
- `GET /api/teacher/exercises/{id}/submissions/{studentId}` — 查看学生详细答案
- `POST /api/teacher/exercises/scores` — 提交批改分数（body: studentExerciseId, score）

客观题自动评分在学生提交时触发。

**改动文件**：
- `ExerciseController.java` — 新增批改相关端点
- `ExerciseService.java` — 新增 getSubmissions / getSubmissionDetail / submitScore 方法
- `StudentService.java` — exercise answer 方法增加客观题自动评分

#### 2.4 作业成绩汇总

新增 API：
- `GET /api/teacher/exercises/{id}/scores` — 班级成绩汇总
- `GET /api/student/exercises/{id}/score` — 学生查看自己成绩

**改动文件**：
- `ExerciseController.java` — 新增端点
- `ExerciseService.java` — 新增 getExerciseScores 方法
- `StudentController.java` — 新增端点

### 3. 考试模块（全新）

#### 3.1 数据库变更

**ALTER TABLE t_exam ADD**：
- `name VARCHAR(100)` — 考试名称
- `start_time DATETIME` — 开始时间
- `end_time DATETIME` — 结束时间
- `created_by INT` — 创建教师 ID

**CREATE TABLE t_exam_item**：
```sql
CREATE TABLE t_exam_item (
  id INT PRIMARY KEY AUTO_INCREMENT,
  exam_id INT NOT NULL,
  type TINYINT NOT NULL COMMENT '题目类型 1-7',
  content TEXT COMMENT '题干',
  options VARCHAR(500) COMMENT '选项(逗号分隔)',
  answer TEXT COMMENT '参考答案',
  score TINYINT NOT NULL DEFAULT 0 COMMENT '满分',
  FOREIGN KEY (exam_id) REFERENCES t_exam(id)
);
```

**CREATE TABLE t_student_exam_answer**：
```sql
CREATE TABLE t_student_exam_answer (
  id INT PRIMARY KEY AUTO_INCREMENT,
  exam_item_id INT NOT NULL,
  student_id INT NOT NULL,
  answer VARCHAR(200) COMMENT '短答案',
  content TEXT COMMENT '长答案',
  file_path VARCHAR(200) COMMENT '附件路径',
  score TINYINT COMMENT '得分',
  auto_scored TINYINT DEFAULT 0 COMMENT '1=已自动评分',
  submit_time DATETIME COMMENT '提交时间',
  UNIQUE KEY (exam_item_id, student_id),
  FOREIGN KEY (exam_item_id) REFERENCES t_exam_item(id),
  FOREIGN KEY (student_id) REFERENCES t_student(id)
);
```

**CREATE TABLE t_exam_submission**（记录学生整场考试提交状态）：
```sql
CREATE TABLE t_exam_submission (
  id INT PRIMARY KEY AUTO_INCREMENT,
  exam_id INT NOT NULL,
  student_id INT NOT NULL,
  start_time DATETIME COMMENT '开始作答时间',
  submit_time DATETIME COMMENT '提交时间',
  total_score DECIMAL(5,1) COMMENT '总成绩',
  status TINYINT DEFAULT 0 COMMENT '0=未提交 1=已提交 2=已批改',
  UNIQUE KEY (exam_id, student_id),
  FOREIGN KEY (exam_id) REFERENCES t_exam(id),
  FOREIGN KEY (student_id) REFERENCES t_student(id)
);
```

#### 3.2 新增 Java 文件

**Entity**：
- `labex/entity/Exam.java`
- `labex/entity/ExamItem.java`
- `labex/entity/StudentExamAnswer.java`
- `labex/entity/ExamSubmission.java`

**Mapper**：
- `labex/mapper/ExamMapper.java`
- `labex/mapper/ExamItemMapper.java`
- `labex/mapper/StudentExamAnswerMapper.java`
- `labex/mapper/ExamSubmissionMapper.java`

**Service**：
- `labex/service/ExamService.java`

**Controller**：
- `labex/controller/ExamController.java`（教师端 `/api/teacher/exams`）

**DTO**：
- `labex/dto/ExamSubmitDTO.java`（学生批量提交答案）

#### 3.3 教师 API（ExamController `/api/teacher/exams`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 列出所有考试 |
| POST | / | 创建考试 |
| PUT | /{id} | 修改考试 |
| DELETE | /{id} | 删除考试 |
| GET | /{id}/items | 列出题目 |
| POST | /{id}/items | 添加题目 |
| PUT | /items/{itemId} | 修改题目 |
| DELETE | /items/{itemId} | 删除题目 |
| GET | /{id}/submissions | 查看提交列表 |
| GET | /{id}/submissions/{studentId} | 查看学生详细答案 |
| POST | /scores | 批改主观题分数 |
| GET | /{id}/scores | 班级成绩汇总 |

#### 3.4 学生 API（StudentController 扩展）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/student/exams | 可参加的考试列表 |
| POST | /api/student/exams/{id}/start | 开始考试（记录 start_time） |
| GET | /api/student/exams/{id}/items | 获取题目 |
| POST | /api/student/exams/{id}/submit | 提交考试（批量答案） |
| GET | /api/student/exams/{id}/score | 查看成绩（需全部批改完） |

#### 3.5 时间限制逻辑

1. 学生调用 `start` 时，在 `t_exam_submission` 记录 `start_time`
2. 学生调用 `submit` 时：
   - 检查 `NOW() > end_time` → 拒绝提交
   - 检查 `NOW() > start_time + duration minutes` → 拒绝提交
   - 通过则保存所有答案，触发自动评分
3. 前端倒计时提示（纯展示，后端强制校验）

### 4. 统一批改体系

#### 4.1 自动评分规则

| 题型 | 自动评分 | 比对方式 |
|------|---------|---------|
| 填空题(1) | 是 | 逐空精确匹配（忽略首尾空格） |
| 单选题(2) | 是 | 精确匹配 |
| 多选题(3) | 是 | 排序后精确匹配 |
| 判断题(4) | 是 | 精确匹配 |
| 简答题(5) | 否 | 教师手动 |
| 编程题(6) | 否 | 教师手动 |
| 综合题(7) | 否 | 教师手动 |

自动评分分数 = 满分（答对）或 0（答错）。填空题按空位数量平均分配。

#### 4.2 各模块批改流程

**实验**：保持逐题评分，客观题在学生提交时自动写入分数和 score_flag=1。

**作业**：新增批改 API，教师查看提交列表 → 逐题评分。客观题自动评分。

**考试**：客观题自动评分（auto_scored=1），教师只需批改主观题。全部评完后 t_exam_submission.status=2，学生可见成绩。

### 5. 成绩汇总

| 模块 | 教师汇总 API | 学生查看 API |
|------|-------------|-------------|
| 实验 | GET /api/teacher/reports/classes/{clazzNo}/experiments/{expId}（已有） | GET /api/student/scores（已有） |
| 作业 | GET /api/teacher/exercises/{id}/scores（新增） | GET /api/student/exercises/{id}/score（新增） |
| 考试 | GET /api/teacher/exams/{id}/scores（新增） | GET /api/student/exams/{id}/score（新增） |

## 文件变更清单

### 新增文件（14个）
- `labex/src/main/java/labex/entity/Exam.java`
- `labex/src/main/java/labex/entity/ExamItem.java`
- `labex/src/main/java/labex/entity/StudentExamAnswer.java`
- `labex/src/main/java/labex/entity/ExamSubmission.java`
- `labex/src/main/java/labex/mapper/ExamMapper.java`
- `labex/src/main/java/labex/mapper/ExamItemMapper.java`
- `labex/src/main/java/labex/mapper/StudentExamAnswerMapper.java`
- `labex/src/main/java/labex/mapper/ExamSubmissionMapper.java`
- `labex/src/main/java/labex/service/ExamService.java`
- `labex/src/main/java/labex/controller/ExamController.java`
- `labex/src/main/java/labex/dto/ExamSubmitDTO.java`
- `labex/src/main/java/labex/dto/ExamSubmitItemDTO.java`
- `doc/exam_tables.sql` — 考试相关表 DDL
- `doc/exam_migration.sql` — t_exam 表结构变更

### 修改文件（~10个）
- `labex/src/main/java/labex/entity/ExperimentItem.java` — 增加 filePath 字段
- `labex/src/main/java/labex/entity/StudentItem.java` — 增加 filePath 字段
- `labex/src/main/java/labex/controller/ExerciseController.java` — 增加题目编辑/删除/批改端点
- `labex/src/main/java/labex/service/ExerciseService.java` — 增加批改/成绩/题目管理方法
- `labex/src/main/java/labex/controller/StudentController.java` — 增加考试/作业相关端点
- `labex/src/main/java/labex/service/StudentService.java` — 增加自动评分/考试/作业方法
- `labex/src/main/java/labex/controller/TeacherController.java` — 增加综合题附件端点
- `labex/src/main/java/labex/service/TeacherService.java` — 增加附件处理方法
- `labex/src/main/java/labex/mapper/ExerciseMapper.java` — 可能需要新增查询方法
