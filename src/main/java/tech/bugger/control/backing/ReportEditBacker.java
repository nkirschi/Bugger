package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.util.Log;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

/**
 * Backing Bean for the report edit page.
 */
@ViewScoped
@Named
public class ReportEditBacker implements Serializable {

    private static final Log log = Log.forClass(ReportEditBacker.class);
    private static final long serialVersionUID = -1310546265441099227L;

    private int reportID;
    private Report report;
    private Topic destination;
    private boolean displayMoveDialog;

    @Inject
    private transient ReportService reportService;

    @Inject
    private UserSession session;

    @Inject
    private FacesContext fctx;

    /**
     * Initializes the report edit page. Loads the report to be edited and checks if the user is allowed to edit the
     * report. If this is not the case, acts as if the page did not exist.
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
     * Opens the move dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openMoveDialog() {
        return null;
    }

    /**
     * Closes the move dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String closeMoveDialog() {
        return null;
    }

    /**
     * Saves the changes made into the database.
     */
    public void saveChanges() {

    }

    /**
     * Moves the report to a new topic.
     *
     * @return {@code null} to reload the page.
     */
    public String move() {
        return null;
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
     * Signals whether the user is allowed to edit the report. Only administrators, moderators of the topic the report
     * belongs to and the creator (provided they are not banned) may edit the report.
     *
     * @return {@code true} if the user may edit the report and {@code false} otherwise.
     */
    public boolean isPrivileged() {
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
     * @return The destination, that is, the topic the report is supposed to be moved to.
     */
    public Topic getDestination() {
        return destination;
    }

    /**
     * @param destination The topic to set as destination for moving the report.
     */
    public void setDestination(Topic destination) {
        this.destination = destination;
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
     * @return The displayMoveDialog.
     */
    public boolean isDisplayMoveDialog() {
        return displayMoveDialog;
    }
}
