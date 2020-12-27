package tech.bugger.persistence.util;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for application-wide access to {@link ConnectionPool} instances.
 */
@Singleton
public class ConnectionPoolRegistry {

    /**
     * The registered {@link ConnectionPool} instances.
     */
    private final Map<String, ConnectionPool> connectionPools;

    /**
     * Constructs a new connection pool registry.
     */
    public ConnectionPoolRegistry() {
        connectionPools = new HashMap<>();
    }

    /**
     * Returns the {@link ConnectionPool} registered for the given key.
     *
     * @param key The key of the desired connection pool.
     * @return The connection pool associated with {@code key}.
     */
    public ConnectionPool get(final String key) {
        if (!connectionPools.containsKey(key)) {
            throw new InternalError();
        }
        return connectionPools.get(key);
    }

    /**
     * Registers a {@link ConnectionPool} with the given key.
     *
     * @param key            The desired key for {@code connectionPool}.
     * @param connectionPool The connection pool to register.
     */
    public void register(final String key, final ConnectionPool connectionPool) {
        connectionPools.put(key, connectionPool);
    }

}
