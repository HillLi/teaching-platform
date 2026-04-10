// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.common.exception.LoginInvalidException
// Thrown when session is invalid/expired
// ============================================================
package labex.common.exception;

/**
 * Thrown when the session has expired or is invalid (no UserToken found).
 *
 * Corresponds to i18n message key: login.invalid = "会话失效，请重新登录！"
 * (Session expired, please log in again!)
 *
 * Mapped in spring-servlet.xml:
 *   -> redirect:/index.do?relogin=invalid
 *
 * Thrown by SecurityInterceptor when session has no valid UserToken.
 */
public class LoginInvalidException extends AuthenticationException {

    public LoginInvalidException() {
        super("login.invalid");
    }

    public LoginInvalidException(String message) {
        super(message);
    }
}
