package tech.bugger.persistence.exception;

/**
 * Exception indicating something is incorrectly referencing itself.
 */
public class SelfReferenceException extends Exception {

    private static final long serialVersionUID = -7793828426342576821L;

    /**
     * Constructs a {@link SelfReferenceException} with no detail message.
     */
    public SelfReferenceException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a {@link SelfReferenceException} with the specified detail message and cause.
     *
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause The cause for this particular exception.
     */
    public SelfReferenceException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a {@link SelfReferenceException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public SelfReferenceException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a new exception with the specified cause and the detail message of {@code cause}. This constructor is
     * useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public SelfReferenceException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
