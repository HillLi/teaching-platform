// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.controller.ExamController
// Handles exam management functionality
// ============================================================
package labex.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import labex.model.UserToken;
import labex.common.SessionUtil;
import labex.service.QuestionService;
import labex.service.StudentService;

/**
 * ExamController handles exam-related operations.
 *
 * The t_exam table structure:
 *   id (int PK), description (varchar 30), duration (int, minutes),
 *   time (datetime), flag (bit, exam open/closed)
 *
 * URL Mappings:
 *   GET  /exam/list.do          -> list exams
 *   GET  /exam/take.do          -> student takes an exam
 *   POST /exam/submit.do        -> submit exam answers
 *   POST /exam/answerQuestion.do -> answer individual question (AJAX)
 *   GET  /exam/result.do        -> view exam results
 *
 * Exam answers are stored in t_student_question (student_id, question_id, answer).
 * The stored procedure `answerQuestion` handles upsert logic for answers.
 */
@Controller
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private StudentService studentService;

    public ExamController() {
    }

    /**
     * List available exams.
     * Students see only open exams (flag=true), teachers see all.
     */
    @RequestMapping(value = "/list.do", method = RequestMethod.GET)
    public ModelAndView list() {
        UserToken token = SessionUtil.getUserToken();
        ModelAndView mav = new ModelAndView("t_exam_list");
        // mav.addObject("exams", ...);
        return mav;
    }

    /**
     * Show exam questions for a student to take.
     */
    @RequestMapping(value = "/take.do", method = RequestMethod.GET)
    public ModelAndView take(@RequestParam("examId") int examId) {
        UserToken token = SessionUtil.getUserToken();
        ModelAndView mav = new ModelAndView("s_exam_take");
        // mav.addObject("paper", ...);
        // mav.addObject("questions", ...);
        return mav;
    }

    /**
     * Answer a single exam question (AJAX endpoint).
     * Calls the stored procedure `answerQuestion(studentId, itemId, type, studentAnswer)`.
     */
    @RequestMapping(value = "/answerQuestion.do", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> answerQuestion(
            @RequestParam("itemId") int itemId,
            @RequestParam("type") int type,
            @RequestParam("answer") String answer) {
        UserToken token = SessionUtil.getUserToken();
        // Calls the MySQL stored procedure: answerQuestion
        questionService.answerQuestion(token.getUserId(), itemId, type, answer);
        return Map.of("success", true);
    }

    /**
     * Submit the exam for grading.
     */
    @RequestMapping(value = "/submit.do", method = RequestMethod.POST)
    public ModelAndView submit(
            @RequestParam("examId") int examId,
            HttpServletRequest request) {
        UserToken token = SessionUtil.getUserToken();
        // Process all answers and calculate score
        return new ModelAndView("redirect:/exam/result.do?examId=" + examId);
    }

    /**
     * View exam results.
     */
    @RequestMapping(value = "/result.do", method = RequestMethod.GET)
    public ModelAndView result(@RequestParam("examId") int examId) {
        UserToken token = SessionUtil.getUserToken();
        ModelAndView mav = new ModelAndView("s_exam_result");
        return mav;
    }
}
