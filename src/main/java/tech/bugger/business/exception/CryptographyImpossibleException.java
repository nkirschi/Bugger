package tech.bugger.business.exception;

import java.io.Serial;

/**
 * Exception indicating cryptography is not possible.
 */
public class CryptographyImpossibleException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -693190241209319033L;

    /**
     * Constructs a {@link CryptographyImpossibleException} with no detail message.
     */
    public CryptographyImpossibleException() {
        super();
    }

    /**
     * Constructs a {@link CryptographyImpossibleException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public CryptographyImpossibleException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and the detail message of {@code cause}. This constructor is
     * useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public CryptographyImpossibleException(final Throwable cause) {
        super(cause);
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
    public CryptographyImpossibleException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
