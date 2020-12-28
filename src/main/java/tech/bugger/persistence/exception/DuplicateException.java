package tech.bugger.persistence.exception;

import java.io.Serial;

/**
 * Exception indicating something already was there unexpectedly.
 */
public class DuplicateException extends Exception {
    @Serial
    private static final long serialVersionUID = 2481138688747787858L;

    /**
     * Constructs a {@link DuplicateException} with no detail message.
     */
    public DuplicateException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a {@link DuplicateException} with the specified detail message and cause.
     *
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause The cause for this particular exception.
     */
    public DuplicateException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a {@link DuplicateException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public DuplicateException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a new exception with the specified cause and the detail message of {@code cause}. This constructor is
     * useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public DuplicateException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
