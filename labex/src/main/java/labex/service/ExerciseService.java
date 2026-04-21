package labex.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import labex.entity.Ex3;
import labex.entity.Ex3Item;
import labex.entity.StudentExercise;
import labex.mapper.Ex3Mapper;
import labex.mapper.Ex3ItemMapper;
import labex.mapper.StudentExerciseMapper;
import org.springframework.stereotype.Service;

import labex.common.ScoringUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExerciseService {

    private final Ex3Mapper ex3Mapper;
    private final Ex3ItemMapper ex3ItemMapper;
    private final StudentExerciseMapper studentExerciseMapper;

    public ExerciseService(Ex3Mapper ex3Mapper, Ex3ItemMapper ex3ItemMapper,
                           StudentExerciseMapper studentExerciseMapper) {
        this.ex3Mapper = ex3Mapper;
        this.ex3ItemMapper = ex3ItemMapper;
        this.studentExerciseMapper = studentExerciseMapper;
    }

    public List<Ex3> listExercises() {
        return ex3Mapper.selectList(new QueryWrapper<>());
    }

    public Ex3 getExercise(Integer id) {
        return ex3Mapper.selectById(id);
    }

    public void addExercise(Ex3 ex) {
        ex3Mapper.insert(ex);
    }

    public void updateExercise(Ex3 ex) {
        ex3Mapper.updateById(ex);
    }

    public void deleteExercise(Integer id) {
        // Get item IDs first to delete student answers
        List<Ex3Item> items = ex3ItemMapper.selectList(
                new QueryWrapper<Ex3Item>().eq("excercise_id", id));
        for (Ex3Item item : items) {
            studentExerciseMapper.delete(
                    new QueryWrapper<StudentExercise>().eq("item_id", item.getExcerciseItemId()));
        }
        ex3ItemMapper.delete(new QueryWrapper<Ex3Item>().eq("excercise_id", id));
        ex3Mapper.deleteById(id);
    }

    public List<Ex3Item> getExerciseItems(Integer exerciseId) {
        return ex3ItemMapper.selectList(
                new QueryWrapper<Ex3Item>().eq("excercise_id", exerciseId));
    }

    public List<Map<String, Object>> getExerciseItemsWithAnswer(Integer exerciseId, Integer studentId) {
        List<Ex3Item> items = getExerciseItems(exerciseId);
        if (items.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        List<StudentExercise> answers = studentExerciseMapper.selectList(
                new QueryWrapper<StudentExercise>()
                        .eq("student_id", studentId)
                        .in("item_id", items.stream().map(Ex3Item::getExcerciseItemId).collect(Collectors.toList())));
        Map<Integer, StudentExercise> answerMap = answers.stream()
                .collect(Collectors.toMap(StudentExercise::getItemId, a -> a));
        return items.stream().map(item -> {
            Map<String, Object> m = new HashMap<>();
            m.put("excerciseItemId", item.getExcerciseItemId());
            m.put("excerciseId", item.getExcerciseId());
            m.put("question", item.getQuestion());
            m.put("options", item.getOptions());
            m.put("answer", item.getAnswer());
            m.put("type", item.getType());
            StudentExercise se = answerMap.get(item.getExcerciseItemId());
            m.put("answered", se != null);
            m.put("studentAnswer", se != null ? (se.getAnswer() != null ? se.getAnswer() : se.getContent()) : null);
            return m;
        }).collect(Collectors.toList());
    }

    public void addExerciseItem(Ex3Item item) {
        ex3ItemMapper.insert(item);
    }

    public void updateExerciseItem(Ex3Item item) {
        ex3ItemMapper.updateById(item);
    }

    public void deleteExerciseItem(Integer itemId) {
        studentExerciseMapper.delete(
                new QueryWrapper<StudentExercise>().eq("item_id", itemId));
        ex3ItemMapper.deleteById(itemId);
    }

    public void answerQuestion(Integer studentId, Integer itemId, Integer type, String answer) {
        Ex3Item item = ex3ItemMapper.selectById(itemId);
        if (item == null) throw new labex.common.BusinessException("题目不存在");

        Integer autoScore = null;
        if (ScoringUtil.isAutoScorable(type)) {
            autoScore = ScoringUtil.autoScore(type, item.getAnswer(), answer, 100);
        }

        StudentExercise existing = studentExerciseMapper.selectOne(
                new QueryWrapper<StudentExercise>()
                        .eq("student_id", studentId)
                        .eq("item_id", itemId));

        if (existing == null) {
            StudentExercise se = new StudentExercise();
            se.setStudentId(studentId);
            se.setItemId(itemId);
            se.setFillTime(LocalDateTime.now());
            if (type == 1 || type == 2) {
                se.setAnswer(answer);
            } else {
                se.setContent(answer);
            }
            if (autoScore != null) {
                se.setScore(autoScore);
            }
            studentExerciseMapper.insert(se);
        } else {
            existing.setFillTime(LocalDateTime.now());
            if (type == 1 || type == 2) {
                existing.setAnswer(answer);
            } else {
                existing.setContent(answer);
            }
            if (autoScore != null) {
                existing.setScore(autoScore);
            }
            studentExerciseMapper.updateById(existing);
        }
    }

    public List<Map<String, Object>> getExerciseSubmissions(Integer exerciseId) {
        List<Ex3Item> items = getExerciseItems(exerciseId);
        if (items.isEmpty()) return new java.util.ArrayList<>();

        List<Integer> itemIds = items.stream().map(Ex3Item::getExcerciseItemId).collect(Collectors.toList());
        List<StudentExercise> answers = studentExerciseMapper.selectList(
                new QueryWrapper<StudentExercise>().in("item_id", itemIds));

        Map<Integer, List<StudentExercise>> byStudent = answers.stream()
                .collect(Collectors.groupingBy(StudentExercise::getStudentId));

        return byStudent.entrySet().stream().map(entry -> {
            Map<String, Object> m = new HashMap<>();
            m.put("studentId", entry.getKey());
            m.put("submittedItemCount", entry.getValue().size());
            m.put("totalItemCount", items.size());
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getExerciseSubmissionDetail(Integer exerciseId, Integer studentId) {
        return getExerciseItemsWithAnswer(exerciseId, studentId);
    }

    public void submitExerciseScore(Long studentExerciseId, Integer score) {
        StudentExercise se = studentExerciseMapper.selectById(studentExerciseId);
        if (se == null) throw new labex.common.BusinessException("学生提交不存在");
        se.setScore(score);
        studentExerciseMapper.updateById(se);
    }

    public List<Map<String, Object>> getExerciseScores(Integer exerciseId) {
        List<Ex3Item> items = getExerciseItems(exerciseId);
        if (items.isEmpty()) return new java.util.ArrayList<>();

        List<Integer> itemIds = items.stream().map(Ex3Item::getExcerciseItemId).collect(Collectors.toList());
        List<StudentExercise> answers = studentExerciseMapper.selectList(
                new QueryWrapper<StudentExercise>().in("item_id", itemIds));

        Map<Integer, Integer> scoreByStudent = new HashMap<>();
        Map<Integer, Integer> countByStudent = new HashMap<>();
        for (StudentExercise se : answers) {
            int s = se.getScore() != null ? se.getScore() : 0;
            scoreByStudent.merge(se.getStudentId(), s, Integer::sum);
            countByStudent.merge(se.getStudentId(), 1, Integer::sum);
        }

        return scoreByStudent.entrySet().stream().map(entry -> {
            Map<String, Object> m = new HashMap<>();
            m.put("studentId", entry.getKey());
            m.put("totalScore", entry.getValue());
            m.put("answeredCount", countByStudent.getOrDefault(entry.getKey(), 0));
            m.put("totalCount", items.size());
            return m;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getExerciseStudentScore(Integer exerciseId, Integer studentId) {
        List<Ex3Item> items = getExerciseItems(exerciseId);
        List<StudentExercise> answers = studentExerciseMapper.selectList(
                new QueryWrapper<StudentExercise>()
                        .eq("student_id", studentId)
                        .in("item_id", items.stream().map(Ex3Item::getExcerciseItemId).collect(Collectors.toList())));

        int totalScore = answers.stream().mapToInt(a -> a.getScore() != null ? a.getScore() : 0).sum();
        int answeredCount = answers.size();

        Map<String, Object> result = new HashMap<>();
        result.put("totalScore", totalScore);
        result.put("answeredCount", answeredCount);
        result.put("totalCount", items.size());
        return result;
    }
}
