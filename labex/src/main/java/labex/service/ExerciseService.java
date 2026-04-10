package labex.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import labex.entity.Ex3;
import labex.entity.Ex3Item;
import labex.entity.StudentExercise;
import labex.mapper.Ex3Mapper;
import labex.mapper.Ex3ItemMapper;
import labex.mapper.StudentExerciseMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
        ex3ItemMapper.delete(new QueryWrapper<Ex3Item>().eq("excercise_id", id));
        ex3Mapper.deleteById(id);
    }

    public List<Ex3Item> getExerciseItems(Integer exerciseId) {
        return ex3ItemMapper.selectList(
                new QueryWrapper<Ex3Item>().eq("excercise_id", exerciseId));
    }

    public void addExerciseItem(Ex3Item item) {
        ex3ItemMapper.insert(item);
    }

    public void answerQuestion(Integer studentId, Integer itemId, Integer type, String answer) {
        // Use the stored procedure logic inline
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
            studentExerciseMapper.insert(se);
        } else {
            existing.setFillTime(LocalDateTime.now());
            if (type == 1 || type == 2) {
                existing.setAnswer(answer);
            } else {
                existing.setContent(answer);
            }
            studentExerciseMapper.updateById(existing);
        }
    }
}
