package tech.bugger.persistence.util;

/**
 * Factory for transactions.
 */
public final class TransactionManager {

    /**
     * Private constructor to prevent initialization of this utility class.
     */
    private TransactionManager() {
        // utility class
    }

    /**
     * Yields a new transaction ready for use.
     *
     * @return The fresh transaction.
     */
    public static Transaction begin() {
        return new DBTransaction(ConnectionPool.getInstance());
    }
}
