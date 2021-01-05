package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.*;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.AttachmentGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Service providing methods related to reports. A {@code Feedback} event is fired, if unexpected circumstances occur.
 */
@Dependent
public class ReportService implements Serializable {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(PostService.class);

    @Inject
    NotificationService notificationService;

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
     * @param transactionManager The transaction manager to use for creating transactions.
     * @param feedbackEvent      The feedback event to use for user feedback.
     * @param messagesBundle     The resource bundle for feedback messages.
     */
    @Inject
    public ReportService(final TransactionManager transactionManager, final Event<Feedback> feedbackEvent,
                         final @RegistryKey("messages") ResourceBundle messagesBundle) {
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
    public void subscribeToReport(User user, Report report) {
    }

    /**
     * Removes the subscription to a certain report from one user.
     *
     * @param user   The user whose subscription is to be removed.
     * @param report The report the user is subscribed to.
     */
    public void unsubscribeFromReport(User user, Report report) {

    }

    /**
     * Closes an open report. User receive no notifications about closed reports.
     *
     * @param report The report to be closed.
     */
    public void close(Report report) {

    }

    /**
     * Opens a closed report.
     *
     * @param report The report to be opened.
     */
    public void open(Report report) {

    }

    /**
     * Moves a report to another topic and notifies users about the movement. Notifications are handled by the {@code
     * NotificationService}.
     *
     * @param report The report to be moved.
     * @param topic  The topic where the report is to be moved to.
     */
    public void move(Report report, Topic topic) {

    }

    /**
     * Increases the relevance of the report by the user's current voting weight.
     *
     * @param report The report the relevance of which is to be increased.
     * @param user   The user voting on the report.
     */
    public void upvote(Report report, User user) {

    }

    /**
     * Decreases the relevance of the report by the user's current voting weight.
     *
     * @param report The report the relevance of which is to be decreased.
     * @param user   The user voting on the report.
     */
    public void downvote(Report report, User user) {

    }

    /**
     * Removes the vote on the report of the user. Does nothing if the user has not voted on the report.
     *
     * @param report The report the vote of the user of which is to be removed.
     * @param user   The user whose vote is to be removed.
     */
    public void removeVote(Report report, User user) {

    }

    /**
     * Checks if the user has voted to increase the relevance of the report.
     *
     * @param report The report in question.
     * @param user   The user in question.
     * @return {@code true} if they have voted to increase the relevance, {@code false} otherwise.
     */
    public boolean hasUpvoted(Report report, User user) {
        return false;
    }

    /**
     * Checks if the user has voted to decrease the relevance of the report.
     *
     * @param report The report in question.
     * @param user   The user in question.
     * @return {@code true} if they have voted to decrease the relevance, {@code false} otherwise.
     */
    public boolean hasDownvoted(Report report, User user) {
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
     * @return {@code true} iff creating the report succeeded.
     */
    public boolean createReport(Report report, Post firstPost) {
        // Notifications will be dealt with when implementing the subscriptions feature.
        try (Transaction tx = transactionManager.begin()) {
            tx.newReportGateway().create(report);
            tx.newPostGateway().create(firstPost);
            tx.commit();
            log.info("Report created successfully.");
            feedbackEvent.fire(new Feedback(messagesBundle.getString("report_created"), Feedback.Type.INFO));
            return true;
        } catch (TransactionException e) {
            log.error("Error while creating a new report", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("create_failure"), Feedback.Type.ERROR));
            return false;
        }
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
    public void deleteReport(Report report) {

    }

    /**
     * Marks the report as a duplicate of another report.
     *
     * @param duplicate     The report which is a duplicate.
     * @param duplicateOfID The ID of the report the other report is a duplicate of.
     */
    public void markDuplicate(Report duplicate, int duplicateOfID) {

    }

    /**
     * Unmarks the report as a duplicate of another report.
     *
     * @param report The report to be unmarked.
     */
    public void unmarkDuplicate(Report report) {

    }

    /**
     * Overwrites the relevance of the report with a set value. Users may still vote on the report, but this will not
     * affect the displayed relevance until the overwriting is undone.
     *
     * @param report    The report the relevance of which is to be overwritten.
     * @param relevance The new value of the relevance.
     */
    public void overwriteRelevance(Report report, Integer relevance) {
        // if relevance == null, restore original relevance value
    }

    /**
     * Returns the number of posts of a report.
     *
     * @param report The report in question.
     * @return The number of posts as an {@code int}.
     */
    public int getNumberOfPosts(Report report) {
        return 0;
    }

    /**
     * Returns selected posts for one report.
     *
     * @param report    The report the posts of which are desired.
     * @param selection Information on which posts to get.
     * @return A list containing the selected posts.
     */
    public List<Post> getPostsFor(Report report, Selection selection) {
        return null;
    }

    /**
     * Returns the time stamp of the last action in one particular report. Creating, editing and moving a report as well
     * as creating and editing posts count as actions.
     *
     * @param report The report in question.
     * @return The time stamp of the last action as a {@code ZonedDateTime}.
     */
    public ZonedDateTime lastChange(Report report) {
        return null;
    }
}
