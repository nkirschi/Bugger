package tech.bugger.global.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Facade for a logging API. Currently: {@link java.util.logging}.
 */
public final class Log {
    private static final Map<String, Log> logMap = new HashMap<>();

    private final java.util.logging.Logger logger;

    private Log(final String name) {
        logger = null;
    }

    /**
     * Initialize the logging settings by providing a configuration file.
     *
     * @param is An input stream yielding access to the file.
     */
    public static void init(InputStream is) {
    }

    /**
     * Gets the logger instance for the class {@code clazz}.
     *
     * @param clazz The class to obtain a logger for.
     * @param <T>   The type of the class modeled by the {@link Class} object.
     * @return The logger instance for {@code clazz}.
     */
    public static <T> Log forClass(final Class<T> clazz) {
        return null;
    }

    /**
     * Logs an error with a given message.
     *
     * @param msg The message to log.
     */
    public void error(final String msg) {
    }

    /**
     * Logs an error with a given message and cause.
     *
     * @param msg   The message to log.
     * @param cause The cause of the error.
     */
    public void error(final String msg, final Throwable cause) {
    }

    /**
     * Logs a warning with a given message.
     *
     * @param msg The message to log.
     */
    public void warning(final String msg) {
    }

    /**
     * Logs a warning with a given message and cause.
     *
     * @param msg   The message to log.
     * @param cause The cause of the warning.
     */
    public void warning(final String msg, final Throwable cause) {
    }

    /**
     * Logs an information message.
     *
     * @param msg The message to log.
     */
    public void info(final String msg) {
    }

    /**
     * Logs an information message with a given cause.
     *
     * @param msg   The message to log.
     * @param cause The cause of the message.
     */
    public void info(final String msg, final Throwable cause) {
    }

    /**
     * Logs a debug message.
     *
     * @param msg The message to log.
     */
    public void debug(final String msg) {
    }

    /**
     * Logs a debug message with a given cause.
     *
     * @param msg   The message to log.
     * @param cause The cause of the message.
     */
    public void debug(final String msg, final Throwable cause) {
    }
}