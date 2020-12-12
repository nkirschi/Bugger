package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;

import java.util.List;

/**
 * A notification gateway allows to query and modify a persistent storage of notifications.
 */
public interface NotificationGateway {

    /**
     * Looks up the number of notifications a given user has received.
     *
     * @param user The user whose notifications to count.
     * @return The number of notifications the user has received.
     * @throws NotFoundException The user could not be found.
     */
    public int getNumberOfNotificationsForUser(User user) throws NotFoundException;

    /**
     * Inserts a given notification into the notification storage.
     *
     * @param notification The notification to insert.
     */
    public void createNotification(Notification notification);

    /**
     * Inserts a whole list of notifications into the notification storage all at once.
     *
     * @param notifications The list of notifications to insert.
     */
    public void createNotificationBulk(List<Notification> notifications);

    /**
     * Retrieves a notification by its ID.
     *
     * @param id The ID of the notification to look for.
     * @return The notification identified by the ID.
     * @throws NotFoundException The notification could not be found.
     */
    public Notification getNotificationByID(int id) throws NotFoundException;

    /**
     * Retrieves the list of a user's notifications that match the given selection criteria.
     *
     * @param user      The user whose notifications to look for.
     * @param selection The search criteria to apply.
     * @return The list of the user's notifications that match {@code selection}.
     * @throws NotFoundException The user could not be found.
     */
    public List<Notification> getNotificationsForUser(User user, Selection selection) throws NotFoundException;

    /**
     * Updates a notification's attributes in the notification storage.
     *
     * @param notification The notification to update.
     * @throws NotFoundException The notification could not be found.
     */
    public void updateNotification(Notification notification) throws NotFoundException;

    /**
     * Deletes a notification from the notification storage.
     *
     * @param notification The notification to delete.
     * @throws NotFoundException The notification could not be found.
     */
    public void deleteNotification(Notification notification) throws NotFoundException;

    /**
     * Marks a notification as read in the notification storage.
     *
     * @param notification The notification to mark as read.
     * @throws NotFoundException The notification could not be found.
     */
    public void markAsRead(Notification notification) throws NotFoundException;

    /**
     * Marks a notification as sent in the notification storage.
     *
     * @param notification The notification to mark as sent.
     * @throws NotFoundException The notification could not be found.
     */
    public void markAsSent(Notification notification) throws NotFoundException;

}
