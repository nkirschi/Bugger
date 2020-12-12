package tech.bugger.business.exception;

/**
 * Exception indicating an image is corrupted.
 */
public class CorruptImageException extends Exception {

    private static final long serialVersionUID = 1900286228042129388L;

    /**
     * Constructs a {@link CorruptImageException} with no detail message.
     */
    public CorruptImageException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a {@link CorruptImageException} with the specified detail message and cause.
     *
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause   The cause for this particular exception.
     */
    public CorruptImageException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a {@link CorruptImageException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public CorruptImageException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a new exception with the specified cause and the detail message of {@code cause}. This constructor is
     * useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public CorruptImageException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
