package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.SearchService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.MarkdownHandler;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
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

    @Serial
    private static final long serialVersionUID = 6893463272223847178L;

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(ProfileBacker.class);

    /**
     * The type of popup dialog to be rendered on the topic page.
     */
    enum DialogType {
        /**
         * No dialogs are to be rendered.
         */
        NONE,

        /**
         * The dialog top delete the topic is to be rendered.
         */
        DELETE,

        /**
         * The dialog to promote a user to a moderator is to be rendered.
         */
        MOD,

        /**
         * The dialog to demote a moderator is to be rendered.
         */
        UNMOD,

        /**
         * The dialog to ban a user from the topic is to be rendered.
         */
        BAN,

        /**
         * The dialog to unban a user is to be rendered.
         */
        UNBAN
    }

    /**
     * ID of the currently displayed topic.
     */
    private int topicID;

    /**
     * The currently displayed Topic.
     */
    private Topic topic;

    /**
     * The username of the User to be banned.
     */
    private String userToBeBanned;

    /**
     * The Username of the User to be made a moderator.
     */
    private String userToBeModded;

    /**
     * List of users similar to the entered name, for modding porpoise.
     */
    private List<User> userBanSuggestions;

    /**
     * List of users similar to the entered name, for banning porpoise.
     */
    private List<User> userModSuggestions;

    /**
     * Paginator for reports in the Topic.
     */
    private Paginator<Report> reports;

    /**
     * Paginator for moderators in this Topic.
     */
    private Paginator<User> moderators;

    /**
     * Paginator for banned users in this Topic.
     */
    private Paginator<User> bannedUsers;

    /**
     * Weather or not open reports should be shown in the Pagination.
     */
    private boolean openReportShown; // default: true

    /**
     * Weather or not closed reports should be shown in the Pagination.
     */
    private boolean closedReportShown; // default: false

    /**
     * A senitized form of the description, ready for display.
     */
    private String sanitizedDescription;

    /**
     * The type of popup dialog to be rendered.
     */
    private DialogType displayDialog;

    /**
     * The current user session.
     */
    @Inject
    private UserSession session;

    /**
     * A transient topic service.
     */
    @Inject
    private transient TopicService topicService;

    /**
     * A trransient report service.
     */
    @Inject
    private transient ReportService reportService;

    /**
     * A transient search service.
     */
    @Inject
    private transient SearchService searchService;

    /**
     * The current faces context.
     */
    @Inject
    private FacesContext fctx;

    /**
     * The current external context.
     */
    @Inject
    private ExternalContext ext;

    /**
     * Initializes the topic page. By default, only open reports are shown. Also checks if the user is allowed to view
     * the page. If not, acts as if the page did not exist.
     */
    @PostConstruct
    public void init() {

        /*
        if ((!ext.getRequestParameterMap().containsKey("t"))) {
            try {
                ext.redirect("public/home.xhtml");
            } catch (IOException e) {
                throw new InternalError("Error while redirecting.", e);
            }
        }
        try {
            topicID = Integer.parseInt(ext.getRequestParameterMap().get("t"));
        } catch (NumberFormatException e) {
            try {
                ext.redirect("public/home.xhtml");
            } catch (IOException e2) {
                throw new InternalError("Error while redirecting.", e2);
            }
        } **/

        topicID = 1;

        topic = topicService.getTopicByID(topicID);
        if (topic == null) {
            try {
                ext.redirect("error.xhtml");
            } catch (IOException e) {
                throw new InternalError("Error while redirecting.", e);
            }
        }
        displayDialog = DialogType.NONE;
        openReportShown = true;
        closedReportShown = false;
        sanitizedDescription = MarkdownHandler.toHtml(topic.getDescription());

        moderators = new Paginator<>("username", Selection.PageSize.SMALL) {
            @Override
            protected Iterable<User> fetch() {
                return topicService.getSelectedModerators(topic, getSelection());
            }

            @Override
            protected int totalSize() {
                return topicService.getNumberOfModerators(topic);
            }
        };

        reports = new Paginator<>("title", Selection.PageSize.NORMAL) {
            @Override
            protected Iterable<Report> fetch() {
                return topicService.getSelectedReports(topic, getSelection(), openReportShown, closedReportShown);
            }

            @Override
            protected int totalSize() {
                return topicService.getNumberOfReports(topic, openReportShown, closedReportShown);
            }
        };
    }

    /**
     * Returns the relevance of a certain report.
     *
     * @param report The report in question.
     * @return The relevance.
     */
    public int getRelevance(final Report report) {
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
     * Apply the current filter to the report pagination.
     */
    public void applyFilters() {
        reports.update();
    }

    /**
     * Opens the ban dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openBanDialog() {
        displayDialog = DialogType.BAN;
        return null;
    }

    /**
     * Opens the unban dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openUnbanDialog() {
        displayDialog = DialogType.UNBAN;
        return null;
    }

    /**
     * Opens the dialog for promoting a user to a moderator of the topic.
     *
     * @return {@code null} to reload the page.
     */
    public String openModDialog() {
        displayDialog = DialogType.MOD;
        return null;
    }

    /**
     * Opens the dialog for demoting a moderator of the topic.
     *
     * @return {@code null} to reload the page.
     */
    public String openUnmodDialog() {
        displayDialog = DialogType.UNMOD;
        return null;
    }

    /**
     * Opens the delete topic dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openDeleteDialog() {
        displayDialog = DialogType.DELETE;
        return null;
    }

    /**
     * Closes any open dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String closeDialog() {
        displayDialog = DialogType.NONE;
        return null;
    }

    /**
     * Checks if the user is a moderator of the topic.
     *
     * @return {@code true} if the user is a moderator, {@code false} otherwise.
     */
    public boolean isModerator() {
        User user = session.getUser();
        if (user == null) {
            return false;
        }
        return user.isAdministrator() || topicService.isModerator(user, topic);
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
     *
     */
    public void delete() {
        topicService.deleteTopic(topic);
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
    public void unbanUser(final User user) {
    }

    /**
     * Makes the user whose username is specified in {@code userToBeModded} a moderator of the topic. This is not
     * possible if they already are a moderator.
     */
    public void makeModerator() {
        if (!isModerator()) {
            log.error("A user was able to to use the promote or demote moderator functionality even though "
                    + "they had no moderator status!");
            return;
        }

        if (topicService.makeModerator(userToBeModded, topic)) {
            displayDialog = DialogType.NONE;
        }
    }

    /**
     * Removes the moderator status of the user specified in {@code unmodUser}. This is not possible if they are an
     * administrator.
     */
    public void removeModerator() {
        if (!isModerator()) {
            log.error("A user was able to to use the promote or demote moderator functionality even though "
                    + "they had no moderator status!");
            return;
        }

        if (topicService.removeModerator(userToBeModded, topic)) {
            displayDialog = DialogType.NONE;
        }
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
    public ZonedDateTime lastChange(final Report report) {
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
    public void setTopic(final Topic topic) {
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
    public void setUserToBeBanned(final String userToBeBanned) {
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
    public void setUserToBeModded(final String userToBeModded) {
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
    public void setOpenReportShown(final boolean showOpenReports) {
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
    public void setClosedReportShown(final boolean showClosedReports) {
        this.closedReportShown = showClosedReports;
    }

    /**
     * @return the sanitized description
     */
    public String getSanitizedDescription() {
        return sanitizedDescription;
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
    public void setSession(final UserSession session) {
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
    public void setTopicService(final TopicService topicService) {
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
    public void setTopicID(final int topicID) {
        this.topicID = topicID;
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
    public void setUserBanSuggestions(final List<User> userBanSuggestions) {
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
    public void setUserModSuggestions(final List<User> userModSuggestions) {
        this.userModSuggestions = userModSuggestions;
    }

    /**
     * @return The DialogType.
     */
    public DialogType getDisplayDialog() {
        return displayDialog;
    }

    /**
     * @param displayDialog The DialogType to set.
     */
    public void setDisplayDialog(final DialogType displayDialog) {
        this.displayDialog = displayDialog;
    }

}
