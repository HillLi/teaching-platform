// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.service.QuestionService
// Handles question bank and exercise answer operations
// ============================================================
package labex.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * QuestionService handles question bank CRUD and student exercise answer operations.
 *
 * Database tables used:
 *   t_question: Question bank (id, question, answer, type)
 *   t_question_type: Question type definitions (type_id, type_name)
 *   t_paper: Exam papers (id, no, name, description, time)
 *   t_paper_question: Paper-question mapping (id, paper_id, question_id, score)
 *   t_student_question: Student answers to questions (id, student_id, question_id, answer)
 *   t_student_excercise: Student exercise answers (id, item_id, student_id, answer, content, score, fill_time)
 *   t_ex3_item: Exercise items (excercise_item_id, excercise_id, question, options, answer, type)
 *
 * The stored procedure `answerQuestion` handles upsert logic for exercise answers.
 */
@Service
public class QuestionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public QuestionService() {
    }

    // ---- Question Bank ----

    /**
     * Get all questions, optionally filtered by type.
     */
    public List<Map<String, Object>> getQuestionList(Integer type) {
        if (type != null) {
            return jdbcTemplate.queryForList(
                "SELECT q.id, q.question, q.answer, q.type, qt.type_name " +
                "FROM t_question q LEFT JOIN t_question_type qt ON q.type = qt.type_id " +
                "WHERE q.type = ? ORDER BY q.id", type);
        }
        return jdbcTemplate.queryForList(
            "SELECT q.id, q.question, q.answer, q.type, qt.type_name " +
            "FROM t_question q LEFT JOIN t_question_type qt ON q.type = qt.type_id " +
            "ORDER BY q.id");
    }

    /**
     * Get a single question by ID.
     */
    public Map<String, Object> getQuestion(int id) {
        return jdbcTemplate.queryForMap(
            "SELECT q.id, q.question, q.answer, q.type, qt.type_name " +
            "FROM t_question q LEFT JOIN t_question_type qt ON q.type = qt.type_id " +
            "WHERE q.id = ?", id);
    }

    /**
     * Save a question (create or update).
     */
    public void saveQuestion(Integer id, String question, String answer, int type) {
        if (id == null) {
            jdbcTemplate.update(
                "INSERT INTO t_question (id, question, answer, type) VALUES " +
                "((SELECT COALESCE(MAX(id), 0) + 1 FROM t_question q), ?, ?, ?)",
                question, answer, type);
        } else {
            jdbcTemplate.update(
                "UPDATE t_question SET question = ?, answer = ?, type = ? " +
                "WHERE id = ?",
                question, answer, type, id);
        }
    }

    /**
     * Get all question types for dropdowns.
     * Returns: (1,'填空'), (2,'单选'), (3,'多选'), (4,'判断'),
     *          (5,'简答'), (6,'编程'), (7,'综合')
     */
    public List<Map<String, Object>> getQuestionTypes() {
        return jdbcTemplate.queryForList(
            "SELECT type_id, type_name FROM t_question_type ORDER BY type_id");
    }

    // ---- Student Exercise Answers ----

    /**
     * Answer an exercise question. Calls the MySQL stored procedure.
     *
     * The stored procedure `answerQuestion` handles:
     *   - If no existing answer: INSERT
     *   - If answer exists: UPDATE
     *   - For type 1 or 2 (fill blank / single choice): stores in `answer` column
     *   - For other types: stores in `content` column
     */
    public void answerQuestion(int studentId, int itemId, int type, String answer) {
        jdbcTemplate.update(
            "CALL answerQuestion(?, ?, ?, ?)",
            studentId, itemId, type, answer);
    }

    // ---- Paper Management ----

    /**
     * Get all exam papers.
     */
    public List<Map<String, Object>> getPaperList() {
        return jdbcTemplate.queryForList(
            "SELECT id, no, name, description, time FROM t_paper ORDER BY id");
    }

    /**
     * Get questions for a specific paper.
     */
    public List<Map<String, Object>> getPaperQuestions(int paperId) {
        return jdbcTemplate.queryForList(
            "SELECT pq.id, pq.paper_id, pq.question_id, pq.score, " +
            "q.question, q.answer, q.type " +
            "FROM t_paper_question pq " +
            "JOIN t_question q ON pq.question_id = q.id " +
            "WHERE pq.paper_id = ? ORDER BY pq.id",
            paperId);
    }
}
