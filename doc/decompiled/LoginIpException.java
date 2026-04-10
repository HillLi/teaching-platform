// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.common.exception.LoginIpException
// Thrown when IP address mismatch detected (session hijacking prevention)
// ============================================================
package labex.common.exception;

/**
 * Thrown when the request IP does not match the IP stored in the UserToken.
 * This prevents session hijacking by binding a session to an IP address.
 *
 * Corresponds to i18n message key: login.iperror = "您的机器是否登录了多个账号？"
 * (Has your machine logged in with multiple accounts?)
 *
 * Mapped in spring-servlet.xml:
 *   -> redirect:/index.do?relogin=iperror
 *
 * Thrown by SecurityInterceptor when IP validation fails.
 */
public class LoginIpException extends AuthenticationException {

    public LoginIpException() {
        super("login.iperror");
    }

    public LoginIpException(String message) {
        super(message);
    }
}
