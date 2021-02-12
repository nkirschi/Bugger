package tech.bugger.control.backing;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Registry;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;

/**
 * Backing Bean for the report edit page.
 */
@ViewScoped
@Named
public class ReportEditBacker implements Serializable {

    @Serial
    private static final long serialVersionUID = -1310546265441099227L;

    /**
     * All possible {@link Report.Type}s.
     */
    public static final Report.Type[] REPORT_TYPES = Report.Type.values();

    /**
     * All possible {@link Report.Severity}s.
     */
    public static final Report.Severity[] REPORT_SEVERITIES = Report.Severity.values();

    /**
     * The current application settings.
     */
    private final ApplicationSettings applicationSettings;

    /**
     * The topic service giving access to topics.
     */
    private final TopicService topicService;

    /**
     * The report service creating reports.
     */
    private final ReportService reportService;

    /**
     * The current user session.
     */
    private final UserSession session;

    /**
     * The current {@link FacesContext}.
     */
    private final FacesContext fctx;

    /**
     * The current {@link ExternalContext}.
     */
    private final ExternalContext ectx;

    /**
     * {@link Registry} for dynamically retrieving resource bundles.
     */
    private final Registry registry;

    /**
     * The ID of the report to edit.
     */
    private int reportID;

    /**
     * The report to edit.
     */
    private Report report;

    /**
     * The current topic of the report to edit.
     */
    private Topic currentTopic;

    /**
     * The name of the topic to move the report to.
     */
    private String destination;

    /**
     * Whether to display the confirmation dialog.
     */
    private boolean displayConfirmDialog;

    /**
     * The available topics.
     */
    private List<Topic> topics;

    /**
     * The available topic titles cached.
     */
    private List<String> topicTitles;

    /**
     * Constructs a new report editing page backing bean with the necessary dependencies.
     *
     * @param applicationSettings The current application settings.
     * @param topicService        The topic service to use.
     * @param reportService       The report service to use.
     * @param session             The current user session.
     * @param registry            The dependency registry to use.
     * @param fctx                The current {@link FacesContext} of the application.
     * @param ectx                The current {@link ExternalContext} of the application.
     */
    @Inject
    public ReportEditBacker(final ApplicationSettings applicationSettings,
                            final TopicService topicService,
                            final ReportService reportService,
                            final UserSession session,
                            final Registry registry,
                            final FacesContext fctx,
                            final ExternalContext ectx) {
        this.applicationSettings = applicationSettings;
        this.topicService = topicService;
        this.reportService = reportService;
        this.session = session;
        this.registry = registry;
        this.fctx = fctx;
        this.ectx = ectx;
    }

    /**
     * Initializes the report edit page. Loads the report to be edited and checks if the user is allowed to edit the
     * report. If this is not the case, acts as if the page did not exist.
     */
    @PostConstruct
    void init() {
        ResourceBundle messagesBundle = registry.getBundle("messages", session.getLocale());

        try {
            reportID = Integer.parseInt(ectx.getRequestParameterMap().get("id"));
        } catch (NumberFormatException e) {
            // Report ID parameter not given or invalid.
            throw new Error404Exception();
        }

        User user = session.getUser();

        report = reportService.getReportByID(reportID);
        if (report == null) {
            throw new Error404Exception();
        } else {
            destination = report.getTopic();
            currentTopic = topicService.getTopicByID(report.getTopicID());
            topics = topicService.discoverTopics();
            topicTitles = topics.stream().map(Topic::getTitle).collect(Collectors.toList());
        }

        boolean closedReportPosting = applicationSettings.getConfiguration().isClosedReportPosting();
        if (!isPrivileged() || (report.getClosingDate() != null && !closedReportPosting)) {
            throw new Error404Exception();
        }

        report.getAuthorship().setModifier(user);
    }

    /**
     * Opens the confirmation dialog.
     */
    public void openConfirmDialog() {
        displayConfirmDialog = true;
    }

    /**
     * Closes the confirmation dialog.
     */
    public void closeConfirmDialog() {
        displayConfirmDialog = false;
    }

    /**
     * Saves the changes made into the database or opens the confirmation dialog if the report's topic changed.
     */
    public void saveChangesWithConfirm() {
        if (report.getTopic().equals(destination)) {
            saveChanges();
            return;
        }

        if (canMoveToTopic()) {
            openConfirmDialog();
        }
    }

    /**
     * Saves the changes made into the database.
     */
    public void saveChanges() {
        boolean success = true;

        report.getAuthorship().setModifiedDate(OffsetDateTime.now());
        report.getAuthorship().setModifier(session.getUser());
        if (!report.getTopic().equals(destination)) {
            Topic topic = getTopicByTitle(destination);
            if (topic != null) {
                report.setTopic(destination);
                report.setTopicID(topic.getId());
                success = reportService.move(report);

            }
        } else {
            success = reportService.updateReport(report);
        }

        if (success) {
            try {
                ectx.redirect(ectx.getRequestContextPath() + "/report?id=" + report.getId());
            } catch (IOException e) {
                throw new InternalError("Redirect failed.", e);
            }
        }
    }

    /**
     * Returns whether to display a warning that the report will be moved to a topic that the user does not moderate.
     *
     * @return Whether to display the warning.
     */
    public boolean isDisplayNoModerationWarning() {
        if (!currentTopic.getTitle().equals(destination)) {
            Topic destination = getTopicByTitle(getDestination());
            User user = session.getUser();
            return destination != null
                    && topicService.isModerator(user, currentTopic)
                    && !topicService.isModerator(user, destination);
        }
        return false;
    }

    /**
     * Checks whether the topic with name {@code destination} exists and the user is allowed to move the report to it.
     * Displays an error message if not.
     *
     * @return Whether the report can be moved to the destination topic.
     */
    private boolean canMoveToTopic() {
        ResourceBundle messagesBundle = registry.getBundle("messages", session.getLocale());
        Topic toTopic = getTopicByTitle(destination);
        User user = session.getUser();

        if (toTopic == null || topicService.isBanned(user, toTopic)) {
            String message = MessageFormat.format(messagesBundle.getString("report_edit_topic_not_found"),
                    destination);
            fctx.addMessage("f-report-edit:s-topic", new FacesMessage(message));
            destination = currentTopic.getTitle();
            return false;
        }
        return true;
    }

    /**
     * Signals whether the user is allowed to edit the report. Only administrators, moderators of the topic the report
     * belongs to and the creator (provided they are not banned) may edit the report.
     *
     * @return {@code true} if the user may edit the report and {@code false} otherwise.
     */
    public boolean isPrivileged() {
        User user = session.getUser();
        if (currentTopic == null) {
            return false;
        }
        if (user.isAdministrator() || topicService.isModerator(user, currentTopic)) {
            return true;
        }
        return user.equals(report.getAuthorship().getCreator()) && !topicService.isBanned(user, currentTopic);
    }

    /**
     * Retrieves a topic by title from the cache.
     *
     * @param title The title to search for.
     * @return The topic corresponding to the title or {@code null} if there is none.
     */
    private Topic getTopicByTitle(final String title) {
        return topics.stream().filter(t -> t.getTitle().equals(title)).findFirst().orElse(null);
    }

    /**
     * Retrieves the titles of all topics in the system.
     *
     * @return A list of all topic titles.
     */
    public List<String> getTopicTitles() {
        return topicTitles;
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
    public void setReport(final Report report) {
        this.report = report;
    }

    /**
     * Returns the current topic of the report to edit.
     *
     * @return The current topic.
     */
    public Topic getCurrentTopic() {
        return currentTopic;
    }

    /**
     * Sets the current topic of the report to edit.
     *
     * @param currentTopic The current topic.
     */
    public void setCurrentTopic(final Topic currentTopic) {
        this.currentTopic = currentTopic;
    }

    /**
     * Returns the name of the topic the report is supposed to be moved to.
     *
     * @return The name of the destination topic.
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets the ID of the topic the report is to be moved to.
     *
     * @param destination The name of the topic to move the report to.
     */
    public void setDestination(final String destination) {
        this.destination = destination;
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
     *
     * @param reportID The ID of the report to edit.
     */
    public void setReportID(final int reportID) {
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
        return REPORT_TYPES;
    }

    /**
     * Returns the list of available report severities.
     *
     * @return The list of available report severities.
     */
    public Report.Severity[] getReportSeverities() {
        return REPORT_SEVERITIES;
    }

}
