// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.common.exception.LoginFailedException
// Thrown when username/password is incorrect
// ============================================================
package labex.common.exception;

/**
 * Thrown when login credentials (username/password) do not match.
 *
 * Corresponds to i18n message key: login.failed = "用户名或密码错误！"
 * (Username or password is incorrect!)
 *
 * This is NOT mapped to a redirect in spring-servlet.xml exception resolver,
 * meaning it is handled directly by the login controller and shown on the
 * login page (login.jsp).
 */
public class LoginFailedException extends AuthenticationException {

    public LoginFailedException() {
        super("login.failed");
    }

    public LoginFailedException(String message) {
        super(message);
    }
}
