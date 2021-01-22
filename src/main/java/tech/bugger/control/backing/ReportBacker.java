package tech.bugger.control.backing;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.PostService;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.MarkdownHandler;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Backing bean for the report page.
 */
@ViewScoped
@Named
public class ReportBacker implements Serializable {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(ReportBacker.class);

    @Serial
    private static final long serialVersionUID = 7260516443406682026L;

    /**
     * The report for this page.
     */
    private Report report;

    /**
     * The topic the {@code report} is in.
     */
    private Topic topic;

    /**
     * The paginated list of posts.
     */
    private Paginator<Post> posts;

    /**
     * The paginated list of all duplicates.
     */
    private Paginator<Report> duplicates;

    /**
     * The post to be deleted.
     */
    private Post postToBeDeleted;

    /**
     * The currently displayed dialog.
     */
    private Dialog currentDialog;

    /**
     * The application settings cache.
     */
    private final ApplicationSettings applicationSettings;

    /**
     * The report service providing logic.
     */
    private final ReportService reportService;

    /**
     * The post service providing logic.
     */
    private final PostService postService;

    /**
     * The topic service providing logic.
     */
    private final TopicService topicService;

    /**
     * The overwriting relevance.
     */
    private Integer overwriteRelevanceValue;

    /**
     * Whether the user has upvoted the report.
     */
    private boolean upvoted;

    /**
     * Whether the user has downvoted the report.
     */
    private boolean downvoted;

    /**
     * Whether the user is banned in the topic this report is in.
     */
    private boolean banned;

    /**
     * Whether the user is moderator for the topic this report is in.
     */
    private boolean moderator;

    /**
     * Whether the user is privileged for this report.
     */
    private boolean privileged;

    /**
     * Whether the user is subscribed to this report.
     */
    private boolean subscribed;

    /**
     * The user session.
     */
    private final UserSession session;

    /**
     * The current {@link FacesContext} of the application.
     */
    private final FacesContext fctx;

    /**
     * The current {@link ExternalContext} of the application.
     */
    private final ExternalContext ectx;

    public enum Dialog {

        /**
         * Delete a post.
         */
        DELETE_POST,

        /**
         * Delete the report.
         */
        DELETE_REPORT,

        /**
         * Open or close the report.
         */
        OPEN_CLOSE,

        /**
         * Mark the report as a duplicate of another report.
         */
        DUPLICATE
    }

    /**
     * Constructs a new report page backing bean with the necessary dependencies.
     *
     * @param applicationSettings The application settings cache.
     * @param topicService        The topic service to use.
     * @param reportService       The report service to use.
     * @param postService         The post service to use.
     * @param session             The user session.
     * @param fctx                The current {@link FacesContext} of the application.
     * @param ectx                The current {@link ExternalContext} of the application.
     */
    @Inject
    public ReportBacker(final ApplicationSettings applicationSettings,
                        final TopicService topicService,
                        final ReportService reportService,
                        final PostService postService,
                        final UserSession session,
                        final FacesContext fctx,
                        final ExternalContext ectx) {
        this.applicationSettings = applicationSettings;
        this.topicService = topicService;
        this.reportService = reportService;
        this.postService = postService;
        this.session = session;
        this.fctx = fctx;
        this.ectx = ectx;
    }

    /**
     * Initializes the report page. Loads the first few posts of the report to display them. Also checks if the user is
     * allowed to view the report. If not, acts as if the page did not exist.
     */
    @PostConstruct
    void init() {
        log.debug(">>>>>> INIT");

        int reportID;
        Integer postID = null;
        if (ectx.getRequestParameterMap().containsKey("p")) {
            try {
                postID = Integer.parseInt(ectx.getRequestParameterMap().get("p"));
            } catch (NumberFormatException e) {
                fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
                return;
            }
            reportID = reportService.findReportOfPost(postID);
        } else {
            if (!ectx.getRequestParameterMap().containsKey("id")) {
                fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
                return;
            }
            try {
                reportID = Integer.parseInt(ectx.getRequestParameterMap().get("id"));
            } catch (NumberFormatException e) {
                fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
                return;
            }
        }

        report = reportService.getReportByID(reportID);
        if (report == null) { // no report with this ID
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
            return;
        }
        topic = topicService.getTopicByID(report.getTopicID());
        if (topic == null) { // this should never happen!
            throw new InternalError("Report " + report + " without topic!");
        }

        // now begin actually initializing the page content
        currentDialog = null;
        banned = topicService.isBanned(session.getUser(), topic);
        moderator = topicService.isModerator(session.getUser(), topic);
        privileged = session.getUser() != null && !banned
                && (session.getUser().isAdministrator() || moderator
                || session.getUser().equals(report.getAuthorship().getCreator()));
        subscribed = reportService.isSubscribed(session.getUser(), report);

        // disallow access for banned users if guest mode is inactive
        if (!applicationSettings.getConfiguration().isGuestReading() && session.getUser() != null && banned) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
            return;
        }

        posts = new Paginator<>("created_at", Selection.PageSize.NORMAL) {
            @Override
            protected Iterable<Post> fetch() {
                List<Post> posts = reportService.getPostsFor(report, getSelection());
                posts.forEach(p -> p.setContent(MarkdownHandler.toHtml(p.getContent())));
                return posts;
            }

            @Override
            protected int totalSize() {
                return reportService.getNumberOfPosts(report);
            }
        };

        duplicates = new Paginator<>("ID", Selection.PageSize.TINY) {
            @Override
            protected Iterable<Report> fetch() {
                return reportService.getDuplicatesFor(report, getSelection());
            }

            @Override
            protected int totalSize() {
                return reportService.getNumberOfDuplicates(report);
            }
        };

        if (postID != null) {
            int pID = postID;
            while (StreamSupport.stream(posts.spliterator(), false).noneMatch(p -> p.getId() == pID)) {
                try {
                    posts.nextPage();
                } catch (IllegalStateException e) {
                    log.error("Could not find post with ID " + postID + " when displaying report page.");
                    fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
                    return;
                }
            }
        }
        updateRelevance();
    }

    /**
     * Updates the values for the relevance interface.
     */
    private void updateRelevance() {
        log.debug(">>>>>> updateRelevance");

        report = reportService.getReportByID(report.getId());
        if (report == null) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
            return;
        }
        if (session.getUser() != null) {
            upvoted = reportService.hasUpvoted(report, session.getUser());
            downvoted = reportService.hasDownvoted(report, session.getUser());
            if (report.isRelevanceOverwritten()) {
                overwriteRelevanceValue = report.getRelevance();
            }
        }
    }

    /**
     * Displays the specified dialog and reloads the page. {@code null} closes the dialog.
     *
     * @param dialog The dialog to display.
     * @return {@code null} to reload the page.
     */
    public String displayDialog(final Dialog dialog) {
        log.debug(">>>>>> displayDialog");
        currentDialog = dialog;
        log.info("Displaying dialog " + dialog + ".");
        return null;
    }

    /**
     * Displays the dialog for deleting a post and remembers which post to delete.
     *
     * @param post The post to delete.
     * @return {@code null} to reload the page.
     */
    public String deletePostDialog(final Post post) {
        log.debug(">>>>>> deletePostDialog");
        postToBeDeleted = post;
        return displayDialog(Dialog.DELETE_POST);
    }

    /**
     * Adds or removes a subscription to the report for the user, whichever is applicable.
     *
     * @return {@code null}
     */
    public String toggleReportSubscription() {
        log.debug(">>>>>> toggleReportSubscription");
        if (session.getUser() == null) {
            return null;
        }
        if (reportService.isSubscribed(session.getUser(), report)) {
            reportService.unsubscribeFromReport(session.getUser(), report);
        } else {
            reportService.subscribeToReport(session.getUser(), report);
        }
        subscribed = !subscribed;
        return null;
    }

    /**
     * Increases the relevance of the report by the user's voting weight.
     *
     * @return {@code null} to reload the page.
     */
    public String upvote() {
        log.debug(">>>>>> upvote");
        if (session.getUser() != null) {
            reportService.upvote(report, session.getUser());
        }
        updateRelevance();
        return null;
    }

    /**
     * Decreases the relevance of the report by the user's voting weight.
     *
     * @return {@code null} to reload the page.
     */
    public String downvote() {
        log.debug(">>>>>> downvote");
        if (session.getUser() != null) {
            reportService.downvote(report, session.getUser());
        }
        updateRelevance();
        return null;
    }

    /**
     * Removes a vote from a report.
     *
     * @return {@code null} to reload the page.
     */
    public String removeVote() {
        log.debug(">>>>>> removeVote");
        if (session.getUser() != null) {
            reportService.removeVote(report, session.getUser());
        }
        updateRelevance();
        return null;
    }

    /**
     * Opens a closed report and closes an open one.
     */
    public void toggleOpenClosed() {
        log.debug(">>>>>> toggleOpenClosed");
        if (report.getClosingDate() == null) {
            reportService.close(report);
        } else {
            reportService.open(report);
        }
        displayDialog(null);
    }

    /**
     * Deletes the report along with all its posts irreversibly.
     */
    public void delete() {
        log.debug(">>>>>> delete");
        reportService.deleteReport(report);
    }

    /**
     * Marks the report as a duplicate of another report. This automatically closes the report.
     */
    public void markDuplicate() {
        log.debug(">>>>>> markDuplicate");
        if (isPrivileged() && reportService.markDuplicate(report, report.getDuplicateOf())) {
            reportService.close(report);
            displayDialog(null);
            duplicates.update();
        }
    }

    /**
     * Removes the marking signifying that the report is a duplicate of another one.
     */
    public void unmarkDuplicate() {
        log.debug(">>>>>> unmarkDuplicate");
        if (isPrivileged()) {
            reportService.unmarkDuplicate(report);
        }
    }

    /**
     * Overwrites the relevance of the report with a set value.
     *
     * @return {@code null} to reload the page.
     */
    public String applyOverwriteRelevance() {
        log.debug(">>>>>> applyOverwriteRelevance");
        if (session.getUser() != null && session.getUser().isAdministrator()) {
            reportService.overwriteRelevance(report, overwriteRelevanceValue);
            updateRelevance();
            return null;
        }
        return "pretty:error";
    }

    /**
     * Deletes the {@code postToBeDeleted} irreversibly. If it is the first post, this deletes the whole report.
     */
    public void deletePost() {
        log.debug(">>>>>> deletePost");
        postService.deletePost(postToBeDeleted, report);
        displayDialog(null);
    }

    /**
     * Checks if the user is allowed to edit the report.
     *
     * @return  {@code true} iff the user is allowed to edit the report.
     */
    public boolean isAllowedToEdit() {
        return session.getUser() != null
                && !isBanned()
                && (report.getClosingDate() == null || applicationSettings.getConfiguration().isClosedReportPosting());
    }

    /**
     * Checks if the user is privileged for the post.
     *
     * @param post The post in question.
     * @return {@code true} iff the user is privileged.
     */
    public boolean privilegedForPost(final Post post) {
        log.debug(">>>>>> privilegedForPost");
        return session.getUser() != null
                && (report.getClosingDate() == null || applicationSettings.getConfiguration().isClosedReportPosting())
                && (session.getUser().isAdministrator() || moderator
                || session.getUser().equals(post.getAuthorship().getCreator()));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // getters and setters                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return The report.
     */
    public Report getReport() {
        return report;
    }

    /**
     * @return The relevance overwriting value.
     */
    public Integer getOverwriteRelevanceValue() {
        return overwriteRelevanceValue;
    }

    /**
     * @param overwriteRelevanceValue The new overwriting relevance.
     */
    public void setOverwriteRelevanceValue(final Integer overwriteRelevanceValue) {
        this.overwriteRelevanceValue = overwriteRelevanceValue;
    }

    /**
     * @return Whether the user has upvoted this report.
     */
    public boolean isUpvoted() {
        return upvoted;
    }

    /**
     * @return Whether the user has downvoted this report.
     */
    public boolean isDownvoted() {
        return downvoted;
    }

    /**
     * @param report The report to set.
     */
    public void setReport(final Report report) {
        this.report = report;
    }

    /**
     * @return The postToBeDeleted.
     */
    public Post getPostToBeDeleted() {
        return postToBeDeleted;
    }

    /**
     * @param postToBeDeleted The postToBeDeleted to set.
     */
    public void setPostToBeDeleted(final Post postToBeDeleted) {
        this.postToBeDeleted = postToBeDeleted;
    }

    /**
     * Returns the paginator managing all posts of the currently shown report.
     *
     * @return The paginator managing the posts.
     */
    public Paginator<Post> getPosts() {
        return posts;
    }

    /**
     * Returns the paginator managing all duplicates of the currently shown report.
     *
     * @return The paginator managing the duplicates.
     */
    public Paginator<Report> getDuplicates() {
        return duplicates;
    }

    /**
     * Gets the current dialog.
     *
     * @return The current dialog.
     */
    public Dialog getCurrentDialog() {
        return currentDialog;
    }

    /**
     * Returns whether the user is subscribed to the report.
     *
     * @return {@code true} iff the user is subscribed to the report.
     */
    public boolean isSubscribed() {
        return subscribed;
    }

    /**
     * Checks if the user is banned from the topic the report is located in.
     *
     * @return {@code true} if the user is banned and {@code false} otherwise.
     */
    public boolean isBanned() {
        return banned;
    }

    /**
     * Checks if the user is privileged, i.e. an admin, a mod or the creator of the report.
     *
     * @return {@code true} if the user is privileged and {@code false} otherwise.
     */
    public boolean isPrivileged() {
        return privileged;
    }

}
