package tech.bugger.global.transfer;

import tech.bugger.global.util.Lazy;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * DTO representing a report.
 */
public class Report implements Serializable {
    @Serial
    private static final long serialVersionUID = -8063035819918388897L;

    /**
     * The available report types.
     */
    public enum Type {
        /**
         * A bug report.
         */
        BUG,

        /**
         * A feature request.
         */
        FEATURE,

        /**
         * A hint to something.
         */
        HINT
    }

    /**
     * The available severity levels.
     */
    public enum Severity {
        /**
         * A low severity.
         */
        MINOR,

        /**
         * A medium severity.
         */
        RELEVANT,

        /**
         * A high severity.
         */
        SEVERE
    }


    private int id;
    private String title;
    private Type type;
    private Severity severity;
    private String version;
    private Authorship authorship;
    private ZonedDateTime closingDate;
    private Lazy<Report> duplicateOf;
    private Integer forcedRelevance;
    private Lazy<Topic> topic;

    /**
     * Returns the ID of this report.
     *
     * @return The report ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of this report.
     *
     * @param id The report ID to be set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the title of this report.
     *
     * @return The report title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this report.
     *
     * @param title The report title to be set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the type of this report.
     *
     * @return The report type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of this report.
     *
     * @param type The report type to be set.
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Returns the severity of this report.
     *
     * @return The report severity.
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Sets the severity of this report.
     *
     * @param severity The report severity to be set.
     */
    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    /**
     * Returns the version this report is associated with.
     *
     * @return The associated version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version this report is associated with.
     *
     * @param version The associated version to be set.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the manipulation metadata of this report.
     *
     * @return The report authorship metadata.
     */
    public Authorship getAuthorship() {
        return authorship;
    }

    /**
     * Sets the manipulation metadata of this report.
     *
     * @param authorship The report authorship metadata to be set.
     */
    public void setAuthorship(Authorship authorship) {
        this.authorship = authorship;
    }

    /**
     * Returns the data this report was closed.
     *
     * @return The report closing date.
     */
    public ZonedDateTime getClosingDate() {
        return closingDate;
    }

    /**
     * Returns the data this report was closed.
     *
     * @param closingDate The report closing date.
     */
    public void setClosingDate(ZonedDateTime closingDate) {
        this.closingDate = closingDate;
    }

    /**
     * Returns the report this report is a duplicate of.
     *
     * @return The original report or {@code null} if this is no duplicate.
     */
    public Lazy<Report> getDuplicateOf() {
        return duplicateOf;
    }

    /**
     * Sets the report this report is a duplicate of.
     *
     * @param duplicateOf The original report to be set
     */
    public void setDuplicateOf(Lazy<Report> duplicateOf) {
        this.duplicateOf = duplicateOf;
    }

    /**
     * Returns the relevance value to override the calculated relevance.
     *
     * @return The forced relevance or {@code null} if it is not overridden.
     */
    public Integer getForcedRelevance() {
        return forcedRelevance;
    }

    /**
     * Sets the relevance value to override the calculated relevance.
     *
     * @param forcedRelevance The forced relevance to be set.
     */
    public void setForcedRelevance(Integer forcedRelevance) {
        this.forcedRelevance = forcedRelevance;
    }

    /**
     * Returns the topic this report belongs to.
     *
     * @return The associated topic.
     */
    public Lazy<Topic> getTopic() {
        return topic;
    }

    /**
     * Sets the topic this report belongs to.
     *
     * @param topic The associated topic to be set.
     */
    public void setTopic(Lazy<Topic> topic) {
        this.topic = topic;
    }

    /**
     * Indicates whether some {@code other} report is semantically equal to this report.
     *
     * @param other The object to compare this report to.
     * @return {@code true} iff {@code other} is a semantically equivalent report.
     */
    @Override
    public boolean equals(Object other) {
        // TODO Auto-generated method stub
        return super.equals(other);
    }

    /**
     * Calculates a hash code for this report for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this report.
     */
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    /**
     * Converts this report into a human-readable string representation.
     *
     * @return A human-readable string representation of this report.
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

}
