package tech.bugger.persistence.util;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for application-wide access to {@link Mailer} instances.
 */
@Singleton
public class MailerRegistry {

    /**
     * The registered {@link Mailer} instances.
     */
    private final Map<String, Mailer> mailers;

    /**
     * Constructs a new mailer registry.
     */
    public MailerRegistry() {
        mailers = new HashMap<>();
    }

    /**
     * Returns the {@link Mailer} registered for the given key.
     *
     * @param key The key of the desired mailer.
     * @return The mailer associated with {@code key}.
     */
    public Mailer get(final String key) {
        if (!mailers.containsKey(key)) {
            throw new InternalError();
        }
        return mailers.get(key);
    }

    /**
     * Registers a {@link Mailer} with the given key.
     *
     * @param key    The desired key for {@code mailer}.
     * @param mailer The mailer to register.
     */
    public void register(final String key, final Mailer mailer) {
        mailers.put(key, mailer);
    }
}
