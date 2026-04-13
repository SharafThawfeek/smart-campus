package Smart.Campus.demo.exception;

/**
 * Thrown when a scheduling conflict is detected (e.g., overlapping bookings).
 * Maps to HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
