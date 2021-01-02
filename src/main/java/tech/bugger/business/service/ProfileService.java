package tech.bugger.business.service;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.business.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Arrays;
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

    /**
     * Feedback event for user feedback.
     */
    private final Event<Feedback> feedback;

    /**
     * The transaction manager used for creating transactions.
     */
    private final TransactionManager transactionManager;

    /**
     * The application settings holding the current settings information.
     */
    private final ApplicationSettings applicationSettings;

    /**
     * The resource bundle for feedback messages.
     */
    private final ResourceBundle messages;

    /**
     * Constructs a new profile service with the given dependencies.
     *
     * @param feedback The feedback event to be used for user feedback.
     * @param transactionManager The transaction manager to be used for creating transactions.
     * @param applicationSettings The current application settings.
     * @param messages The resource bundle to look up feedback messages.
     */
    @Inject
    public ProfileService(final Event<Feedback> feedback, final TransactionManager transactionManager,
                          final ApplicationSettings applicationSettings,
                          final @RegistryKey("messages") ResourceBundle messages) {
        this.feedback = feedback;
        this.transactionManager = transactionManager;
        this.applicationSettings = applicationSettings;
        this.messages = messages;
    }

    /**
     * Returns the user with the specified ID. If no such user exists, returns {@code null} and fires an event.
     *
     * @param id The ID of the user to return.
     * @return The user, if they exist, {@code null} if no user with that ID exists.
     */
    public User getUser(final int id) {
        User user = null;
        try (Transaction transaction = transactionManager.begin()) {
            user = transaction.newUserGateway().getUserByID(id);
            transaction.commit();
        } catch (tech.bugger.persistence.exception.NotFoundException e) {
            log.error("The user with id " + id + " could not be found.", e);
            throw new NotFoundException(messages.getString("not_found_error"), e);
        } catch (TransactionException e) {
            log.error("Error while loading the user with id " + id, e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return user;
    }

    /**
     * Creates a new user without need for verification. This should only be available for administrators.
     *
     * @param user The user to be created.
     */
    public void createUser(final User user) {
    }

    /**
     * Irreversibly deletes a user. This does not delete their created reports and posts.
     *
     * @param user The user to be deleted.
     */
    public void deleteUser(final User user) {

    }

    /**
     * Updates an existing user.
     *
     * @param user The user to update.
     */
    public void updateUser(final User user) {
        try (Transaction transaction = transactionManager.begin()) {
            transaction.newUserGateway().updateUser(user);
            transaction.commit();
            feedback.fire(new Feedback(messages.getString("operation_successful"), Feedback.Type.INFO));
        } catch (tech.bugger.persistence.exception.NotFoundException e) {
            log.error("The user with id " + user.getId() + "could not be found.", e);
            throw new NotFoundException(messages.getString("not_found_error"), e);
        } catch (TransactionException e) {
            log.error("Error while updating the user with id " + user.getId(), e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
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
        int votingWeight = 0;
        if (user.getForcedVotingWeight() != null) {
            return user.getForcedVotingWeight();
        }
        int numPosts = getNumberOfPostsForUser(user);
        if (numPosts == 0) {
            log.error("No voting weight could be calculated for the user with id " + user.getId()
                    + "as the number of posts could not be calculated");
            return votingWeight;
        } else {
            String[] votingDef = applicationSettings.getConfiguration().getVotingWeightDefinition().split(",");
            if (votingDef.length == 0) {
                log.error("The voting weight definition is empty");
                feedback.fire(new Feedback(messages.getString("voting_weight_failure"), Feedback.Type.ERROR));
                return votingWeight;
            }
            try {
                int[] votingWeightDef = convertVotingWeight(votingDef);
                votingWeight = calculateVotingWeight(numPosts, votingWeightDef);
            } catch (NumberFormatException e) {
                log.error("The voting weight definition could not be parsed to a number");
                feedback.fire(new Feedback(messages.getString("voting_weight_failure"), Feedback.Type.ERROR));
            }
        }
        return votingWeight;
    }

    /**
     * Parses the given String array to an int array.
     *
     * @param votingWeightDef The voting weight definition.
     * @return The int array.
     * @throws NumberFormatException The voting weight definition could not be parsed to a number.
     */
    private int[] convertVotingWeight(final String[] votingWeightDef) throws NumberFormatException {
        int[] votingWeight = new int[votingWeightDef.length];
        for (int i = 0; i < votingWeightDef.length; i++) {
            votingWeight[i] = Integer.parseInt(votingWeightDef[i]);
        }
        Arrays.sort(votingWeight);
        return votingWeight;
    }

    /**
     * Calculates the voting weight from the given number of posts and the voting weight definition.
     *
     * @param numPosts The number of posts.
     * @param votingWeightDef The voting weight definition.
     * @return The calculated voting weight.
     */
    private int calculateVotingWeight(final int numPosts, final int[] votingWeightDef) {
        if (votingWeightDef[0] != 0) {
            log.error("The voting weight definition needs to contain a 0.");
            feedback.fire(new Feedback(messages.getString("voting_weight_failure"), Feedback.Type.ERROR));
            return 0;
        }
        for (int i = 1; i < votingWeightDef.length; i++) {
            if (numPosts < votingWeightDef[i]) {
                return i;
            }
        }
        return votingWeightDef.length;
    }

    /**
     * Returns the total number of posts created by a particular user.
     *
     * @param user The user in question.
     * @return The number of posts as an {@code int}.
     */
    public int getNumberOfPostsForUser(final User user) {
        int numPosts = 0;
        try (Transaction transaction = transactionManager.begin()) {
            numPosts = transaction.newUserGateway().getNumberOfPosts(user);
            transaction.commit();
            if (numPosts == 0) {
                log.error("The number of posts could not be calculated for the user with id " + user.getId());
                feedback.fire(new Feedback(messages.getString("not_found_error"), Feedback.Type.ERROR));
            }
        } catch (TransactionException e) {
            log.error("Error while loading the number of posts for the user with id " + user.getId(), e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return numPosts;
    }

    /**
     * Promotes the user whose profile is being viewed to an administrator or demotes the user whose profile is being
     * viewed if they are an administrator. However, if they are the last remaining administrator, a feedback event is
     * fired instead.
     *
     * @param user The user to be promoted/demoted.
     */
    public void toggleAdmin(final User user) {
        if (user.isAdministrator()) {
            try (Transaction transaction = transactionManager.begin()) {
                int admins = transaction.newUserGateway().getNumberOfAdmins();
                transaction.commit();
                if (admins == 0) {
                    log.error("No administrators could be found in the database");
                    throw new InternalError("No administrators could be found in the database");
                } else if (admins == 1) {
                    log.error("The last administrator cannot be deleted");
                    feedback.fire(new Feedback(messages.getString("delete_last_admin"), Feedback.Type.ERROR));
                } else {
                    changeAdminStatus(user, false);
                }
            } catch (TransactionException e) {
                log.error("Error while counting the number of administrators.", e);
                feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
            }
        } else {
            changeAdminStatus(user, true);
        }
    }

    /**
     * Changes the given user's administrator status based on the given boolean.
     *
     * @param user The user to be promoted.
     * @param admin The administration status to change to.
     */
    private void changeAdminStatus(final User user, final boolean admin) {
        try (Transaction transaction = transactionManager.begin()) {
            user.setAdministrator(admin);
            transaction.newUserGateway().updateUser(user);
            transaction.commit();
            feedback.fire(new Feedback(messages.getString("operation_successful"), Feedback.Type.INFO));
        } catch (tech.bugger.persistence.exception.NotFoundException e) {
            user.setAdministrator(!admin);
            log.error("The user with id " + user.getId() + "could not be found.", e);
            feedback.fire(new Feedback(messages.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            user.setAdministrator(!admin);
            log.error("Error while updating the user with id " + user.getId(), e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
    }

    private byte[] generateThumbnail(final byte[] image) {
        return null;
    }

}
