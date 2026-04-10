# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Labex** (教学一体化平台) is a university Teaching Integration Platform for managing lab experiments, student submissions, grading, and course materials. Maven coordinates: `org.impstudy:labex:0.0.1-SNAPSHOT`.

## Repository Status

This repository contains **only deployment artifacts — no source code**. The Java source must be obtained separately or decompiled from the WAR. Contents:

- `doc/labex.war` — Pre-built WAR file (~31 MB)
- `doc/labex_db.sql` — MySQL schema dump (database: `labex`, 20 tables, 9 views)
- `doc/labex_procedure.sql` — 6 stored procedures
- `doc/WEB-INF/` — Partially extracted WAR config (spring-servlet.xml, web.xml, jdbc/constant properties)
- `doc/教学一体化平台改造升级实施计划.docx` — Upgrade implementation plan (Chinese)

## Tech Stack

- **Backend**: Java 8, Spring MVC 4.1.6, Spring JDBC (JdbcTemplate — no ORM)
- **Database**: MySQL 8.x with HikariCP connection pool
- **Frontend**: JSP + JSTL, jQuery/jQuery UI, Bootstrap, TinyMCE, Ace Editor, DevOOPS admin theme
- **Build**: Maven 3.5.3, WAR packaging
- **Server**: Servlet container (Tomcat), URL pattern `*.do`

## Architecture

Traditional server-side rendered MVC with SPA-like AJAX navigation:

- **Controllers** (`labex.controller`): `HomeController` (auth), `TeacherController`, `StudentController`, `QuestionController`, `ExamController`
- **Services** (`labex.service`): Data access layer using `JdbcTemplate` with raw SQL and `RowMapper` implementations
- **Models** (`labex.model`): Domain objects (e.g., `UserToken`)
- **Common** (`labex.common`): `SecurityInterceptor` (session auth), `LabexContext`, `LabexSessionListener`, auth exception hierarchy
- **Frontend pattern**: `t_home.jsp`/`s_home.jsp` serve as shell pages; sidebar navigation loads JSP fragments via `LoadAjaxContent()` into `#ajax-content` div

## Key Configuration

- Spring config: `doc/WEB-INF/spring-servlet.xml` — bean definitions, datasource, interceptors
- JDBC: `doc/WEB-INF/config/jdbc.properties` — MySQL connection (default: localhost:3306/labex, root/root)
- Paths: `doc/WEB-INF/config/constant.properties` — file storage paths (Linux: `/data/labex/`), auto-save interval (600000ms)

## Database

Database name: `labex`. Key tables: `t_student`, `t_teacher`, `t_assistant`, `t_clazz`, `t_experiment`, `t_experiment_item`, `t_student_item`, `t_score`, `t_lecture`, `t_question`, `t_paper`, `t_exam`. Views prefixed `v_`. Stored procedures: `answerQuestion`, `p_clazz_experiment_*`, `p_student_experiment_*`.

## Deployment

1. Create MySQL database: `mysql -u root -p labex < doc/labex_db.sql && mysql -u root -p labex < doc/labex_procedure.sql`
2. Deploy `labex.war` to Tomcat (update `jdbc.properties` for target DB)
3. Ensure Linux file paths from `constant.properties` exist and are writable

## Working with the WAR

To inspect WAR contents:
```bash
jar tf doc/labex.war                    # list files
unzip -o doc/labex.war "PATH" -d out/   # extract specific files
```

Source code is only available as compiled `.class` files under `WEB-INF/classes/labex/`. Decompilation (e.g., `javap`, CFR, Procyon) is needed to read Java logic.
