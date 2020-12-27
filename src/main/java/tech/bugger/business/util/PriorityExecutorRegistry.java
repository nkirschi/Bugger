package tech.bugger.business.util;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for application-wide access to {@link PriorityExecutor} instances.
 */
@Singleton
public class PriorityExecutorRegistry {

    /**
     * The registered {@link PriorityExecutor} instances.
     */
    private final Map<String, PriorityExecutor> priorityExecutors;

    /**
     * Constructs a new priority executor registry.
     */
    public PriorityExecutorRegistry() {
        priorityExecutors = new HashMap<>();
    }

    /**
     * Returns the {@link PriorityExecutor} registered for the given key.
     *
     * @param key The key of the desired priority executor.
     * @return The priority executor associated with {@code key}.
     */
    public PriorityExecutor get(final String key) {
        if (!priorityExecutors.containsKey(key)) {
            throw new InternalError();
        }
        return priorityExecutors.get(key);
    }

    /**
     * Registers a {@link PriorityExecutor} with the given key.
     *
     * @param key              The desired key for {@code priorityExecutor}.
     * @param priorityExecutor The priority executor to register.
     */
    public void register(final String key, final PriorityExecutor priorityExecutor) {
        priorityExecutors.put(key, priorityExecutor);
    }

}
