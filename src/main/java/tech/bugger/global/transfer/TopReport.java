package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO representing a top report entry.
 */
public final class TopReport implements Serializable {

    @Serial
    private static final long serialVersionUID = 5819489567437071885L;

    /**
     * The report ID.
     */
    private final int id;

    /**
     * The report title.
     */
    private final String title;

    /**
     * The report creator username.
     */
    private final String creator;

    /**
     * The relevance recently gained by the report.
     */
    private final int relevanceGain;

    /**
     * Constructs a new top report from  the specified parameters.
     *
     * @param id            The report ID.
     * @param title         The report title.
     * @param creator       The report creator username.
     * @param relevanceGain The relevance recently gained by the report.
     */
    public TopReport(final int id, final String title, final String creator, final int relevanceGain) {
        this.id = id;
        this.title = title;
        this.creator = creator;
        this.relevanceGain = relevanceGain;
    }

    /**
     * Return the ID of this top report.
     *
     * @return The report ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the title of this top report.
     *
     * @return The report title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the creator username of this top report.
     *
     * @return The report creator.
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Returns the relevance recently gained by this top report.
     *
     * @return The recent relevance gain.
     */
    public int getRelevanceGain() {
        return relevanceGain;
    }

    /**
     * Indicates whether some {@code other} top report is semantically equal to this top report.
     *
     * @param other The object to compare this top report to.
     * @return {@code true} iff {@code other} is a semantically equivalent top report.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TopReport)) {
            return false;
        }
        TopReport topReport = (TopReport) other;
        return id == topReport.id
                && relevanceGain == topReport.relevanceGain
                && title.equals(topReport.title)
                && creator.equals(topReport.creator);
    }

    /**
     * Calculates a hash code for this top report for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this top report.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, title, creator, relevanceGain);
    }

    /**
     * Converts this top report into a human-readable string representation.
     *
     * @return A human-readable string representation of this top report.
     */
    @Override
    public String toString() {
        return "TopReport{"
                + "id=" + id
                + ", title='" + title + '\''
                + ", creator='" + creator + '\''
                + ", relevanceGain=" + relevanceGain
                + '}';
    }

}
