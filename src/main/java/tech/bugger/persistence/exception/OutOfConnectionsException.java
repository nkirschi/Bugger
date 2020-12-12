package tech.bugger.persistence.exception;

/**
 * Exception indicating there are not enough connections available.
 */
public class OutOfConnectionsException extends RuntimeException {

    private static final long serialVersionUID = -9055935003841723321L;

    /**
     * Constructs an {@link OutOfConnectionsException} with no detail message.
     */
    public OutOfConnectionsException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs an {@link OutOfConnectionsException} with the specified detail message and cause.
     *
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause The cause for this particular exception.
     */
    public OutOfConnectionsException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs an {@link OutOfConnectionsException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public OutOfConnectionsException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a new exception with the specified cause and the detail message of {@code cause}. This constructor is
     * useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public OutOfConnectionsException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }


}
