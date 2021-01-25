package tech.bugger.persistence.gateway;

import java.time.OffsetDateTime;
import java.util.List;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;

/**
 * A topic gateway allows to query and modify a persistent storage of topics.
 */
public interface TopicGateway {

    /**
     * Looks up the number of posts of a given report, including or excluding open or closed reports.
     *
     * @param topic             The topic whose reports to look for.
     * @param showOpenReports   Whether to include open reports in the list.
     * @param showClosedReports Whether to include closed reports in the list.
     * @return The list of reports of {@code topic}, filtered accordingly.
     * @throws NotFoundException The topic could not be found.
     */
    int countReports(Topic topic, boolean showOpenReports, boolean showClosedReports)
            throws NotFoundException;

    /**
     * Retrieves the total number of topics in the topic storage.
     *
     * @return The total number of topic in the topic storage.
     */
    int countTopics();

    /**
     * Retrieves the number of moderators of a topic.
     *
     * @param topic The topic whose moderators to count.
     * @return The number of moderators of {@code topic}.
     * @throws NotFoundException The topic could not be found.
     */
    int countModerators(Topic topic) throws NotFoundException;

    /**
     * Retrieves the number of users banned for a topic.
     *
     * @param topic The topic whose banned users to count.
     * @return The number of users banned for {@code topic}.
     * @throws NotFoundException The topic could not be found.
     */
    int countBannedUsers(Topic topic) throws NotFoundException;

    /**
     * Retrieves the number of subscribers to a topic.
     *
     * @param topic The topic whose subscribers to count.
     * @return The number of subscribers of {@code topic}.
     * @throws NotFoundException The topic could not be found.
     */
    int countSubscribers(Topic topic) throws NotFoundException;

    /**
     * Retrieves date and time of the last activity on a report of a given topic.
     *
     * @param topic The topic whose last activity to look up.
     * @return The last activity on {@code topic}.
     * @throws NotFoundException The topic could not be found.
     */
    OffsetDateTime determineLastActivity(Topic topic) throws NotFoundException;

    /**
     * Retrieves a topic by its ID.
     *
     * @param id The ID of the topic to look for.
     * @return The topic identified by the ID.
     * @throws NotFoundException The topic could not be found.
     */
    Topic findTopic(int id) throws NotFoundException;

    /**
     * Retrieves a list of topics that match the given selection criteria.
     *
     * @param selection The search criteria to apply.
     * @return The list of topics, filtered accordingly.
     */
    List<Topic> selectTopics(Selection selection);

    /**
     * Inserts a topic into the topic storage.
     *
     * @param topic The topic to insert.
     * @throws NotFoundException The topic could not be found.
     */
    void createTopic(Topic topic) throws NotFoundException;

    /**
     * Updates a topic's attributes in the topic storage.
     *
     * @param topic The topic to update.
     * @throws NotFoundException The topic could not be found.
     */
    void updateTopic(Topic topic) throws NotFoundException;

    /**
     * Deletes a topic from the topic storage.
     *
     * @param topic The topic to delete.
     * @throws NotFoundException The topic could not be found.
     */
    void deleteTopic(Topic topic) throws NotFoundException;

    /**
     * Bans a user from a topic. Does nothing if the user is already banned from the topic.
     *
     * @param topic The topic to ban a user from.
     * @param user  The user to ban.
     * @throws NotFoundException The topic or the user could not be found.
     */
    void banUser(Topic topic, User user) throws NotFoundException;

    /**
     * Lifts a user's ban from a topic. Does nothing if the user is not banned from the topic.
     *
     * @param topic The topic to unban the user from.
     * @param user  The user to unban.
     * @throws NotFoundException The topic or the user could not be found.
     */
    void unbanUser(Topic topic, User user) throws NotFoundException;

    /**
     * Appoints a user a new moderator of a topic. Does nothing if the user is already a moderator for the topic.
     *
     * @param topic The topic to add a moderator to.
     * @param user  The user to appoint a moderator.
     * @throws NotFoundException The topic or the user could not be found.
     */
    void promoteModerator(Topic topic, User user) throws NotFoundException;

    /**
     * Dismisses a user as a moderator of a topic. Does nothing if the user is not listed as a moderator for the topic.
     *
     * @param topic The topic to remove a moderator from.
     * @param user  The user to dismiss as a moderator.
     * @throws NotFoundException The topic or the user could not be found.
     */
    void demoteModerator(Topic topic, User user) throws NotFoundException;

    /**
     * Discovers all topics in the system.
     *
     * @return The list of all topic titles.
     */
    List<String> discoverTopics();

    /**
     * Retrieves the the topics where a user has moderator status.
     *
     * @param user      The user whose moderated topics are to be found.
     * @param selection The search criteria to apply.
     * @return The topics that {@code user} is moderating.
     */
    List<Topic> getModeratedTopics(User user, Selection selection);

    /**
     * Retrieves all topics the user is subscribed to for a given selection.
     *
     * @param user      The user in question.
     * @param selection The given selection.
     * @return A list of topics the user is subscribed to.
     */
    List<Topic> selectSubscribedTopics(User user, Selection selection);

    /**
     * Counts the number of topics the user is subscribed to.
     *
     * @param user The user in question.
     * @return The number of topics the user is subscribed to.
     */
    int countSubscribedTopics(User user);

}
