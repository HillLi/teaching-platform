package labex.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import labex.common.Result;
import labex.dto.ScoreDTO;
import labex.dto.UserTokenVO;
import labex.dto.SessionUtil;
import labex.entity.*;
import labex.service.TeacherService;
import labex.service.LogService;
import labex.entity.StudentLog;
import labex.entity.SysLog;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherService teacherService;
    private final LogService logService;

    @Value("${labex.upload.lecture-path}")
    private String lecturePath;

    public TeacherController(TeacherService teacherService, LogService logService) {
        this.teacherService = teacherService;
        this.logService = logService;
    }

    private void verifyTeacher(HttpSession session) {
        UserTokenVO token = SessionUtil.getUserToken(session);
        if (token.getUserType() != 0) {
            throw new labex.common.BusinessException("无权限访问");
        }
    }

    // ===== Class Management =====

    @GetMapping("/classes")
    public Result<List<Clazz>> listClasses(HttpSession session) {
        verifyTeacher(session);
        return Result.ok(teacherService.listClasses());
    }

    @GetMapping("/classes/{no}")
    public Result<Clazz> getClass(@PathVariable String no, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(teacherService.getClassByNo(no));
    }

    @PostMapping("/classes")
    public Result<Void> addClass(@RequestBody Clazz clazz, HttpSession session) {
        verifyTeacher(session);
        teacherService.addClass(clazz);
        return Result.ok();
    }

    @PutMapping("/classes/{no}")
    public Result<Void> updateClass(@PathVariable String no, @RequestBody Clazz clazz, HttpSession session) {
        verifyTeacher(session);
        clazz.setNo(no);
        teacherService.updateClass(clazz);
        return Result.ok();
    }

    @DeleteMapping("/classes/{no}")
    public Result<Void> deleteClass(@PathVariable String no, HttpSession session) {
        verifyTeacher(session);
        teacherService.deleteClass(no);
        return Result.ok();
    }

    // ===== Student Management =====

    @GetMapping("/students")
    public Result<Page<Student>> listStudents(
            @RequestParam(required = false) String clazzNo,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpSession session) {
        verifyTeacher(session);
        return Result.ok(teacherService.listStudents(clazzNo, pageNum, pageSize));
    }

    @GetMapping("/students/{id}")
    public Result<Student> getStudent(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(teacherService.getStudentById(id));
    }

    @PostMapping("/students")
    public Result<Void> addStudent(@RequestBody Student student, HttpSession session) {
        verifyTeacher(session);
        teacherService.addStudent(student);
        return Result.ok();
    }

    @PutMapping("/students/{id}")
    public Result<Void> updateStudent(@PathVariable Integer id, @RequestBody Student student, HttpSession session) {
        verifyTeacher(session);
        student.setStudentId(id);
        teacherService.updateStudent(student);
        return Result.ok();
    }

    @DeleteMapping("/students/{id}")
    public Result<Void> deleteStudent(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        teacherService.deleteStudent(id);
        return Result.ok();
    }

    @PostMapping("/students/import")
    public Result<Void> importStudents(@RequestBody List<Student> students, HttpSession session) {
        verifyTeacher(session);
        teacherService.importStudents(students);
        return Result.ok();
    }

    // ===== Experiment Management =====

    @GetMapping("/experiments")
    public Result<List<Experiment>> listExperiments(HttpSession session) {
        verifyTeacher(session);
        return Result.ok(teacherService.listExperiments());
    }

    @GetMapping("/experiments/{id}")
    public Result<Experiment> getExperiment(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(teacherService.getExperimentById(id));
    }

    @PostMapping("/experiments")
    public Result<Void> addExperiment(@RequestBody Experiment exp, HttpSession session) {
        verifyTeacher(session);
        teacherService.addExperiment(exp);
        return Result.ok();
    }

    @PutMapping("/experiments/{id}")
    public Result<Void> updateExperiment(@PathVariable Integer id, @RequestBody Experiment exp, HttpSession session) {
        verifyTeacher(session);
        exp.setExperimentId(id);
        teacherService.updateExperiment(exp);
        return Result.ok();
    }

    @DeleteMapping("/experiments/{id}")
    public Result<Void> deleteExperiment(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        teacherService.deleteExperiment(id);
        return Result.ok();
    }

    // ===== Experiment Items =====

    @GetMapping("/experiments/{id}/items")
    public Result<List<ExperimentItem>> listItems(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(teacherService.getExperimentItems(id));
    }

    @PostMapping("/experiments/{id}/items")
    public Result<Void> addItem(@PathVariable Integer id, @RequestBody ExperimentItem item, HttpSession session) {
        verifyTeacher(session);
        item.setExperimentId(id);
        teacherService.addExperimentItem(item);
        return Result.ok();
    }

    @PutMapping("/experiments/items/{itemId}")
    public Result<Void> updateItem(@PathVariable Integer itemId, @RequestBody ExperimentItem item, HttpSession session) {
        verifyTeacher(session);
        item.setExperimentItemId(itemId);
        teacherService.updateExperimentItem(item);
        return Result.ok();
    }

    @DeleteMapping("/experiments/items/{itemId}")
    public Result<Void> deleteItem(@PathVariable Integer itemId, HttpSession session) {
        verifyTeacher(session);
        teacherService.deleteExperimentItem(itemId);
        return Result.ok();
    }

    @PutMapping("/experiments/items/{itemId}/answer")
    public Result<Void> setItemAnswer(@PathVariable Integer itemId, @RequestBody Map<String, String> body, HttpSession session) {
        verifyTeacher(session);
        teacherService.setItemAnswer(itemId, body.get("answer"));
        return Result.ok();
    }

    // ===== Grading / Reports =====

    @GetMapping("/reports/students")
    public Result<Page<Student>> listSubmittedStudents(
            @RequestParam Integer experimentId,
            @RequestParam(required = false) String clazzNo,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpSession session) {
        verifyTeacher(session);
        return Result.ok(teacherService.listSubmittedStudents(experimentId, clazzNo, pageNum, pageSize));
    }

    @GetMapping("/reports/students/{studentId}/experiments/{expId}")
    public Result<List<Map<String, Object>>> getStudentReport(
            @PathVariable Integer studentId, @PathVariable Integer expId, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(teacherService.getStudentExperimentDetail(studentId, expId));
    }

    @PostMapping("/reports/scores")
    public Result<Void> submitScore(@RequestBody ScoreDTO dto, HttpSession session) {
        verifyTeacher(session);
        teacherService.submitScore(dto.getStudentItemId(), dto.getScore());
        return Result.ok();
    }

    @GetMapping("/reports/classes/{clazzNo}/experiments/{expId}")
    public Result<List<Map<String, Object>>> getClassScore(
            @PathVariable String clazzNo, @PathVariable Integer expId, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(teacherService.getClazzScoreSummary(clazzNo, expId));
    }

    // ===== Lectures =====

    @GetMapping("/lectures")
    public Result<List<Lecture>> listLectures(HttpSession session) {
        verifyTeacher(session);
        return Result.ok(teacherService.listLectures());
    }

    @PostMapping("/lectures")
    public Result<Void> uploadLecture(@RequestParam String name,
                                       @RequestParam Integer type,
                                       @RequestParam MultipartFile file,
                                       HttpSession session) throws IOException {
        verifyTeacher(session);
        String originalName = file.getOriginalFilename();
        String fileType = originalName != null ?
                originalName.substring(originalName.lastIndexOf(".") + 1) : "";

        Lecture lecture = new Lecture();
        lecture.setLectureName(name);
        lecture.setLectureType(type);
        lecture.setLectureFiletype(fileType);
        teacherService.addLecture(lecture);

        // Save file
        File dir = new File(lecturePath);
        if (!dir.exists()) dir.mkdirs();
        file.transferTo(new File(dir, lecture.getLectureId() + "." + fileType));

        return Result.ok();
    }

    @PutMapping("/lectures/{id}")
    public Result<Void> updateLecture(@PathVariable Integer id, @RequestBody Lecture lecture, HttpSession session) {
        verifyTeacher(session);
        lecture.setLectureId(id);
        teacherService.updateLecture(lecture);
        return Result.ok();
    }

    @DeleteMapping("/lectures/{id}")
    public Result<Void> deleteLecture(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        teacherService.deleteLecture(id);
        return Result.ok();
    }

    // ===== Dashboard =====

    @GetMapping("/dashboard/stats")
    public Result<Map<String, Object>> dashboardStats(HttpSession session) {
        verifyTeacher(session);
        return Result.ok(teacherService.getDashboardStats());
    }

    // ===== Logs =====

    @GetMapping("/logs")
    public Result<Page<SysLog>> listLogs(
            @RequestParam(required = false) String account,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpSession session) {
        verifyTeacher(session);
        return Result.ok(logService.listSysLogs(account, type, pageNum, pageSize));
    }

    @GetMapping("/students/{id}/logs")
    public Result<List<StudentLog>> getStudentLogs(@PathVariable Integer id, HttpSession session) {
        verifyTeacher(session);
        return Result.ok(logService.listStudentLogs(id));
    }
}
