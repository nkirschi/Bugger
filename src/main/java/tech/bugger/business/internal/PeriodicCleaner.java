package tech.bugger.business.internal;

import tech.bugger.global.util.Log;

/**
 * Periodically running task for maintenance purposes.
 */
public class PeriodicCleaner implements Runnable {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(PeriodicCleaner.class);

    /**
     * Clean up data not needed any more.
     *
     * {@inheritDoc}
     */
    @Override
    public void run() {
        // TODO
    }

}
