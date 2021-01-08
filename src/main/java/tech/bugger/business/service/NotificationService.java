package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Service providing methods related to notifications. A {@code Feedback} event is fired, if unexpected circumstances
 * occur.
 */
@ApplicationScoped
public class NotificationService implements Serializable {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(NotificationService.class);

    /**
     * Transaction manager used for creating transactions.
     */
    private final TransactionManager transactionManager;

    /**
     * Feedback Event for user feedback.
     */
    private final Event<Feedback> feedbackEvent;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new notification service with the given dependencies.
     *
     * @param transactionManager The transaction manager to use for creating transactions.
     * @param feedbackEvent The feedback event to use for user feedback.
     * @param messagesBundle The resource bundle for feedback messages.
     */
    @Inject
    public NotificationService(final TransactionManager transactionManager, final Event<Feedback> feedbackEvent,
                               final @RegistryKey("messages") ResourceBundle messagesBundle) {
        this.transactionManager = transactionManager;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
    }

    /**
     * Irreversibly deletes a notification. Fires a {@code Feedback}-Event if something goes wrong.
     *
     * @param notification The notification to be deleted.
     */
    public void deleteNotification(final Notification notification) {
    }

    /**
     * Marks a notification as read.
     *
     * @param notification The notification to be marked as read.
     */
    public void markAsRead(final Notification notification) {

    }

    /**
     * Returns the number of existing notifications for one particular user, both read and unread.
     *
     * @param user The user in question.
     * @return The number of notifications as an {@code int}.
     */
    public int getNumberOfNotificationsFor(final User user) {
        return 0;
    }

    /**
     * Returns selected notifications for one particular user.
     *
     * @param user      The user whose notifications are to be returned.
     * @param selection Information on which notifications to return.
     * @return A list containing the requested notifications.
     */
    public List<Notification> getNotificationsFor(final User user, final Selection selection) {
        return null;
    }

    /**
     * Creates a new notification in the data storage for every user affected and queues sending the corresponding
     * e-mails. Which users are affected is inferred from the data in the parameter notification.
     *
     * @param notification The notification based on which new notifications are to be created.
     */
    public void createNotification(final Notification notification) {
    }

    /**
     * Queues sending of e-mails for notifications which have not yet been sent.
     */
    public void processUnsentNotifications() {

    }

}
