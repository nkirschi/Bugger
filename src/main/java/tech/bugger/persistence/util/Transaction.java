package tech.bugger.persistence.util;

import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.AttachmentGateway;
import tech.bugger.persistence.gateway.MetadataGateway;
import tech.bugger.persistence.gateway.NotificationGateway;
import tech.bugger.persistence.gateway.PostGateway;
import tech.bugger.persistence.gateway.ReportGateway;
import tech.bugger.persistence.gateway.SearchGateway;
import tech.bugger.persistence.gateway.SettingsGateway;
import tech.bugger.persistence.gateway.StatisticsGateway;
import tech.bugger.persistence.gateway.SubscriptionGateway;
import tech.bugger.persistence.gateway.TokenGateway;
import tech.bugger.persistence.gateway.TopicGateway;
import tech.bugger.persistence.gateway.UserGateway;

/**
 * Transaction to combine multiple data access operations into a single work unit.
 *
 * Once created, a transaction object provides factory methods for all available data gateways that can be used to add
 * actions to the current transaction.
 */
public interface Transaction extends ExceptionlessAutoCloseable {

    /**
     * Commits the current transaction. Changes are applied as single work unit.
     */
    void commit() throws TransactionException;

    /**
     * Aborts the current transaction. Any changes made are reverted.
     */
    void abort();

    /**
     * Fabricates an attachment gateway to use with this transaction.
     *
     * @return A brand-new attachment gateway tied to this transaction's connection.
     */
    AttachmentGateway newAttachmentGateway();

    /**
     * Fabricates a metadata gateway to use with this transaction.
     *
     * @return A brand-new metadata gateway tied to this transaction's connection.
     */
    MetadataGateway newMetadataGateway();

    /**
     * Fabricates a notification gateway to use with this transaction.
     *
     * @return A brand-new notification gateway tied to this transaction's connection.
     */
    NotificationGateway newNotificationGateway();

    /**
     * Fabricates a post gateway to use with this transaction. A
     *
     * @return A brand-new post gateway tied to this transaction's connection.
     */
    PostGateway newPostGateway();

    /**
     * Fabricates a report gateway to use with this transaction.
     *
     * @return A brand-new report gateway tied to this transaction's connection.
     */
    ReportGateway newReportGateway();

    /**
     * Fabricates a search gateway to use with this transaction.
     *
     * @return A brand-new search gateway tied to this transaction's connection.
     */
    SearchGateway newSearchGateway();

    /**
     * Fabricates a settings gateway to use with this transaction.
     *
     * @return A brand-new settings gateway tied to this transaction's connection.
     */
    SettingsGateway newSettingsGateway();

    /**
     * Fabricates a statistics gateway to use with this transaction.
     *
     * @return A brand-new statistics gateway tied to this transaction's connection.
     */
    StatisticsGateway newStatisticsGateway();

    /**
     * Fabricates a subscription gateway to use with this transaction.
     *
     * @return A brand-new subscription gateway tied to this transaction's connection.
     */
    SubscriptionGateway newSubscriptionGateway();

    /**
     * Fabricates a token gateway to use with this transaction.
     *
     * @return A brand-new token gateway tied to this transaction's connection.
     */
    TokenGateway newTokenGateway();

    /**
     * Fabricates a topic gateway to use with this transaction.
     *
     * @return A brand-new topic gateway tied to this transaction's connection.
     */
    TopicGateway newTopicGateway();

    /**
     * Fabricates a user gateway to use with this transaction.
     *
     * @return A brand-new user gateway tied to this transaction's connection.
     */
    UserGateway newUserGateway();

}

