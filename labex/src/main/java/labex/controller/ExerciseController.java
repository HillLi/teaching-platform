package labex.controller;

import labex.common.Result;
import labex.dto.SessionUtil;
import labex.dto.UserTokenVO;
import labex.entity.Ex3;
import labex.entity.Ex3Item;
import labex.entity.QuestionType;
import labex.mapper.QuestionTypeMapper;
import labex.service.ExerciseService;
import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final QuestionTypeMapper questionTypeMapper;

    public ExerciseController(ExerciseService exerciseService, QuestionTypeMapper questionTypeMapper) {
        this.exerciseService = exerciseService;
        this.questionTypeMapper = questionTypeMapper;
    }

    @GetMapping("/types")
    public Result<List<QuestionType>> listTypes() {
        return Result.ok(questionTypeMapper.selectList(null));
    }

    @GetMapping
    public Result<List<Ex3>> list(HttpSession session) {
        return Result.ok(exerciseService.listExercises());
    }

    @PostMapping
    public Result<Void> add(@RequestBody Ex3 ex, HttpSession session) {
        exerciseService.addExercise(ex);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Ex3 ex, HttpSession session) {
        ex.setId(id);
        exerciseService.updateExercise(ex);
        return Result.ok();
    }

    @GetMapping("/{id}/items")
    public Result<List<Ex3Item>> getItems(@PathVariable Integer id, HttpSession session) {
        return Result.ok(exerciseService.getExerciseItems(id));
    }

    @PostMapping("/{id}/items")
    public Result<Void> addItem(@PathVariable Integer id, @RequestBody Ex3Item item, HttpSession session) {
        item.setExcerciseId(id);
        exerciseService.addExerciseItem(item);
        return Result.ok();
    }

    @PutMapping("/{exerciseId}/items/{itemId}")
    public Result<Void> updateItem(@PathVariable Integer exerciseId,
                                    @PathVariable Integer itemId,
                                    @RequestBody Ex3Item item, HttpSession session) {
        item.setExcerciseItemId(itemId);
        item.setExcerciseId(exerciseId);
        exerciseService.updateExerciseItem(item);
        return Result.ok();
    }

    @DeleteMapping("/{exerciseId}/items/{itemId}")
    public Result<Void> deleteItem(@PathVariable Integer exerciseId,
                                    @PathVariable Integer itemId, HttpSession session) {
        exerciseService.deleteExerciseItem(itemId);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id, HttpSession session) {
        exerciseService.deleteExercise(id);
        return Result.ok();
    }

    // Student answer endpoint
    @PostMapping("/answer")
    public Result<Void> answerQuestion(@RequestBody Map<String, Object> body, HttpSession session) {
        UserTokenVO token = SessionUtil.getUserToken(session);
        Integer itemId = (Integer) body.get("itemId");
        Integer type = (Integer) body.get("type");
        String answer = (String) body.get("answer");
        exerciseService.answerQuestion(token.getUserId(), itemId, type, answer);
        return Result.ok();
    }
}
