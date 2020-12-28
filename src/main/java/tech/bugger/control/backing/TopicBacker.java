package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.SearchService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
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
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Backing bean for the topic page.
 */
@ViewScoped
@Named
public class TopicBacker implements Serializable {

    private static final Log log = Log.forClass(TopicBacker.class);
    @Serial
    private static final long serialVersionUID = 6893463272223847178L;

    private int topicID;
    private Topic topic;
    private String userToBeBanned;
    private String userToBeModded;
    private List<User> userBanSuggestions;
    private List<User> userModSuggestions;
    private Paginator<Report> reports;
    private Paginator<User> moderators;
    private Paginator<User> bannedUsers;
    private boolean openReportShown; // default: true
    private boolean closedReportShown; // default: false

    private boolean displayBanDialog;
    private boolean displayUnbanDialog;
    private boolean displayModDialog;
    private boolean displayUnmodDialog;
    private boolean displayDeleteDialog;

    @Inject
    private UserSession session;

    @Inject
    private transient TopicService topicService;

    @Inject
    private transient ReportService reportService;

    @Inject
    private transient SearchService searchService;

    @Inject
    private FacesContext fctx;

    /**
     * Initializes the topic page. By default, only open reports are shown. Also checks if the user is allowed to view
     * the page. If not, acts as if the page did not exist.
     */
    @PostConstruct
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
     * Returns the relevance of a certain report.
     *
     * @param report The report in question.
     * @return The relevance.
     */
    public int getRelevance(Report report) {
        return 0;
    }

    /**
     * Enables suggestions for users to be banned.
     *
     * @return {@code null} to stay on the same page.
     */
    public String searchBanUsers() {
        return null;
    }

    /**
     * Enables suggestions for users to be made moderators.
     *
     * @return {@code null} to stay on the same page.
     */
    public String searchModUsers() {
        return null;
    }

    /**
     * Opens the ban dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openBanDialog() {
        return null;
    }

    /**
     * Closes the ban dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String closeBanDialog() {
        return null;
    }

    /**
     * Opens the unban dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openUnbanDialog() {
        return null;
    }

    /**
     * Closes the unban dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String closeUnbanDialog() {
        return null;
    }

    /**
     * Opens the dialog for promoting a user to a moderator of the topic.
     *
     * @return {@code null} to reload the page.
     */
    public String openModDialog() {
        return null;
    }

    /**
     * Closes the dialog for promoting a user to a moderator of the topic.
     *
     * @return {@code null} to reload the page.
     */
    public String closeModDialog() {
        return null;
    }

    /**
     * Opens the dialog for demoting a moderator of the topic.
     *
     * @return {@code null} to reload the page.
     */
    public String openUnmodDialog() {
        return null;
    }

    /**
     * Closes the dialog for demoting a moderator of the topic.
     *
     * @return {@code null} to reload the page.
     */
    public String closeUnmodDialog() {
        return null;
    }

    /**
     * Opens the delete topic dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openDeleteDialog() {
        return null;
    }

    /**
     * Opens the delete topic dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String closeDeleteDialog() {
        return null;
    }

    /**
     * Checks if the user is a moderator of the topic.
     *
     * @return {@code true} if the user is a moderator, {@code false} otherwise.
     */
    public boolean isModerator() {
        return false;
    }

    /**
     * Checks if the user is banned from the topic.
     *
     * @return {@code true} if the user is banned, {@code false} otherwise.
     */
    public boolean isBanned() {
        return false;
    }

    /**
     * Irreversibly deletes the topic.
     */
    public void delete() {
    }

    /**
     * Applies the selected filters for reports and refreshes them.
     */
    public void updateReportFiltering() {
    }

    /**
     * Bans the user whose username is specified in the attribute {@code userToBeBanned}. Note that administrators and
     * moderators cannot be banned.
     */
    public void banUser() {
    }

    /**
     * Unbans the user specified in {@code unbanUser}.
     *
     * @param user The user to unban.
     */
    public void unbanUser(User user) {
    }

    /**
     * Makes the user whose username is specified in {@code userToBeModded} a moderator of the topic. This is not
     * possible if they already are a moderator.
     */
    public void makeModerator() {
    }

    /**
     * Removes the moderator status of the user specified in {@code unmodUser}. This is not possible if they are an
     * administrator.
     *
     * @param user The user to remove as moderator.
     */
    public void removeModerator(User user) {
    }

    /**
     * Subscribes the user to the topic or unsubscribes them, whichever is applicable.
     */
    public void toggleTopicSubscription() {
    }

    /**
     * Returns the time stamp of the last action in one particular report. Creating, editing and moving a report as well
     * as creating and editing posts count as actions.
     *
     * @param report The report in question.
     * @return The time stamp of the last action as a {@code ZonedDateTime}.
     */
    public ZonedDateTime lastChange(Report report) {
        return null;
    }

    /**
     * @return The topic.
     */
    public Topic getTopic() {
        return topic;
    }

    /**
     * @param topic The topic to set.
     */
    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    /**
     * @return The userToBeBanned.
     */
    public String getUserToBeBanned() {
        return userToBeBanned;
    }

    /**
     * @param userToBeBanned The userToBeBanned to set.
     */
    public void setUserToBeBanned(String userToBeBanned) {
        this.userToBeBanned = userToBeBanned;
    }

    /**
     * @return The userToBeModded.
     */
    public String getUserToBeModded() {
        return userToBeModded;
    }

    /**
     * @param userToBeModded The userToBeModded to set.
     */
    public void setUserToBeModded(String userToBeModded) {
        this.userToBeModded = userToBeModded;
    }

    /**
     * @return The reports.
     */
    public Paginator<Report> getReports() {
        return reports;
    }

    /**
     * @return The moderators.
     */
    public Paginator<User> getModerators() {
        return moderators;
    }

    /**
     * @return The bannedUsers.
     */
    public Paginator<User> getBannedUsers() {
        return bannedUsers;
    }

    /**
     * @return {@code true} if open reports are shown, {@code false} otherwise.
     */
    public boolean isOpenReportShown() {
        return openReportShown;
    }

    /**
     * @param showOpenReports The showOpenReports to set.
     */
    public void setOpenReportShown(boolean showOpenReports) {
        this.openReportShown = showOpenReports;
    }

    /**
     * @return {@code true} if closed reports are shown, {@code false} otherwise.
     */
    public boolean isClosedReportShown() {
        return closedReportShown;
    }

    /**
     * @param showClosedReports The showClosedReports to set.
     */
    public void setClosedReportShown(boolean showClosedReports) {
        this.closedReportShown = showClosedReports;
    }

    /**
     * @return the session
     */
    public UserSession getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(UserSession session) {
        this.session = session;
    }

    /**
     * @return the topicService
     */
    public TopicService getTopicService() {
        return topicService;
    }

    /**
     * @param topicService the topicService to set
     */
    public void setTopicService(TopicService topicService) {
        this.topicService = topicService;
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
     * @return The displayBanDialog.
     */
    public boolean isDisplayBanDialog() {
        return displayBanDialog;
    }

    /**
     * @return The displayUnbanDialog.
     */
    public boolean isDisplayUnbanDialog() {
        return displayUnbanDialog;
    }

    /**
     * @return The displayModDialog.
     */
    public boolean isDisplayModDialog() {
        return displayModDialog;
    }

    /**
     * @return The displayUnmodDialog.
     */
    public boolean isDisplayUnmodDialog() {
        return displayUnmodDialog;
    }

    /**
     * @return The displayDeleteDialog.
     */
    public boolean isDisplayDeleteDialog() {
        return displayDeleteDialog;
    }

    /**
     * @return The userBanSuggestions.
     */
    public List<User> getUserBanSuggestions() {
        return userBanSuggestions;
    }

    /**
     * @param userBanSuggestions The userBanSuggestions to set.
     */
    public void setUserBanSuggestions(List<User> userBanSuggestions) {
        this.userBanSuggestions = userBanSuggestions;
    }

    /**
     * @return The userModSuggestions.
     */
    public List<User> getUserModSuggestions() {
        return userModSuggestions;
    }

    /**
     * @param userModSuggestions The userModSuggestions to set.
     */
    public void setUserModSuggestions(List<User> userModSuggestions) {
        this.userModSuggestions = userModSuggestions;
    }
}
