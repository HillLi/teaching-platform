// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.controller.QuestionController
// Handles question bank management
// ============================================================
package labex.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import labex.service.QuestionService;

/**
 * QuestionController handles the question bank CRUD operations.
 *
 * URL Mappings:
 *   GET  /question/list.do     -> question/t_question_list.jsp
 *   GET  /question/add.do      -> question/t_question_add.jsp
 *   GET  /question/edit.do     -> question/t_question_edit.jsp
 *   POST /question/save.do     -> save question
 *
 * Question types (from t_question_type):
 *   1=填空(fill blank), 2=单选(single choice), 3=多选(multi choice),
 *   4=判断(true/false), 5=简答(short answer), 6=编程(programming),
 *   7=综合(comprehensive)
 *
 * Questions are used in exam papers (t_paper -> t_paper_question).
 */
@Controller
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    public QuestionController() {
    }

    /**
     * List all questions, optionally filtered by type.
     */
    @RequestMapping(value = "/list.do", method = RequestMethod.GET)
    public ModelAndView list(
            @RequestParam(value = "type", required = false) Integer type) {
        ModelAndView mav = new ModelAndView("question/t_question_list");
        mav.addObject("questions", questionService.getQuestionList(type));
        mav.addObject("types", questionService.getQuestionTypes());
        return mav;
    }

    /**
     * Show add question form.
     */
    @RequestMapping(value = "/add.do", method = RequestMethod.GET)
    public ModelAndView add() {
        ModelAndView mav = new ModelAndView("question/t_question_add");
        mav.addObject("types", questionService.getQuestionTypes());
        return mav;
    }

    /**
     * Show edit question form.
     */
    @RequestMapping(value = "/edit.do", method = RequestMethod.GET)
    public ModelAndView edit(@RequestParam("id") int id) {
        ModelAndView mav = new ModelAndView("question/t_question_edit");
        mav.addObject("question", questionService.getQuestion(id));
        mav.addObject("types", questionService.getQuestionTypes());
        return mav;
    }

    /**
     * Save a question (create or update).
     */
    @RequestMapping(value = "/save.do", method = RequestMethod.POST)
    public ModelAndView save(
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam("question") String question,
            @RequestParam("answer") String answer,
            @RequestParam("type") int type) {
        questionService.saveQuestion(id, question, answer, type);
        return new ModelAndView("redirect:/question/list.do");
    }
}
