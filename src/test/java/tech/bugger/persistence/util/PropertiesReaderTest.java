package tech.bugger.persistence.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.LogExtension;
import tech.bugger.persistence.exception.ConfigException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LogExtension.class)
public class PropertiesReaderTest {

    private PropertiesReader propertiesReader;

    @BeforeEach
    public void init() throws IOException {
        propertiesReader = new PropertiesReader(ClassLoader.getSystemResourceAsStream("config.properties"));
    }

    @Test
    public void testConstructorWhenStreamIsInvalid() throws IOException {
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(new byte[]{}));
        is.close();
        assertThrows(IOException.class, () -> new PropertiesReader(is));
    }

    @Test
    public void testGetStringWhenKeyInvalid() {
        assertThrows(ConfigException.class, () -> propertiesReader.getString("invalid"));
    }

    @Test
    public void testGetStringWhenKeyValid() {
        assertEquals("bugger", propertiesReader.getString("string"));
    }

    @Test
    public void testGetIntWhenKeyInvalid() {
        assertThrows(ConfigException.class, () -> propertiesReader.getInt("invalid"));
    }

    @Test
    public void testGetIntWhenNotAnInt() {
        assertThrows(ConfigException.class, () -> propertiesReader.getInt("string"));
    }

    @Test
    public void testGetIntWhenKeyValid() {
        assertEquals(42, propertiesReader.getInt("int"));
    }

    @Test
    public void testGetBoolean() {
        assertTrue(propertiesReader.getBoolean("boolean"));
    }
}