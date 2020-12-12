package tech.bugger.business.exception;

/**
 * Exception indicating cryptography is not possible.
 */
public class CryptographyImpossibleException extends RuntimeException {

    private static final long serialVersionUID = -693190241209319033L;

    /**
     * Constructs a {@link CryptographyImpossibleException} with no detail message.
     */
    public CryptographyImpossibleException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a {@link CryptographyImpossibleException} with the specified detail message and cause.
     *
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause   The cause for this particular exception.
     */
    public CryptographyImpossibleException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a {@link CryptographyImpossibleException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public CryptographyImpossibleException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a new exception with the specified cause and the detail message of {@code cause}. This constructor is
     * useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public CryptographyImpossibleException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
