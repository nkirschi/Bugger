package tech.bugger.global.transfer;

import java.io.Serializable;

/**
 * DTO representing pagination selection data.
 */
public class Selection implements Serializable {

    private static final long serialVersionUID = -4984947542876923184L;

    private int totalSize;
    private int currentPage;
    private int pageSize;
    private String sortedBy;
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
    public Selection(int totalSize, int currentPage, int pageSize, String sortedBy, boolean ascending) {
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
    public void setTotalSize(int totalSize) {
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
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    /**
     * Returns the page size of this selection.
     *
     * @return The selection page size.
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Sets the page size of this selection.
     *
     * @param pageSize The selection page size to be set.
     */
    public void setPageSize(int pageSize) {
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
    public void setSortedBy(String sortedBy) {
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
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Indicates whether some {@code other} selection is semantically equal to this selection.
     *
     * @param other The object to compare this selection to.
     * @return {@code true} iff {@code other} is a semantically equivalent selection.
     */
    @Override
    public boolean equals(Object other) {
        return false;
    }

    /**
     * Calculates a hash code for this selection for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this selection.
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /**
     * Converts this selection into a human-readable string representation.
     *
     * @return A human-readable string representation of this selection.
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

}
