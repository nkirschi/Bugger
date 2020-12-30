package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ProfileService;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
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
     * The id of the profile owner.
     */
    private int userID;

    /**
     * The profile owner's user information.
     */
    private User user;

    /**
     * The password entered to confirm changes.
     */
    private String password;

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
     * The boolean determining if the promote/demote popup is to be rendered.
     */
    private boolean displayPromoteDemoteAdminDialog;

    /**
     * The boolean determining whether the popup for deleting all topic subscriptions is to be rendered.
     */
    private boolean displayDeleteAllTopicSubscriptionsDialog;

    /**
     * The boolean determining whether the popup for deleting all report subscriptions is to be rendered.
     */
    private boolean displayDeleteAllReportSubscriptionsDialog;

    /**
     * The boolean determining whether the popup for deleting all user subscriptions is to be rendered.
     */
    private boolean displayDeleteAllUserSubscriptionsDialog;

    /**
     * The current user session.
     */
    @Inject
    private UserSession session;

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
        // The initialization of the subscriptions will be implemented in the subscriptions feature.
        user = profileService.getUser(userID);
        closeDialogs();
    }

    /**
     * Opens the administrator promotion/demotion dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openPromoteDemoteAdminDialog() {
        closeDialogs();
        displayPromoteDemoteAdminDialog = true;
        return null;
    }

    /**
     * Closes the administrator promotion/demotion dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String closePromoteDemoteAdminDialog() {
        displayPromoteDemoteAdminDialog = false;
        return null;
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
     * Closes the dialog for deleting all topic subscriptions of a particular type.
     *
     * @return {@code null} to reload the page.
     */
    public String closeDeleteAllTopicSubscriptionsDialog() {
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
     * Closes the dialog for deleting all report subscriptions of a particular type.
     *
     * @return {@code null} to reload the page.
     */
    public String closeDeleteAllReportSubscriptionsDialog() {
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
     * Closes the dialog for deleting all user subscriptions of a particular type.
     *
     * @return {@code null} to reload the page.
     */
    public String closeDeleteAllUserSubscriptionsDialog() {
        return null;
    }

    /**
     * Closes the administrator promotion/demotion dialog and all delete subscriptions dialogs.
     */
    private void closeDialogs() {
        displayPromoteDemoteAdminDialog = false;
        displayDeleteAllTopicSubscriptionsDialog = false;
        displayDeleteAllReportSubscriptionsDialog = false;
        displayDeleteAllUserSubscriptionsDialog = false;
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
        return session.getUser().isAdministrator() || session.getUser().equals(user);
    }

    /**
     * Promotes the user whose profile is being viewed to an administrator or demotes the user whose profile is being
     * viewed if they are an administrator. However, if they are the last remaining administrator, an error message is
     * displayed instead.
     */
    public void toggleAdmin() {
        //TODO update user in session if session.getUser().equals(user)?
        profileService.toggleAdmin(user);
    }

    /**
     * @return The userID.
     */
    public int getUserID() {
        return userID;
    }

    /**
     * @param userID The userID to set.
     */
    public void setUserID(final int userID) {
        this.userID = userID;
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
     * @return The displayPromoteDemoteAdminDialog.
     */
    public boolean isDisplayPromoteDemoteAdminDialog() {
        return displayPromoteDemoteAdminDialog;
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
     * @return The displayDeleteAllTopicSubscriptionsDialog.
     */
    public boolean isDisplayDeleteAllTopicSubscriptionsDialog() {
        return displayDeleteAllTopicSubscriptionsDialog;
    }

    /**
     * @return The displayDeleteAllReportSubscriptionsDialog.
     */
    public boolean isDisplayDeleteAllReportSubscriptionsDialog() {
        return displayDeleteAllReportSubscriptionsDialog;
    }

    /**
     * @return The displayDeleteAllUserSubscriptionsDialog.
     */
    public boolean isDisplayDeleteAllUserSubscriptionsDialog() {
        return displayDeleteAllUserSubscriptionsDialog;
    }

}
