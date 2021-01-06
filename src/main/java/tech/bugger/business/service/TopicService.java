package tech.bugger.business.service;

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
import javax.inject.Inject;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Service providing methods related to topics. A {@code Feedback} event is fired, if unexpected circumstances occur.
 */
@Dependent
public class TopicService implements Serializable {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(TopicService.class);

    @Serial
    private static final long serialVersionUID = -8262090151411485508L;

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
     * Constructs a new topic service with the given dependencies.
     *
     * @param transactionManager The transaction manager to use for creating transactions.
     * @param feedbackEvent The feedback event to use for user feedback.
     * @param messagesBundle The resource bundle for feedback messages.
     */
    @Inject
    public TopicService(final TransactionManager transactionManager, final Event<Feedback> feedbackEvent,
                        final @RegistryKey("messages") ResourceBundle messagesBundle) {
        this.transactionManager = transactionManager;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
    }

    /**
     * Bans a user from a topic. Administrators and moderators of the topic cannot be banned.
     *
     * @param username The username of the user to be banned.
     * @param topic    The topic which the user is to be banned from.
     */
    public void ban(final String username, final Topic topic) {
    }

    /**
     * Unbans a user from a topic.
     *
     * @param user  The user to be unbanned.
     * @param topic The topic which the user is to be unbanned from.
     */
    public void unban(final User user, final Topic topic) {

    }

    /**
     * Makes a user a moderator of a topic.
     *
     * @param username The username of the user to be made a moderator.
     * @param topic    The topic which the user is to be made a moderator of.
     */
    public void makeModerator(final String username, final Topic topic) {

    }

    /**
     * Removes the moderator status of a moderator of a topic. Cannot be applied to administrators.
     *
     * @param user  The user who is about to lose moderator privileges.
     * @param topic The topic which the user is a moderator of.
     */
    public void removeModerator(final User user, final Topic topic) {

    }

    /**
     * Subscribes a user to a topic.
     *
     * @param user  The user to be subscribed to the topic.
     * @param topic The topic receiving the subscription.
     */
    public void subscribeToTopic(final User user, final Topic topic) {
    }

    /**
     * Unsubscribes a user from a topic.
     *
     * @param user  The user whose subscription is to be removed.
     * @param topic The topic the user is subscribed to.
     */
    public void unsubscribeFromTopic(final User user, final Topic topic) {

    }

    /**
     * Gets the topic with the specified ID. If no such topic exists, returns {@code null} and fires an event.
     *
     * @param topicID The ID of the desired topic.
     * @return The topic with that ID if it exists, {@code null} if no topic with that ID exists.
     */
    public Topic getTopicByID(final int topicID) {
        return null;
    }

    /**
     * Creates a new topic. Only administrators can do that.
     *
     * @param topic The topic to be created.
     */
    public void createTopic(final Topic topic) {

    }

    /**
     * Updates an existing topic. Only administrators can do that.
     *
     * @param topic The topic to update.
     */
    public void updateTopic(final Topic topic) {
        // if topic does not exist in database, createTopic instead
    }

    /**
     * Irreversibly deletes a topic, along with all the reports and posts within. Only administrators can do that.
     *
     * @param topic The topic to be deleted.
     */
    public void deleteTopic(final Topic topic) {

    }

    /**
     * Gets selected Topics.
     *
     * @param selection Information on which part of the topic results to get.
     * @return A list of topics containing the selected results.
     */
    public List<Topic> selectTopics(final Selection selection) {
        if (selection == null) {
            IllegalArgumentException e = new IllegalArgumentException("Selection cannot be null.");
            log.error("Error when loading topics with Selection null.", e);
            throw e;
        }

        List<Topic> selectedTopics;
        try (Transaction tx = transactionManager.begin()) {
            selectedTopics = tx.newTopicGateway().selectTopics(selection);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when loading selected topics.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
            selectedTopics = null;
        }
        return selectedTopics;
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
    public List<Report> getSelectedReports(final Topic topic, final Selection selection, final boolean showOpenReports,
                                           final boolean showClosedReports) {
        return null;
    }

    /**
     * Gets selected moderators of a particular topic.
     *
     * @param topic     The topic which the moderators belong to.
     * @param selection Information on which part of the moderator results to get.
     * @return A list of users containing the selected results.
     */
    public List<User> getSelectedModerators(final Topic topic, final Selection selection) {
        return null;
    }

    /**
     * Gets selected users banned from a particular topic.
     *
     * @param topic     The topic which the users are banned from.
     * @param selection Information on which part of the user results to get.
     * @return A list of users containing the selected results.
     */
    public List<User> getSelectedBannedUsers(final Topic topic, final Selection selection) {
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
    public int getNumberOfReports(final Topic topic, final boolean showOpenReports, final boolean showClosedReports) {
        return 0;
    }

    /**
     * Gets the number of moderators of a certain topic.
     *
     * @param topic The topic which the moderators belong to.
     * @return The number of moderators.
     */
    public int getNumberOfModerators(final Topic topic) {
        return 0;
    }

    /**
     * Gets the number of users banned from a certain topic.
     *
     * @param topic The topic which the users are banned from.
     * @return The number of banned users.
     */
    public int getNumberOfBannedUsers(final Topic topic) {
        return 0;
    }

    /**
     * Gets the number of subscribers of a certain topic.
     *
     * @param topic The topic in question.
     * @return The number of subscribers.
     */
    public int getNumberOfSubscribers(final Topic topic) {
        return 0;
    }

    /**
     * Gets the number of posts in a certain topic.
     *
     * @param topic The topic which the posts belong to.
     * @return The number of posts.
     */
    public int getNumberOfPosts(final Topic topic) {
        return 0;
    }

    /**
     * Gets the number of existing topics.
     *
     * @return The number of topics.
     */
    public int countTopics() {
        int numberOfTopics = 0;
        try (Transaction tx = transactionManager.begin()) {
            numberOfTopics = tx.newTopicGateway().countTopics();
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when loading number of topics.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return numberOfTopics;
    }

    /**
     * Checks if a user is a moderator of a certain topic.
     *
     * @param user  The user in question.
     * @param topic The topic in question.
     * @return {@code true} if the user is a moderator, {@code false} otherwise.
     */
    public boolean isModerator(final User user, final Topic topic) {
        return false;
    }

    /**
     * Checks if a user is a banned from a certain topic.
     *
     * @param user  The user in question.
     * @param topic The topic in question.
     * @return {@code true} if the user is banned, {@code false} otherwise.
     */
    public boolean isBanned(final User user, final Topic topic) {
        return false;
    }

    /**
     * Checks if a user is subscribed to a certain topic.
     *
     * @param user  The user in question.
     * @param topic The topic in question.
     * @return {@code true} if the user is subscribed, {@code false} otherwise.
     */
    public boolean isSubscribed(final User user, final Topic topic) {
        return false;
    }

    /**
     * Returns the time stamp of the last action in one particular topic. Creating, editing and moving a report as well
     * as creating and editing posts count as actions. Moving a report is an action in the destination topic only.
     *
     * @param topic The topic in question.
     * @return The time stamp of the last action as a {@code ZonedDateTime}.
     */
    public ZonedDateTime lastChange(final Topic topic) {
        if (topic == null) {
            log.error("Error while determining last change with topic null.");
            throw new IllegalArgumentException("Topic cannot be null.");
        } else if (topic.getId() == null) {
            log.error("Error while determining last change with topic ID null.");
            throw new IllegalArgumentException("Topic ID cannot be null.");
        }

        ZonedDateTime lastChange = null;
        try (Transaction tx = transactionManager.begin()) {
            lastChange = tx.newTopicGateway().determineLastActivity(topic);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Topic " + topic + " could not be found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error when determining last change in topic " + topic + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return lastChange;
    }

}
