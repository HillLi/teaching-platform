package labex.controller;

import labex.common.BusinessException;
import labex.common.Result;
import labex.dto.SessionUtil;
import labex.dto.UserTokenVO;
import labex.dto.AnswerDTO;
import labex.entity.*;
import labex.service.StudentService;
import labex.service.ExerciseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;
    private final ExerciseService exerciseService;

    @Value("${labex.upload.lecture-path}")
    private String lecturePath;

    public StudentController(StudentService studentService, ExerciseService exerciseService) {
        this.studentService = studentService;
        this.exerciseService = exerciseService;
    }

    private UserTokenVO verifyStudent(HttpSession session) {
        UserTokenVO token = SessionUtil.getUserToken(session);
        if (token.getUserType() != 1) {
            throw new BusinessException("无权限访问");
        }
        return token;
    }

    @GetMapping("/experiments")
    public Result<List<Experiment>> listExperiments(HttpSession session) {
        verifyStudent(session);
        return Result.ok(studentService.listExperiments());
    }

    @GetMapping("/experiments/{id}")
    public Result<Experiment> getExperiment(@PathVariable Integer id, HttpSession session) {
        verifyStudent(session);
        return Result.ok(studentService.getExperiment(id));
    }

    @GetMapping("/experiments/{id}/items")
    public Result<List<ExperimentItem>> getExperimentItems(@PathVariable Integer id, HttpSession session) {
        UserTokenVO token = verifyStudent(session);
        List<ExperimentItem> items = studentService.getExperimentItems(id);
        return Result.ok(items);
    }

    @GetMapping("/items/{itemId}")
    public Result<Map<String, Object>> getItem(@PathVariable Integer itemId, HttpSession session) {
        UserTokenVO token = verifyStudent(session);
        ExperimentItem item = studentService.getExperimentItemById(itemId);
        StudentItem si = studentService.getStudentItem(itemId, token.getUserId());
        return Result.ok(Map.of(
                "item", item,
                "studentItem", si != null ? si : ""
        ));
    }

    @PostMapping("/items/{itemId}/answer")
    public Result<Void> saveAnswer(@PathVariable Integer itemId,
                                    @RequestBody AnswerDTO dto,
                                    HttpSession session) {
        UserTokenVO token = verifyStudent(session);
        studentService.saveAnswer(itemId, token.getUserId(), dto.getContent());
        return Result.ok();
    }

    @GetMapping("/scores")
    public Result<List<Map<String, Object>>> getMyScores(HttpSession session) {
        UserTokenVO token = verifyStudent(session);
        return Result.ok(studentService.getMyScores(token.getUserId()));
    }

    @GetMapping("/lectures")
    public Result<List<Lecture>> listLectures(HttpSession session) {
        verifyStudent(session);
        return Result.ok(studentService.listLectures());
    }

    @GetMapping("/lectures/{id}/download")
    public ResponseEntity<Resource> downloadLecture(@PathVariable Integer id, HttpSession session) throws IOException {
        verifyStudent(session);
        List<Lecture> lectures = studentService.listLectures();
        Lecture lecture = lectures.stream()
                .filter(l -> l.getLectureId().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("讲义不存在"));

        File file = new File(lecturePath, lecture.getLectureId() + "." + lecture.getLectureFiletype());
        if (!file.exists()) {
            throw new BusinessException("文件不存在");
        }

        FileSystemResource resource = new FileSystemResource(file);
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + lecture.getLectureName() + "." + lecture.getLectureFiletype() + "\"")
                .body(resource);
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body, HttpSession session) {
        UserTokenVO token = verifyStudent(session);
        studentService.changePassword(token.getUserId(), body.get("oldPassword"), body.get("newPassword"));
        return Result.ok();
    }

    // ===== Exercises (Student) =====

    @GetMapping("/exercises")
    public Result<List<Ex3>> listExercises(HttpSession session) {
        verifyStudent(session);
        return Result.ok(exerciseService.listExercises());
    }

    @GetMapping("/exercises/{id}/items")
    public Result<List<Ex3Item>> getExerciseItems(@PathVariable Integer id, HttpSession session) {
        verifyStudent(session);
        return Result.ok(exerciseService.getExerciseItems(id));
    }

    @PostMapping("/exercises/answer")
    public Result<Void> answerExercise(@RequestBody Map<String, Object> body, HttpSession session) {
        UserTokenVO token = verifyStudent(session);
        Integer itemId = (Integer) body.get("itemId");
        Integer type = (Integer) body.get("type");
        String answer = (String) body.get("answer");
        exerciseService.answerQuestion(token.getUserId(), itemId, type, answer);
        return Result.ok();
    }
}
