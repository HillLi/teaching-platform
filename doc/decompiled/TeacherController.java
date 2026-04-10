// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.controller.TeacherController
// Handles all teacher-side functionality
// ============================================================
package labex.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import labex.common.SessionUtil;
import labex.model.UserToken;
import labex.service.TeacherService;
import labex.service.StudentService;
import labex.service.QuestionService;

/**
 * TeacherController handles all teacher-side operations.
 *
 * URL Mappings (all require authenticated teacher via SecurityInterceptor):
 *
 *   Teacher Home:
 *     GET /teacher/home.do -> t_home.jsp
 *
 *   Experiment Management:
 *     GET  /teacher/experiment/list.do     -> t_experiment_list.jsp
 *     GET  /teacher/experiment/edit.do     -> t_experiment_edit.jsp (create/edit form)
 *     POST /teacher/experiment/save.do     -> save experiment
 *     GET  /teacher/experiment/item/list.do -> t_experiment_item_list.jsp (items for an experiment)
 *     GET  /teacher/experiment/item/edit.do -> t_experiment_item_edit.jsp
 *     POST /teacher/experiment/item/save.do -> save experiment item
 *     GET  /teacher/experiment/item/answer.do -> t_experiment_item_answer.jsp
 *
 *   Student Management:
 *     GET  /teacher/student/list.do       -> t_student_list.jsp
 *     GET  /teacher/student/edit.do       -> t_student_edit.jsp
 *     POST /teacher/student/save.do       -> save student
 *     POST /teacher/student/import.do     -> import students from file
 *
 *   Class (Clazz) Management:
 *     GET  /teacher/clazz/list.do         -> t_clazz_list.jsp
 *     GET  /teacher/clazz/edit.do         -> t_clazz_edit.jsp
 *     POST /teacher/clazz/save.do         -> save class
 *
 *   Scoring/Reports:
 *     GET  /teacher/report/list.do         -> t_student_report_list.jsp
 *     GET  /teacher/report/clazz/view.do   -> t_student_report_clazz_view.jsp
 *     GET  /teacher/report/view.do         -> t_student_report_view.jsp
 *     GET  /teacher/report/mark.do         -> t_student_report_mark.jsp (grade items)
 *     POST /teacher/report/score.do        -> save scores
 *     GET  /teacher/report/html.do         -> t_student_report_html.jsp
 *     GET  /teacher/report/jsp.do          -> t_student_report_jsp.jsp
 *
 *   Lecture Management:
 *     GET  /teacher/lecture/list.do        -> t_lecture_list.jsp
 *     GET  /teacher/lecture/edit.do        -> t_lecture_edit.jsp
 *     POST /teacher/lecture/save.do        -> save/upload lecture
 *
 *   Exercise Management:
 *     GET  /teacher/excercise/list.do      -> t_excercise_list.jsp
 *     GET  /teacher/excercise/edit.do      -> t_excercise_edit.jsp
 *     POST /teacher/excercise/save.do      -> save exercise
 */
@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private QuestionService questionService;

    public TeacherController() {
    }

    // ---- Teacher Home ----

    /**
     * Show teacher's home page with dashboard/summary info.
     */
    @RequestMapping(value = "/home.do", method = RequestMethod.GET)
    public ModelAndView home() {
        ModelAndView mav = new ModelAndView("t_home");
        // Add dashboard data: student counts, experiment stats, etc.
        return mav;
    }

    // ---- Experiment Management ----

    /**
     * List all experiments.
     */
    @RequestMapping(value = "/experiment/list.do", method = RequestMethod.GET)
    public ModelAndView experimentList() {
        ModelAndView mav = new ModelAndView("t_experiment_list");
        mav.addObject("experiments", teacherService.getExperimentList());
        return mav;
    }

    /**
     * Show experiment edit form (create new or edit existing).
     */
    @RequestMapping(value = "/experiment/edit.do", method = RequestMethod.GET)
    public ModelAndView experimentEdit(
            @RequestParam(value = "id", required = false) Integer id) {
        ModelAndView mav = new ModelAndView("t_experiment_edit");
        if (id != null) {
            mav.addObject("experiment", teacherService.getExperiment(id));
        }
        return mav;
    }

    /**
     * Save experiment (create or update).
     */
    @RequestMapping(value = "/experiment/save.do", method = RequestMethod.POST)
    public ModelAndView experimentSave(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam("no") int no,
            @RequestParam("name") String name,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "instructionType", required = false) String instructionType,
            @RequestParam(value = "requirement", required = false) String requirement,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "state", required = false) Integer state) {
        teacherService.saveExperiment(id, no, name, type, instructionType,
                requirement, content, state);
        return new ModelAndView("redirect:/teacher/experiment/list.do");
    }

    /**
     * List items for a specific experiment.
     */
    @RequestMapping(value = "/experiment/item/list.do", method = RequestMethod.GET)
    public ModelAndView experimentItemList(
            @RequestParam("experimentId") int experimentId) {
        ModelAndView mav = new ModelAndView("t_experiment_item_list");
        mav.addObject("items", teacherService.getExperimentItems(experimentId));
        mav.addObject("experimentId", experimentId);
        return mav;
    }

    /**
     * Show experiment item edit form.
     */
    @RequestMapping(value = "/experiment/item/edit.do", method = RequestMethod.GET)
    public ModelAndView experimentItemEdit(
            @RequestParam("experimentId") int experimentId,
            @RequestParam(value = "itemId", required = false) Integer itemId) {
        ModelAndView mav = new ModelAndView("t_experiment_item_edit");
        if (itemId != null) {
            mav.addObject("item", teacherService.getExperimentItem(itemId));
        }
        mav.addObject("experimentId", experimentId);
        return mav;
    }

    /**
     * Save an experiment item.
     */
    @RequestMapping(value = "/experiment/item/save.do", method = RequestMethod.POST)
    public ModelAndView experimentItemSave(
            @RequestParam(value = "itemId", required = false) Integer itemId,
            @RequestParam("experimentId") int experimentId,
            @RequestParam("no") int no,
            @RequestParam("name") String name,
            @RequestParam("type") int type,
            @RequestParam("content") String content,
            @RequestParam(value = "answer", required = false) String answer,
            @RequestParam(value = "score", required = false) Integer score,
            @RequestParam(value = "state", required = false) Integer state) {
        teacherService.saveExperimentItem(itemId, experimentId, no, name, type,
                content, answer, score, state);
        return new ModelAndView("redirect:/teacher/experiment/item/list.do?experimentId="
                + experimentId);
    }

    /**
     * Show the answer key for experiment items.
     */
    @RequestMapping(value = "/experiment/item/answer.do", method = RequestMethod.GET)
    public ModelAndView experimentItemAnswer(
            @RequestParam("experimentId") int experimentId) {
        ModelAndView mav = new ModelAndView("t_experiment_item_answer");
        mav.addObject("items", teacherService.getExperimentItems(experimentId));
        mav.addObject("experimentId", experimentId);
        return mav;
    }

    // ---- Student Management ----

    /**
     * List students, optionally filtered by class.
     */
    @RequestMapping(value = "/student/list.do", method = RequestMethod.GET)
    public ModelAndView studentList(
            @RequestParam(value = "clazzNo", required = false) String clazzNo) {
        ModelAndView mav = new ModelAndView("t_student_list");
        mav.addObject("students", studentService.getStudentList(clazzNo));
        mav.addObject("clazzList", studentService.getClazzList());
        return mav;
    }

    /**
     * Show student edit form.
     */
    @RequestMapping(value = "/student/edit.do", method = RequestMethod.GET)
    public ModelAndView studentEdit(
            @RequestParam(value = "id", required = false) Integer id) {
        ModelAndView mav = new ModelAndView("t_student_edit");
        if (id != null) {
            mav.addObject("student", studentService.getStudent(id));
        }
        mav.addObject("clazzList", studentService.getClazzList());
        return mav;
    }

    /**
     * Save student (create or update).
     */
    @RequestMapping(value = "/student/save.do", method = RequestMethod.POST)
    public ModelAndView studentSave(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam("no") String no,
            @RequestParam("name") String name,
            @RequestParam("password") String password,
            @RequestParam("clazzNo") String clazzNo) {
        studentService.saveStudent(id, no, name, password, clazzNo);
        return new ModelAndView("redirect:/teacher/student/list.do");
    }

    /**
     * Import students from an uploaded file (CSV/Excel).
     */
    @RequestMapping(value = "/student/import.do", method = RequestMethod.POST)
    public ModelAndView studentImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("clazzNo") String clazzNo) {
        studentService.importStudents(file, clazzNo);
        return new ModelAndView("redirect:/teacher/student/list.do?clazzNo=" + clazzNo);
    }

    // ---- Class Management ----

    @RequestMapping(value = "/clazz/list.do", method = RequestMethod.GET)
    public ModelAndView clazzList() {
        ModelAndView mav = new ModelAndView("t_clazz_list");
        mav.addObject("clazzList", studentService.getClazzList());
        return mav;
    }

    @RequestMapping(value = "/clazz/edit.do", method = RequestMethod.GET)
    public ModelAndView clazzEdit(
            @RequestParam(value = "no", required = false) String no) {
        ModelAndView mav = new ModelAndView("t_clazz_edit");
        if (no != null) {
            mav.addObject("clazz", studentService.getClazz(no));
        }
        return mav;
    }

    @RequestMapping(value = "/clazz/save.do", method = RequestMethod.POST)
    public ModelAndView clazzSave(
            @RequestParam("no") String no,
            @RequestParam(value = "memo", required = false) String memo,
            @RequestParam(value = "state", defaultValue = "1") int state) {
        studentService.saveClazz(no, memo, state);
        return new ModelAndView("redirect:/teacher/clazz/list.do");
    }

    // ---- Scoring / Reports ----

    /**
     * List experiment scores for students.
     */
    @RequestMapping(value = "/report/list.do", method = RequestMethod.GET)
    public ModelAndView reportList() {
        ModelAndView mav = new ModelAndView("t_student_report_list");
        mav.addObject("experiments", teacherService.getExperimentList());
        mav.addObject("clazzList", studentService.getClazzList());
        return mav;
    }

    /**
     * View class-level experiment scores.
     */
    @RequestMapping(value = "/report/clazz/view.do", method = RequestMethod.GET)
    public ModelAndView reportClazzView(
            @RequestParam("clazzNo") String clazzNo,
            @RequestParam("experimentId") int experimentId) {
        ModelAndView mav = new ModelAndView("t_student_report_clazz_view");
        mav.addObject("scores", teacherService.getClazzExperimentScore(clazzNo, experimentId));
        mav.addObject("clazzNo", clazzNo);
        mav.addObject("experimentId", experimentId);
        return mav;
    }

    /**
     * View individual student's experiment answers for scoring.
     */
    @RequestMapping(value = "/report/view.do", method = RequestMethod.GET)
    public ModelAndView reportView(
            @RequestParam("studentId") int studentId,
            @RequestParam("experimentId") int experimentId) {
        ModelAndView mav = new ModelAndView("t_student_report_view");
        mav.addObject("items", teacherService.getStudentExperimentItems(studentId, experimentId));
        mav.addObject("studentId", studentId);
        mav.addObject("experimentId", experimentId);
        return mav;
    }

    /**
     * Show the grading/scoring form for experiment items.
     */
    @RequestMapping(value = "/report/mark.do", method = RequestMethod.GET)
    public ModelAndView reportMark(
            @RequestParam("studentId") int studentId,
            @RequestParam("experimentId") int experimentId) {
        ModelAndView mav = new ModelAndView("t_student_report_mark");
        mav.addObject("items", teacherService.getStudentExperimentItems(studentId, experimentId));
        return mav;
    }

    /**
     * Save scores for student experiment items.
     */
    @RequestMapping(value = "/report/score.do", method = RequestMethod.POST)
    public ModelAndView reportScore(HttpServletRequest request) {
        // Parse item IDs and scores from request parameters
        teacherService.saveScores(request);
        return new ModelAndView("redirect:/teacher/report/list.do");
    }

    /**
     * Generate HTML report for student experiment results.
     */
    @RequestMapping(value = "/report/html.do", method = RequestMethod.GET)
    public ModelAndView reportHtml(
            @RequestParam("studentId") int studentId,
            @RequestParam("experimentId") int experimentId) {
        ModelAndView mav = new ModelAndView("t_student_report_html");
        mav.addObject("items", teacherService.getStudentExperimentItems(studentId, experimentId));
        return mav;
    }

    /**
     * Generate JSP-based report.
     */
    @RequestMapping(value = "/report/jsp.do", method = RequestMethod.GET)
    public ModelAndView reportJsp(
            @RequestParam(value = "clazzNo", required = false) String clazzNo,
            @RequestParam(value = "experimentId", required = false) Integer experimentId) {
        ModelAndView mav = new ModelAndView("t_student_report_jsp");
        if (clazzNo != null && experimentId != null) {
            mav.addObject("data", teacherService.getClazzExperimentAnswers(experimentId, clazzNo));
        }
        mav.addObject("clazzList", studentService.getClazzList());
        mav.addObject("experiments", teacherService.getExperimentList());
        return mav;
    }

    // ---- Lecture Management ----

    @RequestMapping(value = "/lecture/list.do", method = RequestMethod.GET)
    public ModelAndView lectureList() {
        ModelAndView mav = new ModelAndView("t_lecture_list");
        mav.addObject("lectures", teacherService.getLectureList());
        return mav;
    }

    @RequestMapping(value = "/lecture/edit.do", method = RequestMethod.GET)
    public ModelAndView lectureEdit(
            @RequestParam(value = "id", required = false) Integer id) {
        ModelAndView mav = new ModelAndView("t_lecture_edit");
        if (id != null) {
            mav.addObject("lecture", teacherService.getLecture(id));
        }
        return mav;
    }

    @RequestMapping(value = "/lecture/save.do", method = RequestMethod.POST)
    public ModelAndView lectureSave(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam("name") String name,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam("file") MultipartFile file) {
        teacherService.saveLecture(id, name, type, file);
        return new ModelAndView("redirect:/teacher/lecture/list.do");
    }

    // ---- Exercise Management (ex3) ----

    @RequestMapping(value = "/excercise/list.do", method = RequestMethod.GET)
    public ModelAndView excerciseList() {
        ModelAndView mav = new ModelAndView("t_excercise_list");
        mav.addObject("exercises", teacherService.getExcerciseList());
        return mav;
    }

    @RequestMapping(value = "/excercise/edit.do", method = RequestMethod.GET)
    public ModelAndView excerciseEdit(
            @RequestParam(value = "id", required = false) Integer id) {
        ModelAndView mav = new ModelAndView("t_excercise_edit");
        if (id != null) {
            mav.addObject("exercise", teacherService.getExcercise(id));
        }
        return mav;
    }

    @RequestMapping(value = "/excercise/save.do", method = RequestMethod.POST)
    public ModelAndView excerciseSave(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "no", required = false) Integer no,
            @RequestParam("name") String name,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "extype", required = false) Integer extype,
            @RequestParam(value = "description", required = false) String description) {
        teacherService.saveExcercise(id, no, name, type, extype, description);
        return new ModelAndView("redirect:/teacher/excercise/list.do");
    }
}
