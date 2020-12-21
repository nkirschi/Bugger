package tech.bugger.persistence.util;

/**
 * Modification of {@link AutoCloseable} without the need to throw an exception in {@link #close()}.
 */
interface ExceptionlessAutoCloseable extends AutoCloseable {
    /**
     * {@inheritDoc}
     */
    @Override
    void close();
}
