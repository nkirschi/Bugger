package tech.bugger.control.conversion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class DurationConverterTest {

    private DurationConverter durationConverter;

    @BeforeEach
    public void setUp() {
        durationConverter = new DurationConverter();
    }

    @Test
    public void testGetAsObject() {
        assertThrows(UnsupportedOperationException.class, () -> durationConverter.getAsObject(null, null, null));
    }

    @Test
    public void testGetAsStringWhenLessThanADay() {
        assertEquals("1h:42m", durationConverter.getAsString(null, null, Duration.ofMinutes(102)));
    }

    @Test
    public void testGetAsStringWhenMoreThanADay() {
        assertEquals("4d:02h", durationConverter.getAsString(null, null, Duration.ofHours(98)));
    }

}