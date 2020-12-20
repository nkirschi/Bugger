package tech.bugger.persistence.exception;

/**
 * Exception indicating something went wrong with the config file.
 */
public class ConfigException extends RuntimeException {

    private static final long serialVersionUID = -7813119346738971512L;

    /**
     * Constructs a {@link ConfigException} with the specified detail message.
     *
     * @param message The detail message describing this particular exception.
     */
    public ConfigException(String message) {
        super(message);
    }

    /**
     * Constructs a {@link ConfigException} with the specified detail message and cause.
     *
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated in this
     * exception's detail message.
     *
     * @param message The detail message describing this particular exception.
     * @param cause   The cause for this particular exception.
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@link ConfigException} with no detail message.
     */
    public ConfigException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructs a new exception with the specified cause and the detail message of {@code cause}. This constructor is
     * useful for exceptions that are little more than wrappers for other {@link Throwable}s.
     *
     * @param cause The cause for this particular exception.
     */
    public ConfigException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }
}
