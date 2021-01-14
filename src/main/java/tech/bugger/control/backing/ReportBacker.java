package tech.bugger.control.backing;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.stream.StreamSupport;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.PostService;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

/**
 * Backing bean for the report page.
 */
@ViewScoped
@Named
public class ReportBacker implements Serializable {

    public enum ReportPageDialog {

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
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(ReportBacker.class);

    @Serial
    private static final long serialVersionUID = 7260516443406682026L;

    /**
     * The report.
     */
    private Report report;

    /**
     * The paginated list of posts.
     */
    private Paginator<Post> posts;

    /**
     * The paginated list of all duplicates.
     */
    private Paginator<Report> duplicates;

    /**
     * The ID of the report the current report is potentially a duplicate of.
     */
    private Integer duplicateOfID;

    /**
     * The post to be deleted.
     */
    private Post postToBeDeleted;

    /**
     * The currently displayed dialog.
     */
    private ReportPageDialog currentDialog;

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
     * The overwriting relevance.
     */
    private Integer overwriteRelevanceValue;

    /**
     * Whether the user has upvoted the report.
     */
    private boolean hasUpvoted;

    /**
     * Whether the user has downvoted the report.
     */
    private boolean hasDownvoted;

    /**
     * The user session.
     */
    private final UserSession session;

    /**
     * The current {@link FacesContext}.
     */
    private final FacesContext fctx;

    /**
     * Constructs a new report page backing bean with the necessary dependencies.
     *
     * @param applicationSettings The application settings cache.
     * @param reportService       The report service to use.
     * @param postService         The post service to use.
     * @param session             The user session.
     * @param fctx                The current {@link FacesContext} of the application.
     */
    @Inject
    public ReportBacker(final ApplicationSettings applicationSettings, final ReportService reportService,
                        final PostService postService, final UserSession session,
                        final FacesContext fctx) {
        this.applicationSettings = applicationSettings;
        this.reportService = reportService;
        this.postService = postService;
        this.session = session;
        this.fctx = fctx;
    }

    /**
     * Initializes the report page. Loads the first few posts of the report to display them. Also checks if the user is
     * allowed to view the report. If not, acts as if the page did not exist.
     */
    @PostConstruct
    void init() {
        ExternalContext ext = fctx.getExternalContext();
        int reportID;
        Integer postID = null;
        if (ext.getRequestParameterMap().containsKey("p")) {
            try {
                postID = Integer.parseInt(ext.getRequestParameterMap().get("p"));
            } catch (NumberFormatException e) {
                fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
                return;
            }
            reportID = reportService.findReportOfPost(postID);
        } else {
            if (!ext.getRequestParameterMap().containsKey("id")) {
                fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
                return;
            }
            try {
                reportID = Integer.parseInt(ext.getRequestParameterMap().get("id"));
            } catch (NumberFormatException e) {
                fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
                return;
            }
        }
        report = reportService.getReportByID(reportID);
        if (report == null) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
            return;
        }
        duplicateOfID = report.getDuplicateOf();
        User user = session.getUser();
        boolean maySee = false;
        if (applicationSettings.getConfiguration().isGuestReading()) {
            maySee = true;
        } else if (user != null && !isBanned()) {
            maySee = true;
        }
        if (!maySee) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:home");
            return;
        }
        currentDialog = null;
        posts = new Paginator<>("created_at", Selection.PageSize.NORMAL) {
            @Override
            protected Iterable<Post> fetch() {
                return reportService.getPostsFor(report, getSelection());
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
        log.debug("Paginator initialized with Selection " + posts.getSelection() + " and items "
                + posts.getWrappedData());
        if (postID != null) {
            Post post = new Post(postID, null, null, null, null);
            while (StreamSupport.stream(posts.spliterator(), false).noneMatch(post::equals)) {
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
        report = reportService.getReportByID(report.getId());
        if (report == null) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
            return;
        }
        if (session.getUser() != null) {
            hasUpvoted = hasUpvoted();
            hasDownvoted = hasDownvoted();
            /**
             * Whether the relevance is overwritten.
             */
            boolean overwriteRelevance = report.getRelevanceOverwritten();
            if (overwriteRelevance) {
                overwriteRelevanceValue = report.getRelevance();
            } else {
                overwriteRelevanceValue = null;
            }
        }
    }

    /**
     * Displays the specified dialog and reloads the page. {@code null} closes the dialog.
     *
     * @param dialog The dialog to display.
     * @return {@code null} to reload the page.
     */
    public String displayDialog(final ReportPageDialog dialog) {
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
        postToBeDeleted = post;
        return displayDialog(ReportPageDialog.DELETE_POST);
    }

    /**
     * Adds or removes a subscription to the report for the user, whichever is applicable.
     */
    public void toggleReportSubscription() {
    }

    /**
     * Increases the relevance of the report by the user's voting weight.
     *
     * @return {@code null} to reload the page.
     */
    public String upvote() {
        reportService.upvote(report, session.getUser());
        updateRelevance();
        return null;
    }

    /**
     * Decreases the relevance of the report by the user's voting weight.
     *
     * @return {@code null} to reload the page.
     */
    public String downvote() {
        reportService.downvote(report, session.getUser());
        updateRelevance();
        return null;
    }

    /**
     * Removes a vote from a report.
     *
     * @return {@code null} to reload the page.
     */
    public String removeVote() {
        reportService.removeVote(report, session.getUser());
        updateRelevance();
        return null;
    }

    /**
     * Returns if the user has voted to increase the relevance of the report.
     *
     * @return {@code true} if the user has voted up and {@code false} otherwise.
     */
    public boolean hasUpvoted() {
        return reportService.hasUpvoted(report, session.getUser());
    }

    /**
     * Returns if the user has voted to decrease the relevance of the report.
     *
     * @return {@code true} if the user has voted down and {@code false} otherwise.
     */
    public boolean hasDownvoted() {
        return reportService.hasDownvoted(report, session.getUser());
    }

    /**
     * Opens the current report.
     */
    private void open() {
        report.setClosingDate(null);
        reportService.open(report);
    }

    /**
     * Closes the current report.
     */
    private void close() {
        report.setClosingDate(ZonedDateTime.now());
        reportService.close(report);
    }

    /**
     * Opens a closed report and closes an open one.
     */
    public void toggleOpenClosed() {
        if (report.getClosingDate() == null) {
            close();
        } else {
            open();
        }
        displayDialog(null);
    }

    /**
     * Deletes the report along with all its posts irreversibly.
     */
    public void delete() {
        reportService.deleteReport(report);
    }

    /**
     * Marks the report as a duplicate of another report. This automatically closes the report.
     */
    public void markDuplicate() {
        if (duplicateOfID != null && isPrivileged() && reportService.markDuplicate(report, duplicateOfID)) {
            close();
            displayDialog(null);
            duplicates.update();
        }
    }

    /**
     * Removes the marking signifying that the report is a duplicate of another one.
     */
    public void unmarkDuplicate() {
        if (isPrivileged() && reportService.unmarkDuplicate(report)) {
            duplicateOfID = null;
        }
    }

    /**
     * Overwrites the relevance of the report with a set value.
     *
     * @return {@code null} to reload the page.
     */
    public String applyOverwriteRelevance() {
        if (session.getUser().isAdministrator()) {
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
        postService.deletePost(postToBeDeleted);
    }

    /**
     * Checks if the user is privileged, i.e. an admin, a mod or the creator of the report.
     *
     * @return {@code true} if the user is privileged and {@code false} otherwise.
     */
    public boolean isPrivileged() {
        return reportService.isPrivileged(session.getUser(), report);
    }

    /**
     * Checks if the user is privileged for the post.
     *
     * @param post The post in question.
     * @return {@code true} iff the user is privileged.
     */
    public boolean privilegedForPost(final Post post) {
        return postService.isPrivileged(session.getUser(), post);
    }

    /**
     * Checks if the user is a moderator of the topic the report is located in.
     *
     * @return {@code true} if the user is a moderator and {@code false} otherwise.
     */
    public boolean isModerator() {
        return false;
    }

    /**
     * Checks if the user is banned from the topic the report is located in.
     *
     * @return {@code true} if the user is banned and {@code false} otherwise.
     */
    public boolean isBanned() {
        return false;
    }

    /**
     * @return The report.
     */
    public Report getReport() {
        return report;
    }

    /**
     * @return Whether the relevance was overwritten.
     */
    public boolean getOverwriteRelevance() {
        if (report != null) {
            return report.getRelevanceOverwritten();
        }
        return false;
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
     *
     * @return Whether the user has upvoted this report.
     */
    public boolean getHasUpvoted() {
        return hasUpvoted;
    }

    /**
     * @return Whether the user has downvoted this report.
     */
    public boolean getHasDownvoted() {
        return hasDownvoted;
    }

    /**
     * @param report The report to set.
     */
    public void setReport(final Report report) {
        this.report = report;
    }

    /**
     * Returns the ID of the report this report is a duplicate of.
     *
     * @return The duplicateOfID.
     */
    public Integer getDuplicateOfID() {
        return duplicateOfID;
    }

    /**
     * Sets the ID of the report this report is a duplicate of.
     *
     * @param duplicateOfID The duplicateOfID to set.
     */
    public void setDuplicateOfID(final Integer duplicateOfID) {
        this.duplicateOfID = duplicateOfID;
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
    public ReportPageDialog getCurrentDialog() {
        return currentDialog;
    }

}
