package labex.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import labex.common.Result;
import labex.dto.LoginRequest;
import labex.dto.SessionUtil;
import labex.dto.UserTokenVO;
import labex.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<UserTokenVO> login(@RequestBody LoginRequest req, HttpServletRequest request) {
        UserTokenVO token = authService.login(req, request);
        SessionUtil.setUserToken(request.getSession(true), token);
        return Result.ok(token);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        SessionUtil.removeUserToken(session);
        session.invalidate();
        return Result.ok();
    }

    @GetMapping("/current")
    public Result<UserTokenVO> current(HttpSession session) {
        UserTokenVO token = SessionUtil.getUserToken(session);
        return Result.ok(token);
    }
}
