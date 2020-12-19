package tech.bugger.business.util;

import javax.faces.model.IterableDataModel;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.util.Log;

/**
 * Generic manager for paginated and sortable tabular data.
 *
 * @param <T> Type of the items to be paginated.
 */
public abstract class Paginator<T> extends IterableDataModel<T> {

    /**
     * The {@link Log} to log errors, warnings and other debug information to.
     */
    private static final Log log = Log.forClass(Paginator.class);

    /**
     * The possible values for the "items per page" feature of a paginator.
     */
    private static final int[] ITEMS_PER_PAGE = new int[]{10, 20, 50, 100};

    /**
     * The current {@link Selection}, representing the state of this paginator.
     */
    private final Selection selection;

    /**
     * Constructs a paginator with the given parameters.
     *
     * @param sortedBy     Key of the column to initially sort by.
     * @param itemsPerPage Number of items per page to display.
     */
    public Paginator(final String sortedBy, final int itemsPerPage) {
        this.selection = new Selection(0, 0, itemsPerPage, sortedBy, true);
        update();
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
     * Returns the current page of this paginator for user interaction.
     *
     * @return The current page for user interaction.
     */
    public int getCurrentPage() {
        // User interaction: Add 1 for convenience (1-indexed)
        return selection.getCurrentPage() + 1;
    }

    /**
     * Sets the current page of this paginator for user interaction.
     *
     * @param currentPage The current page to be set for user interaction.
     */
    public void setCurrentPage(final int currentPage) {
        // User interaction: Subtract 1 for convenience (1-indexed)
        selection.setCurrentPage(currentPage - 1);
    }

    /**
     * Loads the data chunk for the previous page.
     */
    public void prevPage() {
        selection.setCurrentPage(selection.getCurrentPage() - 1);
        update();
    }

    /**
     * Loads the data chunk for the next page.
     */
    public void nextPage() {
        selection.setCurrentPage(selection.getCurrentPage() + 1);
        update();
    }

    /**
     * Loads the data chunk for the first page.
     */
    public void firstPage() {
        selection.setCurrentPage(0);
        update();
    }

    /**
     * Loads the data chunk for the last page.
     */
    public void lastPage() {
        selection.setCurrentPage(getLastPage());
        update();
    }

    /**
     * Check whether the current page is the first page of the considered data.
     *
     * @return Whether the current page is the first page.
     */
    public boolean isFirstPage() {
        return selection.getCurrentPage() == 0;
    }

    /**
     * Returns the index of the last page.
     *
     * @return The index of the last page.
     */
    public int getLastPage() {
        return (totalSize() - 1) / selection.getPageSize();
    }

    /**
     * Check whether the current page is the last page of the considered data.
     *
     * @return Whether the current page is the last page.
     */
    public boolean isLastPage() {
        return selection.getTotalSize() <= (selection.getCurrentPage() + 1) * selection.getPageSize();
    }

    /**
     * Sort by the given column key.
     *
     * @param sortKey The key of the column to sort by.
     */
    public void sortBy(final String sortKey) {
        if (selection.getSortedBy().equals(sortKey)) {
            selection.setAscending(!selection.isAscending());
        } else {
            selection.setSortedBy(sortKey);
            selection.setAscending(true);
        }
        updateReset();
    }

    /**
     * Update the paginated data model using the current parameters in {@code pagination}.
     */
    public void update() {
        setWrappedData(fetch());
        selection.setTotalSize(totalSize());
    }

    /**
     * Update the paginated data model while returning to the first page.
     */
    public void updateReset() {
        selection.setCurrentPage(0);
        update();
    }

    /**
     * Checks whether the current slice of data has no items.
     *
     * @return Whether the current chunk is empty.
     */
    public boolean isEmpty() {
        return selection.getTotalSize() == 0;
    }

    /**
     * Returns the possible values for the number of items per page.
     *
     * @return The possible page size values.
     */
    public int[] pageSizeValues() {
        return ITEMS_PER_PAGE;
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
