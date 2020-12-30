package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

/**
 * Service providing methods related to users and user profiles. A {@code Feedback} event is fired, if unexpected
 * circumstances occur.
 */
@Dependent
public class ProfileService {

    private static final Log log = Log.forClass(ProfileService.class);

    @Inject
    @Any
    Event<Feedback> feedback;

    /**
     * Returns the user with the specified ID. If no such user exists, returns {@code null} and fires an event.
     *
     * @param id The ID of the user to return.
     * @return The user, if they exist, {@code null} if no user with that ID exists.
     */
    public User getUser(int id) {
        return null;
    }

    /**
     * Creates a new user without need for verification. This should only be available for administrators.
     *
     * @param user The user to be created.
     */
    public void createUser(User user) {
    }

    /**
     * Irreversibly deletes a user. This does not delete their created reports and posts.
     *
     * @param user The user to be deleted.
     */
    public void deleteUser(User user) {

    }

    /**
     * Updates an existing user.
     *
     * @param user The user to update.
     */
    public void updateUser(User user) {

    }

    /**
     * Removes the subscription to a certain topic for one user.
     *
     * @param subscriber The user subscribed to the topic.
     * @param topic      The topic of which the subscription to is to be removed.
     */
    public void deleteTopicSubscription(User subscriber, Topic topic) {
    }

    /**
     * Removes the subscription to a certain report for one user.
     *
     * @param subscriber The user subscribed to the report.
     * @param report     The report of which the subscription to is to be removed.
     */
    public void deleteReportSubscription(User subscriber, Report report) {

    }

    /**
     * Removes the subscription to a certain other user for one user.
     *
     * @param subscriber The user subscribed to the other user.
     * @param user       The user of which the subscription to is to be removed.
     */
    public void deleteUserSubscription(User subscriber, User user) {

    }

    /**
     * Removes all subscriptions to topics for one user.
     *
     * @param user The user whose topic subscriptions are to be deleted.
     */
    public void deleteAllTopicSubscriptions(User user) {

    }

    /**
     * Removes all subscriptions to reports for one user.
     *
     * @param user The user whose report subscriptions are to be deleted.
     */
    public void deleteAllReportSubscriptions(User user) {

    }

    /**
     * Removes all subscriptions to other users for one user.
     *
     * @param user The user whose user subscriptions are to be deleted.
     */
    public void deleteAllUserSubscriptions(User user) {

    }

    /**
     * Subscribes one user to another user.
     *
     * @param subscriber   The user who will subscribe to the other user.
     * @param subscribedTo The user who will receive a subscription.
     */
    public void subscribeToUser(User subscriber, User subscribedTo) {
    }

    /**
     * Updates the avatar of a user and generates a new thumbnail.
     *
     * @param user The user with a new avatar.
     */
    public void updateAvatar(User user) {
    }

    /**
     * Returns the voting weight of a particular user.
     *
     * @param user The user in question.
     * @return The voting weight as an {@code int}.
     */
    public int getVotingWeightForUser(User user) {
        return 0;
    }

    /**
     * Returns the total number of posts created by a particular user.
     *
     * @param user The user in question.
     * @return The number of posts as an {@code int}.
     */
    public int getNumberOfPostsForUser(User user) {
        return 0;
    }

    /**
     * Checks whether the given {@code emailAddress} is already assigned to any user.
     *
     * @param emailAddress The e-mail address to check.
     * @return Whether the given {@code emailAddress} is already assigned to any user.
     */
    public boolean isEmailAssigned(final String emailAddress) {
        boolean assigned = false;

        Transaction tx = transactionManager.begin();
        try (tx) {
            assigned = tx.newUserGateway().isEmailAssigned(emailAddress);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while searching for email.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return assigned;
    }

    /**
     * Checks whether the given {@code username} is already assigned to any user.
     *
     * @param username The username to check.
     * @return Whether the given {@code username} is already assigned to any user.
     */
    public boolean isUsernameAssigned(final String username) {
        boolean assigned = false;

        Transaction tx = transactionManager.begin();
        try (tx) {
            assigned = tx.newUserGateway().isUsernameAssigned(username);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while searching for username.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return assigned;
    }

    private byte[] generateThumbnail(final byte[] image) {
        return null;
    }

}
