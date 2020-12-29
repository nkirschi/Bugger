package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO representing pagination selection data.
 */
public class Selection implements Serializable {

    @Serial
    private static final long serialVersionUID = -4984947542876923184L;

    public enum PageSize {

        /**
         * A small amount of entries per page.
         */
        SMALL(10),

        /**
         * A normal amount of entries per page (could be considered as default value in most cases).
         */
        NORMAL(20),

        /**
         * A large amount of entries per page.
         */
        LARGE(50),

        /**
         * A huge amount of entries per page.
         */
        HUGE(100);

        /**
         * The amount of entries per page.
         */
        private final int size;

        /**
         * Constructs a new enum representing a valid setting for entries per page.
         *
         * @param size The amount of entries per page.
         */
        PageSize(final int size) {
            this.size = size;
        }

        /**
         * Returns the amount of entries per page.
         *
         * @return The amount of entries per page.
         */
        public int getSize() {
            return size;
        }

    }

    /**
     * The total number of entries.
     */
    private int totalSize;

    /**
     * The currently shown page.
     */
    private int currentPage;

    /**
     * The maximum number of entries per page.
     */
    private PageSize pageSize;

    /**
     * The key of the column to sort by.
     */
    private String sortedBy;

    /**
     * Whether to sort in ascending or descending order.
     */
    private boolean ascending;

    /**
     * Constructs a pagination selection from the specified parameters.
     *
     * @param totalSize   The total data length.
     * @param currentPage The current page.
     * @param pageSize    The selection page size.
     * @param sortedBy    The column to be sorted by.
     * @param ascending   Whether to sort in ascending order.
     */
    public Selection(final int totalSize, final int currentPage, final PageSize pageSize, final String sortedBy,
                     final boolean ascending) {
        this.totalSize = totalSize;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.sortedBy = sortedBy;
        this.ascending = ascending;
    }

    /**
     * Returns the total size of the base data being paginated.
     *
     * @return The total data length.
     */
    public int getTotalSize() {
        return totalSize;
    }

    /**
     * Sets the total size of the base data being paginated.
     *
     * @param totalSize The total data length to be set.
     */
    public void setTotalSize(final int totalSize) {
        this.totalSize = totalSize;
    }

    /**
     * Returns the current page of this selection.
     *
     * @return The current page.
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Sets the current page of this selection.
     *
     * @param currentPage The current page to be set.
     */
    public void setCurrentPage(final int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * Returns the page size of this selection.
     *
     * @return The selection page size.
     */
    public PageSize getPageSize() {
        return pageSize;
    }

    /**
     * Sets the page size of this selection.
     *
     * @param pageSize The selection page size to be set.
     */
    public void setPageSize(final PageSize pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Returns the identifier of the column by which this selection is to be sorted.
     *
     * @return The selection sort column identifier.
     */
    public String getSortedBy() {
        return sortedBy;
    }

    /**
     * Sets the identifier of the column by which this selection is to be sorted.
     *
     * @param sortedBy The selection sort column identifier to be set.
     */
    public void setSortedBy(final String sortedBy) {
        this.sortedBy = sortedBy;
    }

    /**
     * Returns whether this selection is to be sorted in ascending order.
     *
     * @return Whether to sort ascending.
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Sets whether this selection is to be sorted in ascending order.
     *
     * @param ascending Whether to sort ascending.
     */
    public void setAscending(final boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Indicates whether some {@code other} selection is semantically equal to this selection.
     *
     * @param other The object to compare this selection to.
     * @return {@code true} iff {@code other} is a semantically equivalent selection.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Selection)) {
            return false;
        }

        Selection sel = (Selection) other;
        return totalSize == sel.totalSize
                && currentPage == sel.currentPage
                && pageSize == sel.pageSize
                && ascending == sel.ascending
                && Objects.equals(sortedBy, sel.sortedBy);
    }

    /**
     * Calculates a hash code for this selection for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this selection.
     */
    @Override
    public int hashCode() {
        return Objects.hash(totalSize, currentPage, pageSize, sortedBy, ascending);
    }

    /**
     * Converts this selection into a human-readable string representation.
     *
     * @return A human-readable string representation of this selection.
     */
    @Override
    public String toString() {
        return "Selection{"
                + "totalSize=" + totalSize
                + ", currentPage=" + currentPage
                + ", pageSize=" + pageSize
                + ", sortedBy='" + sortedBy + '\''
                + ", ascending=" + ascending
                + '}';
    }

}
