package tech.bugger.global.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogTest {
    private Log log;
    private Handler captureHandler;
    private ByteArrayOutputStream captureStream;

    @BeforeEach
    public void init() throws NoSuchFieldException, IllegalAccessException {
        log = Log.forClass(LogTest.class);
        captureStream = new ByteArrayOutputStream();
        captureHandler = new StreamHandler(captureStream, new SimpleFormatter());
        captureHandler.setLevel(Level.ALL);

        Field field = Log.class.getDeclaredField("logger");
        field.setAccessible(true);
        Logger logger = (Logger) field.get(log);
        logger.addHandler(captureHandler);
        logger.setLevel(Level.ALL);
    }

    private String capture() {
        captureHandler.flush();
        return captureStream.toString();
    }

    @Test
    public void testForClass() {
        Log log = Log.forClass(LogTest.class);
        Log ger = Log.forClass(LogTest.class);
        assertSame(log, ger, "Requesting log for the same class should yield the same object.");
    }

    @Test
    public void testInitNull() {
        assertThrows(IllegalArgumentException.class, () -> Log.init(null),
                "Initialization with null stream is not allowed.");
    }

    @Test
    public void testInitValidStream() {
        assertDoesNotThrow(() -> Log.init(new ByteArrayInputStream(new byte[0])),
                "Initialization with valid input stream should pass.");
    }

    @Test
    public void testInitInvalidStream() throws IOException {
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
        is.close();
        assertThrows(IOException.class, () -> Log.init(is),
                "Initialization with an invalid input stream should fail.");
    }

    @Test
    public void testErrorWithoutThrowable() {
        log.error("Reflection is ugly.");
        assertAll("Message and log level should be present.",
                () -> assertTrue(capture().contains("ugly")),
                () -> assertTrue(capture().contains("SEVERE")));
    }

    @Test
    public void testErrorWithThrowable() {
        log.error("Reflection is ugly.", new Exception());
        assertAll("Message and log level and throwable should be present.",
                () -> assertTrue(capture().contains("ugly")),
                () -> assertTrue(capture().contains("SEVERE")),
                () -> assertTrue(capture().contains("Exception")));
    }

    @Test
    public void testWarningWithoutThrowable() {
        log.warning("Reflection is ugly.");
        assertAll("Message and log level should be present.",
                () -> assertTrue(capture().contains("ugly")),
                () -> assertTrue(capture().contains("WARNING")));
    }

    @Test
    public void testWarningWithThrowable() {
        log.warning("Reflection is ugly.", new Exception());
        assertAll("Message and log level and throwable should be present.",
                () -> assertTrue(capture().contains("ugly")),
                () -> assertTrue(capture().contains("WARNING")),
                () -> assertTrue(capture().contains("Exception")));
    }

    @Test
    public void testInfoWithoutThrowable() {
        log.info("Reflection is ugly.");
        assertAll("Message and log level should be present.",
                () -> assertTrue(capture().contains("ugly")),
                () -> assertTrue(capture().contains("INFO")));
    }

    @Test
    public void testInfoWithThrowable() {
        log.info("Reflection is ugly.", new Exception());
        assertAll("Message and log level and throwable should be present.",
                () -> assertTrue(capture().contains("ugly")),
                () -> assertTrue(capture().contains("INFO")),
                () -> assertTrue(capture().contains("Exception")));
    }

    @Test
    public void testDebugWithoutThrowable() {
        log.debug("Reflection is ugly.");
        assertAll("Message and log level should be present.",
                () -> assertTrue(capture().contains("ugly")),
                () -> assertTrue(capture().contains("FINEST")));
    }

    @Test
    public void testDebugWithThrowable() {
        log.debug("Reflection is ugly.", new Exception());
        assertAll("Message and log level and throwable should be present.",
                () -> assertTrue(capture().contains("ugly")),
                () -> assertTrue(capture().contains("FINEST")),
                () -> assertTrue(capture().contains("Exception")));
    }
}