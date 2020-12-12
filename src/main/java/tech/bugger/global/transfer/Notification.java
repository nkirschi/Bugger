package tech.bugger.global.transfer;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * DTO representing a notification.
 */
public class Notification implements Serializable {
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

    private int id;
    private User actuator;
    private User recipient;
    private Type type;
    private ZonedDateTime date;
    private boolean read;
    private boolean sent;

    private Topic topic;
    private Report report;
    private Post post;

    /**
     * Constructs a new notification from the speicified parameters.
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
    public Notification(int id, User actuator, User recipient, Type type, ZonedDateTime date, boolean read,
                        boolean sent, Topic topic, Report report, Post post) {
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
    public int getId() {
        return id;
    }

    /**
     * Returns the ID of this notification.
     *
     * @param id The notification ID to be set.
     */
    public void setId(int id) {
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
    public void setActuator(User actuator) {
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
    public void setRecipient(User recipient) {
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
    public void setType(Type type) {
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
    public void setDate(ZonedDateTime date) {
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
    public void setRead(boolean read) {
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
    public void setSent(boolean sent) {
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
    public void setTopic(Topic topic) {
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
    public void setReport(Report report) {
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
    public void setPost(Post post) {
        this.post = post;
    }

    /**
     * Indicates whether some {@code other} notification is semantically equal to this notification.
     *
     * @param other The object to compare this notification to.
     * @return {@code true} iff {@code other} is a semantically equivalent notification.
     */
    @Override
    public boolean equals(Object other) {
        // TODO Auto-generated method stub
        return super.equals(other);
    }

    /**
     * Calculates a hash code for this notification for hashing purposes, and to fulfil the {@link
     * Object#equals(Object)} contract.
     *
     * @return The hash code value of this notification.
     */
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    /**
     * Converts this notification into a human-readable string representation.
     *
     * @return A human-readable string representation of this notification.
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

}
