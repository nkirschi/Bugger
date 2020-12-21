package tech.bugger.persistence.exception;

import java.io.Serial;

/**
 * Exception indicating something was not there.
 */
public class NotFoundException extends Exception {
    @Serial
    private static final long serialVersionUID = 6292170139373814152L;

    /**
     * Constructs a {@link NotFoundException} with no detail message.
     */
    public NotFoundException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a {@link NotFoundException} with the specified detail message and cause.
     *
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause The cause for this particular exception.
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a {@link NotFoundException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public NotFoundException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a new exception with the specified cause and the detail message of {@code cause}. This constructor is
     * useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public NotFoundException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

}
