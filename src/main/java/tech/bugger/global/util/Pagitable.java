package tech.bugger.global.util;

import tech.bugger.global.transfer.Selection;

/**
 * Collection of methods useful for pagination.
 */
public final class Pagitable {

    /**
     * Prevents instantiation of this utility class.
     */
    private Pagitable() {
        throw new UnsupportedOperationException(); // for reflection abusers
    }

    /**
     * Returns the recommended item offset to use when fetching items.
     *
     * @param selection The {@link Selection} to use when calculating the offset.
     * @return The recommended item offset to use when fetching items.
     */
    public static int getItemOffset(final Selection selection) {
        return selection.getCurrentPage() * selection.getPageSize().getSize();
    }

    /**
     * Returns the recommended item limit to use when fetching items.
     *
     * @param selection The {@link Selection} to use when calculating the limit.
     * @return The recommended item limit to use when fetching items.
     */
    public static int getItemLimit(final Selection selection) {
        return selection.getPageSize().getSize();
    }

}
