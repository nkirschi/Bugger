package tech.bugger.persistence.util;

import tech.bugger.persistence.gateway.*;

import java.sql.Connection;

/**
 * Database Transaction implementation.
 *
 * {@inheritDoc}
 */
class DBTransaction implements Transaction {

    /**
     * Database connection reserved for this transaction.
     */
    private Connection connection;

    /**
     * Constructs a new transaction with its own database connection.
     */
    public DBTransaction() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttachmentGateway newAttachmentGateway() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationGateway newNotificationGateway() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PostGateway newPostGateway() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReportGateway newReportGateway() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchGateway newSearchGateway() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SettingsGateway newSettingsGateway() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StatisticsGateway newStatisticsGateway() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionGateway newSubscriptionGateway() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TokenGateway newTokenGateway() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicGateway newTopicGateway() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserGateway newUserGateway() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abort() {
    }
}
