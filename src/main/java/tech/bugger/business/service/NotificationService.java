package tech.bugger.business.service;

import tech.bugger.business.exception.DataAccessException;
import tech.bugger.business.util.PriorityExecutor;
import tech.bugger.business.util.PriorityTask;
import tech.bugger.business.util.Registry;
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
import tech.bugger.persistence.util.PropertiesReader;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Service providing methods related to notifications.
 */
@ApplicationScoped
public class NotificationService {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(NotificationService.class);

    /**
     * Transaction manager used for creating transactions.
     */
    private final TransactionManager transactionManager;

    /**
     * The properties reader for the application configuration.
     */
    private final PropertiesReader configReader;

    /**
     * The {@link PriorityExecutor} instance to use when sending e-mails.
     */
    private final PriorityExecutor priorityExecutor;

    /**
     * The {@link Mailer} instance to use when sending e-mails.
     */
    private final Mailer mailer;

    /**
     * The current registry which to retrieve resource bundles from.
     */
    private final Registry registry;

    /**
     * Constructs a new notification service with the given dependencies.
     *
     * @param transactionManager The transaction manager to use for creating transactions.
     * @param registry           The current registry.
     */
    @Inject
    public NotificationService(final TransactionManager transactionManager,
                               final Registry registry) {
        this.transactionManager = transactionManager;
        this.registry = registry;
        configReader = registry.getPropertiesReader("config");
        priorityExecutor = registry.getPriorityExecutor("mails");
        mailer = registry.getMailer("main");
    }

    /**
     * Irreversibly deletes a notification.
     *
     * @param notification The notification to be deleted.
     * @return {@code true} iff deleting the notification was successful.
     */
    public boolean deleteNotification(final Notification notification) throws DataAccessException {
        if (notification == null) {
            log.error("Cannot delete notification null.");
            throw new IllegalArgumentException("Notification cannot be null.");
        } else if (notification.getId() == null) {
            log.error("Cannot delete notification with ID null.");
            throw new IllegalArgumentException("Notification ID cannot be null.");
        }

        try (Transaction tx = transactionManager.begin()) {
            tx.newNotificationGateway().delete(notification);
            tx.commit();
            return true;
        } catch (NotFoundException e) {
            log.error("Could not find notification to delete " + notification + ".", e);
            return false;
        } catch (TransactionException e) {
            log.error("Error when deleting notification " + notification + ".", e);
            throw new DataAccessException("Error when deleting notification " + notification + ".", e);
        }
    }

    /**
     * Irreversibly deletes all notifications addressed to the given user.
     *
     * @param user The given user.
     */
    public void deleteAllNotifications(final User user) throws DataAccessException {
        if (user == null) {
            log.error("Cannot delete all notifications for user null.");
            throw new IllegalArgumentException("User cannot be null.");
        } else if (user.getId() == null) {
            log.error("Cannot delete all notifications for user with ID null.");
            throw new IllegalArgumentException("User ID cannot be null.");
        }

        try (Transaction tx = transactionManager.begin()) {
            tx.newNotificationGateway().deleteAllNotifications(user);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when deleting all notifications for user " + user + ".", e);
            throw new DataAccessException("Error when deleting all notifications for user " + user + ".", e);
        }
    }

    /**
     * Marks a notification as read.
     *
     * @param notification The notification to be marked as read.
     * @return {@code true} iff marking the notification as read was successful.
     */
    public boolean markAsRead(final Notification notification) throws DataAccessException {
        if (notification == null) {
            log.error("Cannot mark notification null as read.");
            throw new IllegalArgumentException("Notification cannot be null.");
        } else if (notification.getId() == null) {
            log.error("Cannot mark notification with ID null as read.");
            throw new IllegalArgumentException("Notification ID cannot be null.");
        }

        notification.setRead(true);
        try (Transaction tx = transactionManager.begin()) {
            tx.newNotificationGateway().update(notification);
            tx.commit();
            return true;
        } catch (NotFoundException e) {
            log.error("Could not find notification to mark as read " + notification + ".", e);
            return false;
        } catch (TransactionException e) {
            log.error("Error when marking notification " + notification + " as sent.", e);
            throw new DataAccessException("Error when marking notification " + notification + " as sent.", e);
        }
    }

    /**
     * Returns the number of existing notifications for one particular user, both read and unread.
     *
     * @param user The user in question.
     * @return The number of notifications as an {@code int}.
     */
    public int countNotifications(final User user) throws DataAccessException {
        if (user == null) {
            log.error("Cannot count notifications for user null.");
            throw new IllegalArgumentException("User cannot be null.");
        } else if (user.getId() == null) {
            log.error("Cannot count notifications for user with ID null.");
            throw new IllegalArgumentException("User ID cannot be null.");
        }

        int numberOfNotifications;
        try (Transaction tx = transactionManager.begin()) {
            numberOfNotifications = tx.newNotificationGateway().countNotifications(user);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when counting notifications for user " + user + ".", e);
            throw new DataAccessException("Error when counting notifications for user " + user + ".", e);
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
    public List<Notification> selectNotifications(final User user, final Selection selection)
            throws DataAccessException {
        if (user == null) {
            log.error("Cannot select notifications for user null.");
            throw new IllegalArgumentException("User cannot be null.");
        } else if (user.getId() == null) {
            log.error("Cannot select notifications for user with ID null.");
            throw new IllegalArgumentException("User ID cannot be null.");
        } else if (selection == null) {
            log.error("Cannot select notifications for user " + user + " when selection is null.");
            throw new IllegalArgumentException("Selection cannot be null.");
        }

        List<Notification> selectedNotifications;
        try (Transaction tx = transactionManager.begin()) {
            selectedNotifications = tx.newNotificationGateway().selectNotifications(user, selection);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when selecting notifications for user " + user + "with selection " + selection + ".", e);
            throw new DataAccessException("Error when selecting notifications for user " + user + "with selection "
                    + selection + ".", e);
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
            affectedUsers.removeAll(gateway.getAllBannedUsers(topic));
            notifications = new ArrayList<>(affectedUsers.size());
            for (User user : affectedUsers) {
                Notification n = new Notification(notification);
                n.setRecipientID(user.getId());
                n.setRecipientMail(user.getEmailAddress());
                n.setEmailLanguage(user.getPreferredLanguage().getLanguage());
                notifications.add(n);
            }
            tx.newNotificationGateway().createNotificationBulk(notifications);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when creating notification " + notification + ".", e);
            return;
        }
        sendMails(notifications);
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
            ResourceBundle interactionsBundle = registry.getBundle("interactions",
                    Locale.forLanguageTag(n.getEmailLanguage()));
            Mail mail = new Mail.Builder()
                    .to(n.getRecipientMail())
                    .subject(interactionsBundle.getString("email_notification_subject_" + n.getType()))
                    .content(new MessageFormat(interactionsBundle.getString("email_notification_content_"
                            + n.getType()))
                            .format(new String[]{n.getReportTitle(), link}))
                    .envelop();
            sendNotification(mail, n);
        }
    }

    /**
     * Tries to send the given {@link Mail} with the given {@link PriorityTask.Priority}.
     *
     * @param mail     The e-mail to send.
     * @param priority The priority for this mail.
     */
    public void sendMail(final Mail mail, final PriorityTask.Priority priority) {
        int maxEmailTries = configReader.getInt("MAX_EMAIL_TRIES");
        priorityExecutor.enqueue(new PriorityTask(priority, () -> {
            int tries = 1;
            log.debug("Sending e-mail " + mail + ".");
            while (!mailer.send(mail) && tries++ <= maxEmailTries) {
                log.warning("Trying to send e-mail again. Try #" + tries + '.');
            }
            if (tries > maxEmailTries) {
                log.error("Couldn't send e-mail for more than " + maxEmailTries + " times! Please investigate!");
            }
        }));
    }

    private void sendNotification(final Mail mail, final Notification notification) {
        int maxEmailTries = configReader.getInt("MAX_EMAIL_TRIES");
        priorityExecutor.enqueue(new PriorityTask(PriorityTask.Priority.LOW, () -> {
            int tries = 1;
            log.debug("Sending e-mail " + mail + ".");
            while (!mailer.send(mail) && tries++ <= maxEmailTries) {
                log.warning("Trying to send e-mail again. Try #" + tries + '.');
            }
            if (tries > maxEmailTries) {
                log.error("Couldn't send e-mail for more than " + maxEmailTries + " times! Please investigate!");
            } else {
                notification.setSent(true);
                try (Transaction tx = transactionManager.begin()) {
                    tx.newNotificationGateway().update(notification);
                    tx.commit();
                } catch (NotFoundException e) {
                    log.error("Could not find notification " + notification + " when trying to mark it as sent.", e);
                } catch (TransactionException e) {
                    log.error("Error when marking notification " + notification + " as sent.", e);
                }
            }
        }));
    }

}
