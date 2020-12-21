package tech.bugger.control.backing;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.PostService;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.util.Log;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Backing bean for the post edit page.
 */
@ViewScoped
@Named
public class PostEditBacker implements Serializable {

    private static final Log log = Log.forClass(PostEditBacker.class);
    @Serial
    private static final long serialVersionUID = -973315118868047411L;

    private Integer postID;
    private int reportID;
    private Post post;
    private Part lastAttachmentUploaded;
    private List<Attachment> attachments;

    @Inject
    private transient PostService postService;

    @Inject
    private transient ReportService reportService;

    @Inject
    private transient TopicService topicService;

    @Inject
    private transient UserSession session;

    @Inject
    private transient ApplicationSettings applicationSettings;

    @Inject
    private transient FacesContext fctx;


    /**
     * Initializes the page for creating and editing posts. Checks if the user is allowed to view the page in the first
     * place. If not, acts as if the page did not exist.
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
     * Creates a new post or saves the changes made to an existing post.
     */
    public void saveChanges() {

    }

    /**
     * Converts the last uploaded attachment into a {@code byte[]} and puts it into the post. If the maximum number of
     * attachments has already been reached, displays an error message instead.
     */
    public void uploadAttachment() {

    }

    /**
     * Clears the list of uploaded attachments.
     */
    public void deleteAllAttachments() {

    }

    /**
     * @return The postID.
     */
    public Integer getPostID() {
        return postID;
    }

    /**
     * @param postID The postID to set.
     */
    public void setPostID(Integer postID) {
        this.postID = postID;
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

    /**
     * @return The post.
     */
    public Post getPost() {
        return post;
    }

    /**
     * @param post The post to set.
     */
    public void setPost(Post post) {
        this.post = post;
    }

    /**
     * @return The lastAttachmentUploaded.
     */
    public Part getLastAttachmentUploaded() {
        return lastAttachmentUploaded;
    }

    /**
     * @param lastAttachmentUploaded The lastAttachmentUploaded to set.
     */
    public void setLastAttachmentUploaded(Part lastAttachmentUploaded) {
        this.lastAttachmentUploaded = lastAttachmentUploaded;
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
