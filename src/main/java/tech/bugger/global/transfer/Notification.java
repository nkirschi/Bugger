package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
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
    private User actuator;

    /**
     * The user receiving this notification.
     */
    private User recipient;

    /**
     * The type of this notification.
     */
    private Type type;

    /**
     * The date of this notification.
     */
    private ZonedDateTime date;

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
    private Topic topic;

    /**
     * The associated report.
     */
    private Report report;

    /**
     * The associated post.
     */
    private Post post;

    /**
     * Constructs a new notification from the specified parameters.
     *
     * @param id        The notification ID.
     * @param actuator  The notification actuator.
     * @param recipient The notification recipient.
     * @param type      The notification type.
     * @param date      The notification creation date.
     * @param read      Whether the notification is marked as read.
     * @param sent      Whether the notification is marked as send.
     * @param topic     The associated topic.
     * @param report    The associated report.
     * @param post      The associated post.
     */
    public Notification(final Integer id, final User actuator, final User recipient, final Type type,
                        final ZonedDateTime date, final boolean read, final boolean sent, final Topic topic,
                        final Report report, final Post post) {
        this.id = id;
        this.actuator = actuator;
        this.recipient = recipient;
        this.type = type;
        this.date = date;
        this.read = read;
        this.sent = sent;
        this.topic = topic;
        this.report = report;
        this.post = post;
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
    public User getActuator() {
        return actuator;
    }

    /**
     * Sets the actuator of this notification.
     *
     * @param actuator The notification actuator to be set.
     */
    public void setActuator(final User actuator) {
        this.actuator = actuator;
    }

    /**
     * Returns the recipient of this notification.
     *
     * @return The notification recipient.
     */
    public User getRecipient() {
        return recipient;
    }

    /**
     * Sets the recipient of this notification.
     *
     * @param recipient The notification recipient to be set.
     */
    public void setRecipient(final User recipient) {
        this.recipient = recipient;
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
    public ZonedDateTime getDate() {
        return date;
    }

    /**
     * Sets the date this notification was created.
     *
     * @param date The notification creation date to be set.
     */
    public void setDate(final ZonedDateTime date) {
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
    public Topic getTopic() {
        return topic;
    }

    /**
     * Sets the topic this notification is associated with.
     *
     * @param topic The associated topic.
     */
    public void setTopic(final Topic topic) {
        this.topic = topic;
    }

    /**
     * Returns the report this notification is associated with.
     *
     * @return The associated report.
     */
    public Report getReport() {
        return report;
    }

    /**
     * Sets the report this notification is associated with.
     *
     * @param report The associated report.
     */
    public void setReport(final Report report) {
        this.report = report;
    }

    /**
     * Returns the post this notification is associated with.
     *
     * @return The associated post.
     */
    public Post getPost() {
        return post;
    }

    /**
     * Sets the post this notification is associated with.
     *
     * @param post The associated post.
     */
    public void setPost(final Post post) {
        this.post = post;
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
        return "Notification{ID = " + id + ", actuator = " + actuator + ", recipient = " + recipient + ", type = "
                + type + ", date = " + date + ", read = " + read + ", sent = " + sent + ", topic = " + topic
                + ", report = " + report + ", post = " + post + "}";
    }

}
