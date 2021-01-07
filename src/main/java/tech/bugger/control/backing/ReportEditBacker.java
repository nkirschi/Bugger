package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Registry;
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
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ResourceBundle;

/**
 * Backing Bean for the report edit page.
 */
@ViewScoped
@Named
public class ReportEditBacker implements Serializable {

    private static final Log log = Log.forClass(ReportEditBacker.class);
    @Serial
    private static final long serialVersionUID = -1310546265441099227L;

    private int reportID;
    private Report report;
    private Topic topic;
    private boolean displayConfirmDialog;

    private boolean priviledged;

    /**
     * The topic service giving access to topics.
     */
    private transient TopicService topicService;

    /**
     * The report service creating reports.
     */
    private transient ReportService reportService;

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
     * Constructs a new report editing page backing bean with the necessary dependencies.
     *
     * @param topicService        The topic service to use.
     * @param reportService       The report service to use.
     * @param session             The current user session.
     * @param ectx                The current {@link ExternalContext} of the application.
     * @param feedbackEvent       The feedback event to use for user feedback.
     * @param registry            The dependency registry to use.
     */
    @Inject
    public ReportEditBacker(final TopicService topicService, final ReportService reportService,
                            final UserSession session, final ExternalContext ectx,
                            final Event<Feedback> feedbackEvent, final Registry registry) {
        this.topicService = topicService;
        this.reportService = reportService;
        this.session = session;
        this.ectx = ectx;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = registry.getBundle("messages", session);
        this.priviledged = false;
    }

    /**
     * Initializes the report edit page. Loads the report to be edited and checks if the user is allowed to edit the
     * report. If this is not the case, acts as if the page did not exist.
     */
    public void init() {
        report = reportService.getReportByID(reportID);
        if (report != null) {
            Lazy<Topic> topic = new Lazy<>(new Topic(1, "", ""));// TODO: Use report.getTopic();
            User user = new User();// TODO: Use session.getUser();
            user.setAdministrator(true);// TODO: Remove
            report.setTopic(topic);// TODO: Remove

            if (topic != null) {
                this.topic = new Topic(1, "", "");// TODO: Clone report.getTopic()?
            }

            priviledged = user != null
                    && topic != null
                    && (user.equals(report.getAuthorship().getCreator())
                        || user.isAdministrator()
                        || topicService.isModerator(user, topic.get()));
        }

        if (!priviledged) {
            // TODO: What means acting "as if the page did not exist"?
            try {
                ectx.redirect(ectx.getRequestContextPath() + "/some/404/page.xhtml");
            } catch (IOException e) {
            }
            return;
        }
    }

    /**
     * Opens the confirmation dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openConfirmDialog() {
        displayConfirmDialog = true;
        return null;
    }

    /**
     * Closes the confirmation dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String closeConfirmDialog() {
        displayConfirmDialog = false;
        return null;
    }

    /**
     * Saves the changes made into the database or opens the confirmation dialog if the report's topic changed.
     *
     * @return The page to navigate to.
     */
    public String saveChangesWithConfirm() {
        if (report.getTopic().get().equals(topic)) {
            return saveChanges();
        } else {
            return openConfirmDialog();
        }
    }

    /**
     * Saves the changes made into the database.
     *
     * @return The page to navigate to.
     */
    public String saveChanges() {
        if (reportService.updateReport(report)) {
            return "report.xhtml?r=" + report.getId();
        } else {
            return null;
        }
    }

    /**
     * Returns whether to display a warning that the report will be moved to a topic that the user does not moderate.
     *
     * @return Whether to display the warning.
     */
    public boolean isDisplayNoModerationWarning() {
        User user = session.getUser();
        Topic oldTopic = report.getTopic().get();
        return user != null
                && !oldTopic.equals(topic)
                && topicService.isModerator(user, oldTopic)
                && !topicService.isModerator(user, topic);
    }

    /**
     * Signals whether the user is allowed to edit the report. Only administrators, moderators of the topic the report
     * belongs to and the creator (provided they are not banned) may edit the report.
     *
     * @return {@code true} if the user may edit the report and {@code false} otherwise.
     */
    public boolean isPrivileged() {
        return priviledged;
    }

    /**
     * Returns the report to edit.
     *
     * @return The report to edit.
     */
    public Report getReport() {
        return report;
    }

    /**
     * Sets the report to edit.
     *
     * @param report The report to edit.
     */
    public void setReport(Report report) {
        this.report = report;
    }

    /**
     * Returns the destination topic, that is, the topic the report is supposed to be moved to.
     * @return The
     */
    public Topic getTopic() {
        return topic;
    }

    /**
     * Sets the topic the report is to be moved to.
     *
     * @param topic The topic to move the report to.
     */
    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    /**
     * Returns the ID of the report to edit.
     *
     * @return The ID of the report to edit.
     */
    public int getReportID() {
        return reportID;
    }

    /**
     * Sets the ID of the report to edit.
     * @param reportID The ID of the report to edit.
     */
    public void setReportID(int reportID) {
        this.reportID = reportID;
    }

    /**
     * Returns whether the confirmation dialog is to be shown.
     *
     * @return Whether the confirmation dialog is to be shown.
     */
    public boolean isDisplayConfirmDialog() {
        return displayConfirmDialog;
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
