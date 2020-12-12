package tech.bugger.business.util;

import tech.bugger.global.transfer.Selection;
import tech.bugger.global.util.Log;

import javax.faces.model.IterableDataModel;

/**
 * Generic manager for paginated and sortable tabular data.
 *
 * @param <T> Type of the items to be paginated.
 */
public abstract class Paginator<T> extends IterableDataModel<T> {
    private static final Log log = Log.forClass(Paginator.class);

    private final Selection selection;

    /**
     * Constructs a paginator with the given parameters.
     *
     * @param sortedBy     Key of the column to initially sort by.
     * @param itemsPerPage Number of items per page to display.
     */
    public Paginator(String sortedBy, int itemsPerPage) {
        this.selection = null;
    }

    /**
     * Fetches a slice of data specified by {@code pagination}.
     *
     * @return The data chunk characterized by the current parameters.
     */
    protected abstract Iterable<T> fetch();

    /**
     * Determines the current total size of the base data.
     *
     * @return The total amount of items ignoring pagination.
     */
    protected abstract int totalSize();

    /**
     * Loads the data chunk for the previous page.
     */
    public void prevPage() {
    }

    /**
     * Loads the data chunk for the next page.
     */
    public void nextPage() {
    }

    /**
     * Loads the data chunk for the first page.
     */
    public void firstPage() {
    }

    /**
     * Loads the data chunk for the last page.
     */
    public void lastPage() {
    }

    /**
     * Check whether the current page is the first page of the considered data.
     *
     * @return Whether the current page is the first page.
     */
    public boolean isFirstPage() {
        return false;
    }

    /**
     * Returns the index of the last page.
     *
     * @return The index of the last page.
     */
    public int getLastPage() {
        return 0;
    }

    /**
     * Check whether the current page is the last page of the considered data.
     *
     * @return Whether the current page is the last page.
     */
    public boolean isLastPage() {
        return false;
    }

    /**
     * Sort by the given column key.
     *
     * @param sortKey The key of the column to sort by.
     */
    public void sortBy(String sortKey) {
    }

    /**
     * Update the paginated data model using the current parameters in {@code pagination}.
     */
    public void update() {
    }

    /**
     * Update the paginated data model while returning to the first page.
     */
    public void updateReset() {
    }

    /**
     * Checks whether the current slice of data has no items.
     *
     * @return Whether the current chunk is empty.
     */
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns the possible values for the number of items per page.
     *
     * @return The possible page size values.
     */
    public int[] pageSizeValues() {
        return null;
    }

    /**
     * Returns the current pagination parameters.
     *
     * @return The pagination parameters.
     */
    public Selection getSelection() {
        return selection;
    }
}
