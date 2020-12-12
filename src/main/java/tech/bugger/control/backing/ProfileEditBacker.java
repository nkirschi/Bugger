package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.service.ProfileService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.Serializable;

/**
 * Backing bean for the profile edit page.
 */
@ViewScoped
@Named
public class ProfileEditBacker implements Serializable {

    private static final Log log = Log.forClass(ProfileEditBacker.class);
    private static final long serialVersionUID = 8894621041245160359L;

    private User user;
    private String password;
    private String passwordNew;
    private String passwordNewConfirm;
    private Part tempAvatar;
    private boolean displayDeleteDialog;
    private boolean displayAdminChangeDialog;

    @Inject
    private transient UserSession session;

    @Inject
    private transient ProfileService profileService;

    @Inject
    private transient AuthenticationService authenticationService;

    /**
     * Initializes the profile edit page. Also checks if the user is allowed to modify this profile.
     */
    @PostConstruct
    private void init() {

    }

    /**
     * Creates a FacesMessage to display if an event is fired in one of the injected services.
     *
     * @param feedback The feedback with details on what to display.
     */
    public void displayFeedback(@Observes @Any Feedback feedback) {

    }

    /**
     * Applies and saves the changes made.
     */
    public void saveChanges() {

    }

    /**
     * Opens the delete profile dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openDeleteDialog() {
        return null;
    }

    /**
     * Closes the delete profile dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String closeDeleteDialog() {
        return null;
    }

    /**
     * Opens the dialog that is displayed if an administrator edits another user's profile.
     *
     * @return {@code null} to reload the page.
     */
    public String openAdminChangeDialog() {
        return null;
    }

    /**
     * Closes the dialog that is displayed if an administrator edits another user's profile.
     *
     * @return {@code null} to reload the page.
     */
    public String closeAdminChangeDialog() {
        return null;
    }

    /**
     * Irreversibly deletes the user, logs them out and redirects to the home page. Their created reports and posts will
     * still remain.
     */
    public void delete() {

    }

    /**
     * Irreversibly deletes the user's avatar.
     */
    public void deleteAvatar() {

    }

    /**
     * Converts the uploaded avatar in {@code tempAvatar} to a {@code byte[]} and puts it into the user.
     */
    public void uploadAvatar() {

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
    public void setUser(User user) {
        this.user = user;
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
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return The passwordNew.
     */
    public String getPasswordNew() {
        return passwordNew;
    }

    /**
     * @param passwordNew The passwordNew to set.
     */
    public void setPasswordNew(String passwordNew) {
        this.passwordNew = passwordNew;
    }

    /**
     * @return The passwordNewConfirm.
     */
    public String getPasswordNewConfirm() {
        return passwordNewConfirm;
    }

    /**
     * @param passwordNewConfirm The passwordNewConfirm to set.
     */
    public void setPasswordNewConfirm(String passwordNewConfirm) {
        this.passwordNewConfirm = passwordNewConfirm;
    }

    /**
     * @return The tempAvatar.
     */
    public Part getTempAvatar() {
        return tempAvatar;
    }

    /**
     * @param tempAvatar The tempAvatar to set.
     */
    public void setTempAvatar(Part tempAvatar) {
        this.tempAvatar = tempAvatar;
    }

    /**
     * @return The displayDeleteDialog.
     */
    public boolean isDisplayDeleteDialog() {
        return displayDeleteDialog;
    }

    /**
     * @param displayDeleteDialog The displayDeleteDialog to set.
     */
    public void setDisplayDeleteDialog(boolean displayDeleteDialog) {
        this.displayDeleteDialog = displayDeleteDialog;
    }

    /**
     * @return The displayAdminChangeDialog.
     */
    public boolean isDisplayAdminChangeDialog() {
        return displayAdminChangeDialog;
    }

    /**
     * @param displayAdminChangeDialog The displayAdminChangeDialog to set.
     */
    public void setDisplayAdminChangeDialog(boolean displayAdminChangeDialog) {
        this.displayAdminChangeDialog = displayAdminChangeDialog;
    }


}
