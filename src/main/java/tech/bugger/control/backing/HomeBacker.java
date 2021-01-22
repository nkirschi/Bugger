package tech.bugger.control.backing;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.NotificationService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;

/**
 * Backing Bean for the home page.
 */
@ViewScoped
@Named
public class HomeBacker implements Serializable {

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
     * The session containing the currently logged in user.
     */
    private final UserSession session;

    /**
     * The service performing tasks concerning notifications.
     */
    private final NotificationService notificationService;

    /**
     * The service performing tasks concerning topics.
     */
    private final TopicService topicService;

    /**
     * The current external context.
     */
    private final ExternalContext ectx;

    /**
     * Constructs a new home page backing bean.
     *
     * @param session             The current user session.
     * @param notificationService The notification service to use.
     * @param topicService        The topic service to use.
     * @param ectx                The current external context.
     */
    @Inject
    public HomeBacker(final UserSession session,
                      final NotificationService notificationService,
                      final TopicService topicService,
                      final ExternalContext ectx) {
        this.session = session;
        this.notificationService = notificationService;
        this.topicService = topicService;
        this.ectx = ectx;
    }

    /**
     * Initializes the paginators for notifications and topics as inner classes.
     */
    @PostConstruct
    void init() {
        if (session.getUser() != null) {
            inbox = new Paginator<>("created_at", Selection.PageSize.SMALL, false) {
                @Override
                protected Iterable<Notification> fetch() {
                    return notificationService.selectNotifications(session.getUser(), getSelection());
                }

                @Override
                protected int totalSize() {
                    return notificationService.countNotifications(session.getUser());
                }
            };
        }
        topics = new Paginator<>("id", Selection.PageSize.NORMAL) {
            @Override
            protected Iterable<Topic> fetch() {
                return topicService.selectTopics(getSelection());
            }

            @Override
            protected int totalSize() {
                return topicService.countTopics();
            }
        };
    }

    /**
     * Irreversibly deletes the notification.
     *
     * @param notification The notification to be deleted.
     * @return {@code null}
     */
    public String deleteNotification(final Notification notification) {
        notificationService.deleteNotification(notification);
        inbox.updateReset();
        return null;
    }

    /**
     * Marks the notification as read and redirects the user to the area of interest.
     *
     * @param notification The notification to be opened.
     * @return A String that is used to redirect a user to the post of the opened notification.
     */
    public String openNotification(final Notification notification) {
        notificationService.markAsRead(notification);
        String query = "/report?";
        if (notification.getPostID() != null) {
            query += "p=" + notification.getPostID() + "#post-" + notification.getPostID();
        } else {
            query += "id=" + notification.getReportID();
        }

        try {
            ectx.redirect(ectx.getApplicationContextPath() + query);
        } catch (IOException e) {
            return "pretty:error";
        }
        return null;
    }

    /**
     * Checks if the user of the current {@code UserSession} is subscribed to the specified topic.
     *
     * @param topic The topic in question.
     * @return {@code true} if the user is subscribed to the topic, {@code false} otherwise.
     */
    public boolean isSubscribed(final Topic topic) {
        return topicService.isSubscribed(session.getUser(), topic);
    }

    /**
     * Returns the time stamp of the last action in one particular topic. Creating, editing and moving a report as well
     * as creating and editing posts count as actions. Moving a report is an action in the destination topic only.
     *
     * @param topic The topic in question.
     * @return The time stamp of the last action as a {@link OffsetDateTime}.
     */
    public OffsetDateTime lastChange(final Topic topic) {
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

}
