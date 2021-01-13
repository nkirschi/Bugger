package tech.bugger.control.backing;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.PostService;
import tech.bugger.business.service.ReportService;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean for the post edit page.
 */
@ViewScoped
@Named
public class PostEditBacker implements Serializable {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(PostEditBacker.class);

    @Serial
    private static final long serialVersionUID = -973315118868047411L;

    /**
     * Whether to create a new post or edit an existing one.
     */
    private boolean create;

    /**
     * The ID of the post to edit.
     */
    private Integer postID;

    /**
     * The ID of the report to create a new post in.
     */
    private Integer reportID;

    /**
     * The post to edit.
     */
    private Post post;

    /**
     * The report of the post.
     */
    private Report report;

    /**
     * The attachment that was last uploaded.
     */
    private Part lastAttachmentUploaded;

    /**
     * The list of attachments of the post to edit.
     */
    private List<Attachment> attachments;

    /**
     * The current application settings.
     */
    private final ApplicationSettings applicationSettings;

    /**
     * The report service creating reports.
     */
    private final ReportService reportService;

    /**
     * The post service validating posts and attachments.
     */
    private final PostService postService;

    /**
     * The current user session.
     */
    private final UserSession session;

    /**
     * The current {@link FacesContext}.
     */
    private final FacesContext fctx;

    /**
     * Constructs a new post editing backing bean with the necessary dependencies.
     *
     * @param applicationSettings The current application settings.
     * @param reportService       The report service to use.
     * @param postService         The post service to use.
     * @param session             The current user session.
     * @param fctx                The current {@link FacesContext} of the application.
     */
    @Inject
    public PostEditBacker(final ApplicationSettings applicationSettings, final ReportService reportService,
                          final PostService postService, final UserSession session, final FacesContext fctx) {
        this.applicationSettings = applicationSettings;
        this.reportService = reportService;
        this.postService = postService;
        this.session = session;
        this.fctx = fctx;
        attachments = new ArrayList<>();
    }

    /**
     * Initializes the page for creating and editing posts. Checks if the user is allowed to view the page in the first
     * place. If not, acts as if the page did not exist.
     */
    @PostConstruct
    void init() {
        User user = session.getUser();
        if (user == null) {
            redirectToErrorPage();
            return;
        }

        create = fctx.getExternalContext().getRequestParameterMap().containsKey("c");
        if (create) {
            reportID = parseRequestParameter("r");
            if (reportID == null) {
                log.debug("ID of report to create post in is null.");
                redirectToErrorPage();
                return;
            }
            report = reportService.getReportByID(reportID);
            if (report == null || !reportService.canPostInReport(user, report)) {
                log.debug("Report to create post in is null or forbidden.");
                redirectToErrorPage();
                return;
            }
            Authorship authorship = new Authorship(user, null, user, null);
            post = new Post(0, "", new Lazy<>(report), authorship, attachments);
        } else {
            postID = parseRequestParameter("p");
            if (postID == null) {
                log.debug("ID of post to edit is null.");
                redirectToErrorPage();
                return;
            }
            post = postService.getPostByID(postID);
            if (post == null || !postService.canModify(user, post)) {
                log.debug("Post to edit is null or forbidden: post=" + post);
                redirectToErrorPage();
                return;
            }
            report = reportService.getReportByID(post.getReport().get().getId());
            if (report == null) {
                log.debug("Report to edit post in is null or forbidden.");
                redirectToErrorPage();
                return;
            }
            attachments = post.getAttachments();
            post.getAuthorship().setModifier(user);
        }

        log.debug("Init done, create=" + create + ", postID=" + postID + "reportID=" + reportID);
    }

    /**
     * Creates a new post or saves the changes made to an existing post. On success, the user is redirected to the post
     * in its report page.
     */
    public void saveChanges() {
        boolean success = create ? postService.createPost(post) : postService.updatePost(post);
        if (success) {
            ExternalContext ectx = fctx.getExternalContext();
            try {
                ectx.redirect(ectx.getRequestContextPath()
                        + "/faces/view/authorized/report.xhtml?id=" + report.getId() + "&p=" + post.getId());

            } catch (IOException e) {
                redirectToErrorPage();
            }
        }
    }

    /**
     * Converts the last uploaded attachment into a {@code byte[]} and puts it into the post. If the maximum number of
     * attachments has already been reached, displays an error message instead.
     */
    public void uploadAttachment() {
        postService.addAttachment(post, lastAttachmentUploaded);
    }

    /**
     * Clears the list of attachments of the post.
     */
    public void deleteAllAttachments() {
        post.getAttachments().clear();
    }

    /**
     * Tries to parse a request parameter to an integer.
     *
     * @param param The name of the parameter to parse.
     * @return The integer value of the request parameter, {@code null} if the parameter could not be parsed.
     */
    private Integer parseRequestParameter(final String param) {
        ExternalContext ectx = fctx.getExternalContext();
        try {
            return Integer.parseInt(ectx.getRequestParameterMap().get(param));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Redirects the user to the error page.
     */
    private void redirectToErrorPage() {
        fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
    }

    /**
     * Returns the ID of the post to edit.
     *
     * @return The post ID to set.
     */
    public Integer getPostID() {
        return postID;
    }

    /**
     * Sets the ID of the post to edit.
     *
     * @param postID The post ID to set.
     */
    public void setPostID(final Integer postID) {
        this.postID = postID;
    }

    /**
     * Returns the ID of the report to create the new post in.
     *
     * @return The ID of the report to create the new post in.
     */
    public Integer getReportID() {
        return reportID;
    }

    /**
     * Sets the ID of the report to create the new post in.
     *
     * @param reportID The report ID to set.
     */
    public void setReportID(final Integer reportID) {
        this.reportID = reportID;
    }

    /**
     * The post to edit.
     *
     * @return The post to edit.
     */
    public Post getPost() {
        return post;
    }

    /**
     * Sets the post to edit.
     *
     * @param post The post ID to set.
     */
    public void setPost(final Post post) {
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
    public void setLastAttachmentUploaded(final Part lastAttachmentUploaded) {
        this.lastAttachmentUploaded = lastAttachmentUploaded;
    }

    /**
     * Returns the list of attachments of the post.
     *
     * @return The list of attachments of the post.
     */
    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * Sets the list of attachments of the post.
     *
     * @param attachments The list of attachments to set.
     */
    public void setAttachments(final List<Attachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * Returns whether to create a new post or edit an existing one.
     *
     * @return Whether to create or edit a post.
     */
    public boolean isCreate() {
        return create;
    }

    /**
     * Sets whether to create a new post or edit an existing one.
     *
     * @param create Whether to create or edit a post.
     */
    public void setCreate(final boolean create) {
        this.create = create;
    }

}
