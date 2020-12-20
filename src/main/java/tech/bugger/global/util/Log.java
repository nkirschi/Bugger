package tech.bugger.global.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Facade for a logging API. Currently: {@link java.util.logging}.
 */
public final class Log {
    private static final Map<String, Log> logMap = new HashMap<>();

    private final java.util.logging.Logger logger;

    private Log(final String name) {
        logger = java.util.logging.Logger.getLogger(name);
    }

    /**
     * Initialize the logging settings by providing a configuration stream.
     *
     * @param is An input stream yielding access to the configuration.
     * @throws IOException if the input stream {@code is} could not be read.
     */
    public static void init(InputStream is) throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("Initalization stream must not be null.");
        }
        try {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            throw new IOException("Initialization stream could not be read.", e);
        }
    }

    /**
     * Gets the logger instance for the class {@code clazz}.
     *
     * @param clazz The class to obtain a logger for.
     * @param <T>   The type of the class modeled by the {@link Class} object.
     * @return The logger instance for {@code clazz}.
     */
    public static <T> Log forClass(final Class<T> clazz) {
        String name = clazz.getName();
        Log log = logMap.get(name);
        if (log == null) {
            log = new Log(name);
            logMap.put(name, log);
        }
        return log;
    }

    /**
     * Logs an error with a given message.
     *
     * @param msg The message to log.
     */
    public void error(final String msg) {
        log(Level.SEVERE, msg, null);
    }

    /**
     * Logs an error with a given message and cause.
     *
     * @param msg   The message to log.
     * @param cause The cause of the error.
     */
    public void error(final String msg, final Throwable cause) {
        log(Level.SEVERE, msg, cause);
    }

    /**
     * Logs a warning with a given message.
     *
     * @param msg The message to log.
     */
    public void warning(final String msg) {
        log(Level.WARNING, msg, null);
    }

    /**
     * Logs a warning with a given message and cause.
     *
     * @param msg   The message to log.
     * @param cause The cause of the warning.
     */
    public void warning(final String msg, final Throwable cause) {
        log(Level.WARNING, msg, cause);
    }

    /**
     * Logs an information message.
     *
     * @param msg The message to log.
     */
    public void info(final String msg) {
        log(Level.INFO, msg, null);
    }

    /**
     * Logs an information message with a given cause.
     *
     * @param msg   The message to log.
     * @param cause The cause of the message.
     */
    public void info(final String msg, final Throwable cause) {
        log(Level.INFO, msg, cause);
    }

    /**
     * Logs a debug message.
     *
     * @param msg The message to log.
     */
    public void debug(final String msg) {
        log(Level.FINEST, msg, null);
    }

    /**
     * Logs a debug message with a given cause.
     *
     * @param msg   The message to log.
     * @param cause The cause of the message.
     */
    public void debug(final String msg, final Throwable cause) {
        log(Level.FINEST, msg, cause);
    }

    /**
     * Logs a debug message with given level and cause.
     *
     * @param level The log level for the message.
     * @param msg   The message to log.
     * @param cause The cause of the message.
     */
    private void log(final Level level, final String msg, final Throwable cause) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            StackTraceElement e = Thread.currentThread().getStackTrace()[3]; // caller
            logger.logp(level, e.getClassName(), e.getMethodName(), msg, cause);
        } else { // should never happen
            logger.log(level, msg, cause);
        }
    }
}
