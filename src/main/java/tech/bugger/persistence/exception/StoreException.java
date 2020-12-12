package tech.bugger.persistence.exception;

/**
 * Exception indicating something went wrong when trying to store something in the database.
 */
public class StoreException extends RuntimeException {

    private static final long serialVersionUID = -1850502033299171433L;

    /**
     * Constructs a {@link StoreException} with no detail message.
     */
    public StoreException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a {@link StoreException} with the specified detail message and cause.
     *
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause The cause for this particular exception.
     */
    public StoreException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a {@link StoreException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public StoreException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a new exception with the specified cause and the detail message of {@code cause}. This constructor is
     * useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public StoreException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }


}
