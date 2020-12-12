package tech.bugger.business.internal;

import tech.bugger.global.util.Log;

/**
 * Periodically running task for maintenance purposes.
 */
public class PeriodicCleaner implements Runnable {
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
