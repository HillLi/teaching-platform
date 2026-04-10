// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.service.TeacherService
// Handles all teacher-side database operations
// ============================================================
package labex.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * TeacherService handles all database operations initiated by teachers.
 *
 * Database tables used:
 *   t_experiment: Experiments (labs) created by teachers
 *   t_experiment_item: Individual items/questions within an experiment
 *   t_student_item: Student answers to experiment items
 *   t_student_item_log: Auto-save log
 *   t_score: Aggregated scores
 *   t_lecture: Lecture materials
 *   t_ex3, t_ex3_item: Exercise/practice questions
 *   t_clazz: Classes
 *   t_student: Students
 *
 * File paths (from constant.properties):
 *   lecture.path = /data/labex/lectures/
 *   experiment.path = /data/labex/experiments/
 *   answers.path = /data/labex/answers/
 */
@Service
public class TeacherService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${lecture.path}")
    private String lecturePath;

    @Value("${experiment.path}")
    private String experimentPath;

    @Value("${answers.path}")
    private String answersPath;

    public TeacherService() {
    }

    // ---- Experiment Management ----

    /**
     * Get all experiments ordered by experiment_no.
     */
    public List<Map<String, Object>> getExperimentList() {
        return jdbcTemplate.queryForList(
            "SELECT experiment_id, experiment_no, experiment_name, " +
            "experiment_type, instruction_type, experiment_requirement, " +
            "experiment_content, state " +
            "FROM t_experiment ORDER BY experiment_no");
    }

    /**
     * Get a single experiment by ID.
     */
    public Map<String, Object> getExperiment(int id) {
        return jdbcTemplate.queryForMap(
            "SELECT * FROM t_experiment WHERE experiment_id = ?", id);
    }

    /**
     * Save an experiment (create or update).
     */
    public void saveExperiment(Integer id, int no, String name, Integer type,
                               String instructionType, String requirement,
                               String content, Integer state) {
        if (id == null) {
            jdbcTemplate.update(
                "INSERT INTO t_experiment (experiment_no, experiment_name, " +
                "experiment_type, instruction_type, experiment_requirement, " +
                "experiment_content, state) VALUES (?, ?, ?, ?, ?, ?, ?)",
                no, name, type, instructionType, requirement, content, state);
        } else {
            jdbcTemplate.update(
                "UPDATE t_experiment SET experiment_no = ?, experiment_name = ?, " +
                "experiment_type = ?, instruction_type = ?, experiment_requirement = ?, " +
                "experiment_content = ?, state = ? WHERE experiment_id = ?",
                no, name, type, instructionType, requirement, content, state, id);
        }
    }

    // ---- Experiment Items ----

    /**
     * Get all items for a specific experiment.
     */
    public List<Map<String, Object>> getExperimentItems(int experimentId) {
        return jdbcTemplate.queryForList(
            "SELECT experiment_item_id, experiment_item_no, experiment_item_name, " +
            "experiment_item_type, experiment_item_content, experiment_item_answer, " +
            "experiment_item_score, state " +
            "FROM t_experiment_item WHERE experiment_id = ? ORDER BY experiment_item_no",
            experimentId);
    }

    /**
     * Get a single experiment item by ID.
     */
    public Map<String, Object> getExperimentItem(int itemId) {
        return jdbcTemplate.queryForMap(
            "SELECT * FROM t_experiment_item WHERE experiment_item_id = ?", itemId);
    }

    /**
     * Save an experiment item (create or update).
     */
    public void saveExperimentItem(Integer itemId, int experimentId, int no,
                                   String name, int type, String content,
                                   String answer, Integer score, Integer state) {
        if (itemId == null) {
            jdbcTemplate.update(
                "INSERT INTO t_experiment_item (experiment_id, experiment_item_no, " +
                "experiment_item_name, experiment_item_type, experiment_item_content, " +
                "experiment_item_answer, experiment_item_score, state) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                experimentId, no, name, type, content, answer, score, state);
        } else {
            jdbcTemplate.update(
                "UPDATE t_experiment_item SET experiment_item_no = ?, " +
                "experiment_item_name = ?, experiment_item_type = ?, " +
                "experiment_item_content = ?, experiment_item_answer = ?, " +
                "experiment_item_score = ?, state = ? " +
                "WHERE experiment_item_id = ?",
                no, name, type, content, answer, score, state, itemId);
        }
    }

    // ---- Scoring ----

    /**
     * Get class-level scores for a specific experiment.
     * Uses stored procedure p_clazz_experiment_score.
     */
    public List<Map<String, Object>> getClazzExperimentScore(String clazzNo, int experimentId) {
        return jdbcTemplate.queryForList(
            "CALL p_clazz_experiment_score(?, ?)", clazzNo, experimentId);
    }

    /**
     * Get a student's experiment items with their answers for grading.
     * Uses stored procedure p_student_experiment_item_score.
     */
    public List<Map<String, Object>> getStudentExperimentItems(int studentId, int experimentId) {
        return jdbcTemplate.queryForList(
            "CALL p_student_experiment_item_score(?, ?)", studentId, experimentId);
    }

    /**
     * Get all student answers for a class and experiment.
     * Uses stored procedure p_clazz_experiment_answers.
     */
    public List<Map<String, Object>> getClazzExperimentAnswers(int experimentId, String clazzNo) {
        return jdbcTemplate.queryForList(
            "CALL p_clazz_experiment_answers(?, ?)", experimentId, clazzNo);
    }

    /**
     * Save scores for student experiment items.
     * Parses the HttpServletRequest for item IDs and score values.
     *
     * Updates t_student_item.score and t_student_item.score_flag.
     */
    public void saveScores(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("score_")) {
                int studentItemId = Integer.parseInt(key.substring(6));
                int score = Integer.parseInt(entry.getValue()[0]);
                jdbcTemplate.update(
                    "UPDATE t_student_item SET score = ?, score_flag = 1 " +
                    "WHERE student_item_id = ?",
                    score, studentItemId);
            }
        }
    }

    // ---- Lecture Management ----

    /**
     * Get all lectures.
     */
    public List<Map<String, Object>> getLectureList() {
        return jdbcTemplate.queryForList(
            "SELECT lecture_id, lecture_name, lecture_type, lecture_filetype " +
            "FROM t_lecture ORDER BY lecture_id");
    }

    /**
     * Get a single lecture by ID.
     */
    public Map<String, Object> getLecture(int id) {
        return jdbcTemplate.queryForMap(
            "SELECT * FROM t_lecture WHERE lecture_id = ?", id);
    }

    /**
     * Save a lecture (create or update) with file upload.
     * Files are stored to ${lecture.path} = /data/labex/lectures/
     */
    public void saveLecture(Integer id, String name, Integer type,
                            MultipartFile file) {
        String filetype = "";
        if (file != null && !file.isEmpty()) {
            filetype = file.getOriginalFilename()
                    .substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            try {
                File dest = new File(lecturePath + name + "." + filetype);
                dest.getParentFile().mkdirs();
                file.transferTo(dest);
            } catch (Exception e) {
                throw new RuntimeException("File upload failed", e);
            }
        }

        if (id == null) {
            jdbcTemplate.update(
                "INSERT INTO t_lecture (lecture_name, lecture_type, lecture_filetype) " +
                "VALUES (?, ?, ?)",
                name, type, filetype);
        } else {
            if (!filetype.isEmpty()) {
                jdbcTemplate.update(
                    "UPDATE t_lecture SET lecture_name = ?, lecture_type = ?, " +
                    "lecture_filetype = ? WHERE lecture_id = ?",
                    name, type, filetype, id);
            } else {
                jdbcTemplate.update(
                    "UPDATE t_lecture SET lecture_name = ?, lecture_type = ? " +
                    "WHERE lecture_id = ?",
                    name, type, id);
            }
        }
    }

    // ---- Exercise Management (ex3) ----

    /**
     * Get all exercises (ex3).
     */
    public List<Map<String, Object>> getExcerciseList() {
        return jdbcTemplate.queryForList(
            "SELECT id, no, name, extype, type, description, begin_time, end_time " +
            "FROM t_ex3 ORDER BY no");
    }

    /**
     * Get a single exercise by ID.
     */
    public Map<String, Object> getExcercise(int id) {
        return jdbcTemplate.queryForMap(
            "SELECT * FROM t_ex3 WHERE id = ?", id);
    }

    /**
     * Save an exercise (create or update).
     */
    public void saveExcercise(Integer id, Integer no, String name,
                              Integer type, Integer extype, String description) {
        if (id == null) {
            jdbcTemplate.update(
                "INSERT INTO t_ex3 (no, name, extype, type, description, begin_time) " +
                "VALUES (?, ?, ?, ?, ?, NOW())",
                no, name, extype, type, description);
        } else {
            jdbcTemplate.update(
                "UPDATE t_ex3 SET no = ?, name = ?, extype = ?, type = ?, " +
                "description = ? WHERE id = ?",
                no, name, extype, type, description, id);
        }
    }
}
