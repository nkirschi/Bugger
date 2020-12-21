package tech.bugger.persistence.util;

import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.ConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reader for contents of a configuration source held in <code>key = value</code> format.
 */
public final class ConfigReader {
    private static final Log log = Log.forClass(ConfigReader.class);

    private static ConfigReader instance;

    private final Properties configuration;
    private boolean loaded;

    private ConfigReader() {
        configuration = new Properties();
    }

    /**
     * Supplies the singleton configuration reader object.
     *
     * @return The one and only instance of the configuration reader.
     */
    public static ConfigReader getInstance() {
        if (instance == null) {
            instance = new ConfigReader();
        }
        return instance;
    }

    /**
     * Loads the configuration from the given input stream.
     *
     * @param is Stream of configuration data.
     */
    public void load(InputStream is) throws IOException {
        try {
            configuration.load(is);
            loaded = true;
        } catch (IOException e) {
            throw new IOException("Config source could not be loaded!", e);
        }
    }

    /**
     * Retrieves a string property from the configuration by the given key.
     *
     * @param key The key of the desired property.
     * @return The string property associated with {@code key}
     * @throws ConfigException       if {@code key} is not associated with a property.
     * @throws IllegalStateException if no configuration has been loaded yet.
     */
    public String getString(String key) {
        if (!loaded) {
            throw new IllegalStateException("Configuration has not been loaded!");
        }
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
     * @throws ConfigException       if {@code key} is not associated with a property or the property is no integer.
     * @throws IllegalStateException if no configuration has been loaded yet.
     */
    public int getInt(String key) {
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
     * @throws ConfigException       if {@code key} is not associated with a property.
     * @throws IllegalStateException if no configuration has been loaded yet.
     */
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }
}
