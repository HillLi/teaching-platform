// ============================================================
// Reconstructed from: labex.war bytecode analysis
// Class: labex.common.exception.AuthenticationException
// Base exception for all login/authentication failures
// ============================================================
package labex.common.exception;

/**
 * Base authentication exception. All login-related exceptions
 * extend this class.
 *
 * Used by SecurityInterceptor and the exception resolver in
 * spring-servlet.xml to handle authentication failures.
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super();
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
