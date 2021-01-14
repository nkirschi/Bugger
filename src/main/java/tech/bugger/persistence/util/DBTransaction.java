package tech.bugger.persistence.util;

import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.AttachmentDBGateway;
import tech.bugger.persistence.gateway.AttachmentGateway;
import tech.bugger.persistence.gateway.MetadataDBGateway;
import tech.bugger.persistence.gateway.MetadataGateway;
import tech.bugger.persistence.gateway.NotificationDBGateway;
import tech.bugger.persistence.gateway.NotificationGateway;
import tech.bugger.persistence.gateway.PostDBGateway;
import tech.bugger.persistence.gateway.PostGateway;
import tech.bugger.persistence.gateway.ReportDBGateway;
import tech.bugger.persistence.gateway.ReportGateway;
import tech.bugger.persistence.gateway.SearchDBGateway;
import tech.bugger.persistence.gateway.SearchGateway;
import tech.bugger.persistence.gateway.SettingsDBGateway;
import tech.bugger.persistence.gateway.SettingsGateway;
import tech.bugger.persistence.gateway.StatisticsDBGateway;
import tech.bugger.persistence.gateway.StatisticsGateway;
import tech.bugger.persistence.gateway.SubscriptionDBGateway;
import tech.bugger.persistence.gateway.SubscriptionGateway;
import tech.bugger.persistence.gateway.TokenDBGateway;
import tech.bugger.persistence.gateway.TokenGateway;
import tech.bugger.persistence.gateway.TopicDBGateway;
import tech.bugger.persistence.gateway.TopicGateway;
import tech.bugger.persistence.gateway.UserDBGateway;
import tech.bugger.persistence.gateway.UserGateway;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database Transaction implementation.
 *
 * {@inheritDoc}
 */
public class DBTransaction implements Transaction {

    /**
     * Database connection reserved for this transaction.
     */
    private Connection connection;

    /**
     * Connection pool to borrow connections from.
     */
    private final ConnectionPool connectionPool;

    /**
     * Whether this transaction has been completed, i.e. committed or aborted.
     */
    private boolean completed;

    /**
     * Constructs a new transaction with a connection pool to use.
     *
     * @param connectionPool The connection pool to borrow connections from.
     */
    public DBTransaction(final ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        this.connection = connectionPool.getConnection();
        completed = false;
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new InternalError("Cannot disable auto-commit.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws TransactionException {
        checkState();
        try {
            connection.commit();
            completed = true;
        } catch (SQLException e) {
            throw new TransactionException("Transaction commit failed.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abort() {
        checkState();
        try {
            connection.rollback();
            completed = true;
        } catch (SQLException e) {
            throw new InternalError("Transaction rollback failed.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (!completed) {
            abort();
        }
        connectionPool.releaseConnection(connection);
        connection = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttachmentGateway newAttachmentGateway() {
        checkState();
        return new AttachmentDBGateway(connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetadataGateway newMetadataGateway() {
        checkState();
        return new MetadataDBGateway(connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationGateway newNotificationGateway() {
        checkState();
        return new NotificationDBGateway(connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostGateway newPostGateway() {
        checkState();
        return new PostDBGateway(connection, newUserGateway(), newAttachmentGateway());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportGateway newReportGateway() {
        checkState();
        return new ReportDBGateway(connection, newUserGateway());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchGateway newSearchGateway() {
        checkState();
        return new SearchDBGateway(connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SettingsGateway newSettingsGateway() {
        checkState();
        return new SettingsDBGateway(connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StatisticsGateway newStatisticsGateway() {
        checkState();
        return new StatisticsDBGateway(connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionGateway newSubscriptionGateway() {
        checkState();
        return new SubscriptionDBGateway(connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TokenGateway newTokenGateway() {
        checkState();
        return new TokenDBGateway(connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicGateway newTopicGateway() {
        checkState();
        return new TopicDBGateway(connection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserGateway newUserGateway() {
        checkState();
        return new UserDBGateway(connection);
    }

    /**
     * Returns whether this transaction is completed.
     *
     * @return {@code true} iff this transaction has been committed or aborted.
     */
    public boolean isCompleted() {
        return completed;
    }

    private void checkState() {
        if (connection == null) {
            throw new IllegalStateException("Transaction cannot be reused.");
        }
    }

}
