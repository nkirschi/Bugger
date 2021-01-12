package tech.bugger.persistence.exception;

import java.io.Serial;

/**
 * Exception indicating something went wrong when trying to store something in the database.
 */
public class StoreException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1850502033299171433L;

    /**
     * Constructs a {@link StoreException} with no detail message.
     */
    public StoreException() {
        super();
    }

    /**
     * Constructs a {@link StoreException} with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause   The cause for this particular exception.
     */
    public StoreException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@link StoreException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public StoreException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and the detail message of {@code cause}. This constructor is
     * useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public StoreException(final Throwable cause) {
        super(cause);
    }

}
