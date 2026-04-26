package labex.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import labex.common.BusinessException;
import labex.common.ScoringUtil;
import labex.dto.ExamSubmitDTO;
import labex.dto.ExamSubmitItemDTO;
import labex.entity.*;
import labex.mapper.*;
import labex.mapper.ExamClazzMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private final ExamMapper examMapper;
    private final ExamItemMapper examItemMapper;
    private final StudentExamAnswerMapper studentExamAnswerMapper;
    private final ExamSubmissionMapper examSubmissionMapper;
    private final ExamClazzMapper examClazzMapper;
    private final StudentMapper studentMapper;

    public ExamService(ExamMapper examMapper, ExamItemMapper examItemMapper,
                       StudentExamAnswerMapper studentExamAnswerMapper,
                       ExamSubmissionMapper examSubmissionMapper,
                       ExamClazzMapper examClazzMapper,
                       StudentMapper studentMapper) {
        this.examMapper = examMapper;
        this.examItemMapper = examItemMapper;
        this.studentExamAnswerMapper = studentExamAnswerMapper;
        this.examSubmissionMapper = examSubmissionMapper;
        this.examClazzMapper = examClazzMapper;
        this.studentMapper = studentMapper;
    }

    // ===== Teacher: Exam CRUD =====

    public List<Exam> listExams() {
        return examMapper.selectList(new QueryWrapper<Exam>().orderByDesc("id"));
    }

    public Exam getExam(Integer id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) throw new BusinessException("考试不存在");
        return exam;
    }

    public void addExam(Exam exam) {
        examMapper.insert(exam);
    }

    public void updateExam(Exam exam) {
        examMapper.updateById(exam);
    }

    public void deleteExam(Integer id) {
        List<ExamItem> items = examItemMapper.selectList(
                new QueryWrapper<ExamItem>().eq("exam_id", id));
        for (ExamItem item : items) {
            studentExamAnswerMapper.delete(
                    new QueryWrapper<StudentExamAnswer>().eq("exam_item_id", item.getId()));
        }
        examItemMapper.delete(new QueryWrapper<ExamItem>().eq("exam_id", id));
        examSubmissionMapper.delete(new QueryWrapper<ExamSubmission>().eq("exam_id", id));
        examMapper.deleteById(id);
    }

    // ===== Teacher: Exam Items =====

    public List<ExamItem> getExamItems(Integer examId) {
        return examItemMapper.selectList(
                new QueryWrapper<ExamItem>().eq("exam_id", examId));
    }

    public void addExamItem(ExamItem item) {
        examItemMapper.insert(item);
    }

    public void updateExamItem(ExamItem item) {
        examItemMapper.updateById(item);
    }

    public void deleteExamItem(Integer itemId) {
        studentExamAnswerMapper.delete(
                new QueryWrapper<StudentExamAnswer>().eq("exam_item_id", itemId));
        examItemMapper.deleteById(itemId);
    }

    // ===== Teacher: Grading =====

    public List<Map<String, Object>> getExamSubmissions(Integer examId) {
        List<ExamSubmission> submissions = examSubmissionMapper.selectList(
                new QueryWrapper<ExamSubmission>().eq("exam_id", examId));
        return submissions.stream().map(sub -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", sub.getId());
            m.put("studentId", sub.getStudentId());
            Student student = studentMapper.selectById(sub.getStudentId());
            m.put("studentName", student != null ? student.getStudentName() : "");
            m.put("studentNo", student != null ? student.getStudentNo() : "");
            m.put("clazzNo", student != null ? student.getClazzNo() : "");
            m.put("startTime", sub.getStartTime());
            m.put("submitTime", sub.getSubmitTime());
            m.put("totalScore", sub.getTotalScore());
            m.put("status", sub.getStatus());
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getExamSubmissionDetail(Integer examId, Integer studentId) {
        List<ExamItem> items = getExamItems(examId);
        List<StudentExamAnswer> answers = studentExamAnswerMapper.selectList(
                new QueryWrapper<StudentExamAnswer>()
                        .in("exam_item_id", items.stream().map(ExamItem::getId).collect(Collectors.toList()))
                        .eq("student_id", studentId));

        Map<Integer, StudentExamAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(StudentExamAnswer::getExamItemId, a -> a));

        return items.stream().map(item -> {
            Map<String, Object> m = new HashMap<>();
            m.put("itemId", item.getId());
            m.put("type", item.getType());
            m.put("content", item.getContent());
            m.put("options", item.getOptions());
            m.put("maxScore", item.getScore());
            m.put("referenceAnswer", item.getAnswer());

            StudentExamAnswer ans = answerMap.get(item.getId());
            m.put("studentAnswer", ans != null ? ans.getAnswer() : null);
            m.put("studentContent", ans != null ? ans.getContent() : null);
            m.put("studentFilePath", ans != null ? ans.getFilePath() : null);
            m.put("score", ans != null ? ans.getScore() : null);
            m.put("autoScored", ans != null ? ans.getAutoScored() : null);
            m.put("answerId", ans != null ? ans.getId() : null);
            return m;
        }).collect(Collectors.toList());
    }

    public void submitExamScore(Integer answerId, Integer examItemId, Integer studentId, Integer score) {
        StudentExamAnswer ans;
        if (answerId != null) {
            ans = studentExamAnswerMapper.selectById(answerId);
            if (ans == null) throw new BusinessException("学生答案不存在");
        } else {
            if (examItemId == null || studentId == null) throw new BusinessException("参数不完整");
            ans = new StudentExamAnswer();
            ans.setExamItemId(examItemId);
            ans.setStudentId(studentId);
            ans.setSubmitTime(LocalDateTime.now());
            studentExamAnswerMapper.insert(ans);
        }
        Integer originalScore = ans.getScore();
        ans.setScore(score);
        boolean wasAutoScored = ans.getAutoScored() != null && ans.getAutoScored() == 1;
        if (wasAutoScored && originalScore != null && originalScore.equals(score)) {
            // Teacher confirmed auto-score unchanged, keep autoScored=1
        } else {
            ans.setAutoScored(0);
        }
        studentExamAnswerMapper.updateById(ans);

        ExamItem item = examItemMapper.selectById(ans.getExamItemId());
        checkAndFinalizeSubmission(item.getExamId(), ans.getStudentId());
    }

    private void checkAndFinalizeSubmission(Integer examId, Integer studentId) {
        List<ExamItem> items = getExamItems(examId);
        List<StudentExamAnswer> answers = studentExamAnswerMapper.selectList(
                new QueryWrapper<StudentExamAnswer>()
                        .in("exam_item_id", items.stream().map(ExamItem::getId).collect(Collectors.toList()))
                        .eq("student_id", studentId));

        boolean allGraded = true;
        BigDecimal total = BigDecimal.ZERO;
        for (ExamItem item : items) {
            StudentExamAnswer ans = answers.stream()
                    .filter(a -> a.getExamItemId().equals(item.getId()))
                    .findFirst().orElse(null);
            if (ans == null || ans.getScore() == null) {
                allGraded = false;
                break;
            }
            total = total.add(BigDecimal.valueOf(ans.getScore()));
        }

        ExamSubmission submission = examSubmissionMapper.selectOne(
                new QueryWrapper<ExamSubmission>()
                        .eq("exam_id", examId)
                        .eq("student_id", studentId));
        if (submission != null) {
            submission.setTotalScore(total);
            if (allGraded) {
                submission.setStatus(2);
            }
            examSubmissionMapper.updateById(submission);
        }
    }

    // ===== Teacher: Score Summary =====

    public List<Map<String, Object>> getExamScores(Integer examId) {
        List<ExamSubmission> submissions = examSubmissionMapper.selectList(
                new QueryWrapper<ExamSubmission>().eq("exam_id", examId));
        return submissions.stream().map(sub -> {
            Map<String, Object> m = new HashMap<>();
            m.put("studentId", sub.getStudentId());
            Student student = studentMapper.selectById(sub.getStudentId());
            m.put("studentName", student != null ? student.getStudentName() : "");
            m.put("studentNo", student != null ? student.getStudentNo() : "");
            m.put("totalScore", sub.getTotalScore());
            m.put("status", sub.getStatus());
            m.put("submitTime", sub.getSubmitTime());
            return m;
        }).collect(Collectors.toList());
    }

    // ===== Student: Exam =====

    public List<Map<String, Object>> listAvailableExams(String clazzNo, Integer studentId) {
        LocalDateTime now = LocalDateTime.now();
        List<ExamClazz> links = examClazzMapper.selectList(new QueryWrapper<>());
        Set<Integer> examIds = links.stream()
                .filter(ec -> ec.getClazzNo().equals(clazzNo))
                .map(ExamClazz::getExamId)
                .collect(Collectors.toSet());
        if (examIds.isEmpty()) return Collections.emptyList();
        List<Exam> exams = examMapper.selectList(
                new QueryWrapper<Exam>()
                        .in("id", examIds)
                        .le("start_time", now)
                        .ge("end_time", now)
                        .orderByAsc("start_time"));
        List<ExamSubmission> submissions = examSubmissionMapper.selectList(
                new QueryWrapper<ExamSubmission>()
                        .eq("student_id", studentId)
                        .in("exam_id", examIds));
        Set<Integer> submittedIds = submissions.stream()
                .filter(s -> s.getSubmitTime() != null)
                .map(ExamSubmission::getExamId)
                .collect(Collectors.toSet());
        Set<Integer> startedIds = submissions.stream()
                .map(ExamSubmission::getExamId)
                .collect(Collectors.toSet());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Exam e : exams) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", e.getId());
            map.put("name", e.getName());
            map.put("description", e.getDescription());
            map.put("duration", e.getDuration());
            map.put("startTime", e.getStartTime());
            map.put("endTime", e.getEndTime());
            map.put("submitted", submittedIds.contains(e.getId()));
            map.put("started", startedIds.contains(e.getId()));
            result.add(map);
        }
        return result;
    }

    public void startExam(Integer examId, Integer studentId) {
        Exam exam = getExam(examId);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime())) {
            throw new BusinessException("考试尚未开始");
        }
        if (now.isAfter(exam.getEndTime())) {
            throw new BusinessException("考试已结束");
        }

        ExamSubmission existing = examSubmissionMapper.selectOne(
                new QueryWrapper<ExamSubmission>()
                        .eq("exam_id", examId)
                        .eq("student_id", studentId));
        if (existing != null && existing.getSubmitTime() != null) {
            throw new BusinessException("已提交过考试");
        }

        if (existing == null) {
            ExamSubmission sub = new ExamSubmission();
            sub.setExamId(examId);
            sub.setStudentId(studentId);
            sub.setStartTime(now);
            sub.setStatus(0);
            examSubmissionMapper.insert(sub);
        }
    }

    public List<Map<String, Object>> getStudentExamItems(Integer examId, Integer studentId) {
        Exam exam = getExam(examId);
        List<ExamItem> items = getExamItems(examId);

        ExamSubmission sub = examSubmissionMapper.selectOne(
                new QueryWrapper<ExamSubmission>()
                        .eq("exam_id", examId)
                        .eq("student_id", studentId));
        if (sub == null) {
            throw new BusinessException("请先开始考试");
        }
        if (sub.getSubmitTime() != null) {
            throw new BusinessException("已提交过考试");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(exam.getEndTime())) {
            throw new BusinessException("考试已结束");
        }
        if (exam.getDuration() != null && sub.getStartTime() != null) {
            LocalDateTime deadline = sub.getStartTime().plusMinutes(exam.getDuration());
            if (now.isAfter(deadline)) {
                throw new BusinessException("考试时长已到");
            }
        }

        return items.stream().map(item -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", item.getId());
            m.put("type", item.getType());
            m.put("content", item.getContent());
            m.put("options", item.getOptions());
            m.put("score", item.getScore());
            return m;
        }).collect(Collectors.toList());
    }

    public void submitExam(Integer examId, Integer studentId, ExamSubmitDTO dto) {
        Exam exam = getExam(examId);

        ExamSubmission sub = examSubmissionMapper.selectOne(
                new QueryWrapper<ExamSubmission>()
                        .eq("exam_id", examId)
                        .eq("student_id", studentId));
        if (sub == null) throw new BusinessException("请先开始考试");
        if (sub.getSubmitTime() != null) throw new BusinessException("已提交过考试");

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(exam.getEndTime())) {
            throw new BusinessException("考试已结束，无法提交");
        }
        if (exam.getDuration() != null && sub.getStartTime() != null) {
            LocalDateTime deadline = sub.getStartTime().plusMinutes(exam.getDuration());
            if (now.isAfter(deadline)) {
                throw new BusinessException("考试时长已到，无法提交");
            }
        }

        for (ExamSubmitItemDTO item : dto.getAnswers()) {
            ExamItem examItem = examItemMapper.selectById(item.getExamItemId());
            if (examItem == null || !examItem.getExamId().equals(examId)) continue;

            StudentExamAnswer ans = new StudentExamAnswer();
            ans.setExamItemId(item.getExamItemId());
            ans.setStudentId(studentId);
            ans.setSubmitTime(now);

            int type = examItem.getType();
            if (type == 1 || type == 2 || type == 3 || type == 4) {
                ans.setAnswer(item.getAnswer());
            } else {
                ans.setContent(item.getAnswer());
            }

            if (ScoringUtil.isAutoScorable(type)) {
                Integer autoScore = ScoringUtil.autoScore(type, examItem.getAnswer(),
                        item.getAnswer(), examItem.getScore());
                if (autoScore != null) {
                    ans.setScore(autoScore);
                    ans.setAutoScored(1);
                }
            }

            studentExamAnswerMapper.insert(ans);
        }

        sub.setSubmitTime(now);
        sub.setStatus(1);
        examSubmissionMapper.updateById(sub);

        checkAndFinalizeSubmission(examId, studentId);
    }

    public Map<String, Object> getStudentExamScore(Integer examId, Integer studentId) {
        ExamSubmission sub = examSubmissionMapper.selectOne(
                new QueryWrapper<ExamSubmission>()
                        .eq("exam_id", examId)
                        .eq("student_id", studentId));
        if (sub == null) throw new BusinessException("未参加该考试");
        if (sub.getStatus() == null || sub.getStatus() < 2) {
            throw new BusinessException("成绩尚未公布");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalScore", sub.getTotalScore());
        result.put("status", sub.getStatus());
        return result;
    }

    // ===== Exam-Class Association =====

    public List<String> getExamClasses(Integer examId) {
        List<ExamClazz> links = examClazzMapper.selectList(
                new QueryWrapper<ExamClazz>().eq("exam_id", examId));
        return links.stream().map(ExamClazz::getClazzNo).collect(Collectors.toList());
    }

    public void setExamClasses(Integer examId, List<String> clazzNos) {
        examClazzMapper.delete(new QueryWrapper<ExamClazz>().eq("exam_id", examId));
        if (clazzNos != null) {
            for (String clazzNo : clazzNos) {
                ExamClazz ec = new ExamClazz();
                ec.setExamId(examId);
                ec.setClazzNo(clazzNo);
                examClazzMapper.insert(ec);
            }
        }
    }
}
