package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Service providing methods related to reports. A {@code Feedback} event is fired, if unexpected circumstances occur.
 */
@ApplicationScoped
public class ReportService {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(PostService.class);

    /**
     * Notification service used for sending notifications.
     */
    private final NotificationService notificationService;

    /**
     * Post service used for creating posts.
     */
    private final PostService postService;

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
     * Constructs a new report service with the given dependencies.
     *
     * @param notificationService The notification service to use.
     * @param postService         The post service to use.
     * @param transactionManager  The transaction manager to use for creating transactions.
     * @param feedbackEvent       The feedback event to use for user feedback.
     * @param messagesBundle      The resource bundle for feedback messages.
     */
    @Inject
    public ReportService(final NotificationService notificationService, final PostService postService,
                         final TransactionManager transactionManager, final Event<Feedback> feedbackEvent,
                         final @RegistryKey("messages") ResourceBundle messagesBundle) {
        this.notificationService = notificationService;
        this.postService = postService;
        this.transactionManager = transactionManager;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
    }

    /**
     * Subscribes a user to a report. Afterwards, they will receive notifications if the report is moved or edited, new
     * posts are created in the report or existing posts are edited.
     *
     * @param user   The user who will subscribe to the report.
     * @param report The report receiving the subscription.
     */
    public void subscribeToReport(final User user, final Report report) {

    }

    /**
     * Removes the subscription to a certain report from one user.
     *
     * @param user   The user whose subscription is to be removed.
     * @param report The report the user is subscribed to.
     */
    public void unsubscribeFromReport(final User user, final Report report) {

    }

    /**
     * Closes an open report. User receive no notifications about closed reports.
     *
     * @param report The report to be closed.
     */
    public void close(final Report report) {
        try (Transaction tx = transactionManager.begin()) {
            tx.newReportGateway().closeReport(report);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Could not find report " + report + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error when closing report " + report + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
    }

    /**
     * Opens a closed report.
     *
     * @param report The report to be opened.
     */
    public void open(final Report report) {
        try (Transaction tx = transactionManager.begin()) {
            tx.newReportGateway().openReport(report);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Could not find report " + report + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error when opening report " + report + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
    }

    /**
     * Moves a report to another topic and notifies users about the movement. Notifications are handled by the {@code
     * NotificationService}.
     *
     * @param report The report to be moved.
     * @param topic  The topic where the report is to be moved to.
     */
    public void move(final Report report, final Topic topic) {

    }

    /**
     * Increases the relevance of the report by the user's current voting weight.
     *
     * @param report The report the relevance of which is to be increased.
     * @param user   The user voting on the report.
     */
    public void upvote(final Report report, final User user) {

    }

    /**
     * Decreases the relevance of the report by the user's current voting weight.
     *
     * @param report The report the relevance of which is to be decreased.
     * @param user   The user voting on the report.
     */
    public void downvote(final Report report, final User user) {

    }

    /**
     * Removes the vote on the report of the user. Does nothing if the user has not voted on the report.
     *
     * @param report The report the vote of the user of which is to be removed.
     * @param user   The user whose vote is to be removed.
     */
    public void removeVote(final Report report, final User user) {

    }

    /**
     * Checks if the user has voted to increase the relevance of the report.
     *
     * @param report The report in question.
     * @param user   The user in question.
     * @return {@code true} if they have voted to increase the relevance, {@code false} otherwise.
     */
    public boolean hasUpvoted(final Report report, final User user) {
        return false;
    }

    /**
     * Checks if the user has voted to decrease the relevance of the report.
     *
     * @param report The report in question.
     * @param user   The user in question.
     * @return {@code true} if they have voted to decrease the relevance, {@code false} otherwise.
     */
    public boolean hasDownvoted(final Report report, final User user) {
        return false;
    }

    /**
     * Returns the report with the specified ID, if it exists. If there is no such report, returns {@code null} and
     * fires an event.
     *
     * @param id The ID of the desired report.
     * @return The report with that ID if it exists, {@code null} if there is no report with that ID.
     */
    public Report getReportByID(int id) {
        return null;
    }

    /**
     * Creates a new report along with its first post and notifies users about the creation. Notifications are handled
     * by the {@code NotificationService}.
     *
     * @param report    The report to be created.
     * @param firstPost The first post of the report.
     */
    public void createReport(Report report, Post firstPost) {

    }

    /**
     * Updates an existing report and notifies users about the change. Notifications are handled by the {@code
     * NotificationService}.
     *
     * @param report The report to update.
     */
    public void updateReport(Report report) {

    }

    /**
     * Irreversibly deletes the report and all its posts.
     *
     * @param report The report to be deleted.
     */
    public void deleteReport(final Report report) {
        try (Transaction tx = transactionManager.begin()) {
            tx.newReportGateway().deleteReport(report);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Could not find report " + report + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error when deleting report " + report + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
    }

    /**
     * Marks the report as a duplicate of another report.
     *
     * @param duplicate     The report which is a duplicate.
     * @param duplicateOfID The ID of the report the other report is a duplicate of.
     */
    public void markDuplicate(final Report duplicate, final int duplicateOfID) {

    }

    /**
     * Unmarks the report as a duplicate of another report.
     *
     * @param report The report to be unmarked.
     */
    public void unmarkDuplicate(final Report report) {

    }

    /**
     * Overwrites the relevance of the report with a set value. Users may still vote on the report, but this will not
     * affect the displayed relevance until the overwriting is undone.
     *
     * @param report    The report the relevance of which is to be overwritten.
     * @param relevance The new value of the relevance.
     */
    public void overwriteRelevance(final Report report, final Integer relevance) {
        // if relevance == null, restore original relevance value
    }

    /**
     * Returns the number of posts of a report.
     *
     * @param report The report in question.
     * @return The number of posts as an {@code int}.
     */
    public int getNumberOfPosts(final Report report) {
        int numberOfPosts = 0;
        try (Transaction tx = transactionManager.begin()) {
            numberOfPosts = tx.newReportGateway().countPosts(report);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Could not find report " + report + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error when counting posts of report " + report + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return numberOfPosts;
    }

    /**
     * Returns selected posts for one report.
     *
     * @param report    The report the posts of which are desired.
     * @param selection Information on which posts to get.
     * @return A list containing the selected posts.
     */
    public List<Post> getPostsFor(final Report report, final Selection selection) {
        return null;
    }

    /**
     * Returns the time stamp of the last action in one particular report. Creating, editing and moving a report as well
     * as creating and editing posts count as actions.
     *
     * @param report The report in question.
     * @return The time stamp of the last action as a {@code ZonedDateTime}.
     */
    public ZonedDateTime lastChange(final Report report) {
        return null;
    }

    /**
     * Returns whether the user is privileged for the report in terms of editing rights.
     *
     * @param user The user in question.
     * @param report The report in question.
     * @return {@code true} iff the user is privileged.
     */
    public boolean isPrivileged(final User user, final Report report) {
        // TODO add checks for mods, banned users
        if (user == null) {
            return false;
        } else if (user.isAdministrator()) {
            return true;
        } else {
            return user.equals(report.getAuthorship().getCreator());
        }
    }

}
