package tech.bugger.persistence.util;

import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.ConfigFileException;

import java.util.Properties;

/**
 * Reader for contents of a configuration file held in <code>key = value</code> format.
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
        return null;
    }

    /**
     * Loads the configuration from the given file path.
     *
     * @param configFile Path to the configuration file relative to the classpath.
     */
    public void load(String configFile) {

    }

    /**
     * Retrieves a string property from the configuration by the given key.
     *
     * @param key The key of the desired property.
     * @return The string property associated with {@code key}
     * @throws ConfigFileException   if {@code key} is not associated with a property.
     * @throws IllegalStateException if no configuration has been loaded yet.
     */
    public String getString(String key) {
        return null;
    }

    /**
     * Retrieves an integer property from the configuration by the given key.
     *
     * @param key The key of the desired property.
     * @return The integer property associated with {@code key}
     * @throws ConfigFileException   if {@code key} is not associated with a property or the property is no integer.
     * @throws IllegalStateException if no configuration has been loaded yet.
     */
    public int getInt(String key) {
        return 0;
    }

    /**
     * Retrieves a booolean property from the configuration by the given key.
     *
     * @param key The key of the desired property.
     * @return The boolean property associated with {@code key}
     * @throws ConfigFileException   if {@code key} is not associated with a property.
     * @throws IllegalStateException if no configuration has been loaded yet.
     */
    public boolean getBoolean(String key) {
        return false;
    }
}
