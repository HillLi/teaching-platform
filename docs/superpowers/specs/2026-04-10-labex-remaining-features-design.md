# Labex 剩余功能开发设计文档

**日期**: 2026-04-10
**范围**: 补齐 Labex 教学一体化平台的剩余功能缺口
**前提**: 核心功能（登录、教师CRUD、学生答题、评分、讲义）已实现并测试通过

---

## 1. 现状分析

### 已完成功能

- 登录/登出 + MD5→BCrypt 密码迁移
- 教师：班级CRUD、学生CRUD+JSON导入、实验CRUD、实验项目CRUD、评分、成绩报告、课件管理、练习管理
- 学生：实验浏览、答题+自动保存、成绩查看、课件下载、修改密码
- 题库后端API（QuestionController CRUD 完整）
- 练习后端API（ExerciseController CRUD + answerQuestion 完整）

### 缺口清单

| # | 缺口 | 影响范围 | 子项目 |
|---|------|---------|--------|
| 1 | AnswerView 答案加载 bug | 学生答题 | SP1 |
| 2 | 缺少富文本编辑器 | 实验内容、题目、答题 | SP1 |
| 3 | 缺少代码编辑器 | 编程类题目 | SP1 |
| 4 | 缺少统一文件上传组件 | 课件、CSV导入 | SP1 |
| 5 | 题库管理前端页面 | 教师端 | SP2 |
| 6 | 学生练习答题前端 | 学生端 | SP2 |
| 7 | 学生练习API权限问题 | 后端 | SP2 |
| 8 | 教师仪表盘只有3个数字 | 教师端 | SP3 |
| 9 | 5个数据库视图从未查询 | 后端 | SP3 |
| 10 | 学生活动日志从未写入 | 后端 | SP4 |
| 11 | 系统日志无查看页面 | 教师端 | SP4 |
| 12 | 教师无法重置学生密码 | 教师端 | SP5 |
| 13 | 学生导入是JSON非CSV | 教师端 | SP5 |
| 14 | SysConfig 无 mapper | 后端 | SP5 |

---

## 2. 子项目1：Bug修复 + 编辑器基础设施

### 2.1 AnswerView 答案加载修复

**问题**: `AnswerView.vue` 进入时调用 `api.saveAnswer(itemId, '')` 创建空记录，而非加载已有答案。

**修复方案**:

后端改动 — 修改 `GET /api/student/items/{itemId}`，返回完整的 ExperimentItem 信息和 StudentItem 已有答案：

```java
// StudentController.getItem() 改为：
@GetMapping("/items/{itemId}")
public Result<Map<String, Object>> getItem(@PathVariable Integer itemId, HttpSession session) {
    UserTokenVO token = verifyStudent(session);
    ExperimentItem item = studentService.getExperimentItemById(itemId);
    StudentItem si = studentService.getStudentItem(itemId, token.getUserId());
    return Result.ok(Map.of("item", item, "studentItem", si));
}
```

StudentService 需新增 `getExperimentItemById(Integer itemId)` 方法。

前端改动 — AnswerView.vue onMounted 改为：

```javascript
onMounted(async () => {
  const res = await api.getItem(itemId)
  item.value = res.data.item
  if (res.data.studentItem && res.data.studentItem.content) {
    content.value = res.data.studentItem.content
  }
  // auto-save timer unchanged
})
```

student.js API 新增：
```javascript
getItem(itemId) { return request.get(`/api/student/items/${itemId}`) }
```

### 2.2 RichTextEditor 组件

**技术选型**: wangEditor v5（轻量、中文友好、无需 API key）

**安装**: `npm install @wangeditor/editor @wangeditor/editor-for-vue`

**组件位置**: `labex-ui/src/components/RichTextEditor.vue`

**接口**:
- Props: `modelValue` (string), `placeholder` (string), `height` (number, default 300)
- Emits: `update:modelValue`
- 内部使用 wangEditor 的 Vue 3 封装 `@wangeditor/editor-for-vue`
- 工具栏配置：标题、加粗、斜体、列表、代码块、图片、表格、链接
- 模式：default（完整工具栏）

### 2.3 CodeEditor 组件

**技术选型**: Monaco Editor（通过 @monaco-editor/vue）

**安装**: `npm install @monaco-editor/vue`

**组件位置**: `labex-ui/src/components/CodeEditor.vue`

**接口**:
- Props: `modelValue` (string), `language` (string, default 'java'), `height` (number, default 400)
- Emits: `update:modelValue`
- 主题：vs-dark
- 自动布局

### 2.4 FileUpload 组件

**组件位置**: `labex-ui/src/components/FileUpload.vue`

**接口**:
- Props: `action` (string, 上传URL), `accept` (string, 文件类型限制), `limit` (number, 默认1), `autoUpload` (boolean, 默认true)
- Emits: `success(file)`, `error(msg)`
- 基于 el-upload 封装
- 支持拖拽上传
- 显示上传进度和文件列表

### 2.5 编辑器集成到答题页

AnswerView 根据 ExperimentItem 的 `experimentItemType`（Integer）字段决定使用哪个编辑器：
- `experimentItemType` 的值对应 t_question_type 表中的类型，需根据实际数据判断哪些是编程类
- 前端判断逻辑：`const isCodeType = item.experimentItemType === CODE_TYPE_ID`（具体 ID 值从 t_question_type 表确认）
- 默认使用 RichTextEditor，编程类使用 CodeEditor

替换当前 `<el-input type="textarea">` 为条件渲染：

```html
<RichTextEditor v-if="!isCodeType" v-model="content" :height="400" />
<CodeEditor v-else v-model="content" language="java" :height="400" />
```

### 2.6 后端文件

**修改**:
- `labex/src/main/java/labex/controller/StudentController.java` — 修改 getItem 返回完整数据
- `labex/src/main/java/labex/service/StudentService.java` — 新增 getExperimentItemById 方法
- `labex-ui/src/api/student.js` — 新增 getItem 方法
- `labex-ui/src/views/student/AnswerView.vue` — 修复加载逻辑，集成编辑器

**新建**:
- `labex-ui/src/components/RichTextEditor.vue`
- `labex-ui/src/components/CodeEditor.vue`
- `labex-ui/src/components/FileUpload.vue`

---

## 3. 子项目2：题库管理 + 学生练习

### 3.1 题库管理前端

**路由**: `/teacher/questions`

**新增文件**: `labex-ui/src/views/teacher/QuestionManageView.vue`

**页面功能**:
- 题目列表表格：题目ID、内容摘要、题型名称、分值、操作
- 按题型筛选下拉框（从 `/api/questions/types` 获取）
- 新增按钮 → 弹窗表单：
  - 题目内容（RichTextEditor）
  - 题型（下拉选择 QuestionType）
  - 选项（选择题时动态显示 A/B/C/D 四个输入框）
  - 正确答案
  - 分值
- 编辑按钮 → 同新增弹窗
- 删除按钮 → 确认对话框

**路由注册**: router/index.js teacher children 新增：
```javascript
{ path: 'questions', name: 'QuestionManage', component: () => import('../views/teacher/QuestionManageView.vue') }
```

**教师布局侧边栏新增**: 题库管理菜单项

**API**: 复用已有 `labex-ui/src/api/question.js`

### 3.2 学生练习功能

**后端改动**:

新增学生端练习 API（从 ExerciseController 拆出，放到 StudentController 或新建 StudentExerciseController）：

```java
// StudentController 新增：
@GetMapping("/exercises")
public Result<List<Ex3>> listExercises(HttpSession session) {
    verifyStudent(session);
    return Result.ok(exerciseService.listExercises());
}

@GetMapping("/exercises/{id}/items")
public Result<List<Ex3Item>> getExerciseItems(@PathVariable Integer id, HttpSession session) {
    verifyStudent(session);
    return Result.ok(exerciseService.getExerciseItems(id));
}

@PostMapping("/exercises/answer")
public Result<Void> answerExercise(@RequestBody Map<String, Object> body, HttpSession session) {
    UserTokenVO token = verifyStudent(session);
    Integer itemId = (Integer) body.get("itemId");
    Integer type = (Integer) body.get("type");
    String answer = (String) body.get("answer");
    exerciseService.answerQuestion(token.getUserId(), itemId, type, answer);
    return Result.ok();
}
```

**前端新增**:

路由（student children）：
```javascript
{ path: 'exercises', name: 'StudentExercises', component: () => import('../views/student/ExerciseListView.vue') },
{ path: 'exercises/:id', name: 'StudentExerciseDetail', component: () => import('../views/student/ExerciseView.vue') }
```

新增文件：
- `labex-ui/src/views/student/ExerciseListView.vue` — 练习列表
- `labex-ui/src/views/student/ExerciseView.vue` — 练习题目列表 + 答题

新增 API：
- `labex-ui/src/api/student.js` 新增 listExercises, getExerciseItems, submitExerciseAnswer

**学生布局侧边栏新增**: 练习中心菜单项

---

## 4. 子项目3：仪表盘 + 统计增强

### 4.1 教师仪表盘

**后端新增**:

```java
// TeacherController 新增：
@GetMapping("/dashboard/stats")
public Result<Map<String, Object>> dashboardStats(HttpSession session) {
    verifyTeacher(session);
    return Result.ok(teacherService.getDashboardStats());
}
```

TeacherService 新增 `getDashboardStats()` 方法，查询5个视图：
- `SELECT * FROM v_student_info` — 学生总数、最大ID、最近访问
- `SELECT * FROM v_clazz_info` — 班级统计
- `SELECT * FROM v_student_answer_data_info` — 答题数据
- `SELECT * FROM v_student_answer_log_info` — 答题日志
- `SELECT * FROM v_sys_log_info` — 系统日志统计

返回格式：
```json
{
  "studentInfo": { "maxId": 100, "lastAccess": "2026-04-10", "count": 50 },
  "clazzInfo": { "count": 5 },
  "answerDataInfo": { ... },
  "answerLogInfo": { ... },
  "sysLogInfo": { "maxId": 200, "lastAccess": "2026-04-10", "count": 100 }
}
```

TeacherMapper 或使用 JdbcTemplate 直接查询视图。

**前端改动**: DashboardView.vue 增强
- 4个 el-card：学生概况、班级概况、答题数据、系统日志
- 每个 card 展示关键指标用 el-statistic
- 最近登录记录（取 sysLogInfo 最近5条）

### 4.2 学生仪表盘

**后端新增**:

```java
// StudentController 新增：
@GetMapping("/dashboard/stats")
public Result<Map<String, Object>> dashboardStats(HttpSession session) {
    UserTokenVO token = verifyStudent(session);
    return Result.ok(studentService.getDashboardStats(token.getUserId()));
}
```

StudentService 新增 `getDashboardStats(Integer studentId)` 方法：
- 统计已完成实验数（有 StudentItem 记录的实验）
- 统计未完成实验数
- 计算平均分（从 p_student_experiment_score 结果计算）

**前端改动**: student/DashboardView.vue 增强
- 展示完成进度（已完成/总实验数）
- 展示平均分
- 最近成绩列表

---

## 5. 子项目4：日志系统

### 5.1 系统日志查看（教师端）

**后端新增**:

```java
// TeacherController 新增：
@GetMapping("/logs")
public Result<IPage<SysLog>> listLogs(
    @RequestParam(defaultValue = "1") int pageNum,
    @RequestParam(defaultValue = "20") int pageSize,
    @RequestParam(required = false) String account,
    @RequestParam(required = false) Integer type,
    HttpSession session) {
    verifyTeacher(session);
    return Result.ok(logService.listLogs(pageNum, pageSize, account, type));
}
```

新增 LogService（或放入 TeacherService）：
- 分页查询 t_sys_log
- 支持按 account、type 筛选

**前端新增**:

路由：`/teacher/logs`，对应 SysLogView.vue
- 筛选栏：账号输入框、类型下拉（登录成功=1、登录失败=2）、查询按钮
- 表格：账号、类型（文字）、信息、IP、时间
- 分页

教师布局侧边栏新增：系统日志菜单项

### 5.2 学生活动日志

**后端改动**:

StudentService 在关键操作中插入日志：
- 查看实验 → 记录到 t_student_log
- 提交答案 → 已有 t_student_item_log 记录，保持
- 查看成绩 → 记录

新增 TeacherController 端点：
```java
@GetMapping("/students/{id}/logs")
public Result<List<StudentLog>> getStudentLogs(@PathVariable Integer id, HttpSession session) {
    verifyTeacher(session);
    return Result.ok(studentService.getStudentLogs(id));
}
```

**前端改动**: StudentListView 增加查看日志按钮 → 弹窗展示学生活动日志

---

## 6. 子项目5：辅助功能

### 6.1 教师重置学生密码

**后端新增**:

```java
// TeacherController 新增：
@PutMapping("/students/{id}/reset-password")
public Result<Void> resetPassword(@PathVariable Integer id, HttpSession session) {
    verifyTeacher(session);
    teacherService.resetStudentPassword(id);
    return Result.ok();
}
```

TeacherService.resetStudentPassword：
- 查找学生
- 将密码设为 BCrypt 加密的 "123456"
- 清零 error 计数（解锁账户）

**前端改动**: StudentListView 增加重置密码按钮，确认后调用

### 6.2 CSV 文件上传导入学生

**后端改动**:

修改 `POST /api/teacher/students/import`：
- 从 `@RequestBody List<Student>` 改为 `@RequestParam("file") MultipartFile`
- 解析 CSV：第一行为表头（学号,姓名），后续行为数据
- 密码默认 123456（BCrypt 加密）

TeacherService.importStudents 改为接受 MultipartFile。

**前端改动**: StudentListView 的导入功能改为 FileUpload 组件上传 CSV 文件

### 6.3 SysConfig 管理

**新建文件**:
- `labex/src/main/java/labex/mapper/SysConfigMapper.java`

**后端新增**:

```java
// TeacherController 新增：
@GetMapping("/config")
public Result<List<SysConfig>> listConfig(HttpSession session) {
    verifyTeacher(session);
    return Result.ok(configService.listConfig());
}

@PutMapping("/config")
public Result<Void> updateConfig(@RequestBody List<SysConfig> configs, HttpSession session) {
    verifyTeacher(session);
    configService.updateConfig(configs);
    return Result.ok();
}
```

**前端**: 在教师仪表盘或独立页面增加配置管理（自动保存间隔等）

---

## 7. 实施顺序

```
SP1 (基础设施) → SP2 (题库+练习) → SP3 (仪表盘) → SP4 (日志) → SP5 (辅助)
```

SP1 是其他子项目的基础（编辑器组件被 SP2 的题库管理使用，FileUpload 被 SP5 使用）。

SP2-SP5 之间无强依赖，可按任意顺序实施。

---

## 8. 不在范围内

- 考试系统（t_exam, t_paper, t_paper_question — 原系统未实现）
- 教学助手角色（t_assistant）
- 答案去重分析（t_student_answer — 需求不明确）
- 查重功能
- 多语言支持
