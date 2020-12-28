package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.NotificationService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Backing Bean for the home page.
 */
@ViewScoped
@Named
public class HomeBacker implements Serializable {
    @Serial
    private static final long serialVersionUID = -6982333692294902179L;
    private static final Log log = Log.forClass(HomeBacker.class);

    private Paginator<Notification> inbox;
    private Paginator<Topic> topics;
    private Notification notificationToBeDeleted;
    private Notification notificationRead;

    @Inject
    private UserSession session;

    @Inject
    private transient NotificationService notificationService;

    @Inject
    private transient TopicService topicService;

    /**
     * Initializes the paginators for notifications and topics as inner classes.
     */
    @PostConstruct
    public void init() {

    }

    /**
     * Creates a FacesMessage to display if an event is fired in one of the injected services.
     *
     * @param feedback The feedback with details on what to display.
     */
    public void displayFeedback(@Observes @Any Feedback feedback) {

    }

    /**
     * Irreversibly deletes the current {@code notificationToBeDeleted}.
     */
    public void deleteNotification() {

    }

    /**
     * Marks the current {@code notificationRead} as read.
     *
     * @return A String that is used to redirect a user to the post of the opened notification.
     */
    public String openNotification() {
        return null;
    }

    /**
     * Checks if the user of the current {@code UserSession} is subscribed to the specified topic.
     *
     * @param topic The topic in question.
     * @return {@code true} if the user is subscribed to the topic, {@code false} otherwise.
     */
    public boolean isSubscribed(Topic topic) {
        return false;
    }

    /**
     * Returns the time stamp of the last action in one particular topic. Creating, editing and moving a report as well
     * as creating and editing posts count as actions. Moving a report is an action in the destination topic only.
     *
     * @param topic The topic in question.
     * @return The time stamp of the last action as a {@code ZonedDateTime}.
     */
    public ZonedDateTime lastChange(Topic topic) {
        return null;
    }

    /**
     * @return The inbox.
     */
    public Paginator<Notification> getInbox() {
        return inbox;
    }

    /**
     * @return The topics.
     */
    public Paginator<Topic> getTopics() {
        return topics;
    }

    /**
     * @return The notificationToBeDeleted.
     */
    public Notification getNotificationToBeDeleted() {
        return notificationToBeDeleted;
    }

    /**
     * @param notificationToBeDeleted The notificationToBeDeleted to set.
     */
    public void setNotificationToBeDeleted(Notification notificationToBeDeleted) {
        this.notificationToBeDeleted = notificationToBeDeleted;
    }

    /**
     * @return The notificationRead.
     */
    public Notification getNotificationRead() {
        return notificationRead;
    }

    /**
     * @param notificationRead The notificationRead to set.
     */
    public void setNotificationRead(Notification notificationRead) {
        this.notificationRead = notificationRead;
    }
}
