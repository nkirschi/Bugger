package tech.bugger.control.conversion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class LocaleConverterTest {

    private LocaleConverter localeConverter;

    @BeforeEach
    public void setUp() {
        localeConverter = new LocaleConverter();
    }

    @Test
    public void testGetAsObject() {
        assertEquals(Locale.ENGLISH, localeConverter.getAsObject(null, null, "en"));
    }

    @Test
    public void testGetAsString() {
        assertEquals("en", localeConverter.getAsString(null, null, Locale.ENGLISH));
    }

}