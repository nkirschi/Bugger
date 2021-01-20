package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * DTO representing a notification.
 */
public class Notification implements Serializable {

    @Serial
    private static final long serialVersionUID = 3155085031027042714L;

    /**
     * The available notification types.
     */
    public enum Type {
        /**
         * Notification when a new report has been created.
         */
        NEW_REPORT,

        /**
         * Notification when a report has been edited.
         */
        EDITED_REPORT,

        /**
         * Notification when a report has been moved.
         */
        MOVED_REPORT,

        /**
         * Notification when a new post has been created.
         */
        NEW_POST,

        /**
         * Notification when a post has been edited.
         */
        EDITED_POST
    }

    /**
     * The notification ID.
     */
    private Integer id;

    /**
     * The user responsible for the action causing this notification.
     */
    private Integer actuatorID;

    /**
     * The user receiving this notification.
     */
    private Integer recipientID;

    /**
     * The type of this notification.
     */
    private Type type;

    /**
     * The date of this notification.
     */
    private OffsetDateTime date;

    /**
     * Whether this notification was read.
     */
    private boolean read;

    /**
     * Whether this notification was sent.
     */
    private boolean sent;

    /**
     * The associated topic.
     */
    private Integer topicID;

    /**
     * The associated report.
     */
    private Integer reportID;

    /**
     * The associated post.
     */
    private Integer postID;

    /**
     * The username of the actuator.
     */
    private String actuatorUsername;

    /**
     * The title of the associated report.
     */
    private String reportTitle;

    /**
     * The recipient's e-mail address.
     */
    private String recipientMail;

    /**
     * Constructs a new notification from the specified parameters.
     *
     * @param id               The notification ID.
     * @param actuatorID       The notification actuator.
     * @param recipientID      The notification recipient.
     * @param type             The notification type.
     * @param date             The notification creation date.
     * @param read             Whether the notification is marked as read.
     * @param sent             Whether the notification is marked as send.
     * @param topicID          The associated topic.
     * @param reportID         The associated report.
     * @param postID           The associated post.
     * @param actuatorUsername The actuator's username.
     * @param reportTitle      The title of the associated report.
     * @param recipientMail    The recipient's e-mail address.
     */
    public Notification(final Integer id, final Integer actuatorID, final Integer recipientID, final Type type,
                        final OffsetDateTime date, final boolean read, final boolean sent, final Integer topicID,
                        final Integer reportID, final Integer postID, final String actuatorUsername,
                        final String reportTitle, final String recipientMail) {
        this.id = id;
        this.actuatorID = actuatorID;
        this.recipientID = recipientID;
        this.type = type;
        this.date = date;
        this.read = read;
        this.sent = sent;
        this.topicID = topicID;
        this.reportID = reportID;
        this.postID = postID;
        this.actuatorUsername = actuatorUsername;
        this.reportTitle = reportTitle;
        this.recipientMail = recipientMail;
    }

    /**
     * Constructs an empty notification.
     */
    public Notification() {
        this(null, null, null, null, null, false, false, null, null, null, null, null, null);
    }

    /**
     * Constructs a new notification as deep clone of the given notification.
     *
     * @param notification The user to clone.
     */
    public Notification(final Notification notification) {
        this(notification.id, notification.actuatorID, notification.recipientID, notification.type, notification.date,
                notification.read, notification.sent, notification.topicID, notification.reportID, notification.postID,
                notification.actuatorUsername, notification.reportTitle, notification.recipientMail);
    }

    /**
     * Returns the ID of this notification.
     *
     * @return The notification ID.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Returns the ID of this notification.
     *
     * @param id The notification ID to be set.
     */
    public void setId(final Integer id) {
        this.id = id;
    }


    /**
     * Returns the actuator of this notification.
     *
     * @return The notification actuator.
     */
    public Integer getActuatorID() {
        return actuatorID;
    }

    /**
     * Sets the actuator of this notification.
     *
     * @param actuatorID The notification actuator to be set.
     */
    public void setActuatorID(final Integer actuatorID) {
        this.actuatorID = actuatorID;
    }

    /**
     * Returns the recipient of this notification.
     *
     * @return The notification recipient.
     */
    public Integer getRecipientID() {
        return recipientID;
    }

    /**
     * Sets the recipient of this notification.
     *
     * @param recipientID The notification recipient to be set.
     */
    public void setRecipientID(final Integer recipientID) {
        this.recipientID = recipientID;
    }

    /**
     * Returns the type of this notification.
     *
     * @return The notification type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of this notification.
     *
     * @param type The notification type to be set.
     */
    public void setType(final Type type) {
        this.type = type;
    }

    /**
     * Returns the date this notification was created.
     *
     * @return The notification creation date.
     */
    public OffsetDateTime getDate() {
        return date;
    }

    /**
     * Sets the date this notification was created.
     *
     * @param date The notification creation date to be set.
     */
    public void setDate(final OffsetDateTime date) {
        this.date = date;
    }

    /**
     * Returns whether this notification has been read by its recipient.
     *
     * @return Whether this notification is marked as read.
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Sets whether this notification has been read by its recipient.
     *
     * @param read Whether this notification is marked as read.
     */
    public void setRead(final boolean read) {
        this.read = read;
    }

    /**
     * Returns whether this notification has already been sent.
     *
     * @return Whether this notification is marked as sent.
     */
    public boolean isSent() {
        return sent;
    }

    /**
     * Sets whether this notification has already been sent.
     *
     * @param sent Whether this notification is marked as sent.
     */
    public void setSent(final boolean sent) {
        this.sent = sent;
    }

    /**
     * Returns the topic this notification is associated with.
     *
     * @return The associated topic.
     */
    public Integer getTopicID() {
        return topicID;
    }

    /**
     * Sets the topic this notification is associated with.
     *
     * @param topicID The associated topic.
     */
    public void setTopicID(final Integer topicID) {
        this.topicID = topicID;
    }

    /**
     * Returns the report this notification is associated with.
     *
     * @return The associated report.
     */
    public Integer getReportID() {
        return reportID;
    }

    /**
     * Sets the report this notification is associated with.
     *
     * @param reportID The associated report.
     */
    public void setReportID(final Integer reportID) {
        this.reportID = reportID;
    }

    /**
     * Returns the post this notification is associated with.
     *
     * @return The associated post.
     */
    public Integer getPostID() {
        return postID;
    }

    /**
     * Sets the post this notification is associated with.
     *
     * @param postID The associated post.
     */
    public void setPostID(final Integer postID) {
        this.postID = postID;
    }

    /**
     * Returns the actuator's username.
     *
     * @return The actuator's username.
     */
    public String getActuatorUsername() {
        return actuatorUsername;
    }

    /**
     * Sets the actuator's username.
     *
     * @param actuatorUsername The actuator username to set.
     */
    public void setActuatorUsername(final String actuatorUsername) {
        this.actuatorUsername = actuatorUsername;
    }

    /**
     * Returns the title of the associated report.
     *
     * @return The report title.
     */
    public String getReportTitle() {
        return reportTitle;
    }

    /**
     * Sets the title of the associated report.
     *
     * @param reportTitle The report title to set.
     */
    public void setReportTitle(final String reportTitle) {
        this.reportTitle = reportTitle;
    }

    /**
     * Returns the recipient's e-mail address.
     *
     * @return The recipient's e-mail address.
     */
    public String getRecipientMail() {
        return recipientMail;
    }

    /**
     * Sets the recipient's e-mail address.
     *
     * @param recipientMail The e-mail address to set.
     */
    public void setRecipientMail(final String recipientMail) {
        this.recipientMail = recipientMail;
    }

    /**
     * Indicates whether some {@code other} notification is semantically equal to this notification.
     *
     * @param other The object to compare this notification to.
     * @return {@code true} iff {@code other} is a semantically equivalent notification.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Notification)) {
            return false;
        }
        Notification notification = (Notification) other;
        return Objects.equals(this.id, notification.id);
    }

    /**
     * Calculates a hash code for this notification for hashing purposes, and to fulfil the {@link
     * Object#equals(Object)} contract.
     *
     * @return The hash code value of this notification.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts this notification into a human-readable string representation.
     *
     * @return A human-readable string representation of this notification.
     */
    @Override
    public String toString() {
        return "Notification{ID = " + id + ", actuator = " + actuatorID + ", recipient = " + recipientID + ", type = "
                + type + ", date = " + date + ", read = " + read + ", sent = " + sent + ", topic = " + topicID
                + ", report = " + reportID + ", post = " + postID + "}";
    }

}
