package tech.bugger.business.exception;

import java.io.Serial;

/**
 * Exception indicating an image is corrupted.
 */
public class CorruptImageException extends Exception {

    @Serial
    private static final long serialVersionUID = 1900286228042129388L;

    /**
     * Constructs a {@link CorruptImageException} with no detail message.
     */
    public CorruptImageException() {
        super();
    }

    /**
     * Constructs a {@link CorruptImageException} with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause   The cause for this particular exception.
     */
    public CorruptImageException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@link CorruptImageException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public CorruptImageException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and the detail message of {@code cause}. This constructor is
     * useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public CorruptImageException(final Throwable cause) {
        super(cause);
    }

}
