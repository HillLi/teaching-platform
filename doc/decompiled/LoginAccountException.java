// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.common.exception.LoginAccountException
// Thrown when account has security issues
// ============================================================
package labex.common.exception;

/**
 * Thrown when the account has security issues (e.g., too many failed login
 * attempts, abnormal password state, or compromised account).
 *
 * Corresponds to i18n message key: login.accounterror = "账号异常，请尽快修改您的密码！"
 * (Account abnormal, please change your password as soon as possible!)
 *
 * Mapped in spring-servlet.xml:
 *   -> redirect:/index.do?relogin=accounterror
 */
public class LoginAccountException extends AuthenticationException {

    public LoginAccountException() {
        super("login.accounterror");
    }

    public LoginAccountException(String message) {
        super(message);
    }
}
