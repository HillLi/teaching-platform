package labex.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import labex.common.BusinessException;
import labex.dto.SessionUtil;
import labex.dto.UserTokenVO;
import labex.entity.*;
import labex.mapper.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    private final ExperimentMapper experimentMapper;
    private final ExperimentItemMapper experimentItemMapper;
    private final StudentItemMapper studentItemMapper;
    private final StudentItemLogMapper studentItemLogMapper;
    private final LectureMapper lectureMapper;
    private final StudentMapper studentMapper;
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public StudentService(ExperimentMapper experimentMapper,
                          ExperimentItemMapper experimentItemMapper,
                          StudentItemMapper studentItemMapper,
                          StudentItemLogMapper studentItemLogMapper,
                          LectureMapper lectureMapper,
                          StudentMapper studentMapper,
                          JdbcTemplate jdbcTemplate) {
        this.experimentMapper = experimentMapper;
        this.experimentItemMapper = experimentItemMapper;
        this.studentItemMapper = studentItemMapper;
        this.studentItemLogMapper = studentItemLogMapper;
        this.lectureMapper = lectureMapper;
        this.studentMapper = studentMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Experiment> listExperiments() {
        return experimentMapper.selectList(
                new QueryWrapper<Experiment>().orderByAsc("experiment_no"));
    }

    public Experiment getExperiment(Integer id) {
        Experiment exp = experimentMapper.selectById(id);
        if (exp == null) throw new BusinessException("实验不存在");
        return exp;
    }

    public List<ExperimentItem> getExperimentItems(Integer experimentId) {
        return experimentItemMapper.selectList(
                new QueryWrapper<ExperimentItem>()
                        .eq("experiment_id", experimentId)
                        .orderByAsc("experiment_item_no"));
    }

    public ExperimentItem getExperimentItemById(Integer itemId) {
        ExperimentItem item = experimentItemMapper.selectById(itemId);
        if (item == null) throw new BusinessException("题目不存在");
        return item;
    }

    public StudentItem getStudentItem(Integer itemId, Integer studentId) {
        return studentItemMapper.selectOne(
                new QueryWrapper<StudentItem>()
                        .eq("item_id", itemId)
                        .eq("student_id", studentId));
    }

    public void saveAnswer(Integer itemId, Integer studentId, String content) {
        StudentItem existing = studentItemMapper.selectOne(
                new QueryWrapper<StudentItem>()
                        .eq("item_id", itemId)
                        .eq("student_id", studentId));

        LocalDateTime now = LocalDateTime.now();

        if (existing == null) {
            // Create new
            StudentItem item = new StudentItem();
            item.setItemId(itemId);
            item.setStudentId(studentId);
            item.setContent(content);
            item.setFillTime(now);
            studentItemMapper.insert(item);

            // Log
            saveLog(item.getStudentItemId(), content, now);
        } else {
            // Update existing
            existing.setContent(content);
            existing.setFillTime(now);
            studentItemMapper.updateById(existing);

            // Log
            saveLog(existing.getStudentItemId(), content, now);
        }
    }

    private void saveLog(Integer studentItemId, String content, LocalDateTime fillTime) {
        StudentItemLog log = new StudentItemLog();
        log.setStudentItem(studentItemId);
        log.setContent(content);
        log.setFillTime(fillTime);
        studentItemLogMapper.insert(log);
    }

    public List<Map<String, Object>> getMyScores(Integer studentId) {
        return studentMapper.selectStudentExperimentScore(studentId);
    }

    public List<Lecture> listLectures() {
        return lectureMapper.selectList(new QueryWrapper<>());
    }

    public void changePassword(Integer studentId, String oldPassword, String newPassword) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) throw new BusinessException("学生不存在");

        // Verify old password
        boolean matched;
        if (student.getStudentPassword().startsWith("$2a$")) {
            matched = passwordEncoder.matches(oldPassword, student.getStudentPassword());
        } else {
            matched = DigestUtils.md5Hex(oldPassword).equalsIgnoreCase(student.getStudentPassword());
        }

        if (!matched) {
            throw new BusinessException("原密码错误");
        }

        student.setStudentPassword(passwordEncoder.encode(newPassword));
        studentMapper.updateById(student);
    }

    // ===== Dashboard Stats =====

    public Map<String, Object> getDashboardStats(Integer studentId) {
        Map<String, Object> stats = new HashMap<>();
        Long totalExps = experimentMapper.selectCount(new QueryWrapper<>());
        stats.put("totalExperiments", totalExps);
        Long completed = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT ei.experiment_id) FROM t_student_item si " +
                "JOIN t_experiment_item ei ON si.item_id = ei.experiment_item_id " +
                "WHERE si.student_id = ?", Long.class, studentId);
        stats.put("completedExperiments", completed != null ? completed : 0);
        List<Map<String, Object>> scores = studentMapper.selectStudentExperimentScore(studentId);
        double avg = scores.stream()
                .filter(m -> m.get("score") != null)
                .mapToInt(m -> ((Number) m.get("score")).intValue())
                .average().orElse(0.0);
        stats.put("averageScore", Math.round(avg * 10.0) / 10.0);
        return stats;
    }
}
