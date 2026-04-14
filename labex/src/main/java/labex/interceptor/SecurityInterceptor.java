package labex.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import labex.common.Result;
import labex.dto.SessionUtil;
import labex.dto.UserTokenVO;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            writeUnauthorized(response, "未登录");
            return false;
        }

        UserTokenVO token = SessionUtil.getUserToken(session);
        if (token == null) {
            writeUnauthorized(response, "会话已过期");
            return false;
        }

        // IP check - update on change
        String currentIp = request.getRemoteAddr();
        token.setIp(currentIp);

        // Account lockout check
        if (token.isAccountLocked()) {
            writeUnauthorized(response, "账户异常，请联系管理员");
            return false;
        }

        return true;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, message)));
    }
}
