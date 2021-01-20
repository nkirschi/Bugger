package tech.bugger.persistence.gateway;

import com.ocpsoft.pretty.faces.util.StringUtils;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.global.util.Pagitable;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
        if (notification == null) {
            log.error("Cannot create notification null.");
            throw new IllegalArgumentException("Notification cannot be null.");
        }

        String sql = "INSERT INTO notification (sent, read, type, recipient, causer, topic, report, post)"
                + " VALUES (?, ?, ?::notification_type, ?, ?, ?, ?, ?);";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .bool(notification.isSent())
                    .bool(notification.isRead())
                    .string(notification.getType().name())
                    .integer(notification.getRecipientID())
                    .integer(notification.getActuatorID())
                    .integer(notification.getTopicID())
                    .integer(notification.getReportID())
                    .integer(notification.getPostID()).toStatement();
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                notification.setId(generatedKeys.getInt("id"));
            } else {
                log.error("Error while retrieving new ID for notification " + notification + ".");
                throw new StoreException("Error while retrieving new ID for notification " + notification + ".");
            }
        } catch (SQLException e) {
            log.error("Error while creating notification " + notification + ".", e);
            throw new StoreException("Error while creating notification " + notification + ".", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Notification find(final int id) throws NotFoundException {
        String sql = "SELECT * FROM notification WHERE notification.id = ?;";
        Notification notification;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(id).toStatement();
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                notification = getNotificationFromResultSet(rs);
            } else {
                log.error("Unable to find notification with ID " + id + ".");
                throw new NotFoundException("Unable to find notification with ID " + id + ".");
            }
        } catch (SQLException e) {
            log.error("Error when finding notification with ID " + id + ".", e);
            throw new StoreException("Error when finding notification with ID " + id + ".", e);
        }
        return notification;
    }

    private Notification getNotificationFromResultSet(final ResultSet rs) throws SQLException {
        Notification notification;
        OffsetDateTime date = null;
        if (rs.getObject("created_at", OffsetDateTime.class) != null) {
            date = rs.getObject("created_at", OffsetDateTime.class);
        }
        notification = new Notification(
                rs.getObject("id", Integer.class),
                rs.getObject("causer", Integer.class),
                rs.getObject("recipient", Integer.class),
                Notification.Type.valueOf(rs.getString("type")),
                date,
                rs.getBoolean("read"),
                rs.getBoolean("sent"),
                rs.getObject("topic", Integer.class),
                rs.getObject("report", Integer.class),
                rs.getObject("post", Integer.class),
                null, null, null);
        return notification;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Notification> selectNotifications(final User user, final Selection selection) {
        if (user == null) {
            log.error("Cannot select notifications for user null.");
            throw new IllegalArgumentException("User cannot be null.");
        } else if (user.getId() == null) {
            log.error("Cannot select notifications for user with ID null.");
            throw new IllegalArgumentException("User ID cannot be null.");
        } else if (selection == null) {
            log.error("Cannot select notifications for user " + user + " with selection null.");
            throw new IllegalArgumentException("Selection cannot be null");
        } else if (StringUtils.isBlank(selection.getSortedBy())) {
            log.error("Cannot select notifications for user " + user + " sorted by nothing.");
            throw new IllegalArgumentException("Sorted by cannot be blank");
        }

        String sql = "SELECT n.*, u.username, r.title FROM notification AS n"
                + " LEFT OUTER JOIN \"user\" u ON u.id = n.causer"
                + " LEFT OUTER JOIN report r ON r.id = n.report"
                + " WHERE n.recipient = ?"
                + " ORDER BY " + selection.getSortedBy() + (selection.isAscending() ? " ASC" : " DESC")
                + " LIMIT ? OFFSET ?;";
        List<Notification> selectedNotifications = new ArrayList<>(Math.min(Pagitable.getItemLimit(selection),
                Math.max(0, selection.getTotalSize())));
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(user.getId())
                    .integer(Pagitable.getItemLimit(selection))
                    .integer(Pagitable.getItemOffset(selection)).toStatement();
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Notification n = getNotificationFromResultSet(rs);
                n.setActuatorUsername(rs.getString("username"));
                n.setReportTitle(rs.getString("title"));
                selectedNotifications.add(n);
            }
        } catch (SQLException e) {
            log.error("Error while selecting notifications for user " + user + " with selection " + selection
                    + ".", e);
            throw new StoreException("Error while selecting notifications for user " + user + " with selection "
                    + selection + ".", e);
        }
        return selectedNotifications;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final Notification notification) throws NotFoundException {
        if (notification == null) {
            log.error("Cannot update notification null.");
            throw new IllegalArgumentException("Notification cannot be null.");
        }
        String sql = "UPDATE notification SET sent = ?, read = ?, type = ?::notification_type, recipient = ?,"
                + " causer = ?, topic = ?, report = ?, post = ? WHERE id = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .bool(notification.isSent())
                    .bool(notification.isRead())
                    .string(notification.getType().name())
                    .integer(notification.getRecipientID())
                    .integer(notification.getActuatorID())
                    .integer(notification.getTopicID())
                    .integer(notification.getReportID())
                    .object(notification.getPostID())
                    .integer(notification.getId()).toStatement();
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new NotFoundException("notification " + notification + " could not be found.");
            }
        } catch (SQLException e) {
            log.error("Error when updating notification " + notification + ".", e);
            throw new StoreException("Error when updating notification " + notification + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Notification notification) throws NotFoundException {
        if (notification == null) {
            log.error("Cannot delete notification null.");
            throw new IllegalArgumentException("Notification cannot be null.");
        } else if (notification.getId() == null) {
            log.error("Cannot delete notification with ID null.");
            throw new IllegalArgumentException("Notification ID cannot be null.");
        }

        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM notification WHERE id = ? RETURNING *;")) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(notification.getId()).toStatement();
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                if (rs.getInt("id") != notification.getId()) {
                    throw new InternalError("Wrong notification deleted! Please investigate! Expected: " + notification
                            + ", actual ID: " + rs.getInt("id"));
                }
            } else {
                log.error("Notification to delete " + notification + " not found.");
                throw new NotFoundException("Notification to delete " + notification + " not found.");
            }
        } catch (SQLException e) {
            log.error("Error when deleting notification " + notification + ".", e);
            throw new StoreException("Error when deleting notification " + notification + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createNotificationBulk(final List<Notification> notifications) {
        if (notifications == null) {
            log.error("Cannot create list of notifications null.");
            throw new IllegalArgumentException("List of notifications cannot be null.");
        }

        String sql = "INSERT INTO notification (sent, read, type, recipient, causer, topic, report, post)"
                + " VALUES (?, ?, ?::notification_type, ?, ?, ?, ?, ?);";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Notification notification : notifications) {
                PreparedStatement statement = new StatementParametrizer(stmt)
                        .bool(notification.isSent())
                        .bool(notification.isRead())
                        .string(notification.getType().name())
                        .integer(notification.getRecipientID())
                        .integer(notification.getActuatorID())
                        .integer(notification.getTopicID())
                        .integer(notification.getReportID())
                        .object(notification.getPostID())
                        .toStatement();
                statement.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            log.error("Error when creating list of notifications " + notifications + ".", e);
            throw new StoreException("Error when creating list of notifications " + notifications + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Notification> getUnsentNotifications() {
        String sql = "SELECT n.*, u.email_address FROM notification n"
                + " JOIN \"user\" u ON u.id = n.recipient WHERE sent = false;";
        List<Notification> notifications;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            notifications = new ArrayList<>();
            while (rs.next()) {
                Notification n = getNotificationFromResultSet(rs);
                n.setRecipientMail(rs.getString("email_address"));
                notifications.add(n);
            }
        } catch (SQLException e) {
            log.error("Error when retrieving all unsent notifications.", e);
            throw new StoreException("Error when retrieving all unsent notifications.", e);
        }
        return notifications;
    }

}
