package tech.bugger.persistence.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tech.bugger.persistence.exception.ConfigException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigReaderTest {

    @Test
    public void testGetInstance() {
        ConfigReader con = ConfigReader.getInstance();
        ConfigReader fig = ConfigReader.getInstance();
        assertSame(con, fig, "Should always return the same singleton instance.");
    }

    @Test
    public void testGetStringWhenNotInitialized() {
        assertThrows(IllegalStateException.class, () -> ConfigReader.getInstance().getString("string"));
    }

    @Test
    public void testLoadWhenStreamIsInvalid() throws IOException {
        InputStream is = new BufferedInputStream(new ByteArrayInputStream(new byte[]{}));
        is.close();
        assertThrows(IOException.class, () -> ConfigReader.getInstance().load(is));
    }

    @Nested
    public class ConfigReaderRetrievalTest {
        @BeforeEach
        public void init() throws IOException {
            ConfigReader.getInstance().load(ClassLoader.getSystemResourceAsStream("config-test.properties"));
        }

        @Test
        public void testGetStringWhenKeyInvalid() {
            assertThrows(ConfigException.class, () -> ConfigReader.getInstance().getString("invalid"));
        }

        @Test
        public void testGetStringWhenKeyValid() {
            assertEquals("bugger", ConfigReader.getInstance().getString("string"));
        }

        @Test
        public void testGetIntWhenKeyInvalid() {
            assertThrows(ConfigException.class, () -> ConfigReader.getInstance().getInt("invalid"));
        }

        @Test
        public void testGetIntWhenNotAnInt() {
            assertThrows(ConfigException.class, () -> ConfigReader.getInstance().getInt("string"));
        }

        @Test
        public void testGetIntWhenKeyValid() {
            assertEquals(42, ConfigReader.getInstance().getInt("int"));
        }

        @Test
        public void testGetBoolean() {
            assertTrue(ConfigReader.getInstance().getBoolean("boolean"));
        }
    }
}