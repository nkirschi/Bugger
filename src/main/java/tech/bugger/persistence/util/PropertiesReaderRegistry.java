package tech.bugger.persistence.util;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for application-wide access to {@link PropertiesReader} instances.
 */
@Singleton
public class PropertiesReaderRegistry {

    /**
     * The registered {@link PropertiesReader} instances.
     */
    private final Map<String, PropertiesReader> propertiesReaders;

    /**
     * Constructs a new properties reader registry.
     */
    public PropertiesReaderRegistry() {
        propertiesReaders = new HashMap<>();
    }

    /**
     * Returns the {@link PropertiesReader} registered for the given key.
     *
     * @param key The key of the desired properties reader.
     * @return The properties reader associated with {@code key}.
     */
    public PropertiesReader get(final String key) {
        if (!propertiesReaders.containsKey(key)) {
            throw new InternalError();
        }
        return propertiesReaders.get(key);
    }

    /**
     * Registers a {@link PropertiesReader} with the given key.
     *
     * @param key              The desired key for {@code propertiesReader}.
     * @param propertiesReader The properties reader to register.
     */
    public void register(final String key, final PropertiesReader propertiesReader) {
        propertiesReaders.put(key, propertiesReader);
    }

}
