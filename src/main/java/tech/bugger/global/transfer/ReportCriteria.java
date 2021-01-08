package tech.bugger.global.transfer;

import java.time.OffsetDateTime;

/**
 * Common criteria for filtering reports.
 */
public class ReportCriteria {

    private String topic;

    private OffsetDateTime latestOpeningDate;

    private OffsetDateTime earliestClosingDate;

    public ReportCriteria(String topic, OffsetDateTime latestOpeningDate, OffsetDateTime earliestClosingDate) {
        this.topic = topic;
        this.latestOpeningDate = latestOpeningDate;
        this.earliestClosingDate = earliestClosingDate;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public OffsetDateTime getLatestOpeningDate() {
        return latestOpeningDate;
    }

    public void setLatestOpeningDate(OffsetDateTime latestOpeningDate) {
        this.latestOpeningDate = latestOpeningDate;
    }

    public OffsetDateTime getEarliestClosingDate() {
        return earliestClosingDate;
    }

    public void setEarliestClosingDate(OffsetDateTime earliestClosingDate) {
        this.earliestClosingDate = earliestClosingDate;
    }

    @Override
    public String toString() {
        return "ReportCriteria{" +
                "topic='" + topic + '\'' +
                ", latestOpeningDate=" + latestOpeningDate +
                ", earliestClosingDate=" + earliestClosingDate +
                '}';
    }

}
