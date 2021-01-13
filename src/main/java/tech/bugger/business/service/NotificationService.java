package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.PriorityExecutor;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.util.Mailer;
import tech.bugger.persistence.util.Transaction;
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
     * Resource bundle for interaction messages.
     */
    private final ResourceBundle interactionsBundle;

    /**
     * The {@link PriorityExecutor} instance to use when sending e-mails.
     */
    private final PriorityExecutor priorityExecutor;

    /**
     * The {@link Mailer} instance to use when sending e-mails.
     */
    private final Mailer mailer;

    /**
     * Constructs a new notification service with the given dependencies.
     *
     * @param transactionManager The transaction manager to use for creating transactions.
     * @param feedbackEvent The feedback event to use for user feedback.
     * @param messagesBundle The resource bundle for feedback messages.
     * @param interactionsBundle The resource bundle for interaction messages.
     * @param priorityExecutor   The priority executor to use when sending e-mails.
     * @param mailer             The mailer to use.
     */
    @Inject
    public NotificationService(final TransactionManager transactionManager, final Event<Feedback> feedbackEvent,
                               final @RegistryKey("messages") ResourceBundle messagesBundle,
                               @RegistryKey("interactions") final ResourceBundle interactionsBundle,
                               @RegistryKey("mails") final PriorityExecutor priorityExecutor,
                               @RegistryKey("main") final Mailer mailer) {
        this.transactionManager = transactionManager;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
        this.interactionsBundle = interactionsBundle;
        this.priorityExecutor = priorityExecutor;
        this.mailer = mailer;
    }

    /**
     * Irreversibly deletes a notification. Fires a {@code Feedback}-Event if something goes wrong.
     *
     * @param notification The notification to be deleted.
     */
    public void deleteNotification(final Notification notification) {
        try (Transaction tx = transactionManager.begin()) {
            tx.newNotificationGateway().delete(notification);
            tx.commit();
        } catch (NotFoundException e) {
           log.error("Could not find notification to delete " + notification + ".", e);
           feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error when deleting notification " + notification + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
    }

    /**
     * Marks a notification as read.
     *
     * @param notification The notification to be marked as read.
     */
    public void markAsRead(final Notification notification) {
        notification.setRead(false);
        try (Transaction tx = transactionManager.begin()) {
            tx.newNotificationGateway().update(notification);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Could not find notification to mark as read " + notification + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error when marking notification " + notification + " as sent.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
    }

    /**
     * Returns the number of existing notifications for one particular user, both read and unread.
     *
     * @param user The user in question.
     * @return The number of notifications as an {@code int}.
     */
    public int countNotifications(final User user) {
        int numberOfNotifications = 0;
        try (Transaction tx = transactionManager.begin()) {
            numberOfNotifications = tx.newNotificationGateway().countNotifications(user);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when counting notifications for user " + user + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return numberOfNotifications;
    }

    /**
     * Returns selected notifications for one particular user.
     *
     * @param user      The user whose notifications are to be returned.
     * @param selection Information on which notifications to return.
     * @return A list containing the requested notifications.
     */
    public List<Notification> selectNotifications(final User user, final Selection selection) {
        List<Notification> selectedNotifications;
        try (Transaction tx = transactionManager.begin()) {
            selectedNotifications = tx.newNotificationGateway().selectNotifications(user, selection);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when selecting notifications for user " + user + "with selection " + selection + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
            selectedNotifications = null;
        }
        return selectedNotifications;
    }

    /**
     * Creates a new notification in the data storage for every user affected and queues sending the corresponding
     * e-mails. Which users are affected is inferred from the data in the parameter notification.
     *
     * @param notification The notification based on which new notifications are to be created.
     */
    public void createNotification(final Notification notification) {
        try (Transaction tx = transactionManager.begin()) {
            tx.newNotificationGateway().create(notification);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when creating notification " + notification + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
    }

    /**
     * Queues sending of e-mails for notifications which have not yet been sent.
     */
    public void processUnsentNotifications() {
        List<Notification> unsentNotifications;
        try (Transaction tx = transactionManager.begin()) {
            unsentNotifications = tx.newNotificationGateway().getUnsentNotifications();
            tx.commit();
        } catch (TransactionException e) {
            return;
        }
        for (Notification notification : unsentNotifications) {
            User recipient;
            try (Transaction tx = transactionManager.begin()) {
                recipient = tx.newUserGateway().getUserByID(notification.getRecipientID());
            } catch (NotFoundException e) {
                continue;
            }
            String email = recipient.getEmailAddress();
        }
    }

}
