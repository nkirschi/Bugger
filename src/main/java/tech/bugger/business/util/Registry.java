package tech.bugger.business.util;

import tech.bugger.business.internal.UserSession;
import tech.bugger.persistence.util.ConnectionPool;
import tech.bugger.persistence.util.Mailer;
import tech.bugger.persistence.util.PropertiesReader;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Registry for application-wide access to shared dependencies.
 */
@ApplicationScoped
public class Registry {

    /**
     * The registered {@link ConnectionPool} instances.
     */
    private final Map<String, ConnectionPool> connectionPools;

    /**
     * The registered {@link Mailer} instances.
     */
    private final Map<String, Mailer> mailers;

    /**
     * The registered {@link PriorityExecutor} instances.
     */
    private final Map<String, PriorityExecutor> priorityExecutors;

    /**
     * The registered {@link PropertiesReader} instances.
     */
    private final Map<String, PropertiesReader> propertiesReaders;

    /**
     * Constructs an empty registry.
     */
    public Registry() {
        connectionPools = new HashMap<>();
        mailers = new HashMap<>();
        priorityExecutors = new HashMap<>();
        propertiesReaders = new HashMap<>();
    }

    /**
     * Returns the {@link ConnectionPool} specified by the given injection point.
     *
     * @param ip The caller injection point, necessarily annotated with {@link RegistryKey}.
     * @return The connection pool associated with {@link RegistryKey#value()}.
     */
    @Produces
    @RegistryKey
    public ConnectionPool getConnectionPool(final InjectionPoint ip) {
        return getConnectionPool(extractKey(ip));
    }

    /**
     * Returns the {@link ConnectionPool} registered for the given key.
     *
     * @param key The key of the desired connection pool.
     * @return The connection pool associated with {@code key}.
     */
    public ConnectionPool getConnectionPool(final String key) {
        if (!connectionPools.containsKey(key)) {
            throw new InternalError("No connection pool registered for key '" + key + "'");
        }
        return connectionPools.get(key);
    }

    /**
     * Registers a {@link ConnectionPool} with the given key.
     *
     * @param key            The desired key for {@code connectionPool}.
     * @param connectionPool The connection pool to register.
     */
    public void registerConnectionPool(final String key, final ConnectionPool connectionPool) {
        connectionPools.put(key, connectionPool);
    }

    /**
     * Returns the {@link Mailer} specified by the given injection point.
     *
     * @param ip The caller injection point, necessarily annotated with {@link RegistryKey}.
     * @return The mailer associated with {@link RegistryKey#value()}.
     */
    @Produces
    @RegistryKey
    public Mailer getMailer(final InjectionPoint ip) {
        return getMailer(extractKey(ip));
    }

    /**
     * Returns the {@link Mailer} registered for the given key.
     *
     * @param key The key of the desired mailer.
     * @return The mailer associated with {@code key}.
     */
    public Mailer getMailer(final String key) {
        if (!mailers.containsKey(key)) {
            throw new InternalError("No mailer registered for key '" + key + "'");
        }
        return mailers.get(key);
    }

    /**
     * Registers a {@link Mailer} with the given key.
     *
     * @param key    The desired key for {@code mailer}.
     * @param mailer The connection pool to register.
     */
    public void registerMailer(final String key, final Mailer mailer) {
        mailers.put(key, mailer);
    }

    /**
     * Returns the {@link PriorityExecutor} specified by the given injection point.
     *
     * @param ip The caller injection point, necessarily annotated with {@link RegistryKey}.
     * @return The priority executor associated with {@link RegistryKey#value()}.
     */
    @Produces
    @RegistryKey
    public PriorityExecutor getPriorityExecutor(final InjectionPoint ip) {
        return getPriorityExecutor(extractKey(ip));
    }

    /**
     * Returns the {@link PriorityExecutor} registered for the given key.
     *
     * @param key The key of the desired priority executor.
     * @return The priority executor associated with {@code key}.
     */
    public PriorityExecutor getPriorityExecutor(final String key) {
        if (!priorityExecutors.containsKey(key)) {
            throw new InternalError("No priority executor registered for key '" + key + "'");
        }
        return priorityExecutors.get(key);
    }

    /**
     * Registers a {@link PriorityExecutor} with the given key.
     *
     * @param key              The desired key for {@code priorityExecutor}.
     * @param priorityExecutor The priority executor to register.
     */
    public void registerPriorityExecutor(final String key, final PriorityExecutor priorityExecutor) {
        priorityExecutors.put(key, priorityExecutor);
    }

    /**
     * Returns the {@link PropertiesReader} specified by the given injection point.
     *
     * @param ip The caller injection point, necessarily annotated with {@link RegistryKey}.
     * @return The properties reader associated with {@link RegistryKey#value()}.
     */
    @Produces
    @RegistryKey
    public PropertiesReader getPropertiesReader(final InjectionPoint ip) {
        return getPropertiesReader(extractKey(ip));
    }

    /**
     * Returns the {@link PropertiesReader} registered for the given key.
     *
     * @param key The key of the desired properties reader.
     * @return The properties reader associated with {@code key}.
     */
    public PropertiesReader getPropertiesReader(final String key) {
        if (!propertiesReaders.containsKey(key)) {
            throw new InternalError("No properties reader registered for key '" + key + "'");
        }
        return propertiesReaders.get(key);
    }

    /**
     * Registers a {@link PropertiesReader} with the given key.
     *
     * @param key              The desired key for {@code propertiesReader}.
     * @param propertiesReader The properties reader to register.
     */
    public void registerPropertiesReader(final String key, final PropertiesReader propertiesReader) {
        propertiesReaders.put(key, propertiesReader);
    }

    /**
     * Returns the {@link ResourceBundle} specified by the given injection point.
     *
     * @param ip          The caller injection point, necessarily annotated with {@link RegistryKey}.
     * @param userSession The current user session yielding locale information.
     * @return The resource bundle associated with {@link RegistryKey#value()}.
     */
    @Produces
    @RegistryKey
    public ResourceBundle getBundle(final InjectionPoint ip, final UserSession userSession) {
        return getBundle(extractKey(ip), userSession);
    }

    /**
     * Returns the {@link ResourceBundle} registered for the given key.
     *
     * @param key         The key of the desired resource bundle.
     * @param userSession The current user session yielding locale information.
     * @return The resource bundle associated with {@code key}.
     */
    public ResourceBundle getBundle(String key, final UserSession userSession) {
        try {
            return ResourceBundle.getBundle("tech.bugger.i18n." + key, userSession.getLocale());
        } catch (MissingResourceException e) {
            throw new InternalError("No resource bundle found for key '" + key + "'", e);
        }
    }

    private String extractKey(final InjectionPoint ip) {
        return ip.getAnnotated().getAnnotation(RegistryKey.class).value();
    }

}
