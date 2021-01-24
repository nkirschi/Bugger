package tech.bugger.business.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.PriorityExecutor;
import tech.bugger.business.util.PriorityTask;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.control.util.JFConfig;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.UserGateway;
import tech.bugger.persistence.util.Mail;
import tech.bugger.persistence.util.Mailer;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

/**
 * Service providing methods related to notifications. A {@code Feedback} event is fired, if unexpected circumstances
 * occur.
 */
@ApplicationScoped
public class NotificationService {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(NotificationService.class);

    /**
     * The maximum number of tries before sending an e-mail is aborted.
     */
    private static final int MAX_EMAIL_TRIES = 3;

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
     * @param feedbackEvent      The feedback event to use for user feedback.
     * @param messagesBundle     The resource bundle for feedback messages.
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
        notification.setRead(true);
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
        if (notification == null) {
            log.error("Cannot create notification null.");
            throw new IllegalArgumentException("Notification cannot be null.");
        } else if (notification.getReportID() == null) {
            log.error("Cannot create notification without report.");
            throw new IllegalArgumentException("Report ID cannot be null.");
        } else if (notification.getTopicID() == null) {
            log.error("Cannot create notification without topic.");
            throw new IllegalArgumentException("Topic ID cannot be null.");
        }

        List<Notification> notifications;
        try (Transaction tx = transactionManager.begin()) {
            UserGateway gateway = tx.newUserGateway();
            User causer = new User();
            causer.setId(notification.getActuatorID());
            Set<User> affectedUsers = new HashSet<>(gateway.getSubscribersOf(causer));
            Report report = new Report();
            report.setId(notification.getReportID());
            affectedUsers.addAll(gateway.getSubscribersOf(report));
            Topic topic = new Topic();
            topic.setId(notification.getTopicID());
            affectedUsers.addAll(gateway.getSubscribersOf(topic));
            affectedUsers.remove(causer);
            notifications = new ArrayList<>(affectedUsers.size());
            for (User user : affectedUsers) {
                Notification n = new Notification(notification);
                n.setRecipientID(user.getId());
                n.setRecipientMail(user.getEmailAddress());
                notifications.add(n);
            }
            tx.newNotificationGateway().createNotificationBulk(notifications);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when creating notification " + notification + ".", e);
            return;
            // feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        sendMails(notifications);
    }

    /**
     * Queues sending of e-mails for notifications which have not yet been sent.
     */
    public void processUnsentNotifications() {
        // TODO Ben: Call this method somewhere? Or is this not needed anymore?
        List<Notification> unsentNotifications;
        try (Transaction tx = transactionManager.begin()) {
            unsentNotifications = tx.newNotificationGateway().getUnsentNotifications();
            tx.commit();
        } catch (TransactionException e) {
            return;
        }
        sendMails(unsentNotifications);
    }

    private void sendMails(final List<Notification> notifications) {
        String domain = JFConfig.getApplicationPath(FacesContext.getCurrentInstance().getExternalContext());
        for (Notification n : notifications) {
            if (n.getRecipientMail() == null || n.getRecipientMail().isBlank()) {
                continue;
            }

            String link = domain + "/report?";
            if (n.getPostID() != null) {
                link += "p=" + n.getPostID() + "#post-" + n.getPostID();
            } else {
                link += "id=" + n.getReportID();
            }

            Mail mail = new Mail.Builder()
                    .to(n.getRecipientMail())
                    .subject(interactionsBundle.getString("email_notification_subject_" + n.getType()))
                    .content(new MessageFormat(interactionsBundle.getString("email_notification_content_"
                            + n.getType()))
                            .format(new String[]{n.getReportTitle(), link}))
                    .envelop();
            sendMail(mail, PriorityTask.Priority.LOW);
        }
    }

    /**
     * Tries to send the given {@link Mail} with the given {@link PriorityTask.Priority}.
     *
     * @param mail     The e-mail to send.
     * @param priority The priority for this mail.
     */
    public void sendMail(final Mail mail, final PriorityTask.Priority priority) {
        priorityExecutor.enqueue(new PriorityTask(priority, () -> {
            int tries = 1;
            log.debug("Sending e-mail " + mail + ".");
            while (!mailer.send(mail) && tries++ <= MAX_EMAIL_TRIES) {
                log.warning("Trying to send e-mail again. Try #" + tries + '.');
            }
            if (tries > MAX_EMAIL_TRIES) {
                log.error("Couldn't send e-mail for more than " + MAX_EMAIL_TRIES + " times! Please investigate!");
            }
        }));
    }

}
