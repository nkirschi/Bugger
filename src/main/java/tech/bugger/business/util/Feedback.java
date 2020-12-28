package tech.bugger.business.util;

/**
 * Wrapper for user feedback messages.
 */
public class Feedback {

    /**
     * Type of a feedback message.
     */
    public enum Type {
        /**
         * An information message.
         */
        INFO,

        /**
         * A warning message.
         */
        WARNING,

        /**
         * An error message.
         */
        ERROR
    }

    private final String message;
    private final Type type;

    /**
     * Constructs a new feedback message with given text and type.
     *
     * @param message The text of the feedback message.
     * @param type    The type of the feedback message.
     */
    public Feedback(String message, Type type) {
        this.message = message;
        this.type = type;
    }

    /**
     * Returns the text of the feedback message.
     *
     * @return The feedback message text.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return the type of the feedback message.
     *
     * @return The feedback message type.
     */
    public Type getType() {
        return type;
    }
}
