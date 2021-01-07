package tech.bugger.business.service;

import tech.bugger.business.exception.NotFoundException;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Service providing methods related to topics. A {@code Feedback} event is fired, if unexpected circumstances occur.
 */
@Dependent
public class TopicService {

    private static final Log log = Log.forClass(TopicService.class);

    @Inject
    @Any
    Event<Feedback> feedback;

    /**
     * The transaction manager used for creating transactions.
     */
    private final TransactionManager transactionManager;

    /**
     * The resource bundle for feedback messages.
     */
    private final ResourceBundle messages;

    /**
     * Constructs a new topic service with the given dependencies.
     *
     * @param feedback The feedback event to be used for user feedback.
     * @param transactionManager The transaction manager to be used for creating transactions.
     * @param messages The resource bundle to look up feedback messages.
     */
    @Inject
    public TopicService(final Event<Feedback> feedback, final TransactionManager transactionManager,
                          final @RegistryKey("messages") ResourceBundle messages) {
        this.feedback = feedback;
        this.transactionManager = transactionManager;
        this.messages = messages;
    }

    /**
     * Bans a user from a topic. Administrators and moderators of the topic cannot be banned.
     *
     * @param username The username of the user to be banned.
     * @param topic    The topic which the user is to be banned from.
     */
    public void ban(String username, Topic topic) {
    }

    /**
     * Unbans a user from a topic.
     *
     * @param user  The user to be unbanned.
     * @param topic The topic which the user is to be unbanned from.
     */
    public void unban(User user, Topic topic) {

    }

    /**
     * Makes a user a moderator of a topic.
     *
     * @param username The username of the user to be made a moderator.
     * @param topic    The topic which the user is to be made a moderator of.
     */
    public void makeModerator(String username, Topic topic) {

    }

    /**
     * Removes the moderator status of a moderator of a topic. Cannot be applied to administrators.
     *
     * @param user  The user who is about to lose moderator privileges.
     * @param topic The topic which the user is a moderator of.
     */
    public void removeModerator(User user, Topic topic) {

    }

    /**
     * Subscribes a user to a topic.
     *
     * @param user  The user to be subscribed to the topic.
     * @param topic The topic receiving the subscription.
     */
    public void subscribeToTopic(User user, Topic topic) {
    }

    /**
     * Unsubscribes a user from a topic.
     *
     * @param user  The user whose subscription is to be removed.
     * @param topic The topic the user is subscribed to.
     */
    public void unsubscribeFromTopic(User user, Topic topic) {

    }

    /**
     * Gets the topic with the specified ID. If no such topic exists, returns {@code null} and fires an event.
     *
     * @param topicID The ID of the desired topic.
     * @return The topic with that ID if it exists, {@code null} if no topic with that ID exists.
     */
    public Topic getTopicByID(int topicID) {
        Topic topic = null;
        try (Transaction transaction = transactionManager.begin()) {
            topic = transaction.newTopicGateway().getTopicByID(topicID);
            transaction.commit();
        } catch (tech.bugger.persistence.exception.NotFoundException e) {
            log.error("The topic with id " + topicID + " could not be found.", e);
            throw new NotFoundException(messages.getString("not_found_error"), e);
        } catch (TransactionException e) {
            log.error("Error while loading the topic with id " + topicID, e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return topic;
    }

    /**
     * Creates a new topic. Only administrators can do that.
     *
     * @param topic The topic to be created.
     */
    public void createTopic(Topic topic) {

    }

    /**
     * Updates an existing topic. Only administrators can do that.
     *
     * @param topic The topic to update.
     */
    public void updateTopic(Topic topic) {
        // if topic does not exist in database, createTopic instead
    }

    /**
     * Irreversibly deletes a topic, along with all the reports and posts within. Only administrators can do that.
     *
     * @param topic The topic to be deleted.
     */
    public void deleteTopic(Topic topic) {
        try (Transaction transaction = transactionManager.begin()) {
            transaction.newTopicGateway().deleteTopic(topic);
            transaction.commit();
        } catch (tech.bugger.persistence.exception.NotFoundException e) {
            log.error("The topic could not be found.", e);
            throw new NotFoundException(messages.getString("not_found_error"), e);
        } catch (TransactionException e) {
            log.error("Error while deleting the topic", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
    }

    /**
     * Gets selected Topics.
     *
     * @param selection Information on which part of the topic results to get.
     * @return A list of topics containing the selected results.
     */
    public List<Topic> getSelectedTopics(Selection selection) {
        return null;
    }

    /**
     * Gets selected reports in a particular topic.
     *
     * @param topic             The topic which the reports belong to.
     * @param selection         Information on which part of the report results to get.
     * @param showOpenReports   Whether or not to include open reports.
     * @param showClosedReports Whether or not to include closed reports.
     * @return A list of reports containing the selected results.
     */
    public List<Report> getSelectedReports(Topic topic, Selection selection, boolean showOpenReports,
                                           boolean showClosedReports) {
        List<Report> reports = null;
        try (Transaction transaction = transactionManager.begin()) {
            reports = transaction.newReportGateway().getSelectedReports(topic, selection, showOpenReports, showClosedReports);
            transaction.commit();
        } catch (tech.bugger.persistence.exception.NotFoundException e) {
            log.error("The topic could not be found.", e);
            throw new NotFoundException(messages.getString("not_found_error"), e);
        } catch (TransactionException e) {
            log.error("Error while loading the selected reports in a topic", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return reports;
    }

    /**
     * Gets selected moderators of a particular topic.
     *
     * @param topic     The topic which the moderators belong to.
     * @param selection Information on which part of the moderator results to get.
     * @return A list of users containing the selected results.
     */
    public List<User> getSelectedModerators(Topic topic, Selection selection) {
        return null;
    }

    /**
     * Gets selected users banned from a particular topic.
     *
     * @param topic     The topic which the users are banned from.
     * @param selection Information on which part of the user results to get.
     * @return A list of users containing the selected results.
     */
    public List<User> getSelectedBannedUsers(Topic topic, Selection selection) {
        return null;
    }

    /**
     * Gets the number of reports in a certain topic.
     *
     * @param topic             The topic which the reports belong to.
     * @param showOpenReports   Whether or not to include open reports.
     * @param showClosedReports Whether or not to include closed reports.
     * @return The number of reports.
     */
    public int getNumberOfReports(Topic topic, boolean showOpenReports, boolean showClosedReports) {
        int numberOfTopics = 0;
        try (Transaction transaction = transactionManager.begin()) {
            numberOfTopics = transaction.newTopicGateway().getNumberOfReports(topic, showOpenReports, showClosedReports);
            transaction.commit();
        } catch (tech.bugger.persistence.exception.NotFoundException e) {
            log.error("The topic could not be found.", e);
            throw new NotFoundException(messages.getString("not_found_error"), e);
        } catch (TransactionException e) {
            log.error("Error while loading the topic.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return numberOfTopics;
    }

    /**
     * Gets the number of moderators of a certain topic.
     *
     * @param topic The topic which the moderators belong to.
     * @return The number of moderators.
     */
    public int getNumberOfModerators(Topic topic) {
        return 0;
    }

    /**
     * Gets the number of users banned from a certain topic.
     *
     * @param topic The topic which the users are banned from.
     * @return The number of banned users.
     */
    public int getNumberOfBannedUsers(Topic topic) {
        return 0;
    }

    /**
     * Gets the number of subscribers of a certain topic.
     *
     * @param topic The topic in question.
     * @return The number of subscribers.
     */
    public int getNumberOfSubscribers(Topic topic) {
        return 0;
    }

    /**
     * Gets the number of posts in a certain topic.
     *
     * @param topic The topic which the posts belong to.
     * @return The number of posts.
     */
    public int getNumberOfPosts(Topic topic) {
        return 0;
    }

    /**
     * Gets the number of existing topics.
     *
     * @return The number of topics.
     */
    public int getNumberOfTopics() {
        return 0;
    }

    /**
     * Checks if a user is a moderator of a certain topic.
     *
     * @param user  The user in question.
     * @param topic The topic in question.
     * @return {@code true} if the user is a moderator, {@code false} otherwise.
     */
    public boolean isModerator(User user, Topic topic) {
        return false;
    }

    /**
     * Checks if a user is a banned from a certain topic.
     *
     * @param user  The user in question.
     * @param topic The topic in question.
     * @return {@code true} if the user is banned, {@code false} otherwise.
     */
    public boolean isBanned(User user, Topic topic) {
        return false;
    }

    /**
     * Checks if a user is subscribed to a certain topic.
     *
     * @param user  The user in question.
     * @param topic The topic in question.
     * @return {@code true} if the user is subscribed, {@code false} otherwise.
     */
    public boolean isSubscribed(User user, Topic topic) {
        return false;
    }

    /**
     * Returns the time stamp of the last action in one particular topic. Creating, editing and moving a report as well
     * as creating and editing posts count as actions. Moving a report is an action in the destination topic only.
     *
     * @param topic The topic in question.
     * @return The time stamp of the last action as a {@code ZonedDateTime}.
     */
    public ZonedDateTime lastChange(Topic topic) {
        return null;
    }
}
