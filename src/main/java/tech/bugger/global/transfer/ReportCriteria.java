package tech.bugger.global.transfer;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Common criteria for filtering reports.
 */
public class ReportCriteria implements Serializable {

    /**
     * The topic to only consider reports in.
     */
    private String topic;

    /**
     * The latest allowed creation date for reports to be considered.
     */
    private OffsetDateTime latestOpeningDate;

    /**
     * The earliest allowed closing date for reports to be considered.
     */
    private OffsetDateTime earliestClosingDate;

    /**
     * Constructs a new report criteria representation.
     *
     * @param topic               The topic to only consider reports in.
     * @param latestOpeningDate   The latest allowed creation date for reports to be considered.
     * @param earliestClosingDate The earliest allowed closing date for reports to be considered.
     */
    public ReportCriteria(final String topic, final OffsetDateTime latestOpeningDate,
                          final OffsetDateTime earliestClosingDate) {
        this.topic = topic;
        this.latestOpeningDate = latestOpeningDate;
        this.earliestClosingDate = earliestClosingDate;
    }

    /**
     * Returns the topic to only consider reports in.
     *
     * @return The topic filter.
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Sets the topic to only consider reports in.
     *
     * @param topic The topic filter to set.
     */
    public void setTopic(final String topic) {
        this.topic = topic;
    }

    /**
     * Returns the latest allowed creation date for reports to be considered.
     *
     * @return The latest report opening filter.
     */
    public OffsetDateTime getLatestOpeningDate() {
        return latestOpeningDate;
    }

    /**
     * Sets the latest allowed creation date for reports to be considered.
     *
     * @param latestOpeningDate The latest report opening filter to set.
     */
    public void setLatestOpeningDate(final OffsetDateTime latestOpeningDate) {
        this.latestOpeningDate = latestOpeningDate;
    }

    /**
     * Returns the earliest allowed closing date for reports to be considered.
     *
     * @return The earliest report closing filter.
     */
    public OffsetDateTime getEarliestClosingDate() {
        return earliestClosingDate;
    }

    /**
     * Sets the earliest allowed closing date for reports to be considered.
     *
     * @param earliestClosingDate The earliest report closing filter to set.
     */
    public void setEarliestClosingDate(final OffsetDateTime earliestClosingDate) {
        this.earliestClosingDate = earliestClosingDate;
    }

    /**
     * Converts this report criteria container into a human-readable string representation.
     *
     * @return A human-readable string representation of this report criteria container.
     */
    @Override
    public String toString() {
        return "ReportCriteria{"
                + "topic='" + topic + '\''
                + ", latestOpeningDate=" + latestOpeningDate
                + ", earliestClosingDate=" + earliestClosingDate
                + '}';
    }

}
