package labex.dto;

import javax.servlet.http.HttpSession;

public class SessionUtil {
    public static final String USER_TOKEN_KEY = "userToken";

    public static UserTokenVO getUserToken(HttpSession session) {
        return (UserTokenVO) session.getAttribute(USER_TOKEN_KEY);
    }

    public static void setUserToken(HttpSession session, UserTokenVO token) {
        session.setAttribute(USER_TOKEN_KEY, token);
    }

    public static void removeUserToken(HttpSession session) {
        session.removeAttribute(USER_TOKEN_KEY);
    }
}
