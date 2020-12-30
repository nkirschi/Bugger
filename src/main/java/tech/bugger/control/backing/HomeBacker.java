package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.NotificationService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Selection;
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

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(HomeBacker.class);

    @Serial
    private static final long serialVersionUID = -6982333692294902179L;

    /**
     * The paginated inbox containing all notifications for the user.
     */
    private Paginator<Notification> inbox;

    /**
     * The paginated list of all topics.
     */
    private Paginator<Topic> topics;

    /**
     * The notification to be deleted.
     */
    private Notification notificationToBeDeleted;

    /**
     * The notification to be marked as read.
     */
    private Notification notificationRead;

    /**
     * The session containing the currently logged in user.
     */
    private UserSession session;

    /**
     * The service performing tasks concerning notifications.
     */
    private transient NotificationService notificationService;

    /**
     * The service performing tasks concerning topics.
     */
    private transient TopicService topicService;

    /**
     * Constructs a new home page backing bean.
     *
     * @param session The current user session.
     * @param notificationService The notification service to use.
     * @param topicService The topic service to use.
     */
    @Inject
    public HomeBacker(final UserSession session, final NotificationService notificationService,
                      final TopicService topicService) {
        this.session = session;
        this.notificationService = notificationService;
        this.topicService = topicService;
    }

    /**
     * Constructs a new home page backing bean.
     *
     * @param session The current user session.
     * @param notificationService The notification service to use.
     * @param topicService The topic service to use.
     */
    public HomeBacker(final UserSession session, final NotificationService notificationService,
                      final TopicService topicService) {
        this.session = session;
        this.notificationService = notificationService;
        this.topicService = topicService;
    }

    /**
     * Constructs a new home page backing bean.
     *
     * @param session The current user session.
     * @param notificationService The notification service to use.
     * @param topicService The topic service to use.
     */
    public HomeBacker(final UserSession session, final NotificationService notificationService,
                      final TopicService topicService) {
        this.session = session;
        this.notificationService = notificationService;
        this.topicService = topicService;
    }

    /**
     * Initializes the paginators for notifications and topics as inner classes.
     */
    @PostConstruct
    void init() {
        topics = new Paginator<Topic>("", Selection.PageSize.NORMAL) {
            @Override
            protected Iterable<Topic> fetch() {
                return topicService.getSelectedTopics(getSelection());
            }

            @Override
            protected int totalSize() {
                return topicService.getNumberOfTopics();
            }
        };
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
    public ZonedDateTime lastChange(final Topic topic) {
        return topicService.lastChange(topic);
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
    public void setNotificationToBeDeleted(final Notification notificationToBeDeleted) {
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
    public void setNotificationRead(final Notification notificationRead) {
        this.notificationRead = notificationRead;
    }

}
