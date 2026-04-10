// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.controller.StudentController
// Handles all student-side functionality
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import labex.common.LabexContext;
import labex.common.SessionUtil;
import labex.model.UserToken;
import labex.service.StudentService;
import labex.service.QuestionService;

/**
 * StudentController handles all student-facing operations.
 *
 * URL Mappings (all require authenticated student via SecurityInterceptor):
 *
 *   Student Home:
 *     GET /student/home.do -> s_home.jsp
 *
 *   Experiments:
 *     GET  /student/experiment/list.do      -> s_experiment_list.jsp (list available experiments)
 *     GET  /student/experiment/item/list.do  -> s_experiment_item_list.jsp (list items in an experiment)
 *     GET  /student/experiment/item/fill.do  -> s_experiment_item_fill.jsp (fill in answer)
 *     POST /student/experiment/item/save.do  -> save answer (AJAX, auto-save)
 *
 *   Lectures:
 *     GET /student/lecture/list.do -> s_lecture_list.jsp
 *
 *   Password:
 *     GET  /student/password.do       -> s_password.jsp (change password form)
 *     POST /student/password/save.do  -> update password
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private LabexContext labexContext;

    public StudentController() {
    }

    // ---- Student Home ----

    /**
     * Show student home page with experiment scores summary.
     */
    @RequestMapping(value = "/home.do", method = RequestMethod.GET)
    public ModelAndView home() {
        UserToken token = SessionUtil.getUserToken();
        ModelAndView mav = new ModelAndView("s_home");
        mav.addObject("scores", studentService.getStudentExperimentScore(token.getUserId()));
        return mav;
    }

    // ---- Experiment Operations ----

    /**
     * List all available experiments with student's scores.
     */
    @RequestMapping(value = "/experiment/list.do", method = RequestMethod.GET)
    public ModelAndView experimentList() {
        UserToken token = SessionUtil.getUserToken();
        ModelAndView mav = new ModelAndView("s_experiment_list");
        mav.addObject("experiments",
                studentService.getStudentExperimentScore(token.getUserId()));
        return mav;
    }

    /**
     * List items for a specific experiment, showing the student's answers
     * and scores if already filled.
     */
    @RequestMapping(value = "/experiment/item/list.do", method = RequestMethod.GET)
    public ModelAndView experimentItemList(
            @RequestParam("experimentId") int experimentId) {
        UserToken token = SessionUtil.getUserToken();
        ModelAndView mav = new ModelAndView("s_experiment_item_list");
        mav.addObject("items", studentService.getStudentExperimentItems(
                token.getUserId(), experimentId));
        mav.addObject("experimentId", experimentId);
        return mav;
    }

    /**
     * Show the fill-in form for a specific experiment item.
     */
    @RequestMapping(value = "/experiment/item/fill.do", method = RequestMethod.GET)
    public ModelAndView experimentItemFill(
            @RequestParam("itemId") int itemId,
            @RequestParam("experimentId") int experimentId) {
        UserToken token = SessionUtil.getUserToken();
        ModelAndView mav = new ModelAndView("s_experiment_item_fill");
        mav.addObject("item", studentService.getExperimentItem(itemId));
        mav.addObject("itemId", itemId);
        mav.addObject("experimentId", experimentId);
        return mav;
    }

    /**
     * Save student's answer for an experiment item.
     *
     * Supports AJAX calls for auto-save functionality.
     * The auto-save timer runs every ${page.autosavetime} ms (600000 = 10 min).
     *
     * @return JSON response or redirect
     */
    @RequestMapping(value = "/experiment/item/save.do", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> experimentItemSave(
            @RequestParam("itemId") int itemId,
            @RequestParam("content") String content,
            @RequestParam(value = "experimentId", required = false) Integer experimentId) {
        UserToken token = SessionUtil.getUserToken();
        studentService.saveStudentItem(token.getUserId(), itemId, content);
        return Map.of("success", true);
    }

    // ---- Lectures ----

    /**
     * List available lectures for the student.
     */
    @RequestMapping(value = "/lecture/list.do", method = RequestMethod.GET)
    public ModelAndView lectureList() {
        ModelAndView mav = new ModelAndView("s_lecture_list");
        mav.addObject("lectures", studentService.getLectureList());
        return mav;
    }

    // ---- Password Management ----

    /**
     * Show change password form.
     */
    @RequestMapping(value = "/password.do", method = RequestMethod.GET)
    public ModelAndView password() {
        return new ModelAndView("s_password");
    }

    /**
     * Save new password.
     */
    @RequestMapping(value = "/password/save.do", method = RequestMethod.POST)
    public ModelAndView passwordSave(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            HttpServletRequest request) {
        UserToken token = SessionUtil.getUserToken();

        boolean success = studentService.changePassword(
                token.getUserId(), oldPassword, newPassword);

        ModelAndView mav = new ModelAndView("s_password");
        if (success) {
            mav.addObject("message", "密码修改成功");
        } else {
            mav.addObject("message", "原密码错误");
        }
        return mav;
    }
}
