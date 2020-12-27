package tech.bugger.persistence.util;

import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.ConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reader for contents of a configuration source held in <code>key = value</code> format.
 */
public final class PropertiesReader {

    /**
     * Log instance for logging in this class.
     */
    private static final Log log = Log.forClass(PropertiesReader.class);

    /**
     * The configuration properties managed by this configuration reader.
     */
    private final Properties configuration;

    /**
     * Constructs a new configuration reader for the provided input.
     *
     * @param is Stream of configuration data.
     */
    public PropertiesReader(final InputStream is) throws IOException {
        configuration = new Properties();
        try {
            configuration.load(is);
        } catch (IOException e) {
            throw new IOException("Config source could not be loaded!", e);
        }
    }

    /**
     * Retrieves a string property from the configuration by the given key.
     *
     * @param key The key of the desired property.
     * @return The string property associated with {@code key}
     * @throws ConfigException if {@code key} is not associated with a property.
     */
    public String getString(final String key) {
        if (!configuration.containsKey(key)) {
            throw new ConfigException("Invalid key!");
        }
        return configuration.getProperty(key);
    }

    /**
     * Retrieves an integer property from the configuration by the given key.
     *
     * @param key The key of the desired property.
     * @return The integer property associated with {@code key}
     * @throws ConfigException if {@code key} is not associated with a property or the property is no integer.
     */
    public int getInt(final String key) {
        String value = getString(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ConfigException("Value " + value + " is not an integer!", e);
        }
    }

    /**
     * Retrieves a boolean property from the configuration by the given key.
     *
     * @param key The key of the desired property.
     * @return The boolean property associated with {@code key}
     * @throws ConfigException if {@code key} is not associated with a property.
     */
    public boolean getBoolean(final String key) {
        return Boolean.parseBoolean(getString(key));
    }
}
