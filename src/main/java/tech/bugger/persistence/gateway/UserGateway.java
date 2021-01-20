package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;

import java.util.List;

/**
 * A user gateway allows to query and modify a persistent storage of users.
 */
public interface UserGateway {

    /**
     * Looks up if a user is a moderator of a given topic.
     *
     * @param user  The user whose moderation status to check.
     * @param topic The topic whose moderators to search.
     * @return Whether {@code user} is a moderator of {@code topic}.
     */
    boolean isModerator(User user, Topic topic);

    /**
     * Looks up if a user is banned from a given topic.
     *
     * @param user  The user for which to check if they are banned from the topic.
     * @param topic The topic whose banned users to search.
     * @return Whether {@code user} is banned from {@code topic}.
     */
    boolean isBanned(User user, Topic topic);

    /**
     * Retrieves the number of posts a user has created.
     *
     * @param user The user whose posts to count.
     * @return The number of posts that {@code user} has created.
     * @throws NotFoundException The user could not be found.
     */
    int getNumberOfPosts(User user) throws NotFoundException;

    /**
     * Retrieves the current number of administrators.
     *
     * @return The number of administrators.
     */
    int getNumberOfAdmins();

    /**
     * Retrieves a user by their ID, leaving the avatar empty.
     *
     * @param id The ID of the user to look for.
     * @return The user identified by the ID.
     * @throws NotFoundException The user could not be found.
     */
    User getUserByID(int id) throws NotFoundException;

    /**
     * Retrieves a user by their username, leaving the avatar empty.
     *
     * @param username The username of the user to look for.
     * @return The user identified by the username.
     * @throws NotFoundException The user could not be found.
     */
    User getUserByUsername(String username) throws NotFoundException;

    /**
     * Retrieves a user by their e-mail address, leaving the avatar empty.
     *
     * @param emailAddress The username of the user to look for.
     * @return The user identified by the given {@code emailAddress}.
     * @throws NotFoundException The user could not be found.
     */
    User getUserByEmail(String emailAddress) throws NotFoundException;

    /**
     * Retrieves a user's avatar.
     *
     * @param id The id of the user to look for.
     * @return The avatar of the user identified by the given {@code id}.
     * @throws NotFoundException The user could not be found.
     */
    byte[] getAvatarForUser(int id) throws NotFoundException;

    /**
     * Retrieves a list of moderators for a topic that match the given selection criteria.
     *
     * @param topic     The topic whose moderators to list.
     * @param selection The search criteria to apply.
     * @return The list of moderators of {@code topic}, filtered accordingly.
     * @throws NotFoundException The topic could not be found.
     */
    List<User> getSelectedModerators(Topic topic, Selection selection) throws NotFoundException;

    /**
     * Retrieves a list of users that are banned from a given topic and match the given selection criteria.
     *
     * @param topic     The topic whose banned users to list.
     * @param selection The search criteria to apply.
     * @return The list of users banned from {@code topic}, filtered accordingly.
     * @throws NotFoundException The topic could not be found.
     */
    List<User> getSelectedBannedUsers(Topic topic, Selection selection) throws NotFoundException;

    /**
     * Retrieves the list of subscribers to a user.
     *
     * @param user The user whose subscribers to list.
     * @return The list of subscribers to {@code user}.
     */
    List<User> getSubscribersOf(User user);

    /**
     * Retrieves the list of subscribers to a report.
     *
     * @param report The report whose subscribers to list.
     * @return The list of subscribers to {@code report}.
     */
    List<User> getSubscribersOf(Report report);

    /**
     * Retrieves the list of subscribers to a topic.
     *
     * @param topic The topic whose subscribers to list.
     * @return The list of subscribers to {@code topic}.
     */
    List<User> getSubscribersOf(Topic topic);

    /**
     * Inserts a user into the user storage and sets its internal id, i.e. changes the given {@code user}.
     *
     * @param user The user to insert.
     */
    void createUser(User user);

    /**
     * Updates a user's attributes in the user storage.
     *
     * @param user The user to update.
     * @throws NotFoundException The user could not be found.
     */
    void updateUser(User user) throws NotFoundException;

    /**
     * Deletes a user from the user storage.
     *
     * @param user The user to delete.
     * @throws NotFoundException The user could not be found.
     */
    void deleteUser(User user) throws NotFoundException;

    /**
     * Retrieves the number of topics where a user has moderator status.
     *
     * @param user The user whose moderated topics are to be counted.
     * @return The number of topics that {@code user} is moderating.
     */
    int getNumberOfModeratedTopics(User user);

    /**
     * Cleans up user corpses due to expired registration.
     */
    void cleanExpiredRegistrations();

    /**
     * Retrieves all users the user is subscribed to for a given selection.
     *
     * @param user      The user in question.
     * @param selection The given selection.
     * @return A list of users the user is subscribed to.
     */
    List<User> selectSubscribedUsers(User user, Selection selection);

    /**
     * Counts the number of users the user is subscribed to.
     *
     * @param user The user in question.
     * @return The number of users the user is subscribed to.
     */
    int countSubscribedUsers(User user);

}
