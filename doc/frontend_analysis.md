# Labex Frontend Analysis Report

**Project:** Labex (教学一体化平台 - Teaching Integration Platform)
**Maven Coordinates:** org.impstudy:labex:0.0.1-SNAPSHOT
**Analysis Date:** 2026-04-09
**Source:** D:/work/xianyu/TeachingPlatform/doc/labex.war

---

## 1. Complete JSP Page Inventory (31 files)

### 1.1 Authentication / Common Pages

| File | Purpose |
|---|---|
| `WEB-INF/jsp/login.jsp` | Login page -- entry point for both teachers and students. Likely contains role selection or auto-detection logic. Handles login form submission to `/login.do`. |
| `WEB-INF/jsp/error.jsp` | Global error page displayed for unhandled exceptions. |
| `WEB-INF/jsp/info.jsp` | General information/status page. Likely used for displaying messages (success, warnings) after actions. |

### 1.2 Teacher Shell / Dashboard

| File | Purpose |
|---|---|
| `WEB-INF/jsp/t_home.jsp` | **Teacher main layout shell.** The primary SPA-like container for the teacher role. Contains the sidebar navigation menu, the `#ajax-content` div where AJAX-loaded JSP fragments are injected, and includes for `js/labex.js`, `js/labex.common.js`, `js/devoops.js`, and `css/style_v2.css`. All other `t_*.jsp` pages are loaded into this shell via `LoadAjaxContent()`. |

### 1.3 Teacher-Side Feature Pages (`t_*.jsp`) -- 17 pages

| File | Purpose |
|---|---|
| `t_clazz_list.jsp` | **Class management list.** Displays all classes (from `t_clazz`). DataTable showing class number (`no`), memo (instructor name), and state. Provides links to edit or create classes. |
| `t_clazz_edit.jsp` | **Class create/edit form.** Form for creating a new class or editing an existing one. Fields: class number (VARCHAR 6), memo/description, state (active/inactive). Uses Bootstrap form validation. |
| `t_student_list.jsp` | **Student roster.** Lists all students (from `t_student`), likely filterable by class. DataTable showing student number, name, class, state, error count, last IP. Links to edit individual students. |
| `t_student_edit.jsp` | **Student create/edit form.** Form for adding or modifying student accounts. Fields: student_no, student_name, password (MD5), clazz_no (dropdown from t_clazz), memo, state. |
| `t_experiment_list.jsp` | **Experiment management list.** Displays all experiments (from `t_experiment`). DataTable with experiment_no, experiment_name, experiment_type, instruction_type, state. Links to view items, edit, or create experiments. |
| `t_experiment_edit.jsp` | **Experiment create/edit form.** Rich form for defining an experiment. Fields: experiment_no, experiment_name, experiment_type, instruction_type, experiment_requirement (likely TinyMCE rich text), experiment_content (TinyMCE rich text), state. |
| `t_experiment_item_list.jsp` | **Experiment items/questions list.** Displays all items (from `t_experiment_item`) for a given experiment. Shows item_no, item_name, item_type, max score, state. Links to edit individual items. |
| `t_experiment_item_edit.jsp` | **Experiment item create/edit form.** Form for creating or editing an experiment task/question. Fields: experiment_item_no, experiment_item_name, experiment_item_type (fill-in/code/essay etc.), experiment_item_content (rich text with TinyMCE), experiment_item_answer (reference answer), experiment_item_score, state. |
| `t_experiment_item_answer.jsp` | **Experiment item answer management.** View/edit reference answers for experiment items. Used by teachers to define correct answers that guide auto-grading or manual grading. |
| `t_lecture_list.jsp` | **Lecture/course materials list.** Lists all uploaded lectures (from `t_lecture`). Shows lecture_name, lecture_type, lecture_filetype. Likely provides links to upload, download, or delete materials. File storage path: `/data/labex/lectures/`. |
| `t_lecture_edit.jsp` | **Lecture upload/edit form.** Form for uploading or editing course material metadata. Uses FineUploader plugin for file upload. Fields: lecture_name, lecture_type, file upload. Files stored at the path defined in `constant.properties`. |
| `t_excercise_list.jsp` | **Practice exercise list.** Lists all practice exercises (from `t_ex3`). Shows exercise no, name, type, description, begin_time, end_time. Links to edit exercises and view results. Note: "excercise" is a typo used consistently in the codebase. |
| `t_excercise_edit.jsp` | **Practice exercise create/edit form.** Form for defining practice exercises. Fields: no, name, extype, type, description, begin_time (datetime picker), end_time (datetime picker). Items within exercises are managed separately. |
| `t_student_report_list.jsp` | **Student reports / grading dashboard.** Lists students with their experiment scores. Entry point for grading -- allows teacher to select a student and experiment to view/grade their submission. |
| `t_student_report_html.jsp` | **Student report HTML view.** Displays a student's experiment submission as formatted HTML. Shows each experiment item with the student's answer content. Uses TinyMCE-rendered content. |
| `t_student_report_view.jsp` | **Student report detailed view.** Detailed view of a single student's submission for a specific experiment. Shows item-by-item results with questions and answers. |
| `t_student_report_mark.jsp` | **Student report grading/score entry.** Grading interface where teachers assign scores to individual experiment items. Shows each item with the student's answer and a score input field. Submits scores to `TeacherController`. |
| `t_student_report_clazz_view.jsp` | **Class-level report view.** Aggregated view showing all students in a class and their scores for a specific experiment. Uses the `p_clazz_experiment_score` stored procedure. Likely shows a summary table with totals. |
| `t_student_report_jsp.jsp` | **Student report JSP generation.** Generates a downloadable/printable JSP-based report. Likely creates a formatted report that can be exported or printed. |

### 1.4 Student Shell / Dashboard

| File | Purpose |
|---|---|
| `WEB-INF/jsp/s_home.jsp` | **Student main layout shell.** The primary SPA-like container for the student role. Same DevOOPS-based layout as `t_home.jsp` but with student-specific sidebar navigation. Contains the `#ajax-content` div for AJAX page loading. |

### 1.5 Student-Side Feature Pages (`s_*.jsp`) -- 5 pages

| File | Purpose |
|---|---|
| `s_experiment_list.jsp` | **Student experiment list.** Shows all available experiments for the student. Displays experiment_no, experiment_name, experiment_type, state. Links to view experiment items and submit answers. |
| `s_experiment_item_list.jsp` | **Student experiment items view.** Shows all items/tasks within a specific experiment. Lists item_no, item_name, item_type. Links to fill in answers. |
| `s_experiment_item_fill.jsp` | **Student answer submission form.** The core student interaction page. Shows the experiment item question (rendered from `experiment_item_content`) and provides an input area for the student's answer. For text items: likely a textarea or TinyMCE editor. For code items: likely an Ace Editor instance. Has auto-save functionality (interval: 600000ms = 10 minutes). Submits content via AJAX. |
| `s_lecture_list.jsp` | **Student lecture/materials viewer.** Lists available course materials uploaded by teachers. Provides download links. Students can view lecture files based on `lecture_filetype`. |
| `s_password.jsp` | **Student password change form.** Self-service password change. Fields: current password, new password, confirm new password. Passwords stored as MD5 hashes (32 hex chars). |

### 1.6 Question Bank Pages (`question/`) -- 3 pages

| File | Purpose |
|---|---|
| `question/t_question_list.jsp` | **Question bank list.** Lists all questions in the bank (from `t_question`). Shows question text, type (fill-in/single-choice/multi-choice/true-false/short-answer/programming/comprehensive), and answer. |
| `question/t_question_add.jsp` | **Question add form.** Form for adding new questions to the question bank. Fields: question text, answer, type (dropdown from `t_question_type` with 7 types). |
| `question/t_question_edit.jsp` | **Question edit form.** Form for modifying existing questions in the question bank. Same fields as the add form, pre-populated with existing data. |

---

## 2. AJAX Navigation Pattern (LoadAjaxContent)

### 2.1 Architecture

The application uses a **single-page application (SPA)-like pattern** built on top of traditional server-rendered JSP. The navigation works as follows:

1. **Initial Load:** User authenticates via `login.jsp` and is redirected to either `t_home.jsp` (teacher) or `s_home.jsp` (student) via `index.do`.

2. **Shell Pages:** `t_home.jsp` and `s_home.jsp` serve as persistent layout shells. Each contains:
   - A sidebar navigation menu with links to different features
   - A `#ajax-content` div where page content is dynamically loaded
   - Header area with user info and logout
   - Script includes for `labex.js`, `labex.common.js`, `devoops.js`
   - CSS includes for `style_v2.css` and `labex.css`

3. **AJAX Content Loading:** The `LoadAjaxContent(url)` JavaScript function:
   - Makes an AJAX GET request to the specified URL (a `.do` endpoint)
   - The Spring controller returns a JSP fragment (not a full page)
   - The HTML response is injected into `#ajax-content`
   - Browser URL may or may not be updated (depends on implementation)
   - Sidebar menu items trigger `LoadAjaxContent()` calls with appropriate URLs

4. **Form Submissions:** Forms within loaded content typically use AJAX submission (likely via `jquery.form.js` plugin) to avoid full page reloads. Success/error callbacks update the content area.

5. **Auto-Save:** Student experiment answer forms (`s_experiment_item_fill.jsp`) implement auto-save at 600000ms (10 minute) intervals, sending content via AJAX to persist drafts in `t_student_item` and `t_student_item_log`.

### 2.2 URL Routing

All backend routes use the `*.do` pattern mapped through Spring's `DispatcherServlet`:

| URL Pattern | Controller | Likely Mappings |
|---|---|---|
| `/index.do`, `/login.do` | `HomeController` | Authentication, session creation, redirect to role-appropriate home |
| `/t_*.do` | `TeacherController` | Teacher feature endpoints (class CRUD, experiment CRUD, grading, reports) |
| `/s_*.do` | `StudentController` | Student feature endpoints (experiment listing, answer submission, lectures) |
| `/question/*.do` | `QuestionController` | Question bank CRUD operations |
| `/exam/*.do` | `ExamController` | Exam management (incomplete/unused) |

### 2.3 Security Interceptor

The `SecurityInterceptor` (class `labex.common.SecurityInterceptor`) intercepts all `*.do` requests except `/index.do` and `/login.do`. It checks for a valid session and handles authentication exceptions:
- `LoginIpException` -- Multiple accounts from same IP detected
- `LoginAccountException` -- Account anomaly, password change required
- `LoginInvalidException` -- Session expired, re-login required
- `LoginForbiddenException` -- Account disabled
- `LoginFailedException` -- General login failure

---

## 3. Teacher-Side Features (Complete Inventory)

### 3.1 Class Management
- **View all classes** -- DataTable with class number, instructor, state
- **Create new class** -- Form with class number, memo, active/inactive state
- **Edit existing class** -- Modify class details
- **Deactivate/activate classes** -- State toggle

### 3.2 Student Management
- **View all students** -- DataTable with student number, name, class, state, error count, last IP
- **Filter students by class** -- Class-based filtering
- **Create new student** -- Form with student number, name, password, class assignment
- **Edit student details** -- Modify student information
- **Manage student state** -- Enable/disable accounts

### 3.3 Experiment Management
- **View all experiments** -- DataTable with experiment number, name, type, state
- **Create new experiment** -- Rich form with TinyMCE editors for requirements and content
- **Edit experiment** -- Modify experiment definition
- **Set experiment type** -- Classification of experiment
- **Set instruction type** -- Instructional category
- **Manage experiment state** -- Draft/published states

### 3.4 Experiment Item/Task Management
- **View items for an experiment** -- List of tasks/questions within an experiment
- **Create experiment item** -- Form with item number, name, type, content (rich text)
- **Edit experiment item** -- Modify task definition
- **Define reference answers** -- Set correct answers for auto-grading guidance
- **Set item score weight** -- Assign point values (TINYINT)
- **Item type classification** -- Fill-in, code, essay, etc.

### 3.5 Practice Exercise Management
- **View all exercises** -- DataTable with exercise name, type, time window
- **Create exercise** -- Define practice exercises with begin/end times
- **Edit exercise** -- Modify exercise settings
- **Manage exercise items** -- Add/edit individual questions within exercises
- **Set time windows** -- Define when exercises are available

### 3.6 Lecture/Course Material Management
- **View all lectures** -- List of uploaded course materials
- **Upload new lecture** -- File upload with metadata (FineUploader plugin)
- **Edit lecture metadata** -- Modify name, type
- **Delete lectures** -- Remove course materials
- **File storage** -- Lectures stored at `/data/labex/lectures/`

### 3.7 Grading and Reports
- **View student submissions** -- Browse student answers by experiment
- **View individual student report** -- Detailed item-by-item view of student's work
- **HTML report view** -- Formatted HTML rendering of student submissions
- **Grade student submissions** -- Enter scores for individual experiment items
- **View class-level scores** -- Aggregated scores for all students in a class for an experiment
- **Generate printable reports** -- JSP-based report generation

### 3.8 Question Bank Management
- **View question bank** -- List all questions with type and answer
- **Add new question** -- Create questions with 7 type options
- **Edit existing question** -- Modify question text, answer, type
- **Question types supported:** Fill-in, Single-choice, Multi-choice, True/false, Short-answer, Programming, Comprehensive

---

## 4. Student-Side Features (Complete Inventory)

### 4.1 Experiment Browsing and Completion
- **View available experiments** -- List of active experiments with status
- **View experiment items** -- See all tasks/questions within an experiment
- **Submit answers** -- Fill in answers for each experiment item
- **Rich text editing** -- TinyMCE for essay/comprehensive items
- **Code editing** -- Ace Editor for programming items
- **Auto-save** -- Content auto-saved every 10 minutes
- **Submission tracking** -- Answers logged with timestamps in `t_student_item` and `t_student_item_log`

### 4.2 Course Material Access
- **View available lectures** -- List of teacher-uploaded materials
- **Download lecture files** -- Access course materials

### 4.3 Account Management
- **Change password** -- Self-service password modification
- **View own experiment scores** -- Likely via experiment list showing score summary

---

## 5. Question/Exam Features

### 5.1 Question Bank System
The question bank subsystem (`t_question`, `t_question_type`, `t_student_question`) provides:
- **7 question types** defined in `t_question_type`:
  1. Fill-in (填空)
  2. Single-choice (单选)
  3. Multi-choice (多选)
  4. True/false (判断)
  5. Short-answer (简答)
  6. Programming (编程)
  7. Comprehensive (综合)
- **CRUD operations** managed via `QuestionController`
- **Student answer tracking** in `t_student_question` with unique constraint per student per question

### 5.2 Exam System (Incomplete/Unused)
The exam subsystem (`t_exam`, `t_paper`, `t_paper_question`) is structurally present but **all tables are empty**:
- `t_exam` -- Examination definition with duration, scheduled time, open/close flag
- `t_paper` -- Exam papers with name and description
- `t_paper_question` -- Questions linked to papers with score allocation
- `ExamController` -- Controller class exists but exam functionality appears undeployed
- The AJAX HTML template pages include exam-related prototypes (dashboard, login v1, register pages) from the DevOOPS theme

### 5.3 Practice Exercise System
The exercise subsystem (`t_ex3`, `t_ex3_item`, `t_student_excercise`) is a secondary practice system:
- Teachers create exercises with time windows (begin_time, end_time)
- Exercises contain items with question text, options, and answers
- Item types: single-choice (type=1) and multi-choice (type=2)
- Student submissions use the `answerQuestion` stored procedure for upsert logic
- Choice answers go to `answer` column (VARCHAR 30); other answers go to `content` column (TEXT)

---

## 6. JavaScript Modules and Their Purposes

### 6.1 Custom Application JS

| File | Purpose |
|---|---|
| `js/labex.js` | **Main application logic.** Contains the `LoadAjaxContent()` function for SPA-like navigation, sidebar click handlers, AJAX form submission wrappers, event bindings for teacher/student features, and initialization code. This is the primary application JavaScript that wires up the UI. |
| `js/labex.common.js` | **Shared utility functions.** Common helper functions used across both teacher and student interfaces. Likely contains: AJAX error handling, notification/toast functions, form serialization helpers, auto-save timer logic, and shared validation utilities. |
| `js/devoops.js` | **DevOOPS theme framework.** Core JavaScript for the DevOOPS admin dashboard theme. Handles: sidebar collapse/expand, responsive layout, theme toggling, breadcrumb updates, panel minimize/maximize, and other theme-level interactions. |

### 6.2 Compatibility JS

| File | Purpose |
|---|---|
| `js/html5shiv.min.js` | HTML5 element support for Internet Explorer 8 and below |
| `js/respond.min.js` | CSS media query support for Internet Explorer 8 and below |

### 6.3 Plugin Libraries (Third-Party)

| Plugin | Version/Path | Purpose |
|---|---|---|
| **jQuery** | `plugins/jquery/jquery.min.js` | Core DOM manipulation and AJAX library |
| **jQuery UI** | `plugins/jquery-ui/jquery-ui.min.js` | UI widgets: dialogs, datepickers, sortable, tabs |
| **jQuery UI Timepicker** | `plugins/jquery-ui-timepicker-addon/` | Date+time selection for exercise scheduling |
| **jQuery Form** | `plugins/jquery/jquery.form.js` | AJAX form submission (used for answer submission) |
| **Bootstrap** | `plugins/bootstrap/bootstrap.min.js` | Responsive grid, modals, dropdowns, tooltips |
| **BootstrapValidator** | `plugins/bootstrapvalidator/` | Form validation with `zh_CN` locale |
| **TinyMCE** | `plugins/tinymce/` | Rich text WYSIWYG editor for experiment content and student answers |
| **Ace Editor** (two versions) | `plugins/ace/` and `plugins/ace-old/` | Source code editor for programming-type experiment items. Two versions bundled (new and old). Supports 50+ language modes including Java, Python, C++, HTML, CSS, JavaScript, SQL, etc. |
| **DataTables** | `plugins/datatables/` | Sortable, searchable, paginated tables for all list views |
| **FineUploader** | `plugins/fineuploader/fineuploader-5.0.5.js` | File upload widget for lecture materials |
| **Select2** | `plugins/select2/` | Enhanced dropdown select with search for class/student selection |
| **InputMask** | `plugins/inputmask3/` | Input formatting (phone, date, numeric patterns) |
| **AmCharts** | `plugins/amcharts/` | Interactive charts (serial, pie, gauge, funnel, radar, xy) |
| **Flot** | `plugins/flot/` | jQuery-based plotting library |
| **Morris** | `plugins/morris/` | Simple time-series and bar charts |
| **Chartist** | `plugins/chartist/` | Responsive chart library |
| **D3.js** | `plugins/d3/d3.min.js` | Data-driven documents for advanced visualizations |
| **Fancybox** | `plugins/fancybox/` | Lightbox for images and content preview |
| **Sparkline** | `plugins/sparkline/` | Inline mini-charts for dashboards |
| **jQuery Knob** | `plugins/jQuery-Knob/` | Circular dial/knob inputs |
| **FullCalendar** | `plugins/fullcalendar/` | Calendar widget with `zh-cn` locale |
| **Justified Gallery** | `plugins/justified-gallery/` | Image gallery layout |
| **Leaflet** | `plugins/leaflet/` | Interactive maps |
| **Moment.js** | `plugins/moment/` | Date/time manipulation with locale support |
| **Springy** | `plugins/springy/` | Force-directed graph layout |

---

## 7. CSS/Theme Structure

### 7.1 Custom Stylesheets

| File | Purpose |
|---|---|
| `css/style_v2.css` | **Primary application stylesheet.** The active theme CSS, extending the DevOOPS admin theme with Labex-specific styles. Defines layout, sidebar, content area, forms, tables, and component overrides. |
| `css/style_v1.css` | **Previous version stylesheet.** An older version of the theme, kept for reference or fallback. |
| `css/labex.css` | **Labex-specific overrides.** Custom styles unique to the Labex application, overriding or extending the base theme. Likely contains styles for experiment forms, code editor containers, grading tables, etc. |

### 7.2 Theme Architecture

The application uses the **DevOOPS** admin dashboard theme (bootstrap-based):
- **Layout:** Fixed sidebar on the left, main content area on the right
- **Sidebar:** Collapsible navigation with icon-based menu items
- **Responsive:** Uses Bootstrap grid + respond.js for IE8 compatibility
- **Dark theme:** The DevOOPS theme typically features a dark sidebar with light content area
- **Icons:** Font Awesome for menu and action icons
- **Chinese fonts:** Custom font package `font-zhs/rzyzk_wb/` for Chinese character rendering

### 7.3 Plugin CSS

The bundled CSS files include:
- `plugins/bootstrap/bootstrap.min.css` -- Base Bootstrap styles
- `plugins/bootstrap/bootstrap-theme.min.css` -- Bootstrap theme
- `plugins/font-awesome/css/font-awesome.min.css` -- Icon library
- `plugins/jquery-ui/jquery-ui.min.css` -- jQuery UI widget styles
- `plugins/jquery-ui-timepicker-addon/jquery-ui-timepicker-addon.min.css` -- Timepicker
- `plugins/datatables/dataTables.bootstrap.js` -- DataTable Bootstrap integration
- `plugins/select2/select2.css` -- Enhanced select dropdown
- `plugins/fineuploader/fineuploader-5.0.5.css` -- File upload widget
- `plugins/tinymce/skins/lightgray/skin.min.css` -- TinyMCE editor skin
- `plugins/justified-gallery/justifiedGallery.css` -- Gallery layout

---

## 8. UI Interactions

### 8.1 Forms

| Feature | Form Type | Submission | Validation |
|---|---|---|---|
| Login | Username/password | POST to `/login.do` | Server-side (MD5 check) |
| Class create/edit | Text input + dropdown | AJAX | BootstrapValidator |
| Student create/edit | Text + password + class dropdown | AJAX | BootstrapValidator |
| Experiment create/edit | Text + TinyMCE rich text | AJAX (multipart for file uploads) | BootstrapValidator |
| Experiment item create/edit | Text + TinyMCE + score input | AJAX | BootstrapValidator |
| Student answer submission | TinyMCE or Ace Editor | AJAX with auto-save | Client + server |
| Practice exercise create/edit | Text + datetime pickers | AJAX | BootstrapValidator |
| Password change | 3-field password form | AJAX | Server-side MD5 comparison |
| Lecture upload | FineUploader drag-and-drop | AJAX multipart | File type/size |
| Question bank CRUD | Text + type dropdown | AJAX | BootstrapValidator |
| Grading/score entry | Numeric inputs per item | AJAX | Server-side range check |

### 8.2 Modals and Dialogs

The application likely uses jQuery UI dialogs and Bootstrap modals for:
- **Confirmations** before delete operations (class, student, experiment)
- **Alert messages** for success/error notifications
- **Detail popups** for viewing student submissions inline
- **File preview** via Fancybox lightbox

### 8.3 AJAX Calls (Inferred Endpoints)

Based on the controller structure, services, and JSP pages, the following AJAX endpoints are expected:

**HomeController:**
- `GET /index.do` -- Serve login page
- `POST /login.do` -- Authenticate user
- `GET /logout.do` -- Invalidate session, redirect to login

**TeacherController:**
- `GET /t_clazz_list.do` -- Load class list fragment
- `GET /t_clazz_edit.do?id=X` -- Load class edit form
- `POST /t_clazz_save.do` -- Save class (create/update)
- `GET /t_student_list.do` -- Load student list fragment
- `GET /t_student_list.do?clazz=X` -- Load students filtered by class
- `GET /t_student_edit.do?id=X` -- Load student edit form
- `POST /t_student_save.do` -- Save student (create/update)
- `GET /t_experiment_list.do` -- Load experiment list fragment
- `GET /t_experiment_edit.do?id=X` -- Load experiment edit form
- `POST /t_experiment_save.do` -- Save experiment (create/update)
- `GET /t_experiment_item_list.do?expId=X` -- Load items for experiment
- `GET /t_experiment_item_edit.do?id=X` -- Load item edit form
- `POST /t_experiment_item_save.do` -- Save experiment item
- `GET /t_student_report_list.do` -- Load grading dashboard
- `GET /t_student_report_view.do?studentId=X&expId=Y` -- View student report
- `GET /t_student_report_mark.do?studentId=X&expId=Y` -- Load grading form
- `POST /t_student_report_mark.do` -- Submit scores
- `GET /t_student_report_clazz_view.do?clazzNo=X&expId=Y` -- Class report
- `GET /t_student_report_html.do?studentId=X&expId=Y` -- HTML report
- `GET /t_student_report_jsp.do` -- Generate printable report
- `GET /t_lecture_list.do` -- Load lecture list
- `GET /t_lecture_edit.do?id=X` -- Load lecture edit/upload form
- `POST /t_lecture_save.do` -- Upload/save lecture
- `GET /t_excercise_list.do` -- Load practice exercise list
- `GET /t_excercise_edit.do?id=X` -- Load exercise edit form
- `POST /t_excercise_save.do` -- Save exercise

**StudentController:**
- `GET /s_experiment_list.do` -- Load available experiments
- `GET /s_experiment_item_list.do?expId=X` -- Load items for experiment
- `GET /s_experiment_item_fill.do?itemId=X` -- Load answer form
- `POST /s_experiment_item_fill.do` -- Submit/update answer
- `GET /s_lecture_list.do` -- Load available lectures
- `GET /s_password.do` -- Load password change form
- `POST /s_password.do` -- Change password

**QuestionController:**
- `GET /question/t_question_list.do` -- Load question bank list
- `GET /question/t_question_add.do` -- Load question add form
- `POST /question/t_question_save.do` -- Save question
- `GET /question/t_question_edit.do?id=X` -- Load question edit form
- `POST /question/t_question_update.do` -- Update question

**ExamController:**
- Endpoints for exam management (incomplete/unused, tables empty)

### 8.4 DataTable Integration

All list views use the DataTables jQuery plugin with these features:
- Column sorting
- Search/filtering
- Pagination
- Bootstrap integration via `dataTables.bootstrap.js`
- Export capabilities via TableTools (copy, CSV, Excel, PDF via SWF)

### 8.5 Rich Text Editing

TinyMCE is the rich text editor used for:
- Experiment requirements (`experiment_requirement`)
- Experiment content (`experiment_content`)
- Experiment item content (`experiment_item_content`)
- Student answers for essay/comprehensive items
- Configuration: minimal toolbar, Chinese language support, auto-save plugin

### 8.6 Code Editing

Ace Editor is integrated for programming-type experiment items:
- Two versions bundled (ace and ace-old)
- 50+ language modes supported (Java, Python, C++, HTML, CSS, JavaScript, SQL, etc.)
- Multiple themes (monokai, eclipse, tomorrow_night, etc.)
- Features: syntax highlighting, auto-completion, line numbers, code folding

### 8.7 File Upload

FineUploader (v5.0.5) handles file uploads for:
- Lecture/course material uploads
- Potentially experiment attachment uploads
- Drag-and-drop support
- Progress bars
- File type/size validation

---

## 9. Complete Feature Inventory

### 9.1 Authentication and Authorization
1. User login (student/teacher/assistant roles)
2. Session-based authentication with `SecurityInterceptor`
3. IP-based duplicate login detection
4. Account state management (active/disabled)
5. Login error counting and lockout
6. Activity logging (`t_sys_log`, `t_student_log`)
7. Password change (student self-service)
8. Session invalidation on logout
9. Forced re-login on session expiry
10. Account anomaly detection with forced password change

### 9.2 Teacher Features
11. Dashboard with system statistics (student count, class count, submission count, log counts)
12. Class CRUD (create, read, update, delete classes)
13. Class state management (activate/deactivate)
14. Student CRUD (create, read, update, delete students)
15. Student filtering by class
16. Student password reset
17. Student account state management
18. Experiment CRUD (create, read, update, delete experiments)
19. Experiment rich text editing (requirements, content)
20. Experiment state management (draft/published)
21. Experiment item CRUD (create, read, update, delete tasks within experiments)
22. Experiment item type classification (fill-in, code, essay, etc.)
23. Experiment item reference answer definition
24. Experiment item score weight assignment
25. Practice exercise CRUD
26. Practice exercise time window scheduling (begin/end datetime)
27. Practice exercise item management (choice questions)
28. Lecture/course material upload
29. Lecture metadata management
30. Student submission viewing (per student, per experiment)
31. Student submission HTML rendering
32. Manual grading interface (score entry per item)
33. Class-level score aggregation view
34. Printable report generation
35. Question bank CRUD
36. Question type management (7 types)

### 9.3 Student Features
37. Available experiment browsing
38. Experiment item/task viewing
39. Rich text answer submission (essay items)
40. Code answer submission (programming items with Ace Editor)
41. Answer auto-save (10-minute interval)
42. Answer submission history tracking (via `t_student_item_log`)
43. Course material viewing and downloading
44. Password self-service change
45. Experiment score viewing (via `p_student_experiment_score`)

### 9.4 Grading and Analytics
46. Per-item manual grading by teacher
47. Per-experiment score aggregation
48. Per-class score summary
49. Answer statistics and analysis (`t_student_answer` with content hashing)
50. Dashboard statistics views (v_student_info, v_clazz_info, etc.)

### 9.5 System/Administrative
51. System logging (login events, admin actions)
52. Student activity logging
53. Session management (`LabexSessionListener`)
54. Internationalization support (message_zh_CN.properties)
55. File storage management (lectures, experiments, answers, temp JSP)
56. Auto-save configuration (via constant.properties)

---

## 10. File Storage Structure

Based on `constant.properties`:

| Path | Purpose |
|---|---|
| `/data/labex/lectures/` | Uploaded lecture/course material files |
| `/data/labex/experiments/` | Experiment-related file attachments |
| `/usr/local/tomcat/labex/temp/` | Temporary JSP files (dynamic report generation) |
| `/data/labex/answers/` | Student answer file storage (for file-based submissions) |

---

## 11. Data Flow Summary

### 11.1 Student Answer Submission Flow
```
Student clicks experiment item
  -> LoadAjaxContent('/s_experiment_item_fill.do?itemId=X')
  -> s_experiment_item_fill.jsp rendered with item content
  -> Student types answer (TinyMCE or Ace Editor)
  -> Auto-save timer fires every 10 min -> AJAX POST to save
  -> Student clicks Submit -> AJAX POST
  -> StudentController saves to t_student_item
  -> Audit log written to t_student_item_log
  -> Answer analysis data written to t_student_answer
```

### 11.2 Teacher Grading Flow
```
Teacher opens grading dashboard
  -> LoadAjaxContent('/t_student_report_list.do')
  -> Selects student + experiment
  -> LoadAjaxContent('/t_student_report_mark.do?studentId=X&expId=Y')
  -> Views each item with student answer
  -> Enters score per item -> AJAX POST
  -> TeacherController updates t_student_item.score
```

### 11.3 AJAX Page Navigation Flow
```
User clicks sidebar menu item
  -> JavaScript calls LoadAjaxContent('/some_page.do')
  -> AJAX GET request to Spring controller
  -> Controller returns JSP fragment (not full HTML page)
  -> Response injected into #ajax-content div
  -> JavaScript re-initializes components (DataTables, TinyMCE, etc.)
```

---

## 12. Technology Stack Summary

| Layer | Technology | Details |
|---|---|---|
| **Server** | Tomcat (Servlet 2.4) | Spring MVC 4.1.6, JSP + JSTL |
| **Database** | MySQL 8.x | HikariCP connection pool, JdbcTemplate |
| **CSS Framework** | Bootstrap 3.x | DevOOPS admin theme |
| **JS Framework** | jQuery + jQuery UI | SPA-like AJAX navigation |
| **Rich Text** | TinyMCE 4.x | WYSIWYG editing |
| **Code Editor** | Ace Editor (2 versions) | 50+ language modes |
| **Tables** | DataTables | Sort, search, paginate, export |
| **File Upload** | FineUploader 5.0.5 | Drag-and-drop |
| **Validation** | BootstrapValidator | zh_CN locale |
| **Charts** | AmCharts, Flot, Morris, Chartist, D3 | Multiple charting libraries |
| **Icons** | Font Awesome | Icon library |
| **Build** | Maven 3.x | WAR packaging |

---

## 13. Notes and Observations

1. **Exam system is incomplete.** All exam-related tables (`t_exam`, `t_paper`, `t_paper_question`) are empty, and the `ExamController` exists but appears unused. The question bank subsystem is more developed.

2. **Two editor systems.** Ace Editor is bundled in two versions (ace/ and ace-old/), suggesting an upgrade was attempted but the old version was kept for compatibility.

3. **DevOOPS AJAX pages.** The `ajax/` directory contains 30+ HTML template pages from the DevOOPS theme (dashboards, charts, forms, tables, gallery, maps, login pages, etc.). Most of these are theme demo pages that are **not directly used** by the application but serve as reference templates.

4. **Inconsistent naming.** The practice exercise tables and JSP files use "excercise" (misspelling) consistently, while the exam tables use correct spelling.

5. **Password security.** Passwords are stored as unsalted MD5 hashes (CHAR(32)), which is cryptographically weak.

6. **Auto-save.** The auto-save interval is configurable via `page.autosavetime` in constant.properties, defaulting to 600000ms (10 minutes).

7. **Chinese localization.** The application is fully Chinese-language. Error messages, validation messages, and UI text are in Chinese. `message_zh_CN.properties` provides i18n strings.

8. **Large plugin footprint.** The WAR includes extensive third-party libraries including many that may not be actively used (e.g., Leaflet maps, Springy graphs, multiple charting libraries), contributing to the ~31MB WAR size.

9. **Session-based auth.** Authentication uses HTTP sessions with `LabexSessionListener` for session lifecycle management. The `SecurityInterceptor` enforces authentication on all `*.do` endpoints except login.

10. **MD5 content hashing.** Student answers are hashed (MD5) and deduplicated in `t_student_answer` for analytics, tracking unique answer patterns across all students.
