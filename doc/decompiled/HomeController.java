// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.controller.HomeController
// Handles login, logout, and the index page
// ============================================================
package labex.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import labex.common.LabexContext;
import labex.common.SessionUtil;
import labex.common.exception.*;
import labex.model.UserToken;
import labex.service.LoginService;

/**
 * HomeController handles the index/login page and authentication flow.
 *
 * URL Mappings:
 *   GET  /index.do   -> Show login page (login.jsp)
 *   POST /login.do   -> Process login authentication
 *   GET  /logout.do  -> Invalidate session and redirect to login
 *   GET  /info.do    -> Show info page (info.jsp)
 *
 * The SecurityInterceptor excludes /index.do and /login.do from
 * authentication checks (see spring-servlet.xml).
 */
@Controller
public class HomeController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private LabexContext labexContext;

    public HomeController() {
    }

    /**
     * Show the login/index page.
     *
     * Handles the "relogin" query parameter set by the exception resolver:
     *   relogin=invalid       -> "Session expired" message
     *   relogin=iperror       -> "IP mismatch" message
     *   relogin=accounterror  -> "Account abnormal" message
     */
    @RequestMapping(value = "/index.do", method = RequestMethod.GET)
    public ModelAndView index(
            @RequestParam(value = "relogin", required = false) String relogin,
            HttpServletRequest request) {

        ModelAndView mav = new ModelAndView("login");

        if (relogin != null) {
            String messageKey = null;
            if ("invalid".equals(relogin)) {
                messageKey = "login.invalid";
            } else if ("iperror".equals(relogin)) {
                messageKey = "login.iperror";
            } else if ("accounterror".equals(relogin)) {
                messageKey = "login.accounterror";
            }
            if (messageKey != null) {
                mav.addObject("message", labexContext.getMessage(messageKey));
            }
        }

        return mav;
    }

    /**
     * Process login form submission.
     *
     * Validates credentials via LoginService. On success, creates a UserToken
     * in the session and redirects to the appropriate home page:
     *   - Teacher -> /teacher/home.do (t_home.jsp)
     *   - Student -> /student/home.do (s_home.jsp)
     *
     * On failure, returns to login.jsp with an error message.
     *
     * @param account  Login account (student number or teacher account)
     * @param password Login password
     * @param type     User type: 0=teacher, 1=student
     */
    @RequestMapping(value = "/login.do", method = RequestMethod.POST)
    public ModelAndView login(
            @RequestParam("account") String account,
            @RequestParam("password") String password,
            @RequestParam("type") int type,
            HttpServletRequest request) {

        try {
            UserToken token = loginService.login(account, password, type,
                    request.getRemoteAddr());

            // Store token in session
            SessionUtil.setUserToken(token);

            // Log successful login via LoginService
            loginService.logLogin(token.getAccount(), 1, "登录成功",
                    token.getIp());

            // Redirect based on user type
            if (token.isTeacher()) {
                return new ModelAndView("redirect:/teacher/home.do");
            } else {
                return new ModelAndView("redirect:/student/home.do");
            }
        } catch (LoginFailedException e) {
            // Log failed attempt
            loginService.logLogin(account, 2, "登录失败",
                    request.getRemoteAddr());
            ModelAndView mav = new ModelAndView("login");
            mav.addObject("message", labexContext.getMessage("login.failed"));
            mav.addObject("account", account);
            mav.addObject("type", type);
            return mav;
        } catch (LoginForbiddenException e) {
            ModelAndView mav = new ModelAndView("login");
            mav.addObject("message", labexContext.getMessage("login.forbidden"));
            mav.addObject("account", account);
            mav.addObject("type", type);
            return mav;
        }
    }

    /**
     * Logout: invalidate session and redirect to login page.
     */
    @RequestMapping(value = "/logout.do", method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest request) {
        UserToken token = SessionUtil.getUserToken();
        if (token != null) {
            loginService.logLogin(token.getAccount(), 1, "您已成功注销！",
                    token.getIp());
        }
        SessionUtil.invalidate();
        ModelAndView mav = new ModelAndView("login");
        mav.addObject("message", labexContext.getMessage("logout.succeed"));
        return mav;
    }

    /**
     * Info page - displays system information or messages.
     */
    @RequestMapping(value = "/info.do", method = RequestMethod.GET)
    public ModelAndView info() {
        return new ModelAndView("info");
    }
}
