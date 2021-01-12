package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Notification gateway that gives access to notifications stored in a database.
 */
public class NotificationDBGateway implements NotificationGateway {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(NotificationDBGateway.class);

    /**
     * Database connection used by this gateway.
     */
    private final Connection conn;

    /**
     * Constructs a new notification gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public NotificationDBGateway(final Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countNotifications(final User user) {
        if (user == null) {
            log.error("Cannot count notifications for user null.");
            throw new IllegalArgumentException("User cannot be null.");
        } else if (user.getId() == null) {
            log.error("Cannot count notifications for user with ID null.");
            throw new IllegalArgumentException("User ID cannot be null.");
        }

        int count = 0;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM notification "
                + "WHERE recipient = ?;")) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(user.getId()).toStatement();
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException e) {
            log.error("Error when counting notifications for user " + user + ".", e);
            throw new StoreException("Error when counting notifications for user " + user + ".", e);
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(final Notification notification) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     * @param id
     */
    @Override
    public Notification find(final int id) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM notification WHERE id = ?;")) {

        } catch (SQLException e) {
            log.error("Error when finding notification with ID " + id + ".", e);
            throw new StoreException("Error when finding notification with ID " + id + ".", e);
        }
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
