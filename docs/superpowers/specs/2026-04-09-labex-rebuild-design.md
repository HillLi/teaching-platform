# Labex 教学平台重构设计文档

**日期**: 2026-04-09
**范围**: 复现原系统完整功能，技术栈从 Spring MVC + JSP 升级到 Spring Boot + Vue
**目标**: 最小可用产品，本地部署

---

## 1. 背景

原 Labex 系统是一个基于 Spring MVC 4.1.6 + JSP 的教学实验管理平台，源码已丢失，仅剩 WAR 包和数据库脚本。通过反编译和逆向工程，已完整还原了系统设计：

- 24 个 Java 类（5 个 Controller、4 个 Service、6 个异常、4 个通用类、1 个 Model）
- 31 个 JSP 页面（17 个教师页、5 个学生页、3 个题库页、6 个公共页）
- 20 张数据库表、9 个视图、6 个存储过程
- 56 个用户功能（26 个教师功能、9 个学生功能、10 个认证功能、5 个批改/分析、6 个系统管理）

本次重构目标：使用现代技术栈复现原系统全部已实现功能。

### 已有分析文档

- `doc/database_analysis.md` — 数据库完整分析（ER图、表结构、视图、存储过程）
- `doc/frontend_analysis.md` — 前端功能清单（56个功能、31个JSP页面分析）
- `doc/decompiled/` — 20个重建的Java源文件

---

## 2. 技术架构

### 2.1 技术选型

| 层 | 技术 | 版本 | 替代原系统 |
|---|---|---|---|
| **运行时** | Java | 17 | Java 8 |
| **后端框架** | Spring Boot | 3.4.x | Spring MVC 4.1.6 |
| **数据访问** | MyBatis-Plus | 3.5.x | JdbcTemplate |
| **数据库** | MySQL | 8.x | MySQL 5.6/8.x |
| **连接池** | HikariCP (Spring Boot内置) | - | HikariCP 2.5.0 |
| **前端框架** | Vue 3 | 3.x | JSP + jQuery |
| **构建工具** | Vite | 5.x | Maven only |
| **UI组件库** | Element Plus | 2.x | Bootstrap + DevOOPS |
| **富文本编辑器** | TinyMCE | 6.x | TinyMCE 4.x |
| **代码编辑器** | Monaco Editor | 0.45+ | Ace Editor |
| **HTTP客户端** | Axios | 1.x | jQuery AJAX |
| **状态管理** | Pinia | 2.x | 无（JSP session） |
| **路由** | Vue Router | 4.x | 无（服务端路由） |

### 2.2 架构图

```
┌─────────────────────────────────┐
│  Vue 3 SPA (Vite dev :5173)     │
│  ┌─────┐ ┌──────┐ ┌──────────┐ │
│  │Login│ │Teacher│ │ Student  │ │
│  │Page │ │ Views │ │ Views    │ │
│  └──┬──┘ └──┬───┘ └────┬─────┘ │
│     └───────┼──────────┘        │
│        Axios HTTP Client        │
└────────────┬────────────────────┘
             │ REST API (JSON)
             ↓
┌─────────────────────────────────┐
│  Spring Boot (:8080)            │
│  ┌─────────────────────────────┐│
│  │ Auth Interceptor            ││
│  │ (Session/Cookie)            ││
│  ├─────────────────────────────┤│
│  │ Controllers (REST API)      ││
│  │ - AuthController            ││
│  │ - TeacherController         ││
│  │ - StudentController         ││
│  │ - QuestionController        ││
│  │ - ExerciseController        ││
│  ├─────────────────────────────┤│
│  │ Services                    ││
│  │ - LoginService              ││
│  │ - StudentService            ││
│  │ - TeacherService            ││
│  │ - QuestionService           ││
│  ├─────────────────────────────┤│
│  │ MyBatis-Plus Mappers        ││
│  └─────────────────────────────┘│
└────────────┬────────────────────┘
             │
             ↓
┌─────────────────────────────────┐
│  MySQL 8 (labex database)       │
│  20 tables + 9 views + 6 procs │
└─────────────────────────────────┘
```

### 2.3 认证方案

保持原系统的 Session 认证模式，不引入 JWT：

- 登录后服务端创建 Session，返回 `JSESSIONID` Cookie
- `SecurityInterceptor` 拦截除 `/api/auth/**` 外的所有 API
- 前端 Axios 配置 `withCredentials: true`
- Session 中存储 `UserToken`（userType, userId, account, userName, clazzNo, ip）
- IP 校验：检测同一 Session 的 IP 是否变化

### 2.4 数据库策略

**保持原有 20 张表结构不变**。具体策略：

- 字符集升级：建库脚本中 `utf8mb3` 改为 `utf8mb4`
- 保留全部 9 个视图和 6 个存储过程
- 密码存储：登录时先尝试 MD5 匹配（兼容原系统历史数据），如果 MD5 匹配成功则自动升级为 BCrypt 重新存储。新增用户直接使用 BCrypt
- 不新增表、不修改列定义
- 使用 `labex_db.sql` 和 `labex_procedure.sql` 初始化数据库

---

## 3. 后端模块设计

### 3.1 项目结构

```
labex/
├── pom.xml
├── src/main/java/labex/
│   ├── LabexApplication.java          # Spring Boot启动类
│   ├── config/
│   │   ├── WebMvcConfig.java          # CORS、拦截器注册
│   │   └── MyBatisPlusConfig.java     # MyBatis-Plus配置
│   ├── controller/
│   │   ├── AuthController.java        # 登录/登出/当前用户
│   │   ├── TeacherController.java     # 教师：班级/学生/实验/批改/讲义
│   │   ├── StudentController.java     # 学生：实验/答题/讲义/密码
│   │   ├── QuestionController.java    # 题库CRUD
│   │   └── ExerciseController.java    # 练习题管理
│   ├── service/
│   │   ├── AuthService.java           # 认证逻辑
│   │   ├── TeacherService.java        # 教师业务
│   │   ├── StudentService.java        # 学生业务
│   │   ├── QuestionService.java       # 题库业务
│   │   └── ExerciseService.java       # 练习业务
│   ├── mapper/
│   │   ├── TeacherMapper.java
│   │   ├── StudentMapper.java
│   │   ├── ClassMapper.java
│   │   ├── ExperimentMapper.java
│   │   ├── ExperimentItemMapper.java
│   │   ├── StudentItemMapper.java
│   │   ├── StudentItemLogMapper.java
│   │   ├── ScoreMapper.java
│   │   ├── LectureMapper.java
│   │   ├── QuestionMapper.java
│   │   ├── QuestionTypeMapper.java
│   │   ├── Ex3Mapper.java
│   │   ├── Ex3ItemMapper.java
│   │   ├── StudentExerciseMapper.java
│   │   ├── StudentAnswerMapper.java
│   │   └── SysLogMapper.java
│   ├── entity/                        # 20个实体类对应20张表
│   ├── dto/                           # 请求/响应DTO
│   │   ├── LoginRequest.java          # {account, password, type}
│   │   ├── UserTokenVO.java           # {userType, userId, account, userName, clazzNo}
│   │   ├── ClassDTO.java              # 班级请求/响应
│   │   ├── StudentDTO.java            # 学生请求/响应
│   │   ├── ExperimentDTO.java         # 实验请求/响应
│   │   ├── ExperimentItemDTO.java     # 实验题目项请求/响应
│   │   ├── ScoreDTO.java              # 评分请求
│   │   ├── AnswerDTO.java             # 学生答案提交
│   │   └── Result.java                # 统一响应 {code, message, data}
│   ├── interceptor/
│   │   └── SecurityInterceptor.java   # 认证拦截器
│   └── common/
│       ├── Result.java                # 统一响应封装
│       ├── BusinessException.java     # 业务异常
│       └── SessionUtil.java           # Session工具
├── src/main/resources/
│   ├── application.yml                # Spring Boot配置
│   ├── mapper/                        # MyBatis XML映射
│   └── db/
│       ├── schema.sql                 # 建表脚本（基于labex_db.sql）
│       └── data.sql                   # 初始数据
└── src/test/java/labex/
    └── ...                            # 单元测试
```

### 3.2 REST API 设计

#### 认证 `/api/auth`

| Method | Path | 描述 | 对应原端点 |
|--------|------|------|-----------|
| POST | /api/auth/login | 登录 | POST /login.do |
| POST | /api/auth/logout | 登出 | GET /logout.do |
| GET | /api/auth/current | 获取当前用户 | — (原存在Session中) |

Request:
```json
{ "account": "teacher01", "password": "xxx", "type": 0 }
```
type: 0=教师, 1=学生

#### 班级管理 `/api/teacher/classes`

| Method | Path | 描述 |
|--------|------|------|
| GET | /api/teacher/classes | 班级列表 |
| GET | /api/teacher/classes/{no} | 班级详情 |
| POST | /api/teacher/classes | 新增班级 |
| PUT | /api/teacher/classes/{no} | 修改班级 |
| DELETE | /api/teacher/classes/{no} | 删除班级 |

#### 学生管理 `/api/teacher/students`

| Method | Path | 描述 |
|--------|------|------|
| GET | /api/teacher/students | 学生列表（支持clazzNo过滤） |
| GET | /api/teacher/students/{id} | 学生详情 |
| POST | /api/teacher/students | 新增学生 |
| PUT | /api/teacher/students/{id} | 修改学生 |
| DELETE | /api/teacher/students/{id} | 删除学生 |
| POST | /api/teacher/students/import | CSV批量导入 |

#### 实验管理 `/api/teacher/experiments`

| Method | Path | 描述 |
|--------|------|------|
| GET | /api/teacher/experiments | 实验列表 |
| GET | /api/teacher/experiments/{id} | 实验详情（含题目项） |
| POST | /api/teacher/experiments | 新增实验 |
| PUT | /api/teacher/experiments/{id} | 修改实验 |
| DELETE | /api/teacher/experiments/{id} | 删除实验 |
| POST | /api/teacher/experiments/{id}/items | 新增题目项 |
| PUT | /api/teacher/experiments/items/{itemId} | 修改题目项 |
| DELETE | /api/teacher/experiments/items/{itemId} | 删除题目项 |
| PUT | /api/teacher/experiments/items/{itemId}/answer | 设置参考答案 |

#### 批改/报告 `/api/teacher/reports`

| Method | Path | 描述 |
|--------|------|------|
| GET | /api/teacher/reports/students | 查看提交学生列表 |
| GET | /api/teacher/reports/students/{studentId}/experiments/{expId} | 查看学生实验详情 |
| POST | /api/teacher/reports/scores | 提交评分 |
| GET | /api/teacher/reports/classes/{clazzNo}/experiments/{expId} | 班级成绩汇总 |
| GET | /api/teacher/reports/students/{studentId}/experiments/{expId}/html | HTML报告 |

#### 讲义管理 `/api/teacher/lectures`

| Method | Path | 描述 |
|--------|------|------|
| GET | /api/teacher/lectures | 讲义列表 |
| POST | /api/teacher/lectures | 上传讲义（multipart） |
| PUT | /api/teacher/lectures/{id} | 修改讲义信息 |
| DELETE | /api/teacher/lectures/{id} | 删除讲义 |

#### 练习管理 `/api/teacher/exercises`

| Method | Path | 描述 |
|--------|------|------|
| GET | /api/teacher/exercises | 练习列表 |
| POST | /api/teacher/exercises | 新增练习 |
| PUT | /api/teacher/exercises/{id} | 修改练习 |
| POST | /api/teacher/exercises/{id}/items | 新增练习题目 |

#### 学生端 `/api/student`

| Method | Path | 描述 |
|--------|------|------|
| GET | /api/student/experiments | 可用实验列表 |
| GET | /api/student/experiments/{id} | 实验详情（含我的答题状态） |
| GET | /api/student/experiments/{id}/items | 实验题目列表 |
| GET | /api/student/items/{itemId} | 获取题目（含我的答案） |
| POST | /api/student/items/{itemId}/answer | 提交/保存答案 |
| GET | /api/student/scores | 我的实验成绩 |
| GET | /api/student/lectures | 讲义列表 |
| GET | /api/student/lectures/{id}/download | 下载讲义 |
| PUT | /api/student/password | 修改密码 |

#### 题库 `/api/questions`

| Method | Path | 描述 |
|--------|------|------|
| GET | /api/questions | 题目列表 |
| POST | /api/questions | 新增题目 |
| PUT | /api/questions/{id} | 修改题目 |
| DELETE | /api/questions/{id} | 删除题目 |
| GET | /api/questions/types | 题目类型列表 |

---

## 4. 前端设计

### 4.1 项目结构

```
labex-ui/
├── package.json
├── vite.config.js
├── index.html
├── src/
│   ├── main.js
│   ├── App.vue
│   ├── router/
│   │   └── index.js               # 路由配置（含角色守卫）
│   ├── store/
│   │   └── user.js                 # Pinia用户状态
│   ├── api/
│   │   ├── auth.js                 # 登录/登出API
│   │   ├── teacher.js              # 教师端API
│   │   ├── student.js              # 学生端API
│   │   └── question.js             # 题库API
│   ├── utils/
│   │   ├── request.js              # Axios实例（withCredentials）
│   │   └── auth.js                 # 路由守卫
│   ├── layouts/
│   │   ├── TeacherLayout.vue       # 教师端布局（侧边栏+内容区）
│   │   └── StudentLayout.vue       # 学生端布局
│   ├── views/
│   │   ├── login/
│   │   │   └── LoginView.vue       # 登录页
│   │   ├── teacher/
│   │   │   ├── DashboardView.vue   # 仪表盘
│   │   │   ├── ClassListView.vue   # 班级管理
│   │   │   ├── ClassEditView.vue   # 班级编辑
│   │   │   ├── StudentListView.vue # 学生管理
│   │   │   ├── StudentEditView.vue # 学生编辑
│   │   │   ├── ExperimentListView.vue    # 实验列表
│   │   │   ├── ExperimentEditView.vue    # 实验编辑
│   │   │   ├── ItemListView.vue    # 实验题目管理
│   │   │   ├── ItemEditView.vue    # 题目编辑
│   │   │   ├── GradingView.vue     # 批改中心
│   │   │   ├── ReportView.vue      # 学生报告详情
│   │   │   ├── ClassReportView.vue # 班级成绩汇总
│   │   │   ├── LectureListView.vue # 讲义管理
│   │   │   ├── ExerciseListView.vue # 练习管理
│   │   │   └── ExerciseEditView.vue # 练习编辑
│   │   └── student/
│   │       ├── DashboardView.vue   # 学生首页（实验列表+成绩）
│   │       ├── ExperimentView.vue  # 实验详情
│   │       ├── AnswerView.vue      # 答题页（富文本/代码编辑器）
│   │       ├── LectureListView.vue # 讲义列表
│   │       └── PasswordView.vue    # 修改密码
│   └── components/
│       ├── RichTextEditor.vue      # TinyMCE封装
│       ├── CodeEditor.vue          # Monaco Editor封装
│       └── FileUpload.vue          # 文件上传封装
```

### 4.2 页面清单（15个核心页面）

| 页面 | Vue组件 | 替代原JSP | 功能 |
|------|---------|-----------|------|
| 登录 | LoginView | login.jsp | 账号/密码/角色选择 |
| 教师仪表盘 | TeacherDashboard | t_home.jsp (dashboard部分) | 统计概览 |
| 班级管理 | ClassListView + ClassEditView | t_clazz_list.jsp + t_clazz_edit.jsp | CRUD |
| 学生管理 | StudentListView + StudentEditView | t_student_list.jsp + t_student_edit.jsp | CRUD + CSV导入 |
| 实验管理 | ExperimentListView + ExperimentEditView | t_experiment_list.jsp + t_experiment_edit.jsp | CRUD |
| 实验题目 | ItemListView + ItemEditView | t_experiment_item_*.jsp | 题目CRUD + 答案设置 |
| 批改中心 | GradingView + ReportView + ClassReportView | t_student_report_*.jsp | 评分 + 报告 |
| 讲义管理 | LectureListView | t_lecture_*.jsp | 上传/下载 |
| 练习管理 | ExerciseListView + ExerciseEditView | t_excercise_*.jsp | 练习CRUD |
| 学生首页 | StudentDashboard | s_home.jsp + s_experiment_list.jsp | 实验列表+成绩 |
| 学生答题 | AnswerView | s_experiment_item_fill.jsp | 富文本/代码编辑器+自动保存 |
| 学生讲义 | StudentLectureListView | s_lecture_list.jsp | 下载 |
| 修改密码 | PasswordView | s_password.jsp | 密码修改 |

### 4.3 关键交互设计

**答题页自动保存**：每 10 分钟（可配置）将编辑器内容通过 `POST /api/student/items/{id}/answer` 提交，与原系统行为一致。

**教师批改流程**：选择实验 → 选择学生 → 逐项查看答案并打分 → 提交，与原系统 `t_student_report_mark.jsp` 流程一致。

**文件存储**：保持原系统的文件路径配置方式（通过 `application.yml` 配置），上传的讲义存到本地文件系统。

---

## 5. 功能复现对照表

| 原功能 | 原实现 | 新实现 | 状态 |
|--------|--------|--------|------|
| 用户登录/登出 | HomeController + LoginService | AuthController + AuthService | 复现 |
| IP校验/会话管理 | SecurityInterceptor | 新SecurityInterceptor | 复现 |
| 班级CRUD | TeacherController + StudentService | TeacherController + ClassMapper | 复现 |
| 学生CRUD+CSV导入 | TeacherController + StudentService | TeacherController + StudentMapper | 复现 |
| 实验CRUD | TeacherController + TeacherService | TeacherController + ExperimentMapper | 复现 |
| 实验题目CRUD | TeacherController + TeacherService | TeacherController + ExperimentItemMapper | 复现 |
| 学生答题+自动保存 | StudentController + StudentService | StudentController + StudentItemMapper | 复现 |
| 教师批改 | TeacherController + TeacherService | TeacherController + ScoreMapper | 复现 |
| 班级成绩汇总 | 存储过程 p_clazz_experiment_score | 同一存储过程 | 复现 |
| 学生成绩查看 | 存储过程 p_student_experiment_score | 同一存储过程 | 复现 |
| 讲义上传/下载 | TeacherController + TeacherService | TeacherController + LectureMapper | 复现 |
| 练习题管理 | TeacherController + TeacherService | ExerciseController + Ex3Mapper | 复现 |
| 练习答题 | 存储过程 answerQuestion | 同一存储过程 | 复现 |
| 题库管理 | QuestionController + QuestionService | QuestionController + QuestionMapper | 复现 |
| 系统日志 | 自动记录到 t_sys_log / t_student_log | 同表记录 | 复现 |
| 答案自动保存日志 | t_student_item_log | 同表记录 | 复现 |

---

## 6. 不在本次范围内

以下功能在升级计划中提到，但**不在本次最小可用范围内**：

- 考试系统（原系统未实现，题库/试卷/答题均为空壳）
- 查重功能
- 成绩统计分析/可视化图表
- 教学助手（t_assistant）角色
- 多语言支持（仅中文）

---

## 7. 部署方案

### 开发环境

```bash
# 后端
cd labex
mvn spring-boot:run    # 启动 :8080

# 前端
cd labex-ui
npm install
npm run dev            # 启动 :5173，代理API到 :8080
```

### 生产部署（本地）

```bash
# 构建
cd labex-ui && npm run build   # 产出 dist/
cd labex && mvn package         # 产出 labex.jar

# 运行
java -jar labex.jar             # 内嵌Tomcat，静态资源从 classpath:/static/ 提供
```

### 数据库初始化

```bash
mysql -u root -p -e "CREATE DATABASE labex CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
mysql -u root -p labex < doc/labex_db.sql
mysql -u root -p labex < doc/labex_procedure.sql
```
