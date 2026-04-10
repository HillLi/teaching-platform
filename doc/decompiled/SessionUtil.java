// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.common.SessionUtil
// ============================================================
package labex.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import labex.model.UserToken;

/**
 * Utility class for accessing session-scoped data without needing
 * an explicit HttpServletRequest parameter.
 *
 * Uses Spring's RequestContextHolder to obtain the current request
 * and its associated session.
 *
 * Purpose: Convenience methods to get/set the UserToken and other
 * session attributes from anywhere in the application layer.
 */
public class SessionUtil {

    public SessionUtil() {
    }

    /**
     * Get the current HTTP session from the request context.
     */
    public static HttpSession getSession() {
        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return attrs.getRequest().getSession(true);
        }
        return null;
    }

    /**
     * Get the current HttpServletRequest from the request context.
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            return attrs.getRequest();
        }
        return null;
    }

    /**
     * Retrieve the UserToken from the current session.
     * @return UserToken or null if not logged in
     */
    public static UserToken getUserToken() {
        HttpSession session = getSession();
        if (session != null) {
            return (UserToken) session.getAttribute("userToken");
        }
        return null;
    }

    /**
     * Store a UserToken in the current session.
     */
    public static void setUserToken(UserToken token) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute("userToken", token);
        }
    }

    /**
     * Invalidate the current session (logout).
     */
    public static void invalidate() {
        HttpSession session = getSession();
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Get the remote IP address of the current request.
     */
    public static String getRemoteIp() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            return request.getRemoteAddr();
        }
        return null;
    }
}
