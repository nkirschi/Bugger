package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.PostService;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;

/**
 * Backing bean for the report page.
 */
@ViewScoped
@Named
public class ReportBacker implements Serializable {

    private static final Log log = Log.forClass(ReportBacker.class);
    @Serial
    private static final long serialVersionUID = 7260516443406682026L;

    private int reportID;
    private Report report;
    private Paginator<Post> posts;
    private int duplicateOfID;
    private Integer overwritingRelevance;
    private Post postToBeDeleted;

    private boolean displayDeletePostDialog;
    private boolean displayDeleteReportDialog;
    private boolean displayOpenCloseDialog;
    private boolean displayDuplicateDialog;

    @Inject
    private transient ReportService reportService;

    @Inject
    private transient PostService postService;

    @Inject
    private transient TopicService topicService;

    @Inject
    private transient UserSession session;

    @Inject
    private transient FacesContext fctx;

    /**
     * Initializes the report page. Loads the first few posts of the report to display them. Also checks if the user is
     * allowed to view the report. If not, acts as if the page did not exist.
     */
    @PostConstruct
    private void init() {

    }

    /**
     * Creates a FacesMessage to display if an event is fired in one of the injected services.
     *
     * @param feedback The feedback with details on what to display.
     */
    public void displayFeedback(@Observes @Any Feedback feedback) {

    }

    /**
     * Opens the delete post dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openDeletePostDialog() {
        return null;
    }

    /**
     * Closes the delete post dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String closeDeletePostDialog() {
        return null;
    }

    /**
     * Opens the delete report dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openDeleteReportDialog() {
        return null;
    }

    /**
     * Closes the delete report dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String closeDeleteReportDialog() {
        return null;
    }

    /**
     * Opens the dialog for opening or closing the report.
     *
     * @return {@code null} to reload the page.
     */
    public String openOpenCloseDialog() {
        return null;
    }

    /**
     * Closes the dialog for opening or closing the report.
     *
     * @return {@code null} to reload the page.
     */
    public String closeOpenCloseDialog() {
        return null;
    }

    /**
     * Opens the dialog for marking the report as a duplicate of another report.
     *
     * @return {@code null} to reload the page.
     */
    public String openDuplicateDialog() {
        return null;
    }

    /**
     * Closes the dialog for marking the report as a duplicate of another report.
     *
     * @return {@code null} to reload the page.
     */
    public String closeDuplicateDialog() {
        return null;
    }

    /**
     * Returns the relevance of the report as saved in the data source.
     *
     * @return the relevance of the report.
     */
    public int getRelevance() {
        return 0;
    }

    /**
     * Adds or removes a subscription to the report for the user, whichever is applicable.
     */
    public void toggleReportSubscription() {
    }

    /**
     * Increases the relevance of the report by the user's voting weight.
     */
    public void upvote() {

    }

    /**
     * Decreases the relevance of the report by the user's voting weight.
     */
    public void downvote() {

    }

    /**
     * Returns if the user has voted to increase the relevance of the report.
     *
     * @return {@code true} if the user has voted up and {@code false} otherwise.
     */
    public boolean hasUpvoted() {
        return false;
    }

    /**
     * Returns if the user has voted to decrease the relevance of the report.
     *
     * @return {@code true} if the user has voted down and {@code false} otherwise.
     */
    public boolean hasDownvoted() {
        return false;
    }

    /**
     * Opens a closed report and closes an open one.
     */
    public void toggleOpenClosed() {
    }

    /**
     * Deletes the report along with all its posts irreversibly.
     */
    public void delete() {

    }

    /**
     * Marks the report as a duplicate of another report. This automatically closes the report.
     */
    public void markDuplicate() {

    }

    /**
     * Removes the marking signifying that the report is a duplicate of another one.
     */
    public void unmarkDuplicate() {

    }

    /**
     * Overwrites the relevance of the report with a set value.
     */
    public void overwriteRelevance() {

    }

    /**
     * Deletes the selected post irreversibly. If it is the first post, this deletes the whole report.
     */
    public void deletePost() {

    }

    /**
     * Checks if the user is privileged, i.e. an admin, a mod or the creator of the report.
     *
     * @return {@code true} if the user is privileged and {@code false} otherwise.
     */
    public boolean isPrivileged() {
        return false;
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
     * @param report The report to set.
     */
    public void setReport(Report report) {
        this.report = report;
    }

    /**
     * @return The duplicateOfID.
     */
    public int getDuplicateOfID() {
        return duplicateOfID;
    }

    /**
     * @param duplicateOfID The duplicateOfID to set.
     */
    public void setDuplicateOfID(int duplicateOfID) {
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
    public void setPostToBeDeleted(Post postToBeDeleted) {
        this.postToBeDeleted = postToBeDeleted;
    }

    /**
     * @return The posts.
     */
    public Paginator<Post> getPosts() {
        return posts;
    }

    /**
     * @return The displayDeletePostDialog.
     */
    public boolean isDisplayDeletePostDialog() {
        return displayDeletePostDialog;
    }

    /**
     * @return The displayDeleteReportDialog.
     */
    public boolean isDisplayDeleteReportDialog() {
        return displayDeleteReportDialog;
    }

    /**
     * @return The displayOpenCloseDialog.
     */
    public boolean isDisplayOpenCloseDialog() {
        return displayOpenCloseDialog;
    }

    /**
     * @return The displayDuplicateDialog.
     */
    public boolean isDisplayDuplicateDialog() {
        return displayDuplicateDialog;
    }

    /**
     * @param overwritingRelevance The overwritingRelevance to set.
     */
    public void setOverwritingRelevance(Integer overwritingRelevance) {
        this.overwritingRelevance = overwritingRelevance;
    }

    /**
     * @return The overwritingRelevance.
     */
    public Integer getOverwritingRelevance() {
        return overwritingRelevance;
    }

    /**
     * @return The reportID.
     */
    public int getReportID() {
        return reportID;
    }

    /**
     * @param reportID The reportID to set.
     */
    public void setReportID(int reportID) {
        this.reportID = reportID;
    }
}
