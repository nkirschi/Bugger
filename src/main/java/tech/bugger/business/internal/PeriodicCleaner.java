package tech.bugger.business.internal;

import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.inject.Inject;
import java.time.Duration;

/**
 * Periodically running task for maintenance purposes.
 */
public class PeriodicCleaner implements Runnable {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(PeriodicCleaner.class);

    private static final Duration EXPIRATION_AGE = Duration.ofHours(1);

    private final TransactionManager transactionManager;

    @Inject
    public PeriodicCleaner(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Clean up data not needed any more.
     *
     * {@inheritDoc}
     */
    @Override
    public void run() {
        log.info("Periodic cleaner started.");
        try (Transaction tx = transactionManager.begin()) {
            tx.newTokenGateway().cleanExpiredTokens(EXPIRATION_AGE);
            tx.newUserGateway().cleanExpiredRegistrations();
            tx.commit();
            log.debug("Finished cleaning data source successfully.");
        } catch (TransactionException e) {
            log.error("Transaction commit error when cleaning data source.", e);
        }
        log.info("Periodic cleaner finished.");
    }

}
