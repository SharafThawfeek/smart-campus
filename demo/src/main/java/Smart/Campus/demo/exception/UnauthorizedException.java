package Smart.Campus.demo.exception;

/**
 * Thrown when a user tries an action they don't have permission to perform.
 * Maps to HTTP 403 Forbidden.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
