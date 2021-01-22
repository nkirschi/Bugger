package tech.bugger.control.exception;

import java.io.Serial;

/**
 * Exception indicating that the requested content was not found.
 */
public class Error404Exception extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2933389155700805148L;

    /**
     * Constructs an {@link Error404Exception} with no detail message.
     */
    public Error404Exception() {
        super();
    }

    /**
     * Constructs a new {@link Error404Exception} with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A {@code null} value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public Error404Exception(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link Error404Exception} with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public Error404Exception(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@link Error404Exception} with the specified cause and the detail message of {@code cause}.
     * This constructor is useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public Error404Exception(final Throwable cause) {
        super(cause);
    }

}
