# LabEx Database Schema Analysis

**Database:** labex
**Server:** MySQL 8.0 (dumped from 8.0.24 on Win64, procedures from 8.0.18 on Linux)
**Dump date:** 2021-06-24
**Character set:** utf8mb3 (with utf8mb4 client connections)
**Engine:** InnoDB throughout

---

## 1. Complete Table List

The database contains **20 tables**, organized below by functional domain.

### 1.1 User/Identity Tables

#### `t_student` -- Student accounts
| Column | Type | Nullable | Notes |
|---|---|---|---|
| student_id | INT | NOT NULL, AUTO_INCREMENT | Primary key |
| student_no | VARCHAR(8) | NOT NULL | Unique student number (indexed) |
| student_name | VARCHAR(20) | NOT NULL | |
| student_password | CHAR(32) | NOT NULL | MD5 hash (32 hex chars) |
| clazz_no | VARCHAR(6) | NOT NULL | FK -> t_clazz.no |
| memo | TEXT | YES | Free-form notes |
| state | INT(10) UNSIGNED ZEROFILL | YES | Account state |
| error | INT(10) UNSIGNED ZEROFILL | YES | Login error count |
| ip | VARCHAR(20) | YES | Last known IP |

Auto-increment at 2226 (approx. 2225 students registered).

#### `t_teacher` -- Teacher/admin accounts
| Column | Type | Nullable | Notes |
|---|---|---|---|
| teacher_id | INT | NOT NULL, AUTO_INCREMENT | Primary key |
| teacher_account | VARCHAR(6) | NOT NULL | Login account |
| teacher_password | CHAR(32) | NOT NULL | MD5 hash |
| teacher_name | VARCHAR(20) | NOT NULL | Display name |

Auto-increment at 3 (2 teachers seeded).

#### `t_assistant` -- Teaching assistants
| Column | Type | Nullable | Notes |
|---|---|---|---|
| assistant_account | VARCHAR(255) | NOT NULL | Login account (no PK defined) |
| assistant_password | VARCHAR(255) | YES | |
| assistant_student_no | VARCHAR(8) | YES | Linked student number |
| assistant_student_name | VARCHAR(255) | YES | |
| assistant_student_clazz | VARCHAR(6) | YES | Linked class |

No primary key or indexes defined. Empty at dump time.

#### `t_clazz` -- Classes / class groups
| Column | Type | Nullable | Notes |
|---|---|---|---|
| no | VARCHAR(6) | NOT NULL | Primary key (class number) |
| memo | TEXT | YES | Instructor name or description |
| state | INT | YES, default 0 | Active/inactive flag |

Seeded data: 3 classes (182011, 182012, 992011).

---

### 1.2 Experiment System Tables

#### `t_experiment` -- Lab experiments (main)
| Column | Type | Nullable | Notes |
|---|---|---|---|
| experiment_id | INT | NOT NULL, AUTO_INCREMENT | Primary key |
| experiment_no | INT | NOT NULL | Sequence number (indexed) |
| experiment_name | VARCHAR(30) | NOT NULL | Title |
| experiment_type | INT | YES | Type classification |
| instruction_type | VARCHAR(10) | YES | Instruction category |
| experiment_requirement | TEXT | YES | Requirements description |
| experiment_content | TEXT | YES | Content body |
| state | INT | YES | Active/draft state |

Auto-increment at 37 (up to 36 experiments created). Indexed on `experiment_no`.

#### `t_experiment_item` -- Questions/tasks within an experiment
| Column | Type | Nullable | Notes |
|---|---|---|---|
| experiment_item_id | INT | NOT NULL, AUTO_INCREMENT | Primary key |
| experiment_item_no | INT | NOT NULL | Item sequence number (indexed) |
| experiment_item_name | VARCHAR(100) | NOT NULL | Item title |
| experiment_item_type | INT | NOT NULL | Item type (e.g., fill-in, code) |
| experiment_item_content | TEXT | YES | Full question/task text |
| experiment_id | INT | NOT NULL | FK -> t_experiment.experiment_id |
| experiment_item_answer | VARCHAR(255) | YES | Reference answer |
| experiment_item_score | TINYINT | YES | Points allocated |
| state | INT | YES | Active state |

Auto-increment at 143. FK constraint to `t_experiment`. Indexed on `experiment_item_no` and `experiment_id`.

---

### 1.3 Student Submission Tables

#### `t_student_item` -- Student answers to experiment items
| Column | Type | Nullable | Notes |
|---|---|---|---|
| student_item_id | INT | NOT NULL, AUTO_INCREMENT | Primary key |
| student_id | INT | NOT NULL | FK -> t_student.student_id |
| item_id | INT | NOT NULL | FK -> t_experiment_item.experiment_item_id |
| content | TEXT | NOT NULL | Student's submitted answer |
| score | TINYINT | YES | Graded score |
| fill_time | DATETIME | NOT NULL | Submission timestamp |
| score_flag | INT | YES | Grading status flag |

Auto-increment at 25112. Unique constraint on `(student_id, item_id)` -- one submission per student per item. Two FK constraints.

#### `t_student_item_log` -- Audit log for student item submissions
| Column | Type | Nullable | Notes |
|---|---|---|---|
| log_id | BIGINT | NOT NULL, AUTO_INCREMENT | Primary key |
| student_item | INT | YES | FK -> t_student_item.student_item_id |
| content | TEXT | YES | Content snapshot |
| fill_time | DATETIME | YES | Log timestamp |

Auto-increment at 136670. Composite index on `(student_item, fill_time)`. This is the largest table by auto-increment counter, tracking every edit/update to student submissions.

#### `t_student_answer` -- Normalized answer data for analysis
| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | BIGINT | NOT NULL, AUTO_INCREMENT | Primary key |
| item_id | INT | YES | FK -> t_experiment_item.experiment_item_id |
| fill_no | INT | YES | Fill-in slot number |
| content | VARCHAR(255) | YES, utf8_bin collation | Answer text |
| content_hash | CHAR(32) | YES | MD5 hash of content |
| count | INT | YES | Frequency count |
| is_correct | BIT(1) | YES | Correctness flag |

Auto-increment at 11811. Unique constraint on `(item_id, fill_no, content_hash)` for deduplication. Used for answer statistics and auto-grading.

---

### 1.4 Score Aggregation Table

#### `t_score` -- Experiment-level scores per student
| Column | Type | Nullable | Notes |
|---|---|---|---|
| score_id | INT | NOT NULL, AUTO_INCREMENT | Primary key |
| student_id | INT | YES | FK -> t_student.student_id |
| experiment_id | INT | YES | FK -> t_experiment.experiment_id |
| score | INT | YES | Total score for experiment |

Two FK constraints (student, experiment). Empty at dump time (scores likely computed on-the-fly via views/procedures).

---

### 1.5 Exercise/Practice System (Secondary)

#### `t_ex3` -- Practice exercises (main)
| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | INT | NOT NULL, AUTO_INCREMENT | Primary key |
| no | INT | YES | Exercise number |
| name | VARCHAR(255) | YES | Exercise title |
| extype | INT | YES | Exercise type |
| type | INT | YES | Question type |
| description | VARCHAR(255) | YES | Description |
| begin_time | DATETIME | YES | Start time |
| end_time | DATETIME | YES | End time |

Auto-increment at 2 (1 exercise seeded: "HTML练习").

#### `t_ex3_item` -- Items within practice exercises
| Column | Type | Nullable | Notes |
|---|---|---|---|
| excercise_item_id | INT | NOT NULL, AUTO_INCREMENT | Primary key |
| excercise_id | INT | NOT NULL | FK -> t_ex3.id (implicit, indexed) |
| question | VARCHAR(255) | YES | Question text |
| options | VARCHAR(255) | YES | Answer options |
| answer | VARCHAR(255) | YES | Correct answer |
| type | INT | YES | 1=single-choice, 2=multi-choice |

Auto-increment at 3. Index on `excercise_id`.

#### `t_student_excercise` -- Student answers to practice exercises
| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | BIGINT | NOT NULL, AUTO_INCREMENT | Primary key |
| item_id | INT | YES | FK -> t_ex3_item.excercise_item_id |
| student_id | INT | YES | FK -> t_student.student_id |
| answer | VARCHAR(30) | YES | Short answer (choice/fill-in) |
| content | TEXT | YES | Long answer (essay/code) |
| score | INT | YES | Graded score |
| fill_time | DATETIME | YES | Submission timestamp |

Two FK constraints. The `answer` vs `content` split mirrors the type-based logic in the `answerQuestion` procedure.

---

### 1.6 Exam System (Appears Incomplete/Unused)

#### `t_exam` -- Examinations
| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | INT | NOT NULL | Primary key |
| description | VARCHAR(30) | NOT NULL | Exam title |
| duration | INT | YES | Duration in minutes |
| time | DATETIME | YES | Scheduled time |
| flag | BIT(1) | YES | Exam open/close flag |

Empty at dump time. No FK relationships.

#### `t_paper` -- Exam papers
| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | INT | NOT NULL | Primary key |
| no | INT | NOT NULL | Paper number |
| name | VARCHAR(255) | YES | Paper title |
| description | VARCHAR(255) | YES | |
| time | DATETIME | YES | |

Empty at dump time. Composite PK with `id` and `no`.

#### `t_paper_question` -- Questions in exam papers
| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | INT | NOT NULL | Primary key |
| paper_id | INT | YES | Logical FK -> t_paper.id |
| question_id | INT | YES | Logical FK -> t_question.id |
| score | INT | YES | Points for this question |

Empty at dump time. No enforced FK constraints.

#### `t_question` -- Question bank
| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | INT | NOT NULL | Primary key |
| question | VARCHAR(255) | YES | Question text |
| answer | VARCHAR(255) | YES | Correct answer |
| type | INT | YES | Question type |

Empty at dump time. Referenced by `t_student_question` and `t_paper_question`.

#### `t_question_type` -- Question type lookup
| Column | Type | Nullable | Notes |
|---|---|---|---|
| type_id | INT | NOT NULL | Primary key |
| type_name | VARCHAR(10) | YES | Type label |

Seeded data (7 types): 1=填空 (fill-in), 2=单选 (single-choice), 3=多选 (multi-choice), 4=判断 (true/false), 5=简答 (short-answer), 6=编程 (programming), 7=综合 (comprehensive).

#### `t_student_question` -- Student answers to question bank items
| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | INT | NOT NULL, AUTO_INCREMENT | Primary key |
| student_id | INT | YES | FK -> t_student.student_id |
| question_id | INT | YES | FK -> t_question.id |
| answer | VARCHAR(255) | YES | Student's answer |

Unique constraint on `(student_id, question_id)`. Two FK constraints. Empty at dump time.

---

### 1.7 Course Material Table

#### `t_lecture` -- Lecture/course materials
| Column | Type | Nullable | Notes |
|---|---|---|---|
| lecture_id | INT | NOT NULL, AUTO_INCREMENT | Primary key |
| lecture_name | VARCHAR(50) | YES | Title |
| lecture_type | INT | YES | Material type |
| lecture_filetype | VARCHAR(10) | YES | File extension |

Empty at dump time.

---

### 1.8 System Tables

#### `t_sys_config` -- System configuration key-value store
| Column | Type | Nullable | Notes |
|---|---|---|---|
| param | VARCHAR(20) | NOT NULL | Primary key (config key) |
| value | VARCHAR(100) | YES | Config value |

Empty at dump time.

#### `t_sys_log` -- System/admin activity log
| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | INT | NOT NULL, AUTO_INCREMENT | Primary key |
| account | VARCHAR(20) | YES | Login account |
| type | INT | YES | Log type (1=login success, 2=login failure) |
| info | VARCHAR(255) | YES | Description |
| time | DATETIME | YES | Timestamp |
| ip | VARCHAR(100) | YES | Client IP |

Auto-increment at 231223. Indexed on `time`. The largest counter after `t_student_item_log`.

#### `t_student_log` -- Student activity log
| Column | Type | Nullable | Notes |
|---|---|---|---|
| id | INT | NOT NULL, AUTO_INCREMENT | Primary key |
| account | VARCHAR(20) | YES | Student account |
| type | INT | YES | Log type |
| info | VARCHAR(255) | YES | Description |
| time | DATETIME | YES | Timestamp |
| ip | VARCHAR(20) | YES | Client IP |

Auto-increment at 352452. Indexed on `time`. The single largest auto-increment counter in the database.

---

## 2. Relationships

### 2.1 Declared Foreign Keys (8 constraints)

| Constraint Name | Child Table | Child Column | Parent Table | Parent Column |
|---|---|---|---|---|
| fk_clazz_no | t_student | clazz_no | t_clazz | no |
| fk_experiment_id | t_experiment_item | experiment_id | t_experiment | experiment_id |
| fk_score_student | t_score | student_id | t_student | student_id |
| fk_score_experiment | t_score | experiment_id | t_experiment | experiment_id |
| fk_student_id | t_student_item | student_id | t_student | student_id |
| fk_item_id | t_student_item | item_id | t_experiment_item | experiment_item_id |
| fk_student_item | t_student_item_log | student_item | t_student_item | student_item_id |
| fk_answer_item_id | t_student_answer | item_id | t_experiment_item | experiment_item_id |
| fk_excercise_item_id | t_student_excercise | item_id | t_ex3_item | excercise_item_id |
| fk_student_excercise_id | t_student_excercise | student_id | t_student | student_id |
| fk_student | t_student_question | student_id | t_student | student_id |
| fk_question | t_student_question | question_id | t_question | id |

### 2.2 Logical (Unenforced) Relationships

| From | To | Join Condition | Context |
|---|---|---|---|
| t_ex3_item.excercise_id | t_ex3.id | Indexed but no FK | Exercise items belong to exercises |
| t_paper_question.paper_id | t_paper.id | No FK | Questions in exam papers |
| t_paper_question.question_id | t_question.id | No FK | Paper references question bank |
| t_assistant.assistant_student_no | t_student.student_no | No FK | TA linked to student record |
| t_assistant.assistant_student_clazz | t_clazz.no | No FK | TA linked to class |

---

## 3. Views (9 total)

### 3.1 `v_student_experiment_score`
**Purpose:** Maps individual item scores to experiments with experiment metadata.
**Query:**
```sql
SELECT
    t_student_item.score,
    t_student_item.student_id,
    t_experiment.experiment_id,
    t_experiment.experiment_no,
    t_experiment.experiment_name,
    t_experiment.experiment_type,
    t_experiment.instruction_type
FROM t_experiment
LEFT JOIN (t_student_item JOIN t_experiment_item
           ON t_student_item.item_id = t_experiment_item.experiment_item_id)
    ON t_experiment_item.experiment_id = t_experiment.experiment_id
```
**Columns:** score, student_id, experiment_id, experiment_no, experiment_name, experiment_type, instruction_type

### 3.2 `v_clazz_experiments_score`
**Purpose:** Aggregates experiment scores per student with student info, built on top of `v_student_experiment_score`.
**Query:**
```sql
SELECT
    v.student_id,
    (SELECT student_no FROM t_student WHERE student_id = v.student_id) AS student_no,
    (SELECT student_name FROM t_student WHERE student_id = v.student_id) AS student_name,
    (SELECT clazz_no FROM t_student WHERE student_id = v.student_id) AS clazz_no,
    v.experiment_id,
    v.experiment_no,
    SUM(v.score) AS score
FROM v_student_experiment_score v
GROUP BY v.student_id, v.experiment_id
```
**Columns:** student_id, student_no, student_name, clazz_no, experiment_id, experiment_no, score
**Note:** Uses correlated subqueries instead of JOINs -- potential performance concern.

### 3.3 `v_student_expeirment_items` (note: typo in name)
**Purpose:** Joins experiment item definitions with student submissions.
**Query:**
```sql
SELECT
    t_experiment_item.experiment_item_no AS itemNo,
    t_experiment_item.experiment_item_name AS itemName,
    t_experiment_item.experiment_item_type AS itemType,
    t_student_item.score AS score,
    t_student_item.fill_time AS fillTime,
    t_student_item.student_id AS studentId,
    t_experiment_item.experiment_item_id AS itemId,
    t_experiment_item.experiment_id AS experimentId
FROM t_experiment_item
JOIN t_student_item ON t_student_item.item_id = t_experiment_item.experiment_item_id
```

### 3.4 `v_studentexperimentitems`
**Purpose:** Identical to `v_student_expeirment_items` -- an exact duplicate (likely a renamed/corrected version).
**Query:** Same as 3.3 above.

### 3.5 `v_student_info`
**Purpose:** Dashboard statistics for student data.
**Query:**
```sql
SELECT
    MAX(student_id) AS maxId,
    MAX(ip) AS lastAccess,
    COUNT(student_id) AS count
FROM t_student
```
**Columns:** maxId, lastAccess, count
**Note:** `MAX(ip)` is used as a proxy for "last access" since there is no proper last-login timestamp.

### 3.6 `v_clazz_info`
**Purpose:** Dashboard statistics for class data.
**Query:**
```sql
SELECT MAX(no) AS maxId, '----' AS lastAccess, COUNT(no) AS count FROM t_clazz
```
**Columns:** maxId, lastAccess, count

### 3.7 `v_student_answer_data_info`
**Purpose:** Dashboard statistics for student answer submissions.
**Query:**
```sql
SELECT
    MAX(student_item_id) AS maxId,
    MAX(fill_time) AS lastAccess,
    COUNT(student_item_id) AS count
FROM t_student_item
```
**Columns:** maxId, lastAccess, count

### 3.8 `v_student_answer_log_info`
**Purpose:** Dashboard statistics for answer edit logs.
**Query:**
```sql
SELECT
    MAX(log_id) AS maxId,
    MAX(fill_time) AS lastAccess,
    COUNT(log_id) AS count
FROM t_student_item_log
```
**Columns:** maxId, lastAccess, count

### 3.9 `v_sys_log_info`
**Purpose:** Dashboard statistics for system logs.
**Query:**
```sql
SELECT MAX(id) AS maxId, MAX(time) AS lastAccess, COUNT(id) AS count FROM t_sys_log
```
**Columns:** maxId, lastAccess, count

### View Summary Pattern
Views 3.5 through 3.9 all share the same structure (maxId, lastAccess, count) and serve as lightweight dashboard/reporting monitors for their respective tables.

---

## 4. Stored Procedures (5 total)

### 4.1 `answerQuestion`
**Parameters:** `studentId INT`, `itemId INT`, `type INT`, `studentAnswer VARCHAR(500)`
**Purpose:** Submit or update a student's answer to a practice exercise item. Implements upsert logic.

**Logic:**
1. Check if a record exists in `t_student_excercise` for this student + item.
2. If no record exists (INSERT):
   - type 1 or 2 (choice questions): store answer in the `answer` column.
   - Other types: store answer in the `content` column.
3. If a record exists (UPDATE):
   - Same type-based column logic, updating the answer and timestamp.

**Business rule:** The `answer` column (VARCHAR(30)) stores short answers for choice/fill-in questions, while the `content` column (TEXT) stores longer answers for essay/code questions. The `fill_time` is always set to `now()`.

### 4.2 `p_clazz_experiment_answers`
**Parameters:** `expid INT`, `cno VARCHAR(6)`
**Purpose:** Retrieve all student answers for a specific experiment within a specific class.

**Logic:**
```sql
SELECT student_item_id, item_id, b.experiment_item_score, a.content
FROM t_student_item a, t_experiment_item b, t_student c
WHERE a.item_id = b.experiment_item_id
  AND a.student_id = c.student_id
  AND b.experiment_id = expid
  AND c.clazz_no = cno
```
Returns: submission IDs, item IDs, max possible scores, and student answer content.

### 4.3 `p_clazz_experiment_score`
**Parameters:** `cno VARCHAR(6)`, `expid INT`
**Purpose:** Calculate per-student total scores for a specific experiment within a class.

**Logic:**
```sql
SELECT student_id, student_no, student_name, clazz_no, memo,
    (SELECT SUM(score) FROM t_student_item, t_experiment_item
     WHERE t_student_item.item_id = t_experiment_item.experiment_item_id
       AND t_experiment_item.experiment_id = expid
       AND t_student_item.student_id = t_student.student_id) AS score
FROM t_student
WHERE clazz_no = cno
ORDER BY student_no
```
Uses a correlated subquery to sum item scores. Returns all students in the class even if they have no submissions (score will be NULL).

### 4.4 `p_student_experiment_item_score`
**Parameters:** `sid INT`, `expid INT`
**Purpose:** Get detailed item-level results for a specific student on a specific experiment.

**Logic:** For each item in the experiment, returns:
- Item definition (id, no, name, type, content, max score, state)
- Student's submission (via correlated subqueries): student_item_id, student_answer (content), score
- Ordered by item number

Three correlated subqueries (one per student data field) against `t_student_item`. Returns items even if the student has not answered them (NULL values).

### 4.5 `p_student_experiment_score`
**Parameters:** `sid INT`
**Purpose:** Get per-experiment total scores for a specific student across ALL experiments.

**Logic:**
```sql
SELECT
    experiment_id, experiment_no, experiment_name, experiment_type, instruction_type,
    (SELECT SUM(score) FROM t_student_item, t_experiment_item
     WHERE t_student_item.item_id = t_experiment_item.experiment_item_id
       AND t_experiment_item.experiment_id = t_experiment.experiment_id
       AND student_id = sid) AS score,
    state
FROM t_experiment
ORDER BY experiment_no
```
Returns all experiments with the student's total score (NULL if unanswered). Uses a correlated subquery for score aggregation.

---

## 5. ER Diagram (Text-Based)

```
+------------------+          +------------------+
|    t_teacher     |          |   t_assistant    |
|------------------|          |------------------|
| PK: teacher_id   |          | (no PK defined)  |
| teacher_account  |          | assistant_account|
| teacher_password |          | assistant_passwd |
| teacher_name     |          | asst_student_no  |
+------------------+          | asst_student_name|
                              | asst_student_clazz|
                              +------------------+

+----------+       +---------------------------+
| t_clazz  |1     N|       t_student           |
|----------|-------|---------------------------|
| PK: no   |       | PK: student_id (AI:2226)  |
| memo     |       | student_no (UNIQUE)       |
| state    |       | student_name              |
+----------+       | student_password (MD5)    |
                    | FK: clazz_no -> t_clazz   |
                    | memo, state, error, ip    |
                    +---------------------------+
                          |              |
                  +-------+              +-------+
                  |                              |
         +----------------+            +--------------------+
         | t_student_item |            | t_student_excercise|
         |----------------|            |--------------------|
         |PK:student_item |            | PK: id (BIGINT)    |
         |    _id(AI:25112|            | FK: item_id->t_ex3 |
         | FK: student_id |            |      _item         |
         | FK: item_id    |            | FK: student_id     |
         | content        |            | answer             |
         | score          |            | content            |
         | fill_time      |            | score              |
         | score_flag     |            | fill_time          |
         +--------+-------+            +--------------------+
                  |                              |
       +----------+----------+          +--------+--------+
       |                     |          |                 |
+------+------+    +---------+---+  +--+--------+  +-----+------+
|t_student    |    |t_student_    |  |t_ex3_item |  |t_ex3       |
|_item_log    |    |answer        |  |-----------|  |------------|
|-------------|    |--------------|  |PK:excercis|  |PK: id(AI:2)|
|PK:log_id    |    |PK: id(BIGINT)|  |  e_item_id|  | no, name   |
|  (AI:136670)|    |  (AI:11811)  |  |FK:excercis|  | extype     |
|FK:student_  |    |FK: item_id   |  |  e_id     |  | type, desc |
|  item       |    | fill_no      |  | question  |  | begin/end  |
| content     |    | content      |  | options   |  | _time      |
| fill_time   |    | content_hash |  | answer    |  +------------+
+--------------+    | count        |  | type      |
                    | is_correct   |  +-----------+
                    +--------------+
                          |
                    +-----+------------+
                    |                  |
             +------+------+   +-------+------+
             |t_experiment  |   |t_experiment  |
             |              |   |_item         |
             |--------------|   |--------------|
             |PK:experiment |   |PK:experiment |
             |  _id(AI:37)  |   |  _item_id    |
             | experiment_no|   |  (AI:143)    |
             | experiment_  |   | experiment_  |
             |  name        |   |  item_no     |
             | experiment_  |   | experiment_  |
             |  type        |   |  item_name   |
             | instruction_ |   | experiment_  |
             |  type        |   |  item_type   |
             | experiment_  |   | experiment_  |
             |  requirement |   |  item_content|
             | experiment_  |   | FK:experiment|
             |  content     |   |  _id --------+---> t_experiment
             | state        |   | experiment_  |
             +--------------+   |  item_answer |
                    |           | experiment_  |
                    |           |  item_score  |
              +-----+-----+    | state        |
              | t_score   |    +--------------+
              |-----------|
              |PK:score_id|
              |FK:student |
              |  _id      +----> t_student
              |FK:experiment
              |  _id      +----> t_experiment
              | score     |
              +-----------+

  === Exam System (unused/incomplete) ===

  +----------+     +----------------+     +-----------+
  | t_exam   |     | t_paper        |     |t_question |
  |----------|     |----------------|     |-----------|
  |PK: id    |     |PK: (id, no)    |     |PK: id     |
  |descript. |     | name           |     | question  |
  | duration |     | description    |     | answer    |
  | time     |     | time           |     | type      |
  | flag     |     +-------+--------+     +--+--------+
  +----------+             |                 |       |
                    +------+------+    +------+------+
                    |t_paper_     |    |t_student_    |
                    |question     |    |question      |
                    |-------------|    |--------------|
                    |PK: id       |    |PK: id (AI)   |
                    | paper_id    |    |FK: student_id|
                    | question_id |    |FK: question_ |
                    | score       |    |  id          |
                    +-------------+    | answer       |
                                       +--------------+

  === System/Lookup ===

  +--------------+  +-------------+  +------------+  +-----------+
  | t_question_  |  | t_sys_config|  | t_sys_log  |  |t_student  |
  | type         |  |-------------|  |------------|  |_log       |
  |--------------|  |PK: param    |  |PK: id      |  |-----------|
  |PK: type_id   |  | value       |  | account    |  |PK: id(AI:|
  | type_name    |  +-------------+  | type       |  |  352452)  |
  +--------------+                   | info       |  | account   |
                                     | time       |  | type, info|
                                     | ip         |  | time, ip  |
                                     +------------+  +-----------+

  +------------+
  | t_lecture  |
  |------------|
  |PK:lecture_id(AI)|
  | lecture_name|
  | lecture_type|
  |lecture_filetype|
  +------------+
```

---

## 6. Data Volume Considerations

Based on auto-increment counters (which approximate row counts):

| Table | Auto-Increment | Estimated Rows | Growth Pattern |
|---|---|---|---|
| t_student_log | 352,452 | ~352K | High -- every student action logged |
| t_student_item_log | 136,670 | ~137K | High -- every edit tracked |
| t_sys_log | 231,223 | ~231K | Moderate -- admin login events |
| t_student_item | 25,112 | ~25K | Moderate -- 1 per student per item |
| t_student_answer | 11,811 | ~12K | Moderate -- deduplication applies |
| t_student | 2,226 | ~2.2K | Low -- grows with enrollment |
| t_experiment_item | 143 | ~142 | Very low -- instructor-created |
| t_experiment | 37 | ~36 | Very low -- instructor-created |

**Key observations:**
- The log tables (`t_student_log`, `t_student_item_log`, `t_sys_log`) will be the largest and fastest-growing tables. They lack any archiving or partitioning strategy.
- `t_student_item` grows as O(students x experiment_items). With ~2225 students and ~142 items, the 25K rows is consistent (about 8% completion rate, or some items/experiments not yet active).
- `t_score` is empty despite having FK constraints. Scores appear to be computed dynamically through views and stored procedures rather than persisted.
- The exam-related tables (`t_exam`, `t_paper`, `t_paper_question`, `t_question`, `t_student_question`) are all empty, suggesting this subsystem was either not yet deployed or has been superseded.

---

## 7. Data Integrity Constraints and Notable Patterns

### 7.1 Primary Keys
All tables except `t_assistant` have a defined primary key. `t_assistant` has no PK, no indexes, and no constraints -- this is a schema deficiency.

### 7.2 Unique Constraints
- `t_student.student_no` -- ensures unique student numbers
- `t_student_item(student_id, item_id)` -- one submission per student per experiment item
- `t_student_question(student_id, question_id)` -- one answer per student per question
- `t_student_answer(item_id, fill_no, content_hash)` -- deduplicates answer submissions using MD5 hash

### 7.3 Indexes (Performance)
- `idx_experiment_no` on `t_experiment.experiment_no` -- lookup by experiment number
- `idx_experiment_item_no` on `t_experiment_item.experiment_item_no` -- ordering/lookup
- `idx_student_no` on `t_student.student_no` -- login lookup
- `idx_student_item` on `t_student_item(student_id, item_id)` -- also unique constraint
- `idx_time` on `t_sys_log.time` and `t_student_log.time` -- time-range queries
- `idx_answer_pk` on `t_student_answer(item_id, fill_no, content_hash)` -- also unique constraint
- `fk_student_item` on `t_student_item_log(student_item, fill_time)` -- composite for log lookups

### 7.4 Notable Patterns and Concerns

**Password Storage:** All passwords are stored as CHAR(32) MD5 hashes with no salt. This is cryptographically weak by modern standards.

**Duplicated View:** `v_student_expeirment_items` and `v_studentexperimentitems` are identical views. The first has a typo ("expeirment" instead of "experiment"). The second is likely the corrected version, but the old one was never dropped.

**Correlated Subqueries in Views:** `v_clazz_experiments_score` uses three correlated subqueries to fetch student_no, student_name, and clazz_no individually rather than joining `t_student` once. This is inefficient and will degrade as data grows.

**No CASCADE Deletes:** None of the FK constraints specify ON DELETE or ON UPDATE behavior, meaning MySQL defaults to RESTRICT. Deleting a student will fail if they have any submissions.

**Score Duplication:** Scores exist in `t_student_item.score` (item-level) and `t_score.score` (experiment-level), but `t_score` is empty. Experiment scores are computed dynamically via `SUM()` in views and procedures. This creates a design inconsistency -- `t_score` appears to be an abandoned approach.

**Implicit Relationships:** The exercise subsystem (`t_ex3`, `t_ex3_item`) and exam subsystem (`t_exam`, `t_paper`, `t_paper_question`, `t_question`) rely on logical rather than enforced FK constraints. This risks orphaned records.

**Type-Based Column Selection:** The `answerQuestion` procedure uses the `type` parameter to decide whether to write to the `answer` or `content` column of `t_student_excercise`. This type discrimination is not documented in the schema itself and must be maintained in application code.

**Content Hash for Deduplication:** `t_student_answer` uses `content_hash` (MD5 of answer text) combined with `item_id` and `fill_no` as a unique constraint. This is an analytics-oriented design that tracks unique answer patterns across all students.

**Charset:** All tables use `utf8mb3` (3-byte UTF-8 in MySQL), which cannot store 4-byte characters like emojis or rare CJK characters. The client connections use `utf8mb4` but the storage is `utf8mb3`.

**t_student_answer.content Collation:** Uses `utf8_bin` (case-sensitive) while all other text columns use the database default collation. This ensures answer matching is case-sensitive.

**Missing Constraints:**
- `t_assistant` has no PK or indexes at all
- `t_paper` has a composite PK on `(id, no)` but `no` alone might be intended as unique
- `t_exam.id` is the PK but `t_paper.id` is also part of PK -- the relationship between exam and paper is not modeled
- No NOT NULL constraints on many critical columns (e.g., experiment_item_score, question text)
