package tech.bugger.persistence.util;

import tech.bugger.business.util.Registry;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for transactions.
 */
@Singleton
public final class TransactionManager {

    private static final String MAIN_DB_POOL = "db";

    /**
     * The connection pool registry of the application.
     */
    private final Registry registry;

    /**
     * Constructs a transaction manager with the given connection pool registry.
     *
     * @param registry The registry to use for transaction management.
     */
    @Inject
    public TransactionManager(final Registry registry) {
        this.registry = registry;
    }

    /**
     * Yields a new transaction ready for use.
     *
     * @return The fresh transaction.
     */
    public Transaction begin() {
        return new DBTransaction(registry.getConnectionPool(MAIN_DB_POOL));
    }

}
