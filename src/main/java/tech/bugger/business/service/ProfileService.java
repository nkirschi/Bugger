package tech.bugger.business.service;

import java.util.ResourceBundle;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

/**
 * Service providing methods related to users and user profiles. A {@code Feedback} event is fired, if unexpected
 * circumstances occur.
 */
@Dependent
public class ProfileService {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(ProfileService.class);

    /**
     * Transaction manager used for creating transactions.
     */
    private final TransactionManager transactionManager;

    /**
     * Feedback Event for user feedback.
     */
    private final Event<Feedback> feedbackEvent;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new profile service with the given dependencies.
     *
     * @param transactionManager The transaction manager to use for creating transactions.
     * @param feedbackEvent      The feedback event to use for user feedback.
     * @param messagesBundle     The resource bundle for feedback messages.
     */
    @Inject
    public ProfileService(final TransactionManager transactionManager, final Event<Feedback> feedbackEvent,
                          @RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.transactionManager = transactionManager;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
    }

    /**
     * Returns the user with the specified ID. If no such user exists, returns {@code null} and fires an event.
     *
     * @param id The ID of the user to return.
     * @return The user, if they exist, {@code null} if no user with that ID exists.
     */
    public User getUser(final int id) {
        User user = null;

        try (Transaction tx = transactionManager.begin()) {
            user = tx.newUserGateway().getUserByID(id);
            tx.commit();
        } catch (NotFoundException e) {
            log.debug("User in search for ID could not be found.");
        } catch (TransactionException e) {
            log.error("User could not be created.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return user;
    }

    /**
     * Creates a new user without need for verification. This should only be available for administrators.
     * Also generates and sets the internal user id inside the given {@code user} object.
     *
     * @param user The user to be created.
     * @return Whether the action was successful or not.
     */
    public boolean createUser(final User user) {
        try (Transaction tx = transactionManager.begin()) {
            tx.newUserGateway().createUser(user);
            tx.commit();
            return true;
        } catch (TransactionException e) {
            log.error("User could not be created.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return false;
    }

    /**
     * Irreversibly deletes a user. This does not delete their created reports and posts.
     *
     * @param user The user to be deleted.
     */
    public void deleteUser(final User user) {
    }

    /**
     * Updates an existing user and returns whether the action was successful.
     *
     * @param user The user to update.
     * @return {@code true} iff the action was successful, {@code false} otherwise.
     */
    public boolean updateUser(final User user) {
        try (Transaction tx = transactionManager.begin()) {
            tx.newUserGateway().updateUser(user);
            tx.commit();
            return true;
        } catch (NotFoundException e) {
            log.error("User could not be found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while updating the user.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return false;
    }

    /**
     * Removes the subscription to a certain topic for one user.
     *
     * @param subscriber The user subscribed to the topic.
     * @param topic      The topic of which the subscription to is to be removed.
     */
    public void deleteTopicSubscription(final User subscriber, final Topic topic) {
    }

    /**
     * Removes the subscription to a certain report for one user.
     *
     * @param subscriber The user subscribed to the report.
     * @param report     The report of which the subscription to is to be removed.
     */
    public void deleteReportSubscription(final User subscriber, final Report report) {
    }

    /**
     * Removes the subscription to a certain other user for one user.
     *
     * @param subscriber The user subscribed to the other user.
     * @param user       The user of which the subscription to is to be removed.
     */
    public void deleteUserSubscription(final User subscriber, final User user) {
    }

    /**
     * Removes all subscriptions to topics for one user.
     *
     * @param user The user whose topic subscriptions are to be deleted.
     */
    public void deleteAllTopicSubscriptions(final User user) {
    }

    /**
     * Removes all subscriptions to reports for one user.
     *
     * @param user The user whose report subscriptions are to be deleted.
     */
    public void deleteAllReportSubscriptions(final User user) {
    }

    /**
     * Removes all subscriptions to other users for one user.
     *
     * @param user The user whose user subscriptions are to be deleted.
     */
    public void deleteAllUserSubscriptions(final User user) {
    }

    /**
     * Subscribes one user to another user.
     *
     * @param subscriber   The user who will subscribe to the other user.
     * @param subscribedTo The user who will receive a subscription.
     */
    public void subscribeToUser(final User subscriber, final User subscribedTo) {
    }

    /**
     * Updates the avatar of a user and generates a new thumbnail.
     *
     * @param user The user with a new avatar.
     */
    public void updateAvatar(final User user) {
    }

    /**
     * Returns the voting weight of a particular user.
     *
     * @param user The user in question.
     * @return The voting weight as an {@code int}.
     */
    public int getVotingWeightForUser(final User user) {
        return 0;
    }

    /**
     * Returns the total number of posts created by a particular user.
     *
     * @param user The user in question.
     * @return The number of posts as an {@code int}.
     */
    public int getNumberOfPostsForUser(final User user) {
        return 0;
    }

    /**
     * Searches and returns the {@link User} with the given {@code emailAddress}.
     *
     * @param emailAddress The e-mail address to search for.
     * @return The complete {@link User} or {@code null} iff the {@code emailAddress} is not assigned to any user.
     */
    public User getUserByEmail(final String emailAddress) {
        User user = null;

        try (Transaction tx = transactionManager.begin()) {
            user = tx.newUserGateway().getUserByEmail(emailAddress);
            tx.commit();
        } catch (NotFoundException e) {
            log.debug("User in search for e-mail could not be found.");
        } catch (TransactionException e) {
            log.error("Error while searching for e-mail.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return user;
    }

    /**
     * Searches and returns the {@link User} with the given {@code username}.
     *
     * @param username The username to search for.
     * @return The complete {@link User} or {@code null} iff the {@code username} is not assigned to any user.
     */
    public User getUserByUsername(final String username) {
        User user = null;

        try (Transaction tx = transactionManager.begin()) {
            user = tx.newUserGateway().getUserByUsername(username);
            tx.commit();
        } catch (NotFoundException e) {
            log.debug("User in search for username could not be found.");
        } catch (TransactionException e) {
            log.error("Error while searching for username.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return user;
    }

    private byte[] generateThumbnail(final byte[] image) {
        return null;
    }

}
