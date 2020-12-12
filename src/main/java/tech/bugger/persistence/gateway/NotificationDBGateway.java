package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import java.util.List;

/**
 * Notification gateway that gives access to notifications stored in a database.
 */
public class NotificationDBGateway implements NotificationGateway {

    private static final Log log = Log.forClass(NotificationDBGateway.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfNotificationsForUser(User user) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNotification(Notification notification) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Notification getNotificationByID(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Notification> getNotificationsForUser(User user, Selection selection) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateNotification(Notification notification) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteNotification(Notification notification) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markAsRead(Notification notification) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markAsSent(Notification notification) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNotificationBulk(List<Notification> notifications) {
        // TODO Auto-generated method stub

    }

}
