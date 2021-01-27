package tech.bugger.control.backing;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.SearchService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.MarkdownHandler;
import tech.bugger.business.util.Paginator;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

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
    public enum TopicDialog {

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
        UNBAN,

        /**
         * A dialog to confirm the current user action is to be rendered.
         */
        SIMPLE

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
    private String userBan;

    /**
     * The Username of the User to be made a moderator.
     */
    private String userMod;

    /**
     * List of users similar to the entered name, for modding purpose.
     */
    private List<String> userBanSuggestions;

    /**
     * List of users similar to the entered name, for banning purpose.
     */
    private List<String> userModSuggestions;

    /**
     * Paginator for reports in the topic.
     */
    private Paginator<Report> reports;

    /**
     * Paginator for moderators in this topic.
     */
    private Paginator<User> moderators;

    /**
     * Paginator for banned users in this topic.
     */
    private Paginator<User> bannedUsers;

    /**
     * Whether the user is subscribed to the current topic.
     */
    private boolean subscribed;

    /**
     * Whether the user is banned.
     */
    private boolean banned;

    /**
     * Whether the user is administrator.
     */
    private boolean administrator;

    /**
     * Whether the user is moderator in the current topic.
     */
    private boolean moderator;

    /**
     * Whether or not open reports should be shown in the Pagination.
     */
    private boolean openReportShown; // default: true

    /**
     * Whether or not closed reports should be shown in the Pagination.
     */
    private boolean closedReportShown; // default: false

    /**
     * A sanitized form of the description, ready for display.
     */
    private String sanitizedDescription;

    /**
     * The type of popup dialog to be rendered.
     */
    private TopicDialog displayDialog;

    /**
     * The current user session.
     */
    private final UserSession session;

    /**
     * A transient topic service.
     */
    private final transient TopicService topicService;

    /**
     * A transient search service.
     */
    private final transient SearchService searchService;

    /**
     * The current {@link ExternalContext} of the application.
     */
    private final ExternalContext ectx;

    /**
     * Constructs a new topic page backing bean with the necessary dependencies.
     *
     * @param topicService  The topic service to use.
     * @param searchService The search service to use.
     * @param ectx          The current {@link ExternalContext} of the application.
     * @param session       The current {@link UserSession}.
     */
    @Inject
    public TopicBacker(final TopicService topicService,
                       final SearchService searchService,
                       final ExternalContext ectx,
                       final UserSession session) {
        this.topicService = topicService;
        this.searchService = searchService;
        this.ectx = ectx;
        this.session = session;
    }

    /**
     * Initializes the topic page. By default, only open reports are shown. Also checks if the user is allowed to view
     * the page. If not, acts as if the page did not exist.
     */
    @PostConstruct
    void init() {
        if (!ectx.getRequestParameterMap().containsKey("id")) {
            throw new Error404Exception();
        }

        try {
            topicID = Integer.parseInt(ectx.getRequestParameterMap().get("id"));
        } catch (NumberFormatException e) {
            throw new Error404Exception();
        }

        User user = session.getUser();
        topic = topicService.getTopicByID(topicID);
        if (topic == null) {
            throw new Error404Exception();
        }
        banned = topicService.isBanned(user, topic);
        if (banned) {
            throw new Error404Exception();
        }

        administrator = user != null && user.isAdministrator();
        moderator = topicService.isModerator(user, topic);
        subscribed = topicService.isSubscribed(user, topic);
        displayDialog = null;
        userBanSuggestions = new ArrayList<>();
        userModSuggestions = new ArrayList<>();
        openReportShown = true;
        closedReportShown = false;
        sanitizedDescription = MarkdownHandler.toHtml(topic.getDescription());

        moderators = new Paginator<>("username", Selection.PageSize.TINY) {
            @Override
            protected Iterable<User> fetch() {
                return topicService.getSelectedModerators(topic, getSelection());
            }

            @Override
            protected int totalSize() {
                return topicService.getNumberOfModerators(topic);
            }
        };

        bannedUsers = new Paginator<>("username", Selection.PageSize.TINY) {
            @Override
            protected Iterable<User> fetch() {
                return topicService.getSelectedBannedUsers(topic, getSelection());
            }

            @Override
            protected int totalSize() {
                return topicService.getNumberOfBannedUsers(topic);
            }
        };

        reports = new Paginator<>("id", Selection.PageSize.NORMAL) {
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
     * Enables suggestions for users to be banned.
     */
    public void searchBanUsers() {
        if (userBan != null && !userBan.isBlank()) {
            userBanSuggestions = searchService.getUserBanSuggestions(userBan, topic);
        }
    }

    /**
     * Enables suggestions for users to be unbanned.
     */
    public void searchUnbanUsers() {
        if (userBan != null && !userBan.isBlank()) {
            userBanSuggestions = searchService.getUserUnbanSuggestions(userBan, topic);
        }
    }

    /**
     * Enables suggestions for users to be made moderators.
     */
    public void searchModUsers() {
        if (userMod != null && !userMod.isBlank()) {
            userModSuggestions = searchService.getUserModSuggestions(userMod, topic);
        }
    }

    /**
     * Enables suggestions for moderators to be demoted.
     */
    public void searchUnmodUsers() {
        if (userMod != null && !userMod.isBlank()) {
            userModSuggestions = searchService.getUserUnmodSuggestions(userMod, topic);
        }
    }

    /**
     * Apply the current filter to the report pagination.
     */
    public void applyFilters() {
        if (reports != null) {
            reports.updateReset();
        }
    }

    /**
     * Opens the ban dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openBanDialog() {
        displayDialog = TopicDialog.BAN;
        return null;
    }

    /**
     * Opens the unban dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openUnbanDialog() {
        displayDialog = TopicDialog.UNBAN;
        return null;
    }

    /**
     * Opens the dialog for promoting a user to a moderator of the topic.
     *
     * @return {@code null} to reload the page.
     */
    public String openModDialog() {
        displayDialog = TopicDialog.MOD;
        return null;
    }

    /**
     * Opens the dialog for demoting a moderator of the topic.
     *
     * @return {@code null} to reload the page.
     */
    public String openUnmodDialog() {
        displayDialog = TopicDialog.UNMOD;
        return null;
    }

    /**
     * Opens the delete topic dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openDeleteDialog() {
        displayDialog = TopicDialog.DELETE;
        return null;
    }

    /**
     * Closes any open dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String closeDialog() {
        displayDialog = null;
        return null;
    }

    /**
     * Opens the unban dialog and fills the input field with the given username.
     *
     * @param username The username of the user to be demoted.
     */
    public void unbanSingleUser(final String username) {
        userBan = username;
        openUnbanDialog();
    }

    /**
     * Opens the dialog for demoting a moderator of the topic and fills the input field with the given username.
     *
     * @param username The username of the user to be demoted.
     */
    public void unmodSingleUser(final String username) {
        userMod = username;
        openUnmodDialog();
    }

    /**
     * Checks if the user is a moderator of the topic.
     *
     * @return {@code true} if the user is a moderator, {@code false} otherwise.
     */
    public boolean isModerator() {
        return administrator || moderator;
    }

    /**
     * Checks if the user is banned from the topic.
     *
     * @return {@code true} if the user is banned, {@code false} otherwise.
     */
    public boolean isBanned() {
        return banned;
    }

    /**
     * Irreversibly deletes the topic.
     *
     * @return The site to redirect to.
     */
    public String delete() {
        topicService.deleteTopic(topic);
        return "pretty:home";
    }

    /**
     * Bans the user whose username is specified in the attribute {@link #userBan}. Note that administrators and
     * moderators cannot be banned.
     *
     * @return {@code null} to reload the page if no user was banned or an empty string to call init() again and update
     *         the ban results.
     */
    public String banUser() {
        User user = session.getUser();
        if (!topicService.isModerator(user, topic) && !user.isAdministrator()) {
            log.error("A user was able to use the ban user functionality even though they were no moderator!");
            displayDialog = null;
            return null;
        }

        if (topicService.ban(userBan, topic)) {
            displayDialog = null;
            bannedUsers.update();
            return "";
        }

        return null;
    }

    /**
     * Unbans the user specified whose username is specified in the attribute {@link #userBan}.
     *
     * @return {@code null} to reload the page if no user was unbanned or an empty string to call init() again and
     *         update the ban results.
     */
    public String unbanUser() {
        User user = session.getUser();
        if (!topicService.isModerator(user, topic) && !user.isAdministrator()) {
            log.error("A user was able to use the unban user functionality even though they were no moderator!");
            displayDialog = null;
            return null;
        }

        if (topicService.unban(userBan, topic)) {
            displayDialog = null;
            bannedUsers.update();
            return "";
        }

        return null;
    }

    /**
     * Makes the user whose username is specified in {@link #userMod} a moderator of the topic. This is not possible if
     * they already are a moderator.
     *
     * @return {@code null} to reload the page if no user was promoted or an empty string to call init() again and
     *         update the moderation results.
     */
    public String makeModerator() {
        User user = session.getUser();
        if (!topicService.isModerator(user, topic) && !user.isAdministrator()) {
            log.error("A user was able to use the promote functionality even though they were no moderator!");
            displayDialog = null;
            return null;
        }

        if (topicService.makeModerator(userMod, topic)) {
            displayDialog = null;
            moderators.update();
            bannedUsers.update();
            return "";
        }

        return null;
    }

    /**
     * Removes the moderator status of the user specified in {@link #userMod}. This is not possible if they are an
     * administrator.
     *
     * @return {@code null} to reload the page if no user was promoted or an empty string to call init() again and
     *         update the moderation results.
     */
    public String removeModerator() {
        User user = session.getUser();
        if (!topicService.isModerator(user, topic) && !user.isAdministrator()) {
            log.error("A user was able to use the demote functionality even though they were no moderator!");
            displayDialog = null;
            return null;
        }

        if (topicService.removeModerator(userMod, topic)) {
            displayDialog = null;
            moderators.update();
            return "";
        }

        return null;
    }

    /**
     * Subscribes the user to the topic or unsubscribes them, whichever is applicable.
     *
     * @return The site to redirect to or {@code null} to reload the page.
     */
    public String toggleTopicSubscription() {
        User user = session.getUser();
        if (user == null) {
            return null;
        }
        if (isSubscribed()) {
            topicService.unsubscribeFromTopic(session.getUser(), topic);
        } else {
            topicService.subscribeToTopic(user, topic);
        }
        subscribed = topicService.isSubscribed(user, topic);
        return null;
    }

    /**
     * Checks whether the user is subscribed to the topic.
     *
     * @return {@code true} iff the user is subscribed to the topic.
     */
    public boolean isSubscribed() {
        return subscribed;
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
    public String getUserBan() {
        return userBan;
    }

    /**
     * @param userBan The userToBeBanned to set.
     */
    public void setUserBan(final String userBan) {
        this.userBan = userBan;
    }

    /**
     * @return The userToBeModded.
     */
    public String getUserMod() {
        return userMod;
    }

    /**
     * @param userMod The userToBeModded to set.
     */
    public void setUserMod(final String userMod) {
        this.userMod = userMod;
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
    public List<String> getUserBanSuggestions() {
        return userBanSuggestions;
    }

    /**
     * @param userBanSuggestions The userBanSuggestions to set.
     */
    public void setUserBanSuggestions(final List<String> userBanSuggestions) {
        this.userBanSuggestions = userBanSuggestions;
    }

    /**
     * @return The userModSuggestions.
     */
    public List<String> getUserModSuggestions() {
        return userModSuggestions;
    }

    /**
     * @param userModSuggestions The userModSuggestions to set.
     */
    public void setUserModSuggestions(final List<String> userModSuggestions) {
        this.userModSuggestions = userModSuggestions;
    }

    /**
     * @return The DialogType.
     */
    public TopicDialog getTopicDialog() {
        return displayDialog;
    }

    /**
     * @param displayDialog The DialogType to set.
     */
    public void setTopicDialog(final TopicDialog displayDialog) {
        this.displayDialog = displayDialog;
    }

}
