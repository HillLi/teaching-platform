// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.common.exception.LoginForbiddenException
// Thrown when account is disabled/forbidden
// ============================================================
package labex.common.exception;

/**
 * Thrown when a user account has been disabled or forbidden from logging in.
 *
 * Corresponds to i18n message key: login.forbidden = "账号被禁用！"
 * (Account is disabled!)
 *
 * In the database, t_student.state field controls account status.
 * This is NOT mapped to a redirect in spring-servlet.xml, so it is
 * handled directly by the login controller.
 */
public class LoginForbiddenException extends AuthenticationException {

    public LoginForbiddenException() {
        super("login.forbidden");
    }

    public LoginForbiddenException(String message) {
        super(message);
    }
}
