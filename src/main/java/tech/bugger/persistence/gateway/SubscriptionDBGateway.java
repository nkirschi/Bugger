package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.DuplicateException;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Subscription gateway that gives access to subscription relationships stored in a database.
 */
public class SubscriptionDBGateway implements SubscriptionGateway {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(SubscriptionDBGateway.class);

    /**
     * Database connection used by this gateway.
     */
    private final Connection conn;

    /**
     * Constructs a new subscription gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public SubscriptionDBGateway(final Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(final Topic topic, final User subscriber) throws NotFoundException, DuplicateException {
        if (topic == null) {
            log.error("Cannot subscribe to topic null.");
            throw new IllegalArgumentException("Topic cannot be null.");
        } else if (topic.getId() == null) {
            log.error("Cannot subscribe to topic with ID null.");
            throw new IllegalArgumentException("Topic ID cannot be null.");
        } else if (subscriber == null) {
            log.error("Cannot subscribe user null to topic " + topic + ".");
            throw new IllegalArgumentException("User cannot be null.");
        } else if (subscriber.getId() == null) {
            log.error("Cannot subscribe user with ID null to topic " + topic + ".");
            throw new IllegalArgumentException("User ID cannot be null.");
        }
        String sql = "INSERT INTO topic_subscription (subscriber, topic) VALUES (?, ?);";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(subscriber.getId())
                    .integer(topic.getId()).toStatement();
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                log.error("Could not find topic " + topic + " or user " + subscriber + ".");
                throw new NotFoundException("Could not find topic " + topic + " or user " + subscriber + ".");
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                // 23505 insertion of duplicate key value
                log.error("User " + subscriber + " is already subscribed to topic " + topic + ".", e);
                throw new DuplicateException("User " + subscriber + " is already subscribed to topic " + topic + ".",
                        e);
            } else {
                log.error("Error when subscribing user " + subscriber + " to topic " + topic + ".", e);
                throw new StoreException("Error when subscribing user " + subscriber + " to topic " + topic + ".", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(final Report report, final User subscriber) throws NotFoundException, DuplicateException {
        if (report == null) {
            log.error("Cannot subscribe to report null.");
            throw new IllegalArgumentException("Report cannot be null.");
        } else if (report.getId() == null) {
            log.error("Cannot subscribe to report with ID null.");
            throw new IllegalArgumentException("Report ID cannot be null.");
        } else if (subscriber == null) {
            log.error("Cannot subscribe user null to report " + report + ".");
            throw new IllegalArgumentException("User cannot be null.");
        } else if (subscriber.getId() == null) {
            log.error("Cannot subscribe user with ID null to report " + report + ".");
            throw new IllegalArgumentException("User ID cannot be null.");
        }
        String sql = "INSERT INTO report_subscription (subscriber, report) VALUES (?, ?);";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(subscriber.getId())
                    .integer(report.getId()).toStatement();
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                log.error("Could not find report " + report + " or user " + subscriber + ".");
                throw new NotFoundException("Could not find report " + report + " or user " + subscriber + ".");
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                // 23505 insertion of duplicate key value
                log.error("User " + subscriber + " is already subscribed to report " + report + ".", e);
                throw new DuplicateException("User " + subscriber + " is already subscribed to report " + report + ".",
                        e);
            } else {
                log.error("Error when subscribing user " + subscriber + " to report " + report + ".", e);
                throw new StoreException("Error when subscribing user " + subscriber + " to report " + report + ".", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subscribe(final User subscribeTo, final User subscriber) throws NotFoundException, DuplicateException {
        if (subscribeTo == null) {
            log.error("Cannot subscribe to user null.");
            throw new IllegalArgumentException("User to subscribe to cannot be null.");
        } else if (subscribeTo.getId() == null) {
            log.error("Cannot subscribe to user with ID null.");
            throw new IllegalArgumentException("ID of user to subscribe to cannot be null.");
        } else if (subscriber == null) {
            log.error("Cannot subscribe user null to user " + subscribeTo + ".");
            throw new IllegalArgumentException("Subscriber cannot be null.");
        } else if (subscriber.getId() == null) {
            log.error("Cannot subscribe user with ID null to user " + subscribeTo + ".");
            throw new IllegalArgumentException("Subscriber ID cannot be null.");
        }

        String sql = "INSERT INTO user_subscription (subscriber, subscribee) VALUES (?, ?);";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(subscriber.getId())
                    .integer(subscribeTo.getId()).toStatement();
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                log.error("Could not find user " + subscribeTo + " or user " + subscriber + ".");
                throw new NotFoundException("Could not find user " + subscribeTo + " or user " + subscriber + ".");
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                // 23505 insertion of duplicate key value
                log.error("User " + subscriber + " is already subscribed to user " + subscribeTo + ".", e);
                throw new DuplicateException("User " + subscriber + " is already subscribed to user " + subscribeTo
                        + ".", e);
            } else {
                log.error("Error when subscribing user " + subscriber + " to user " + subscribeTo + ".", e);
                throw new StoreException("Error when subscribing user " + subscriber + " to user " + subscribeTo + ".",
                        e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(final Topic topic, final User subscriber) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(final Report report, final User subscriber) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(final User subscribedTo, final User subscriber) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeAllReports(final User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeAllTopics(final User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribeAllUsers(final User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSubscribed(final User user, final Topic topic) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSubscribed(final User user, final Report report) throws NotFoundException {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSubscribed(final User subscriber, final User subscribedTo) throws NotFoundException {
        // TODO Auto-generated method stub
        return false;
    }

}
