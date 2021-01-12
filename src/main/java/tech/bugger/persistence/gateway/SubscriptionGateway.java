package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.DuplicateException;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.SelfReferenceException;

/**
 * A subscription gateway allows to query and modify a persistent storage of subscription relationships.
 */
public interface SubscriptionGateway {

    /**
     * Adds a subscriber to a topic.
     *
     * @param topic      The topic to add a subscriber to.
     * @param subscriber The user to make a subscriber of the topic.
     * @throws NotFoundException  The user or the topic could not be found.
     * @throws DuplicateException The user was already subscribed to the topic.
     */
    void subscribe(Topic topic, User subscriber) throws NotFoundException, DuplicateException;

    /**
     * Adds a subscriber to a report.
     *
     * @param report     The report to add a subscriber to.
     * @param subscriber The user to make a subscriber of the report.
     * @throws NotFoundException  The user or the report could not be found.
     * @throws DuplicateException The user was already subscribed to the report.
     */
    void subscribe(Report report, User subscriber) throws NotFoundException, DuplicateException;

    /**
     * Adds a subscriber to another user. Does nothing if the user is already subscribed to the report.
     *
     * @param subscribeTo The user to add a subscriber to.
     * @param subscriber  The user to make a subscriber of {@code subscribeTo}. Must be different from {@code
     *                    subscribeTo} as a user can't subscribe to themselves.
     * @throws NotFoundException      One of the users could not be found.
     * @throws DuplicateException     The user was already subscribed to the user.
     * @throws SelfReferenceException The two users are the same.
     */
    void subscribe(User subscribeTo, User subscriber) throws NotFoundException, DuplicateException,
            SelfReferenceException;

    /**
     * Removes the subscription of a user to a topic.
     *
     * @param topic      The topic to remove a subscriber from.
     * @param subscriber The user to unsubscribe from the topic.
     * @throws NotFoundException The user or the topic could not be found or the user was not subscribed to the topic.
     */
    void unsubscribe(Topic topic, User subscriber) throws NotFoundException;

    /**
     * Removes the subscription of a user to a report.
     *
     * @param report     The report to remove a subscriber from.
     * @param subscriber The user to unsubscribe from the report.
     * @throws NotFoundException The user or the report could not be found or the user was not subscribed to the
     *                           report.
     */
    void unsubscribe(Report report, User subscriber) throws NotFoundException;

    /**
     * Removes the subscription of a user to another user.
     *
     * @param subscribedTo The user to remove a subscriber from.
     * @param subscriber   The user to unsubscribe from {@code subscribedTo}.
     * @throws NotFoundException One of the users could not be found or {@code subscriber} was not subscribed to {@code
     *                           subscribedTo}.
     */
    void unsubscribe(User subscribedTo, User subscriber) throws NotFoundException;

    /**
     * Removes all subscriptions of a user to reports.
     *
     * @param user The user to unsubscribe from all reports.
     * @throws NotFoundException The user could not be found.
     */
    void unsubscribeAllReports(User user) throws NotFoundException;

    /**
     * Removes all subscriptions of a user to topics.
     *
     * @param user The user to unsubscribe from all topics.
     * @throws NotFoundException The user could not be found.
     */
    void unsubscribeAllTopics(User user) throws NotFoundException;

    /**
     * Removes all subscriptions of a user to other users.
     *
     * @param user The user to unsubscribe from all users.
     * @throws NotFoundException The user could not be found.
     */
    void unsubscribeAllUsers(User user) throws NotFoundException;

    /**
     * Checks whether a user is subscribed to a topic.
     *
     * @param user  The user whose subscription to check.
     * @param topic The topic to which {@code user} might be subscribed.
     * @return Whether {@code user} is subscribed to {@code topic}.
     * @throws NotFoundException The user or the topic could not be found.
     */
    boolean isSubscribed(User user, Topic topic) throws NotFoundException;

    /**
     * Checks whether a user is subscribed to a report.
     *
     * @param user   The user whose subscription to check.
     * @param report The report to which {@code user} might be subscribed.
     * @return Whether {@code user} is subscribed to {@code report}.
     * @throws NotFoundException The user or the report could not be found.
     */
    boolean isSubscribed(User user, Report report) throws NotFoundException;

    /**
     * Checks whether a user is subscribed to another user.
     *
     * @param subscriber   The user whose subscription to check.
     * @param subscribedTo The user to which {@code subscriber} might be subscribed.
     * @return Whether {@code subscriber} is subscribed to {@code subscribedTo}.
     * @throws NotFoundException One of the users could not be found.
     */
    boolean isSubscribed(User subscriber, User subscribedTo) throws NotFoundException;

}
