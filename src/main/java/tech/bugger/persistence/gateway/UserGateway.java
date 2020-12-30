package tech.bugger.persistence.gateway;

import java.util.List;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;

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
     * @throws NotFoundException The user or the topic could not be found.
     */
    boolean isModerator(User user, Topic topic) throws NotFoundException;

    /**
     * Looks up if a user is banned from a given topic.
     *
     * @param user  The user for which to check if they are banned from the topic.
     * @param topic The topic whose banned users to search.
     * @return Whether {@code user} is banned from {@code topic}.
     * @throws NotFoundException The user or the topic could not be found.
     */
    boolean isBanned(User user, Topic topic) throws NotFoundException;

    /**
     * Retrieves the number of posts a user has created.
     *
     * @param user The user whose posts to count.
     * @return The number of posts that {@code user} has created.
     * @throws NotFoundException The user could not be found.
     */
    int getNumberOfPosts(User user) throws NotFoundException;

    /**
     * Retrieves the voting weight of a user.
     *
     * @param user The user whose voting weight to look up.
     * @return The user's voting weight.
     * @throws NotFoundException The user could not be found.
     */
    int getVotingWeight(User user) throws NotFoundException;

    /**
     * Retrieves the list of email addresses of all administrators.
     *
     * @return The list of email addresses of all administrators.
     */
    List<String> getAdminEmails();

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user to look for.
     * @return The user identified by the ID.
     * @throws NotFoundException The user could not be found.
     */
    User getUserByID(int id) throws NotFoundException;

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user to look for.
     * @return The user identified by the username.
     * @throws NotFoundException The user could not be found.
     */
    User getUserByUsername(String username) throws NotFoundException;

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
     * @throws NotFoundException The user could not be found.
     */
    List<User> getSubscribersOf(User user) throws NotFoundException;

    /**
     * Retrieves the list of subscribers to a report.
     *
     * @param report The report whose subscribers to list.
     * @return The list of subscribers to {@code report}.
     * @throws NotFoundException The report could not be found.
     */
    List<User> getSubscribersOf(Report report) throws NotFoundException;

    /**
     * Retrieves the list of subscribers to a topic.
     *
     * @param topic The topic whose subscribers to list.
     * @return The list of subscribers to {@code topic}.
     * @throws NotFoundException The topic could not be found.
     */
    List<User> getSubscribersOf(Topic topic) throws NotFoundException;

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
     * Checks whether the given {@code emailAddress} is already assigned to any user.
     *
     * @param emailAddress The e-mail address to check.
     * @return Whether the given {@code emailAddress} is already assigned to any user.
     */
    boolean isEmailAssigned(String emailAddress);

    /**
     * Checks whether the given {@code username} is already assigned to any user.
     *
     * @param username The username to check.
     * @return Whether the given {@code username} is already assigned to any user.
     */
    boolean isUsernameAssigned(String username);

}
