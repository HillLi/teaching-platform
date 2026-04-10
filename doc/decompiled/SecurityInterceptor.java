// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.common.SecurityInterceptor
// Registered in spring-servlet.xml as MVC interceptor
// ============================================================
package labex.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import labex.common.exception.*;

/**
 * Spring MVC interceptor that enforces authentication for all *.do URLs.
 *
 * Configured in spring-servlet.xml:
 *   <mvc:interceptor>
 *     <mvc:mapping path="/**.do"/>
 *     <mvc:exclude-mapping path="/index.do"/>
 *     <mvc:exclude-mapping path="/login.do"/>
 *     <bean class="labex.common.SecurityInterceptor"/>
 *   </mvc:interceptor>
 *
 * Excluded paths: /index.do (login page), /login.do (login handler)
 *
 * Purpose: Checks that a valid UserToken exists in the session before
 * allowing the request to proceed. If not, throws an appropriate
 * AuthenticationException subclass.
 *
 * Exception mappings in spring-servlet.xml:
 *   LoginIpException       -> redirect:/index.do?relogin=iperror
 *   LoginAccountException  -> redirect:/index.do?relogin=accounterror
 *   LoginInvalidException  -> redirect:/index.do?relogin=invalid
 */
public class SecurityInterceptor implements HandlerInterceptor {

    public SecurityInterceptor() {
    }

    /**
     * Pre-handle: Called before the controller method is invoked.
     *
     * Checks session for a valid UserToken. Validates:
     *   1. Token exists (not null) -> if null, session expired/invalid
     *   2. Token IP matches current request IP (anti-session-hijacking)
     *   3. Account status is valid (not locked/disabled)
     *
     * @return true if the request should proceed, false otherwise
     * @throws LoginInvalidException if session token is missing
     * @throws LoginIpException if IP address changed
     * @throws LoginAccountException if account has issues
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new LoginInvalidException();
        }

        UserToken token = (UserToken) session.getAttribute("userToken");

        if (token == null) {
            throw new LoginInvalidException();
        }

        // Validate IP address matches the session's bound IP
        String currentIp = request.getRemoteAddr();
        if (!currentIp.equals(token.getIp())) {
            throw new LoginIpException();
        }

        // Validate account state (error count, locked state)
        // The student table has `error` and `state` fields
        if (token.isAccountLocked()) {
            throw new LoginAccountException();
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) throws Exception {
        // No post-processing needed
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) throws Exception {
        // No cleanup needed
    }
}
