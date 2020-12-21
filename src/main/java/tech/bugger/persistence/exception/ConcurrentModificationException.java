package tech.bugger.persistence.exception;

import java.io.Serial;

/**
 * Exception indicating a race condition while editing some content.
 */
public class ConcurrentModificationException extends Exception {
    @Serial
    private static final long serialVersionUID = -7858198918413564101L;

    /**
     * Constructs a {@link ConcurrentModificationException} with no detail message.
     */
    public ConcurrentModificationException() {
        super();
    }

    /**
     * Constructs a {@link ConcurrentModificationException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public ConcurrentModificationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and the detail message of {@code cause}. This constructor is
     * useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public ConcurrentModificationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a {@link ConcurrentModificationException} with the specified detail message and cause.
     *
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause   The cause for this particular exception.
     */
    public ConcurrentModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
