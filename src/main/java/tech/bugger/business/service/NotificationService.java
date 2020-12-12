package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.util.List;

/**
 * Service providing methods related to notifications. A {@code Feedback} event is fired, if unexpected circumstances
 * occur.
 */
@Dependent
public class NotificationService {

    private static final Log log = Log.forClass(NotificationService.class);

    @Inject
    @Any
    Event<Feedback> feedback;

    /**
     * Irreversibly deletes a notification. Fires a {@code Feedback}-Event if something goes wrong.
     *
     * @param notification The notification to be deleted.
     */
    public void deleteNotification(Notification notification) {
    }

    /**
     * Marks a notification as read.
     *
     * @param notification The notification to be marked as read.
     */
    public void markAsRead(Notification notification) {

    }

    /**
     * Returns the number of existing notifications for one particular user, both read and unread.
     *
     * @param user The user in question.
     * @return The number of notifications as an {@code int}.
     */
    public int getNumberOfNotificationsFor(User user) {
        return 0;
    }

    /**
     * Returns selected notifications for one particular user.
     *
     * @param user      The user whose notifications are to be returned.
     * @param selection Information on which notifications to return.
     * @return A list containing the requested notifications.
     */
    public List<Notification> getNotificationsFor(User user, Selection selection) {
        return null;
    }

    /**
     * Creates a new notification in the data storage for every user affected and queues sending the corresponding
     * e-mails. Which users are affected is inferred from the data in the parameter notification.
     *
     * @param notification The notification based on which new notifications are to be created.
     */
    public void createNotification(Notification notification) {
    }

    /**
     * Queues sending of e-mails for notifications which have not yet been sent.
     */
    public void processUnsentNotifications() {

    }
}
