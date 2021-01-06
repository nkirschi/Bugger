package tech.bugger.control.backing;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.global.util.Log;

import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Backing Bean for the report create page.
 */
@ViewScoped
@Named
public class ReportCreateBacker implements Serializable {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(ReportCreateBacker.class);

    @Serial
    private static final long serialVersionUID = 6375834226080077144L;

    /**
     * The ID of the topic to create the report in.
     */
    private int topicID;

    /**
     * The report to create.
     */
    private Report report;

    /**
     * The first post of the report to create.
     */
    private Post firstPost;

    /**
     * The attachment that was just uploaded.
     */
    private Part uploadedAttachment;

    /**
     * The list of attachments of the first post.
     */
    private List<Attachment> attachments;

    /**
     * Whether the user is banned from the topic to create the report in.
     */
    private boolean banned;

    /**
     * The current application settings.
     */
    private ApplicationSettings applicationSettings;

    /**
     * The report service creating reports.
     */
    private transient ReportService reportService;

    /**
     * The topic service giving access to topics.
     */
    private transient TopicService topicService;

    /**
     * The current user session.
     */
    private UserSession session;

    /**
     * The current {@link ExternalContext}.
     */
    private ExternalContext ectx;

    /**
     * Feedback event for user feedback.
     */
    private transient Event<Feedback> feedbackEvent;

    /**
     * Resource bundle for feedback message.
     */
    private transient ResourceBundle messagesBundle;

    /**
     * Constructs a new report creation page backing bean with the necessary dependencies.
     *
     * @param applicationSettings The current application settings.
     * @param reportService       The report service to use.
     * @param topicService        The topic service to use.
     * @param session             The current user session.
     * @param ectx                The current {@link ExternalContext} of the application.
     * @param feedbackEvent       The feedback event to use for user feedback.
     * @param messagesBundle      The resource bundle for feedback messages.
     */
    @Inject
    public ReportCreateBacker(final ApplicationSettings applicationSettings, final ReportService reportService,
                              final TopicService topicService, final UserSession session, final ExternalContext ectx,
                              final Event<Feedback> feedbackEvent/*,
                              @RegistryKey("messages") final ResourceBundle messagesBundle*/) {
        this.applicationSettings = applicationSettings;
        this.reportService = reportService;
        this.topicService = topicService;
        this.session = session;
        this.ectx = ectx;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = ResourceBundle.getBundle("tech.bugger.i18n.messages");
        /*this.messagesBundle = messagesBundle;*/
        this.banned = true;
    }

    /**
     * Initializes the report create page. Checks if the user is allowed to create reports in the first place, i.e. is
     * not banned from the topic the report is located in. If the user may not create reports, acts as if the page did
     * not exist.
     */
    public void init() {
        User user = session.getUser();
        Topic topic = topicService.getTopicByID(topicID);
        if (user == null || topic == null || topicService.isBanned(user, topic)) {
            // TODO: What means acting "as if the page did not exist"?
            try {
                ectx.redirect(ectx.getRequestContextPath() + "/some/404/page.xhtml");
            } catch (IOException e) {
            }
            return;
        }

        banned = false;
        Authorship authorship = new Authorship(session.getUser(), null, session.getUser(), null);
        report = new Report(0, "", Report.Type.BUG, Report.Severity.MINOR, "", authorship, null, null, null, null);
        report.setTopic(new Lazy<>(topic));
        attachments = new ArrayList<>();
        firstPost = new Post(0, "", new Lazy<>(report), authorship, attachments);
    }

    /**
     * Saves the new report and its first post to the database.
     *
     * On success, the user is redirected to the report page of the newly created report.
     *
     * @return The site to redirect to.
     */
    public String create() {
        if (reportService.createReport(report, firstPost)) {
            return "report.xhtml?r=" + report.getId();
        } else {
            return null;
        }
    }

    /**
     * Converts the uploaded attachment from a {@code Part} to a {@code byte[]}. The attachment is then associated with
     * the post.
     */
    public void saveAttachment() {
        if (uploadedAttachment == null) { // user didn't enter attachment
            return;
        }

        int maxAttachments = applicationSettings.getConfiguration().getMaxAttachmentsPerPost();
        if (attachments.size() >= maxAttachments) {
            log.info("Trying to create first post with too many attachments");
            String message = MessageFormat.format(messagesBundle.getString("too_many_attachments"), maxAttachments);
            feedbackEvent.fire(new Feedback(message, Feedback.Type.ERROR));
            return;
        }

        String name = uploadedAttachment.getSubmittedFileName();
        if (attachments.stream().map(Attachment::getName).anyMatch(a -> a.equals(name))) {
            log.info("Trying to create post where attachment names are not unique.");
            String message = MessageFormat.format(messagesBundle.getString("attachment_names_not_unique"),
                    maxAttachments);
            feedbackEvent.fire(new Feedback(message, Feedback.Type.ERROR));
            return;
        }

        byte[] content;
        try {
            content = uploadedAttachment.getInputStream().readAllBytes();
        } catch (IOException e) {
            log.info("Uploaded attachment could not be read", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("attachment_invalid"), Feedback.Type.ERROR));
            return;
        }

        Attachment attachment = new Attachment();
        attachment.setName(uploadedAttachment.getSubmittedFileName());
        attachment.setContent(new Lazy<>(content));
        attachment.setMimetype(uploadedAttachment.getContentType());
        attachment.setPost(new Lazy<>(firstPost));
        attachments.add(attachment);
        log.debug("Attachment '" + uploadedAttachment.getSubmittedFileName() + "' uploaded.");
    }

    /**
     * Deletes all attachments of the post irreversibly.
     */
    public void deleteAllAttachments() {
        attachments.clear();
    }

    /**
     * Checks if the user is banned from the topic they want to create the report in.
     *
     * @return {@code true} iff the user is banned.
     */
    public boolean isBanned() {
        return banned;
    }

    /**
     * Returns the ID of the topic the report is to be created in.
     *
     * @return The ID of the topic the report is to be created in.
     */
    public int getTopicID() {
        return topicID;
    }

    /**
     * Sets the ID of the topic the report is to be created in.
     *
     * @param topicID The ID of the topic the report is to be created in.
     */
    public void setTopicID(final int topicID) {
        this.topicID = topicID;
    }

    /**
     * Returns the report to create.
     *
     * @return The report to create.
     */
    public Report getReport() {
        return report;
    }

    /**
     * Sets the report to create.
     *
     * @param report The report to create..
     */
    public void setReport(final Report report) {
        this.report = report;
    }

    /**
     * Returns the first post of the report to be created.
     *
     * @return The first post of the report to be created.
     */
    public Post getFirstPost() {
        return firstPost;
    }

    /**
     * Sets the first post of the report to be created.
     *
     * @param firstPost The first post of the report to be created.
     */
    public void setFirstPost(final Post firstPost) {
        this.firstPost = firstPost;
    }

    /**
     * Returns the uploaded attachment.
     *
     * @return The uploaded attachment.
     */
    public Part getUploadedAttachment() {
        return uploadedAttachment;
    }

    /**
     * Sets the uploaded attachment.
     *
     * @param uploadedAttachment The uploaded attachment.
     */
    public void setUploadedAttachment(final Part uploadedAttachment) {
        this.uploadedAttachment = uploadedAttachment;
    }

    /**
     * Returns the list of attachments of the first post.
     *
     * @return The list of attachments.
     */
    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * Set the list of attachments of the first post.
     *
     * @param attachments The list of attachments.
     */
    public void setAttachments(final List<Attachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * Returns the current user session.
     *
     * @return The current user session.
     */
    public UserSession getSession() {
        return session;
    }

    /**
     * Sets the user session.
     *
     * @param session The user session.
     */
    public void setSession(final UserSession session) {
        this.session = session;
    }

    /**
     * Returns the list of available report types.
     *
     * @return The list of available report types.
     */
    public Report.Type[] getReportTypes() {
        return Report.Type.values();
    }

    /**
     * Returns the list of available report severities.
     *
     * @return The list of available report severities.
     */
    public Report.Severity[] getReportSeverities() {
        return Report.Severity.values();
    }

}
