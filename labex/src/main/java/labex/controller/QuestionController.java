package labex.controller;

import labex.common.Result;
import labex.entity.Question;
import labex.entity.QuestionType;
import labex.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public Result<List<Question>> list() {
        return Result.ok(questionService.listQuestions());
    }

    @PostMapping
    public Result<Void> add(@RequestBody Question q) {
        questionService.addQuestion(q);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Question q) {
        q.setId(id);
        questionService.updateQuestion(q);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        questionService.deleteQuestion(id);
        return Result.ok();
    }

    @GetMapping("/types")
    public Result<List<QuestionType>> listTypes() {
        return Result.ok(questionService.listTypes());
    }
}
