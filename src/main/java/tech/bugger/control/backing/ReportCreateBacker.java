package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.util.Log;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.Serializable;
import java.util.List;

/**
 * Backing Bean for the report create page.
 */
@ViewScoped
@Named
public class ReportCreateBacker implements Serializable {

    private static final Log log = Log.forClass(ReportCreateBacker.class);
    private static final long serialVersionUID = 6375834226080077144L;

    private int topicID;
    private Report report;
    private Post firstPost;
    private Part uploadedAttachment;
    private List<Attachment> attachments;

    @Inject
    private transient ReportService reportService;

    @Inject
    private transient TopicService topicService;

    @Inject
    private UserSession session;

    @Inject
    private FacesContext fctx;

    /**
     * Initializes the report create page. Checks if the user is allowed to create reports in the first place, i.e. is
     * not banned from the topic the report is located in. If the user may not create reports, acts as if the page did
     * not exist.
     */
    public void init() {

    }

    /**
     * Creates a FacesMessage to display if an event is fired in one of the injected services.
     *
     * @param feedback The feedback with details on what to display.
     */
    public void displayFeedback(@Observes @Any Feedback feedback) {

    }

    /**
     * Saves the new report and its first post to the database.
     */
    public void create() {

    }

    /**
     * Converts the uploaded attachment from a {@code Part} to a {@code byte[]}. The attachment is then associated with
     * the post.
     */
    public void saveAttachment() {

    }

    /**
     * Deletes all attachments of the post irreversibly.
     */
    public void deleteAllAttachments() {

    }

    /**
     * Checks if the user is banned from the topic they want to create the report in.
     *
     * @return {@code true} if the user is banned and {@code false} otherwise.
     */
    public boolean isBanned() {
        return false;
    }

    /**
     * @return The topicID.
     */
    public int getTopicID() {
        return topicID;
    }

    /**
     * @param topicID The topicID to set.
     */
    public void setTopicID(int topicID) {
        this.topicID = topicID;
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
     * @return The firstPost.
     */
    public Post getFirstPost() {
        return firstPost;
    }

    /**
     * @param firstPost The firstPost to set.
     */
    public void setFirstPost(Post firstPost) {
        this.firstPost = firstPost;
    }

    /**
     * @return The uploadedAttachment.
     */
    public Part getUploadedAttachment() {
        return uploadedAttachment;
    }

    /**
     * @param uploadedAttachment The uploadedAttachment to set.
     */
    public void setUploadedAttachment(Part uploadedAttachment) {
        this.uploadedAttachment = uploadedAttachment;
    }

    /**
     * @return The attachments.
     */
    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * @param attachments The attachments to set.
     */
    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
