package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ProfileService;
import tech.bugger.business.util.MarkdownHandler;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Constants;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Backing bean for the profile page.
 */
@ViewScoped
@Named
public class ProfileBacker implements Serializable {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(ProfileBacker.class);

    @Serial
    private static final long serialVersionUID = -4606230292807293380L;

    /**
     * The type of popup dialog to be rendered on the profile page.
     */
    enum DialogType {
        /**
         * No dialogs are to be rendered.
         */
        NONE,

        /**
         * The dialog to change the profile owner's administrator status is to be rendered.
         */
        ADMIN,

        /**
         * The dialog to delete all topic subscriptions is to be rendered.
         */
        TOPIC,

        /**
         * The dialog to delete all report subscriptions is to be rendered.
         */
        REPORT,

        /**
         * The dialog to delete all user subscriptions is to be rendered.
         */
        USER
    }

    /**
     * The username of the profile owner.
     */
    private String username;

    /**
     * The profile owner's user information.
     */
    private User user;

    /**
     * The password entered to confirm changes.
     */
    private String password;

    /**
     * The profile owner's sanitized biography.
     */
    private String sanitizedBiography;

    /**
     * The profile owner's topic subscriptions.
     */
    private Paginator<Topic> topicSubscriptions;

    /**
     * The profile owner's report subscriptions.
     */
    private Paginator<Report> reportSubscriptions;

    /**
     * The profile owner's user subscriptions.
     */
    private Paginator<User> userSubscriptions;

    /**
     * The profile owner's moderated topics.
     */
    private Paginator<Topic> moderatedTopics;

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
     * The current external context.
     */
    @Inject
    private FacesContext fctx;

    /**
     * The profile service providing the business logic.
     */
    @Inject
    private transient ProfileService profileService;

    /**
     * Initializes the profile page. Checks whether this is the user's own profile page.
     */
    @PostConstruct
    void init() {
        ExternalContext ext = fctx.getExternalContext();
        // The initialization of the subscriptions will be implemented in the subscriptions feature.
        if ((!ext.getRequestParameterMap().containsKey("u")) || (ext.getRequestParameterMap().get("u").length()
                > Constants.USERNAME_MAX)) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:home");
            return;
        }

        username = ext.getRequestParameterMap().get("u");
        user = profileService.getUserByUsername(username);

        if (user == null) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
            return;
        }

        if (user.getBiography() != null) {
            sanitizedBiography = MarkdownHandler.toHtml(user.getBiography());
        }

        if ((session.getUser() != null) && (session.getUser().equals(user))) {
            session.setUser(new User(user));
        }
        displayDialog = DialogType.NONE;
    }

    /**
     * Opens the administrator promotion/demotion dialog.
     */
    public void openPromoteDemoteAdminDialog() {
        displayDialog = DialogType.ADMIN;
    }

    /**
     * Opens the dialog for deleting all topic subscriptions of a particular type.
     *
     * @return {@code null} to reload the page.
     */
    public String openDeleteAllTopicSubscriptionsDialog() {
        return null;
    }

    /**
     * Opens the dialog for deleting all report subscriptions of a particular type.
     *
     * @return {@code null} to reload the page.
     */
    public String openDeleteAllReportSubscriptionsDialog() {
        return null;
    }

    /**
     * Opens the dialog for deleting all user subscriptions of a particular type.
     *
     * @return {@code null} to reload the page.
     */
    public String openDeleteAllUserSubscriptionsDialog() {
        return null;
    }

    /**
     * Closes any open dialog.
     */
    public void closeDialog() {
        displayDialog = DialogType.NONE;
    }

    /**
     * Returns the timestamp of the last action in one particular topic. Creating, editing and moving a report as well
     * as creating and editing posts count as actions. Moving a report is an action in the destination topic only.
     *
     * @param topic The topic in question.
     * @return The timestamp of the last action as a {@code ZonedDateTime}.
     */
    public ZonedDateTime lastChanged(final Topic topic) {
        return null;
    }

    /**
     * Returns the timestamp of the last action in one particular report. Creating, editing and moving a report as well
     * as creating and editing posts count as actions.
     *
     * @param report The report in question.
     * @return The timestamp of the last action as a {@code ZonedDateTime}.
     */
    public ZonedDateTime lastChanged(final Report report) {
        return null;
    }

    /**
     * Returns the voting weight of the user whose profile is viewed. The voting weight is either determined by the
     * number of posts the user has created or directly overwritten by an administrator.
     *
     * @return The voting weight.
     */
    public int getVotingWeight() {
        return profileService.getVotingWeightForUser(user);
    }

    /**
     * Returns the number of posts the user whose profile is viewed has created. Only posts that have not been deleted
     * are counted.
     *
     * @return The number of posts.
     */
    public int getNumberOfPosts() {
        return profileService.getNumberOfPostsForUser(user);
    }

    /**
     * Removes the subscription to one particular topic for the user.
     *
     * @param topic The topic of which the subscription to should be removed.
     */
    public void deleteTopicSubscription(final Topic topic) {

    }

    /**
     * Removes the subscription to one particular report for the user.
     *
     * @param report The report of which the subscription to should be removed.
     */
    public void deleteReportSubscription(final Report report) {

    }

    /**
     * Removes the subscription to one particular other user for the user.
     *
     * @param subscribee The user of which the subscription to should be removed.
     */
    public void deleteUserSubscription(final User subscribee) {

    }

    /**
     * Removes all subscriptions to topics for the user.
     */
    public void deleteAllTopicSubscriptions() {

    }

    /**
     * Removes all subscriptions to reports for the user.
     */
    public void deleteAllReportSubscriptions() {

    }

    /**
     * Removes all subscriptions to other users for the user.
     */
    public void deleteAllUserSubscriptions() {

    }

    /**
     * Subscribes the user to the user whose profile is being viewed.
     */
    public void toggleUserSubscription() {

    }

    /**
     * Checks if the user is privileged, i.e. whether they are viewing their own profile or are an administrator.
     *
     * @return Whether the user is privileged.
     */
    public boolean isPrivileged() {
        return (session.getUser() != null) && ((session.getUser().isAdministrator())
                || (session.getUser().equals(user)));
    }

    /**
     * Promotes the user whose profile is being viewed to an administrator or demotes the user whose profile is being
     * viewed if they are an administrator. However, if they are the last remaining administrator, an error message is
     * displayed instead.
     */
    public void toggleAdmin() {
        if ((session.getUser() == null) || (!session.getUser().isAdministrator())) {
            log.error("A user was able to to use the promote or demote administrator functionality even though "
                    + "they had no administrator status!");
            return;
        }
        if (!profileService.matchingPassword(session.getUser(), password)) {
            return;
        }
        profileService.toggleAdmin(user);
        if (session.getUser().equals(user)) {
            session.getUser().setAdministrator(user.isAdministrator());
        }
    }

    /**
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username The username to set.
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * @return The user.
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user The user to set.
     */
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * @return The sanitized biography.
     */
    public String getSanitizedBiography() {
        return sanitizedBiography;
    }

    /**
     * @return The topicSubscriptions.
     */
    public Paginator<Topic> getTopicSubscriptions() {
        return topicSubscriptions;
    }

    /**
     * @return The reportSubscriptions.
     */
    public Paginator<Report> getReportSubscriptions() {
        return reportSubscriptions;
    }

    /**
     * @return The userSubscriptions.
     */
    public Paginator<User> getUserSubscriptions() {
        return userSubscriptions;
    }

    /**
     * @return The moderatedTopics.
     */
    public Paginator<Topic> getModeratedTopics() {
        return moderatedTopics;
    }

    /**
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(final String password) {
        this.password = password;
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
