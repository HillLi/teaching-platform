package labex.controller;

import labex.common.Result;
import labex.dto.SessionUtil;
import labex.dto.UserTokenVO;
import labex.entity.Exam;
import labex.entity.ExamItem;
import labex.service.ExamService;
import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    private void verifyTeacher(HttpSession session) {
        UserTokenVO token = SessionUtil.getUserToken(session);
        if (token.getUserType() != 0) {
            throw new labex.common.BusinessException("无权限访问");
        }
    }

    @GetMapping
    public Result<List<Exam>> list(HttpSession session) {
        verifyTeacher(session);
        return Result.ok(examService.listExams());
    }

    @PostMapping
    public Result<Void> add(@RequestBody Exam exam, HttpSession session) {
        verifyTeacher(session);
        UserTokenVO token = SessionUtil.getUserToken(session);
        exam.setCreatedBy(token.getUserId());
        examService.addExam(exam);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Exam exam, HttpSession session) {
        verifyTeacher(session);
        exam.setId(id);
        examService.updateExam(exam);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        examService.deleteExam(id);
        return Result.ok();
    }

    @GetMapping("/{id}/items")
    public Result<List<ExamItem>> getItems(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(examService.getExamItems(id));
    }

    @PostMapping("/{id}/items")
    public Result<Void> addItem(@PathVariable Integer id, @RequestBody ExamItem item, HttpSession session) {
        verifyTeacher(session);
        item.setExamId(id);
        examService.addExamItem(item);
        return Result.ok();
    }

    @PutMapping("/items/{itemId}")
    public Result<Void> updateItem(@PathVariable Integer itemId, @RequestBody ExamItem item, HttpSession session) {
        verifyTeacher(session);
        item.setId(itemId);
        examService.updateExamItem(item);
        return Result.ok();
    }

    @DeleteMapping("/items/{itemId}")
    public Result<Void> deleteItem(@PathVariable Integer itemId, HttpSession session) {
        verifyTeacher(session);
        examService.deleteExamItem(itemId);
        return Result.ok();
    }

    @GetMapping("/{id}/submissions")
    public Result<List<Map<String, Object>>> getSubmissions(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(examService.getExamSubmissions(id));
    }

    @GetMapping("/{id}/submissions/{studentId}")
    public Result<List<Map<String, Object>>> getSubmissionDetail(
            @PathVariable Integer id, @PathVariable Integer studentId, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(examService.getExamSubmissionDetail(id, studentId));
    }

    @PostMapping("/scores")
    public Result<Void> submitScore(@RequestBody Map<String, Object> body, HttpSession session) {
        verifyTeacher(session);
        Integer answerId = body.get("answerId") != null ? ((Number) body.get("answerId")).intValue() : null;
        Integer examItemId = body.get("examItemId") != null ? ((Number) body.get("examItemId")).intValue() : null;
        Integer studentId = body.get("studentId") != null ? ((Number) body.get("studentId")).intValue() : null;
        Integer score = body.get("score") != null ? ((Number) body.get("score")).intValue() : null;
        examService.submitExamScore(answerId, examItemId, studentId, score);
        return Result.ok();
    }

    @GetMapping("/{id}/scores")
    public Result<List<Map<String, Object>>> getScores(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(examService.getExamScores(id));
    }

    @GetMapping("/{id}/classes")
    public Result<List<String>> getExamClasses(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(examService.getExamClasses(id));
    }

    @PostMapping("/{id}/classes")
    public Result<Void> setExamClasses(@PathVariable Integer id,
                                        @RequestBody List<String> clazzNos,
                                        HttpSession session) {
        verifyTeacher(session);
        examService.setExamClasses(id, clazzNos);
        return Result.ok();
    }
}
