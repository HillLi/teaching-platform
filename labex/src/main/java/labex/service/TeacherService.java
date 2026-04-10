package labex.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import labex.common.BusinessException;
import labex.entity.*;
import labex.mapper.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TeacherService {

    private final ClassMapper classMapper;
    private final StudentMapper studentMapper;
    private final ExperimentMapper experimentMapper;
    private final ExperimentItemMapper experimentItemMapper;
    private final StudentItemMapper studentItemMapper;
    private final StudentItemLogMapper studentItemLogMapper;
    private final LectureMapper lectureMapper;
    private final JdbcTemplate jdbcTemplate;

    public TeacherService(ClassMapper classMapper, StudentMapper studentMapper,
                          ExperimentMapper experimentMapper, ExperimentItemMapper experimentItemMapper,
                          StudentItemMapper studentItemMapper, StudentItemLogMapper studentItemLogMapper,
                          LectureMapper lectureMapper, JdbcTemplate jdbcTemplate) {
        this.classMapper = classMapper;
        this.studentMapper = studentMapper;
        this.experimentMapper = experimentMapper;
        this.experimentItemMapper = experimentItemMapper;
        this.studentItemMapper = studentItemMapper;
        this.studentItemLogMapper = studentItemLogMapper;
        this.lectureMapper = lectureMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ===== Class Management =====

    public List<Clazz> listClasses() {
        return classMapper.selectList(new QueryWrapper<Clazz>().orderByAsc("no"));
    }

    public Clazz getClassByNo(String no) {
        Clazz clazz = classMapper.selectById(no);
        if (clazz == null) throw new BusinessException("班级不存在");
        return clazz;
    }

    public void addClass(Clazz clazz) {
        if (classMapper.selectById(clazz.getNo()) != null) {
            throw new BusinessException("班级编号已存在");
        }
        clazz.setState(1);
        classMapper.insert(clazz);
    }

    public void updateClass(Clazz clazz) {
        classMapper.updateById(clazz);
    }

    public void deleteClass(String no) {
        // Check if class has students
        Long count = studentMapper.selectCount(
                new QueryWrapper<Student>().eq("clazz_no", no));
        if (count > 0) {
            throw new BusinessException("该班级下还有学生，无法删除");
        }
        classMapper.deleteById(no);
    }

    // ===== Student Management =====

    public Page<Student> listStudents(String clazzNo, int pageNum, int pageSize) {
        QueryWrapper<Student> qw = new QueryWrapper<>();
        if (clazzNo != null && !clazzNo.isEmpty()) {
            qw.eq("clazz_no", clazzNo);
        }
        qw.orderByAsc("student_no");
        return studentMapper.selectPage(new Page<>(pageNum, pageSize), qw);
    }

    public Student getStudentById(Integer id) {
        Student student = studentMapper.selectById(id);
        if (student == null) throw new BusinessException("学生不存在");
        return student;
    }

    public void addStudent(Student student) {
        // Check duplicate student_no
        Student existing = studentMapper.selectOne(
                new QueryWrapper<Student>().eq("student_no", student.getStudentNo()));
        if (existing != null) {
            throw new BusinessException("学号已存在");
        }
        // Default password MD5('123456')
        student.setStudentPassword("e10adc3949ba59abbe56e057f20f883e");
        student.setState(1);
        student.setError(0);
        studentMapper.insert(student);
    }

    public void updateStudent(Student student) {
        studentMapper.updateById(student);
    }

    public void deleteStudent(Integer id) {
        studentMapper.deleteById(id);
    }

    public void importStudents(List<Student> students) {
        for (Student s : students) {
            Student existing = studentMapper.selectOne(
                    new QueryWrapper<Student>().eq("student_no", s.getStudentNo()));
            if (existing == null) {
                s.setStudentPassword("e10adc3949ba59abbe56e057f20f883e");
                s.setState(1);
                s.setError(0);
                studentMapper.insert(s);
            }
        }
    }

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void resetStudentPassword(Integer id) {
        Student student = studentMapper.selectById(id);
        if (student == null) throw new BusinessException("学生不存在");
        student.setStudentPassword(passwordEncoder.encode("123456"));
        student.setError(0);
        studentMapper.updateById(student);
    }

    public int importStudentsFromCsv(MultipartFile file) throws Exception {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
        String line = reader.readLine(); // Skip header
        int count = 0;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length < 2) continue;
            String studentNo = parts[0].trim();
            String studentName = parts[1].trim();
            if (studentNo.isEmpty() || studentName.isEmpty()) continue;

            Student existing = studentMapper.selectOne(
                    new QueryWrapper<Student>().eq("student_no", studentNo));
            if (existing == null) {
                Student s = new Student();
                s.setStudentNo(studentNo);
                s.setStudentName(studentName);
                s.setStudentPassword(passwordEncoder.encode("123456"));
                s.setState(1);
                s.setError(0);
                if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                    s.setClazzNo(parts[2].trim());
                }
                studentMapper.insert(s);
                count++;
            }
        }
        return count;
    }

    // ===== Experiment Management =====

    public List<Experiment> listExperiments() {
        return experimentMapper.selectList(
                new QueryWrapper<Experiment>().orderByAsc("experiment_no"));
    }

    public Experiment getExperimentById(Integer id) {
        Experiment exp = experimentMapper.selectById(id);
        if (exp == null) throw new BusinessException("实验不存在");
        return exp;
    }

    public Experiment getExperimentDetail(Integer id) {
        Experiment exp = getExperimentById(id);
        List<ExperimentItem> items = experimentItemMapper.selectList(
                new QueryWrapper<ExperimentItem>()
                        .eq("experiment_id", id)
                        .orderByAsc("experiment_item_no"));
        // Use a wrapper approach since we can't add fields to entity easily
        return exp;
    }

    public List<ExperimentItem> getExperimentItems(Integer experimentId) {
        return experimentItemMapper.selectList(
                new QueryWrapper<ExperimentItem>()
                        .eq("experiment_id", experimentId)
                        .orderByAsc("experiment_item_no"));
    }

    public void addExperiment(Experiment exp) {
        exp.setState(1);
        experimentMapper.insert(exp);
    }

    public void updateExperiment(Experiment exp) {
        experimentMapper.updateById(exp);
    }

    public void deleteExperiment(Integer id) {
        // Delete items first
        experimentItemMapper.delete(
                new QueryWrapper<ExperimentItem>().eq("experiment_id", id));
        experimentMapper.deleteById(id);
    }

    // ===== Experiment Items =====

    public void addExperimentItem(ExperimentItem item) {
        experimentItemMapper.insert(item);
    }

    public void updateExperimentItem(ExperimentItem item) {
        experimentItemMapper.updateById(item);
    }

    public void deleteExperimentItem(Integer itemId) {
        experimentItemMapper.deleteById(itemId);
    }

    public void setItemAnswer(Integer itemId, String answer) {
        ExperimentItem item = experimentItemMapper.selectById(itemId);
        if (item == null) throw new BusinessException("题目不存在");
        item.setExperimentItemAnswer(answer);
        experimentItemMapper.updateById(item);
    }

    // ===== Grading =====

    public Page<Student> listSubmittedStudents(Integer experimentId, String clazzNo, int pageNum, int pageSize) {
        // Find students who have submitted items for this experiment
        QueryWrapper<Student> qw = new QueryWrapper<>();
        if (clazzNo != null && !clazzNo.isEmpty()) {
            qw.eq("clazz_no", clazzNo);
        }
        qw.inSql("student_id",
                "SELECT DISTINCT student_id FROM t_student_item WHERE item_id IN " +
                "(SELECT experiment_item_id FROM t_experiment_item WHERE experiment_id = " + experimentId + ")");
        qw.orderByAsc("student_no");
        return studentMapper.selectPage(new Page<>(pageNum, pageSize), qw);
    }

    public List<Map<String, Object>> getStudentExperimentDetail(Integer studentId, Integer experimentId) {
        return studentMapper.selectStudentExperimentItemScore(studentId, experimentId);
    }

    public void submitScore(Integer studentItemId, Integer score) {
        StudentItem item = studentItemMapper.selectById(studentItemId);
        if (item == null) throw new BusinessException("学生提交不存在");
        item.setScore(score);
        item.setScoreFlag(1);
        studentItemMapper.updateById(item);
    }

    public List<Map<String, Object>> getClazzScoreSummary(String clazzNo, Integer experimentId) {
        return studentMapper.selectClazzExperimentScore(clazzNo, experimentId);
    }

    // ===== Lectures =====

    public List<Lecture> listLectures() {
        return lectureMapper.selectList(new QueryWrapper<>());
    }

    public void addLecture(Lecture lecture) {
        lectureMapper.insert(lecture);
    }

    public void updateLecture(Lecture lecture) {
        lectureMapper.updateById(lecture);
    }

    public void deleteLecture(Integer id) {
        lectureMapper.deleteById(id);
    }

    // ===== Dashboard Stats =====

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new java.util.HashMap<>();
        try { stats.put("studentInfo", jdbcTemplate.queryForMap("SELECT * FROM v_student_info")); } catch (Exception e) { stats.put("studentInfo", Map.of()); }
        try { stats.put("clazzInfo", jdbcTemplate.queryForMap("SELECT * FROM v_clazz_info")); } catch (Exception e) { stats.put("clazzInfo", Map.of()); }
        try { stats.put("answerDataInfo", jdbcTemplate.queryForMap("SELECT * FROM v_student_answer_data_info")); } catch (Exception e) { stats.put("answerDataInfo", Map.of()); }
        try { stats.put("answerLogInfo", jdbcTemplate.queryForMap("SELECT * FROM v_student_answer_log_info")); } catch (Exception e) { stats.put("answerLogInfo", Map.of()); }
        try { stats.put("sysLogInfo", jdbcTemplate.queryForMap("SELECT * FROM v_sys_log_info")); } catch (Exception e) { stats.put("sysLogInfo", Map.of()); }
        return stats;
    }
}
