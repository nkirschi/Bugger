package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.User;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.DuplicateException;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.TopicGateway;
import tech.bugger.persistence.gateway.UserGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Service providing methods related to topics. A {@code Feedback} event is fired, if unexpected circumstances occur.
 */
@ApplicationScoped
public class TopicService {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(TopicService.class);

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
     * @param feedbackEvent      The feedback event to use for user feedback.
     * @param messagesBundle     The resource bundle for feedback messages.
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
     * @return {@code true} iff the user with given username has successfully been banned from the topic.
     */
    public boolean ban(final String username, final Topic topic) {
        try (Transaction tx = transactionManager.begin()) {
            UserGateway gateway = tx.newUserGateway();
            User user = gateway.getUserByUsername(username);

            if (user.isAdministrator() || gateway.isModerator(user, topic)) {
                log.debug("The user with id " + user.getId() + " cannot be banned from the topic.");
                feedbackEvent.fire(new Feedback(messagesBundle.getString("ban_illegal"), Feedback.Type.ERROR));
                tx.commit();
            } else if (gateway.isBanned(user, topic)) {
                log.debug("The user with id " + user.getId() + " is already banned from the topic with id "
                        + topic.getId());
                feedbackEvent.fire(new Feedback(messagesBundle.getString("is_banned"), Feedback.Type.INFO));
                tx.commit();
            } else {
                tx.newTopicGateway().banUser(topic, user);
                tx.commit();
                feedbackEvent.fire(new Feedback(messagesBundle.getString("operation_successful"),
                        Feedback.Type.INFO));
                return true;
            }

        } catch (NotFoundException e) {
            log.error("The user with the username " + username + " or the topic with id " + topic.getId()
                    + " could not be found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while banning the user with the username " + username + " from the topic with id "
                    + topic.getId(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return false;
    }

    /**
     * Unbans a user from a topic.
     *
     * @param username The user to be unbanned.
     * @param topic    The topic which the user is to be unbanned from.
     * @return {@code true} iff the user with given username has successfully been unbanned from the topic.
     */
    public boolean unban(final String username, final Topic topic) {
        try (Transaction tx = transactionManager.begin()) {
            User user = getUser(username, tx);

            if (user == null) {
                tx.commit();
                return false;
            }

            try {
                tx.newTopicGateway().unbanUser(topic, user);
                tx.commit();
                feedbackEvent.fire(new Feedback(messagesBundle.getString("operation_successful"),
                        Feedback.Type.INFO));
                return true;
            } catch (NotFoundException e) {
                log.warning("No banned user with the username " + username + " could be found for the topic with "
                        + "id " + topic.getId(), e);
                feedbackEvent.fire(new Feedback(messagesBundle.getString("no_ban_found"), Feedback.Type.WARNING));
            }
        } catch (TransactionException e) {
            log.error("Error while banning the user with the username " + username + " from the topic with id "
                    + topic.getId(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return false;
    }

    /**
     * Makes a user a moderator of a topic.
     *
     * @param username The username of the user to be made a moderator.
     * @param topic    The topic which the user is to be made a moderator of.
     * @return {@code true} if the user has successfully been promoted to a moderator or {@code false} if not.
     */
    public boolean makeModerator(final String username, final Topic topic) {
        User user;
        try (Transaction tx = transactionManager.begin()) {
            UserGateway gateway = tx.newUserGateway();
            user = gateway.getUserByUsername(username);

            if (gateway.isModerator(user, topic) || user.isAdministrator()) {
                log.debug("The user with id " + user.getId() + " is already a moderator of the topic with id "
                        + topic.getId());
                feedbackEvent.fire(new Feedback(messagesBundle.getString("is_moderator"), Feedback.Type.INFO));
                tx.commit();
                return false;
            } else {
                TopicGateway topicGateway = tx.newTopicGateway();

                if (isBanned(user, topic)) {
                    topicGateway.unbanUser(topic, user);
                }

                tx.newTopicGateway().promoteModerator(topic, user);
                tx.commit();
                feedbackEvent.fire(new Feedback(messagesBundle.getString("operation_successful"),
                        Feedback.Type.INFO));
            }
        } catch (NotFoundException e) {
            log.error("The user with the username " + username + " or the topic with id " + topic.getId()
                    + " could not be found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
            return false;
        } catch (TransactionException e) {
            log.error("Error while promoting the user with the username " + username + " to a moderator for the "
                    + "topic with id " + topic.getId(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
            return false;
        }
        subscribeToTopic(user, topic);
        return true;
    }

    /**
     * Removes the moderator status of a moderator of a topic. Cannot be applied to administrators.
     *
     * @param username The username of the user who is about to lose moderator privileges.
     * @param topic    The topic which the user is a moderator of.
     * @return {@code true} if the user has successfully been demoted as a moderator or {@code false} if not.
     */
    public boolean removeModerator(final String username, final Topic topic) {
        try (Transaction tx = transactionManager.begin()) {

            User user = getUser(username, tx);

            if (user == null) {
                tx.commit();
                return false;
            }

            if (user.isAdministrator()) {
                log.debug("An administrator cannot be demoted as a moderator.");
                feedbackEvent.fire(new Feedback(messagesBundle.getString("mod_admin"), Feedback.Type.WARNING));
                tx.commit();
                return false;
            }

            try {
                tx.newTopicGateway().demoteModerator(topic, user);
                tx.commit();
                feedbackEvent.fire(new Feedback(messagesBundle.getString("operation_successful"),
                        Feedback.Type.INFO));
                return true;
            } catch (NotFoundException e) {
                log.warning("No moderator with the username " + username + " could be found for the topic with id "
                        + topic.getId(), e);
                feedbackEvent.fire(new Feedback(messagesBundle.getString("no_mod_found"), Feedback.Type.WARNING));
            }

        } catch (TransactionException e) {
            log.error("Error while promoting the user with the username " + username + " to a moderator for the "
                    + "topic with id " + topic.getId(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return false;
    }

    /**
     * Retrieves the user with the given username from the database.
     *
     * @param username The username to search for.
     * @param tx       The current transaction.
     * @return The user if the username could be found in the database or {@code null} if not.
     */
    private User getUser(final String username, final Transaction tx) {
        User user = null;

        try {
            user = tx.newUserGateway().getUserByUsername(username);
        } catch (NotFoundException e) {
            log.error("The user with the username " + username + " could not be found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        }

        return user;
    }

    /**
     * Subscribes a user to a topic.
     *
     * @param user  The user to be subscribed to the topic.
     * @param topic The topic receiving the subscription.
     */
    public void subscribeToTopic(final User user, final Topic topic) {
        if (user == null) {
            log.error("Anonymous users cannot subscribe to anything.");
            throw new IllegalArgumentException("User cannot be null.");
        } else if (user.getId() == null) {
            log.error("Cannot subscribe when user ID is null.");
            throw new IllegalArgumentException("User ID cannot be null.");
        } else if (topic == null) {
            log.error("Cannot subscribe to topic null.");
            throw new IllegalArgumentException("Topic cannot be null.");
        } else if (topic.getId() == null) {
            log.error("Cannot subscribe to topic with ID null.");
            throw new IllegalArgumentException("Topic ID cannot be null.");
        }

        try (Transaction tx = transactionManager.begin()) {
            tx.newSubscriptionGateway().subscribe(topic, user);
            tx.commit();
        } catch (DuplicateException e) {
            log.error("User " + user + " is already subscribed to topic " + topic + ".");
            feedbackEvent.fire(new Feedback(messagesBundle.getString("already_subscribed"), Feedback.Type.WARNING));
        } catch (NotFoundException e) {
            log.error("User " + user + " or topic " + topic + " not found.");
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error when user " + user + " is subscribing to topic " + topic + ".");
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
    }

    /**
     * Unsubscribes a user from a topic.
     *
     * @param user  The user whose subscription is to be removed.
     * @param topic The topic the user is subscribed to.
     */
    public void unsubscribeFromTopic(final User user, final Topic topic) {
        if (user == null) {
            log.error("Anonymous users cannot unsubscribe from anything.");
            throw new IllegalArgumentException("User cannot be null.");
        } else if (user.getId() == null) {
            log.error("Cannot unsubscribe when user ID is null.");
            throw new IllegalArgumentException("User ID cannot be null.");
        } else if (topic == null) {
            log.error("Cannot unsubscribe from topic null.");
            throw new IllegalArgumentException("Topic cannot be null.");
        } else if (topic.getId() == null) {
            log.error("Cannot unsubscribe from topic with ID null.");
            throw new IllegalArgumentException("Topic ID cannot be null.");
        }

        try (Transaction tx = transactionManager.begin()) {
            tx.newSubscriptionGateway().unsubscribe(topic, user);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("User " + user + " or topic " + topic + " not found.");
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error when user " + user + " is unsubscribing from topic " + topic + ".");
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        }
    }

    /**
     * Gets the topic with the specified ID. If no such topic exists, returns {@code null} and fires an event.
     *
     * @param topicID The ID of the desired topic.
     * @return The topic with that ID if it exists, {@code null} if no topic with that ID exists.
     */
    public Topic getTopicByID(final int topicID) {
        Topic topic = null;
        try (Transaction transaction = transactionManager.begin()) {
            topic = transaction.newTopicGateway().findTopic(topicID);
            transaction.commit();
        } catch (tech.bugger.persistence.exception.NotFoundException e) {
            log.error("The topic with id " + topicID + " could not be found.", e);
            throw new tech.bugger.business.exception.NotFoundException(messagesBundle.getString("not_found_error"), e);
        } catch (TransactionException e) {
            log.error("Error while loading the topic with id " + topicID, e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return topic;
    }

    /**
     * Creates a new topic. Only administrators can do that.
     *
     * @param topic The topic to be created.
     * @return {@code true} iff creating the topic succeeded.
     */
    public boolean createTopic(final Topic topic) {
        try (Transaction tx = transactionManager.begin()) {
            tx.newTopicGateway().createTopic(topic);
            tx.commit();
            log.info("Topic created successfully.");
            feedbackEvent.fire(new Feedback(messagesBundle.getString("topic_created"), Feedback.Type.INFO));
            return true;
        } catch (TransactionException | NotFoundException e) {
            log.error("Error while creating a new Topic.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("create_failure"), Feedback.Type.ERROR));
        }
        return false;
    }

    /**
     * Updates an existing topic. Only administrators can do that.
     *
     * @param topic The topic to update.
     * @return {@code true} iff creating the topic succeeded.
     */
    public boolean updateTopic(final Topic topic) {
        // if topic does not exist in database, createTopic instead
        try (Transaction tx = transactionManager.begin()) {
            tx.newTopicGateway().updateTopic(topic);
            tx.commit();
            return true;
        } catch (NotFoundException e) {
            createTopic(topic);
            return true;
        } catch (TransactionException e) {
            log.error("Error while updating a report.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("update_failure"), Feedback.Type.ERROR));
            return false;
        }
    }

    /**
     * Irreversibly deletes a topic, along with all the reports and posts within. Only administrators can do that.
     *
     * @param topic The topic to be deleted.
     */
    public void deleteTopic(final Topic topic) {
        try (Transaction transaction = transactionManager.begin()) {
            transaction.newTopicGateway().deleteTopic(topic);
            transaction.commit();
        } catch (tech.bugger.persistence.exception.NotFoundException e) {
            log.error("The topic could not be found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while deleting the topic", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
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
        List<Report> reports = null;
        try (Transaction transaction = transactionManager.begin()) {
            reports = transaction.newReportGateway()
                    .getSelectedReports(topic, selection, showOpenReports, showClosedReports);
            transaction.commit();
        } catch (tech.bugger.persistence.exception.NotFoundException e) {
            log.error("The topic could not be found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while loading the selected reports in a topic", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
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
    public List<User> getSelectedModerators(final Topic topic, final Selection selection) {
        List<User> users = null;
        try (Transaction tx = transactionManager.begin()) {
            users = tx.newUserGateway().getSelectedModerators(topic, selection);
            tx.commit();
        } catch (NotFoundException e) {
            log.warning("The topic with id " + topic.getId() + " has no moderators.", e);
        } catch (TransactionException e) {
            log.error("Error while loading the selected moderators for the topic with id " + topic.getId(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return users;
    }

    /**
     * Gets selected users banned from a particular topic.
     *
     * @param topic     The topic which the users are banned from.
     * @param selection Information on which part of the user results to get.
     * @return A list of users containing the selected results.
     */
    public List<User> getSelectedBannedUsers(final Topic topic, final Selection selection) {
        List<User> users = null;
        try (Transaction tx = transactionManager.begin()) {
            users = tx.newUserGateway().getSelectedBannedUsers(topic, selection);
            tx.commit();
        } catch (NotFoundException e) {
            log.debug("The topic with id " + topic.getId() + " has no banned users.", e);
        } catch (TransactionException e) {
            log.error("Error while loading the banned users for the topic with id " + topic.getId(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return users;
    }

    /**
     * Returns whether {@code user} is allowed to create a report in {@code topic}.
     *
     * @param user  The user whose rights to check.
     * @param topic The topic in question.
     * @return Whether {@code user} is allowed to create a report in {@code topic}.
     */
    public boolean canCreateReportIn(final User user, final Topic topic) {
        return user != null && topic != null && (user.isAdministrator() || !isBanned(user, topic));
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
        int numberOfTopics = 0;
        try (Transaction transaction = transactionManager.begin()) {
            numberOfTopics = transaction.newTopicGateway().countReports(topic, showOpenReports, showClosedReports);
            transaction.commit();
        } catch (tech.bugger.persistence.exception.NotFoundException e) {
            log.error("The topic could not be found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while loading the topic.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return numberOfTopics;
    }

    /**
     * Gets the number of moderators of a certain topic.
     *
     * @param topic The topic which the moderators belong to.
     * @return The number of moderators.
     */
    public int getNumberOfModerators(final Topic topic) {
        int numberMods = 0;

        try (Transaction transaction = transactionManager.begin()) {
            numberMods = transaction.newTopicGateway().countModerators(topic);
            transaction.commit();
        } catch (NotFoundException e) {
            log.error("The topic with id " + topic.getId() + " could not be found!", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while counting the number of moderators for the topic with id " + topic.getId(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return numberMods;
    }

    /**
     * Gets the number of users banned from a certain topic.
     *
     * @param topic The topic which the users are banned from.
     * @return The number of banned users.
     */
    public int getNumberOfBannedUsers(final Topic topic) {
        int bannedUsers = 0;
        try (Transaction tx = transactionManager.begin()) {
            bannedUsers = tx.newTopicGateway().countBannedUsers(topic);
            tx.commit();
        } catch (NotFoundException e) {
            log.debug("No banned users could be found for the topic with id " + topic.getId(), e);
        } catch (TransactionException e) {
            log.error("Error while counting the number of banned users for the topic with id " + topic.getId(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return bannedUsers;
    }

    /**
     * Gets the number of subscribers of a certain topic.
     *
     * @param topic The topic in question.
     * @return The number of subscribers.
     */
    public int getNumberOfSubscribers(final Topic topic) {
        if (topic == null) {
            log.error("Cannot count subscribers of topic null.");
            throw new IllegalArgumentException("Topic cannot be null.");
        } else if (topic.getId() == null) {
            log.error("Cannot count subscribers of topic with ID null.");
            throw new IllegalArgumentException("Topic ID cannot be null.");
        }

        int count = 0;
        try (Transaction tx = transactionManager.begin()) {
            count = tx.newTopicGateway().countSubscribers(topic);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Cannot find topic " + topic + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error when counting subscribers of topic " + topic + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return count;
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
        boolean isMod = false;
        try (Transaction tx = transactionManager.begin()) {
            isMod = tx.newUserGateway().isModerator(user, topic);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while checking the moderator status of the user with id " + user.getId() + " for the "
                    + "topic with id " + topic.getId(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return isMod;
    }

    /**
     * Checks if a user is a banned from a certain topic.
     *
     * @param user  The user in question.
     * @param topic The topic in question.
     * @return {@code true} if the user is banned, {@code false} otherwise.
     */
    public boolean isBanned(final User user, final Topic topic) {
        boolean isBanned = false;
        try (Transaction tx = transactionManager.begin()) {
            isBanned = tx.newUserGateway().isBanned(user, topic);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while checking if the user with id " + user.getId() + " is banned from the topic "
                    + "with id " + topic.getId(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return isBanned;
    }

    /**
     * Checks if a user is subscribed to a certain topic.
     *
     * @param user  The user in question.
     * @param topic The topic in question.
     * @return {@code true} if the user is subscribed, {@code false} otherwise.
     */
    public boolean isSubscribed(final User user, final Topic topic) {
        if (user == null) {
            return false;
        } else if (user.getId() == null) {
            log.error("Cannot determine subscription status of user with ID null.");
            throw new IllegalArgumentException("User ID cannot be null.");
        } else if (topic == null) {
            log.error("Cannot determine subscription status to topic null.");
            throw new IllegalArgumentException("Topic cannot be null.");
        } else if (topic.getId() == null) {
            log.error("Cannot determine subscription status to topic with ID null.");
            throw new IllegalArgumentException("Topic ID cannot be null.");
        }

        boolean status;
        try (Transaction tx = transactionManager.begin()) {
            status = tx.newSubscriptionGateway().isSubscribed(user, topic);
            tx.commit();
        } catch (NotFoundException e) {
            status = false;
            log.error("Could not find user " + user + " or topic " + topic + ".");
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            status = false;
            log.error("Error when determining subscription status of user " + user + " to topic " + topic + ".");
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return status;
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

    /**
     * Discover all topics in the system.
     *
     * @return A list of all topic titles.
     */
    public List<String> discoverTopics() {
        List<String> topicTitles = Collections.emptyList();
        try (Transaction tx = transactionManager.begin()) {
            topicTitles = tx.newTopicGateway().discoverTopics();
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when discovering all topics.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return topicTitles;
    }

    /**
     * Returns all topics moderated by the given user for a given selection.
     *
     * @param user      The user in question.
     * @param selection The given selection.
     * @return A list of topics moderated by the user.
     */
    public List<Topic> getModeratedTopics(final User user, final Selection selection) {
        List<Topic> moderatedTopics = Collections.emptyList();
        try (Transaction tx = transactionManager.begin()) {
            moderatedTopics = tx.newTopicGateway().getModeratedTopics(user, selection);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while loading the topics moderated by the user with id " + user.getId(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return moderatedTopics;
    }

    /**
     * Returns all topics the user is subscribed to for a given selection.
     *
     * @param user      The user in question.
     * @param selection The given selection.
     * @return A list of topics the user is subscribed to.
     */
    public List<Topic> selectSubscribedTopics(final User user, final Selection selection) {
        if (selection == null) {
            log.error("Cannot select subscribed topics when selection is null.");
            throw new IllegalArgumentException("Selection cannot be null.");
        } else if (user == null) {
            log.error("Cannot select subscribed topics when user is null.");
            throw new IllegalArgumentException("User cannot be null.");
        } else if (user.getId() == null) {
            log.error("Cannot select subscribed topics when user ID is null.");
            throw new IllegalArgumentException("User ID cannot be null.");
        }

        List<Topic> selectedTopics;
        try (Transaction tx = transactionManager.begin()) {
            selectedTopics = tx.newTopicGateway().selectSubscribedTopics(user, selection);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when selecting subscribed topics for user " + user + " with selection " + selection + ".",
                    e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
            selectedTopics = null;
        }
        return selectedTopics;
    }

    /**
     * Counts the number of topics the user is subscribed to.
     *
     * @param user The user in question.
     * @return The number of topics the user is subscribed to.
     */
    public int countSubscribedTopics(final User user) {
        if (user == null) {
            return 0;
        } else if (user.getId() == null) {
            log.error("Cannot count subscribed topics when user ID is null.");
            throw new IllegalArgumentException("User ID cannot be null.");
        }

        int count;
        try (Transaction tx = transactionManager.begin()) {
            count = tx.newTopicGateway().countSubscribedTopics(user);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when counting subscribed topics for user " + user + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
            count = 0;
        }
        return count;
    }

}
