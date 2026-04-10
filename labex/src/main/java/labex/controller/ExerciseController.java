package labex.controller;

import labex.common.Result;
import labex.dto.SessionUtil;
import labex.dto.UserTokenVO;
import labex.entity.Ex3;
import labex.entity.Ex3Item;
import labex.service.ExerciseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
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
