package tech.bugger.persistence.util;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for transactions.
 */
@Singleton
public final class TransactionManager {

    /**
     * The connection pool registry of the application.
     */
    private final ConnectionPoolRegistry connectionPoolRegistry;

    /**
     * Constructs a transaction manager with the given connection pool registry.
     *
     * @param connectionPoolRegistry The connection pool registry to use for transaction management.
     */
    @Inject
    public TransactionManager(final ConnectionPoolRegistry connectionPoolRegistry) {
        this.connectionPoolRegistry = connectionPoolRegistry;
    }


    /**
     * Yields a new transaction ready for use.
     *
     * @return The fresh transaction.
     */
    public Transaction begin() {
        return new DBTransaction(connectionPoolRegistry.get("db"));
    }

}
