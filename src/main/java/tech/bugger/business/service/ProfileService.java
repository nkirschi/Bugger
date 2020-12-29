package tech.bugger.business.service;

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

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.ResourceBundle;

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

    private Event<Feedback> feedback;

    private TransactionManager transactionManager;

    @RegistryKey("messages")
    private ResourceBundle messages;

    @Inject
    public ProfileService(final Event<Feedback> feedback, final TransactionManager transactionManager,
                          final ResourceBundle messages) {
        this.feedback = feedback;
        this.transactionManager = transactionManager;
        this.messages = messages;
    }

    /**
     * Returns the user with the specified ID. If no such user exists, returns {@code null} and fires an event.
     *
     * @param id The ID of the user to return.
     * @return The user, if they exist, {@code null} if no user with that ID exists.
     */
    public User getUser(int id) {
        User user = null;
        try(Transaction transaction = transactionManager.begin()) {
            user = transaction.newUserGateway().getUserByID(id);
            transaction.commit();
        } catch (NotFoundException e) {
            log.error("The user could not be found.", e);
            feedback.fire(new Feedback(messages.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while loading the user.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return user;
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
        try(Transaction transaction = transactionManager.begin()) {
            transaction.newUserGateway().updateUser(user);
            transaction.commit();
        } catch (NotFoundException e) {
            log.error("The user could not be found.", e);
            feedback.fire(new Feedback(messages.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while updating the user.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
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
        int votingWeight = 0;
        try(Transaction transaction = transactionManager.begin()) {
            votingWeight = transaction.newUserGateway().getVotingWeight(user);
            transaction.commit();
        } catch (NotFoundException e) {
            log.error("The user could not be found.", e);
            feedback.fire(new Feedback(messages.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while calculating the user's voting weight.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return votingWeight;
    }

    /**
     * Returns the total number of posts created by a particular user.
     *
     * @param user The user in question.
     * @return The number of posts as an {@code int}.
     */
    public int getNumberOfPostsForUser(User user) {
        int numPosts = 0;
        try(Transaction transaction = transactionManager.begin()) {
            numPosts = transaction.newUserGateway().getNumberOfPosts(user);
            transaction.commit();
        } catch (NotFoundException e) {
            log.error("The user could not be found.", e);
            feedback.fire(new Feedback(messages.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while calculating the user's voting weight.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return numPosts;
    }

    /**
     * Promotes the user whose profile is being viewed to an administrator or demotes the user whose profile is being
     * viewed if they are an administrator. However, if they are the last remaining administrator, a feedback event is
     * fired instead.
     * @param user The user to be promoted/demoted.
     */
    public void toggleAdmin(User user) {
        if (user.isAdministrator()) {
            try (Transaction transaction = transactionManager.begin()) {
                int admins = transaction.newUserGateway().getNumberOfAdmins();
                transaction.commit();
                transaction.close();
                if (admins > 1) {
                    demoteAdmin(user);
                } else {
                    log.error("The last administrator cannot be deleted");
                    feedback.fire(new Feedback(messages.getString("delete_last_admin"), Feedback.Type.ERROR));
                }
            } catch (NotFoundException e) {
                log.error("No administrators could be found.", e);
                feedback.fire(new Feedback(messages.getString("not_found_error"), Feedback.Type.ERROR));
            } catch (TransactionException e) {
                log.error("Error while counting the number of administrators.", e);
                feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
            }
        } else {
            promoteAdmin(user);
        }
    }

    /**
     * Promotes the given user to an administrator.
     * @param user The user to be promoted.
     */
    private void promoteAdmin(User user) {
        try(Transaction transaction = transactionManager.begin()) {
            user.setAdministrator(true);
            transaction.newUserGateway().updateUser(user);
            transaction.commit();
        } catch (NotFoundException e) {
            user.setAdministrator(false);
            log.error("The user could not be found.", e);
            feedback.fire(new Feedback(messages.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            user.setAdministrator(false);
            log.error("Error while updating the user.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
    }

    /**
     * Deprives the given user of his administrator status.
     * @param user The user to be demoted.
     */
    private void demoteAdmin(User user) {
        try(Transaction transaction = transactionManager.begin()) {
            user.setAdministrator(false);
            transaction.newUserGateway().updateUser(user);
            transaction.commit();
        } catch (NotFoundException e) {
            user.setAdministrator(true);
            log.error("The user could not be found.", e);
            feedback.fire(new Feedback(messages.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            user.setAdministrator(true);
            log.error("Error while updating the user.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
    }

    private byte[] generateThumbnail(byte[] image) {
        return null;
    }
}
