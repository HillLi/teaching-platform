// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.service.StudentService
// Inner classes: StudentService$1, StudentService$2 (anonymous RowMappers)
// ============================================================
package labex.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import labex.common.SessionUtil;
import labex.model.UserToken;

/**
 * StudentService handles student-related database operations.
 *
 * Database tables used:
 *   t_student: Student accounts
 *   t_clazz: Class (班级) definitions
 *   t_student_item: Student answers to experiment items
 *   t_student_item_log: Auto-save log of student answers
 *   t_student_log: Student activity log
 *   t_experiment: Experiments
 *   t_experiment_item: Experiment items/questions
 *   t_lecture: Lecture files
 *   t_score: Experiment scores
 *
 * The two anonymous inner classes ($1, $2) are RowMapper implementations
 * for different query result shapes.
 */
@Service
public class StudentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public StudentService() {
    }

    // ---- Student CRUD ----

    /**
     * Get a list of students, optionally filtered by class number.
     */
    public List<Map<String, Object>> getStudentList(String clazzNo) {
        if (clazzNo != null && !clazzNo.isEmpty()) {
            String sql = "SELECT student_id, student_no, student_name, " +
                         "clazz_no, memo, state, error, ip " +
                         "FROM t_student WHERE clazz_no = ? ORDER BY student_no";
            return jdbcTemplate.query(sql, new Object[]{clazzNo},
                    // Anonymous inner class $1: Student list RowMapper
                    new RowMapper<Map<String, Object>>() {
                        @Override
                        public Map<String, Object> mapRow(ResultSet rs, int rowNum)
                                throws SQLException {
                            return Map.of(
                                "student_id", rs.getInt("student_id"),
                                "student_no", rs.getString("student_no"),
                                "student_name", rs.getString("student_name"),
                                "clazz_no", rs.getString("clazz_no"),
                                "memo", rs.getString("memo"),
                                "state", rs.getInt("state"),
                                "error", rs.getInt("error"),
                                "ip", rs.getString("ip")
                            );
                        }
                    });
        } else {
            String sql = "SELECT student_id, student_no, student_name, " +
                         "clazz_no, memo, state, error, ip " +
                         "FROM t_student ORDER BY student_no";
            return jdbcTemplate.query(sql,
                    // Reuses same RowMapper pattern as above
                    new RowMapper<Map<String, Object>>() {
                        @Override
                        public Map<String, Object> mapRow(ResultSet rs, int rowNum)
                                throws SQLException {
                            return Map.of(
                                "student_id", rs.getInt("student_id"),
                                "student_no", rs.getString("student_no"),
                                "student_name", rs.getString("student_name"),
                                "clazz_no", rs.getString("clazz_no"),
                                "memo", rs.getString("memo"),
                                "state", rs.getInt("state"),
                                "error", rs.getInt("error"),
                                "ip", rs.getString("ip")
                            );
                        }
                    });
        }
    }

    /**
     * Get a single student by ID.
     */
    public Map<String, Object> getStudent(int id) {
        String sql = "SELECT student_id, student_no, student_name, " +
                     "student_password, clazz_no, memo, state, error, ip " +
                     "FROM t_student WHERE student_id = ?";
        return jdbcTemplate.queryForMap(sql, id);
    }

    /**
     * Save a student (create new or update existing).
     * Password is MD5-hashed before storage.
     */
    public void saveStudent(Integer id, String no, String name,
                            String password, String clazzNo) {
        if (id == null) {
            // Insert new student
            String sql = "INSERT INTO t_student (student_no, student_name, " +
                         "student_password, clazz_no, state) VALUES (?, ?, MD5(?), ?, 1)";
            jdbcTemplate.update(sql, no, name, password, clazzNo);
        } else {
            // Update existing student
            if (password != null && !password.isEmpty()) {
                String sql = "UPDATE t_student SET student_no = ?, student_name = ?, " +
                             "student_password = MD5(?), clazz_no = ? WHERE student_id = ?";
                jdbcTemplate.update(sql, no, name, password, clazzNo, id);
            } else {
                String sql = "UPDATE t_student SET student_no = ?, student_name = ?, " +
                             "clazz_no = ? WHERE student_id = ?";
                jdbcTemplate.update(sql, no, name, clazzNo, id);
            }
        }
    }

    /**
     * Import students from an uploaded file (CSV/text format).
     * Batch inserts students into a class.
     */
    public void importStudents(MultipartFile file, String clazzNo) {
        // Parse the uploaded file and batch-insert students
        // Expected format per line: student_no,student_name,password
        try {
            List<Object[]> batchArgs = new ArrayList<>();
            String content = new String(file.getBytes(), "UTF-8");
            String[] lines = content.split("\n");
            for (String line : lines) {
                String[] parts = line.trim().split(",");
                if (parts.length >= 3) {
                    batchArgs.add(new Object[]{
                        parts[0].trim(),         // student_no
                        parts[1].trim(),         // student_name
                        parts[2].trim(),         // password (raw, will be MD5'd)
                        clazzNo
                    });
                }
            }
            String sql = "INSERT INTO t_student (student_no, student_name, " +
                         "student_password, clazz_no, state) VALUES (?, ?, MD5(?), ?, 1)";
            jdbcTemplate.batchUpdate(sql, batchArgs);
        } catch (Exception e) {
            throw new RuntimeException("Import failed: " + e.getMessage(), e);
        }
    }

    /**
     * Change a student's password.
     * @return true if old password was correct and change succeeded
     */
    public boolean changePassword(int studentId, String oldPassword, String newPassword) {
        // Verify old password
        String sql = "SELECT COUNT(*) FROM t_student " +
                     "WHERE student_id = ? AND student_password = MD5(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId, oldPassword);
        if (count != null && count > 0) {
            jdbcTemplate.update(
                "UPDATE t_student SET student_password = MD5(?) WHERE student_id = ?",
                newPassword, studentId);
            return true;
        }
        return false;
    }

    // ---- Class (Clazz) Management ----

    /**
     * Get all classes.
     */
    public List<Map<String, Object>> getClazzList() {
        String sql = "SELECT no, memo, state FROM t_clazz ORDER BY no";
        return jdbcTemplate.queryForList(sql);
    }

    /**
     * Get a single class by its number.
     */
    public Map<String, Object> getClazz(String no) {
        return jdbcTemplate.queryForMap(
            "SELECT no, memo, state FROM t_clazz WHERE no = ?", no);
    }

    /**
     * Save a class (create or update).
     */
    public void saveClazz(String no, String memo, int state) {
        try {
            jdbcTemplate.queryForMap("SELECT no FROM t_clazz WHERE no = ?", no);
            // Exists - update
            jdbcTemplate.update(
                "UPDATE t_clazz SET memo = ?, state = ? WHERE no = ?",
                memo, state, no);
        } catch (Exception e) {
            // Does not exist - insert
            jdbcTemplate.update(
                "INSERT INTO t_clazz (no, memo, state) VALUES (?, ?, ?)",
                no, memo, state);
        }
    }

    // ---- Student Experiment Items ----

    /**
     * Get experiment items with the student's answers for a specific experiment.
     * Uses the stored procedure p_student_experiment_item_score.
     */
    public List<Map<String, Object>> getStudentExperimentItems(int studentId, int experimentId) {
        return jdbcTemplate.queryForList(
            "CALL p_student_experiment_item_score(?, ?)", studentId, experimentId);
    }

    /**
     * Get a single experiment item by its ID.
     */
    public Map<String, Object> getExperimentItem(int itemId) {
        return jdbcTemplate.queryForMap(
            "SELECT * FROM t_experiment_item WHERE experiment_item_id = ?", itemId);
    }

    /**
     * Save a student's answer for an experiment item.
     * Handles both creating new and updating existing answers.
     * Also logs the answer change to t_student_item_log.
     */
    public void saveStudentItem(int studentId, int itemId, String content) {
        // Check if answer already exists
        String checkSql = "SELECT COUNT(*) FROM t_student_item " +
                          "WHERE student_id = ? AND item_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class,
                studentId, itemId);

        if (count != null && count > 0) {
            // Update existing answer
            jdbcTemplate.update(
                "UPDATE t_student_item SET content = ?, fill_time = NOW() " +
                "WHERE student_id = ? AND item_id = ?",
                content, studentId, itemId);

            // Get the student_item_id for logging
            Map<String, Object> item = jdbcTemplate.queryForMap(
                "SELECT student_item_id FROM t_student_item " +
                "WHERE student_id = ? AND item_id = ?",
                studentId, itemId);
            int studentItemId = ((Number) item.get("student_item_id")).intValue();

            // Log the change
            jdbcTemplate.update(
                "INSERT INTO t_student_item_log (student_item, content, fill_time) " +
                "VALUES (?, ?, NOW())",
                studentItemId, content);
        } else {
            // Insert new answer
            jdbcTemplate.update(
                "INSERT INTO t_student_item (student_id, item_id, content, fill_time) " +
                "VALUES (?, ?, ?, NOW())",
                studentId, itemId, content);

            // Log the new entry
            Map<String, Object> item = jdbcTemplate.queryForMap(
                "SELECT student_item_id FROM t_student_item " +
                "WHERE student_id = ? AND item_id = ?",
                studentId, itemId);
            int studentItemId = ((Number) item.get("student_item_id")).intValue();

            jdbcTemplate.update(
                "INSERT INTO t_student_item_log (student_item, content, fill_time) " +
                "VALUES (?, ?, NOW())",
                studentItemId, content);
        }
    }

    /**
     * Get experiment scores for a student across all experiments.
     * Uses the stored procedure p_student_experiment_score.
     */
    public List<Map<String, Object>> getStudentExperimentScore(int studentId) {
        return jdbcTemplate.queryForList(
            "CALL p_student_experiment_score(?)", studentId);
    }

    // ---- Lectures ----

    /**
     * Get list of available lectures.
     */
    public List<Map<String, Object>> getLectureList() {
        return jdbcTemplate.queryForList(
            "SELECT lecture_id, lecture_name, lecture_type, lecture_filetype " +
            "FROM t_lecture ORDER BY lecture_id");
    }
}
