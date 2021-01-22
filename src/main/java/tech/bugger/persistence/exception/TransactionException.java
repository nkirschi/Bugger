package tech.bugger.persistence.exception;

import java.io.Serial;

/**
 * Exception indicating that a transaction was not executed successfully.
 */
public class TransactionException extends Exception {

    @Serial
    private static final long serialVersionUID = 7666151903503703118L;

    /**
     * Constructs a {@link TransactionException} with no detail message.
     */
    public TransactionException() {
        super();
    }

    /**
     * Constructs a {@link TransactionException} with the specified detail message and cause.
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause   The cause for this particular exception.
     */
    public TransactionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@link TransactionException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public TransactionException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and the detail message of {@code cause}. This constructor is
     * useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public TransactionException(final Throwable cause) {
        super(cause);
    }

}
