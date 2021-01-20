package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.service.ProfileService;
import tech.bugger.business.util.MarkdownHandler;
import tech.bugger.control.util.JFConfig;
import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

/**
 * Backing bean for the profile edit page.
 */
@ViewScoped
@Named
public class ProfileEditBacker implements Serializable {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(ProfileEditBacker.class);

    @Serial
    private static final long serialVersionUID = 8894621041245160359L;

    /**
     * The type of popup dialog to be rendered on the profile page.
     */
    public enum ProfileEditDialog {
        /**
         * No dialogs are to be rendered.
         */
        NONE,

        /**
         * The dialog to update the profile owner's information is to be rendered.
         */
        UPDATE,

        /**
         * The dialog to delete the profile owner's profile is to be rendered.
         */
        DELETE,

        /**
         * The dialog showing a preview of how the user's biography will look on the profile page is to be rendered.
         */
        PREVIEW
    }

    /**
     * The profile owner's user information.
     */
    private User user;

    /**
     * The password entered to confirm changes.
     */
    private String password;

    /**
     * The new password to be set.
     */
    private String passwordNew;

    /**
     * The confirmation password that has to match the new password.
     */
    private String passwordNewConfirm;

    /**
     * The new username to be set.
     */
    private String usernameNew;

    /**
     * The new email to be set.
     */
    private String emailNew;

    /**
     * The new avatar to be set.
     */
    private Part uploadedAvatar;

    /**
     * Whether to delete the existing avatar.
     */
    private boolean deleteAvatar;

    /**
     * The user's sanitized biography.
     */
    private String sanitizedBio;

    /**
     * The type of popup dialog to be rendered.
     */
    private ProfileEditDialog dialog;

    /**
     * Whether the user is being created by an administrator or not.
     */
    private boolean create;

    /**
     * The current user session.
     */
    private final UserSession session;

    /**
     * The current {@link FacesContext} of the application.
     */
    private final FacesContext fctx;

    /**
     * The current {@link ExternalContext} of the application.
     */
    private final ExternalContext ectx;

    /**
     * The profile service providing the business logic.
     */
    private final transient ProfileService profileService;

    /**
     * The authentication service providing the business logic for authentication.
     */
    private final transient AuthenticationService authenticationService;

    /**
     * Constructs a new profile edit page backing bean with the necessary dependencies.
     *
     * @param authenticationService The authentication service to use.
     * @param profileService        The profile service to use.
     * @param session               The current {@link UserSession}.
     * @param fctx                The current {@link FacesContext} of the application.
     * @param ectx                The current {@link ExternalContext} of the application.
     */
    @Inject
    public ProfileEditBacker(final AuthenticationService authenticationService,
                             final ProfileService profileService,
                             final UserSession session,
                             final FacesContext fctx,
                             final ExternalContext ectx) {
        this.authenticationService = authenticationService;
        this.profileService = profileService;
        this.session = session;
        this.fctx = fctx;
        this.ectx = ectx;
    }

    /**
     * Initializes the profile edit page. Also checks if the user is allowed to modify this profile.
     */
    @PostConstruct
    void init() {
        if (ectx.getRequestParameterMap().containsKey("token")) {
            updateUserEmail(authenticationService.findToken(ectx.getRequestParameterMap().get("token")));
            log.debug("Updating the user's email address with the given token.");
        }

        if (session.getUser() == null) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:home");
            return;
        }
        dialog = ProfileEditDialog.NONE;

        if (ectx.getRequestParameterMap().containsKey("c") && session.getUser().isAdministrator()) {
            create = true;
            user = new User();
            log.debug("Creating new user.");
        } else if (ectx.getRequestParameterMap().containsKey("e") && session.getUser().isAdministrator()) {
            user = profileService.getUserByUsername(ectx.getRequestParameterMap().get("e"));
            log.debug("Using the edit key to find the user in the database.");
        } else {
            user = profileService.getUser(session.getUser().getId());
            log.debug("Using the session user's id to find the user in the database.");
        }

        // When editing an existing user, fetch their avatar.
        if (user != null && !create) {
            user.setAvatar(profileService.getAvatarForUser(user.getId()));
        }

        if (user == null) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
        } else {
            emailNew = user.getEmailAddress();
            usernameNew = user.getUsername();
            passwordNew = "";
            passwordNewConfirm = "";
        }

        deleteAvatar = false;
    }

    /**
     * Finalizes updating a user's email address using the {@link Token} to update the user information in the database.
     *
     * @param token The token containing the user's new email address.
     */
    private void updateUserEmail(final Token token) {
        if (token != null && token.getType() == Token.Type.CHANGE_EMAIL) {
            User updateUser = profileService.getUser(token.getUser().getId());
            updateUser.setEmailAddress(token.getMeta());
            profileService.updateUser(updateUser);
        } else {
            log.error("The token " + token + " was invalid for updating the email address.");
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:error");
        }
    }

    /**
     * Applies and saves the changes made.
     *
     * @return The page to redirect to or {@code null} to reload the current page.
     */
    public String saveChanges() {
        if (!passwordNew.isBlank()) {
            if (passwordNew.equals(passwordNewConfirm)) {
                authenticationService.hashPassword(user, passwordNew);
            } else {
                closeDialog();
                return null;
            }
        }

        if (!profileService.matchingPassword(session.getUser(), password)) {
            closeDialog();
            return null;
        }

        user.setUsername(usernameNew);
        if (create) {
            user.setEmailAddress(emailNew.toLowerCase());
            profileService.createUser(user);
        } else {
            if (!emailNew.equalsIgnoreCase(user.getEmailAddress()) && !updateEmail(user, emailNew.toLowerCase())) {
                closeDialog();
                return null;
            }

            if (profileService.updateUser(user)) {
                closeDialog();
            }
        }

        String base = ectx.getApplicationContextPath();
        String redirect = base + "/profile?u=" + user.getUsername();
        try {
            ectx.redirect(redirect);
        } catch (IOException e) {
            // Ignore the exception and just stay on the page
        }
        return null;
    }

    /**
     * Generates a new token with which the user's email address can be updated. An e-mail to finalize the process
     * is sent to the given email address if the provided data checks out.
     *
     * @param user  The user whose email is to be updated.
     * @param email The user's new email address.
     * @return Whether the operation was successful or not.
     */
    private boolean updateEmail(final User user, final String email) {
        User updateUser = new User(user);

        return authenticationService.updateEmail(updateUser, JFConfig.getApplicationPath(ectx), email);
    }

    /**
     * Irreversibly deletes the user, logs them out and redirects to the home page. Their created reports and posts will
     * still remain.
     *
     * @return {@code null} to reload the page.
     */
    public String delete() {
        if (!profileService.matchingPassword(session.getUser(), password)) {
            closeDialog();
            return null;
        }

        if (profileService.deleteUser(user)) {
            if (user.equals(session.getUser())) {
                session.setUser(null);
                ectx.invalidateSession();
            }

            return "pretty:home";
        }

        return null;
    }

    /**
     * Converts the uploaded avatar in {@code uploadedAvatar} to a {@code byte[]}, creates a thumbnail,
     * and puts it into the user.
     *
     * @return {@code true} iff the avatar was successfully processed.
     */
    boolean uploadAvatar() {
        byte[] image = deleteAvatar ? new byte[0] : profileService.uploadAvatar(uploadedAvatar);
        if (image != null) {
            byte[] thumbnail = deleteAvatar ? new byte[0] : profileService.generateThumbnail(image);
            if (thumbnail != null) {
                user.setAvatar(image);
                user.setAvatarThumbnail(thumbnail);
                return true;
            }
        }
        uploadedAvatar = null;
        deleteAvatar = false;
        return false;
    }

    /**
     * Opens the delete profile dialog.
     *
     * @return {@code null} to reload the page.
     */
    public String openDeleteDialog() {
        dialog = ProfileEditDialog.DELETE;
        return null;
    }

    /**
     * Opens the dialog that is displayed if the user wants to save the changes made to the given profile.
     *
     * @return {@code null} to reload the page.
     */
    public String openChangeDialog() {
        if (uploadedAvatar != null || deleteAvatar) {
            if (!uploadAvatar()) {
                return null;
            }
        }
        dialog = ProfileEditDialog.UPDATE;
        return null;
    }

    /**
     * Opens the dialog that is displayed if the user wants a preview of the biography.
     *
     * @return {@code null} to reload the page.
     */
    public String openPreviewDialog() {
        if (uploadedAvatar != null || deleteAvatar) {
            if (!uploadAvatar()) {
                return null;
            }
        }
        if (user.getBiography() != null) {
            sanitizedBio = MarkdownHandler.toHtml(user.getBiography());
        }
        dialog = ProfileEditDialog.PREVIEW;
        return null;
    }

    /**
     * Closes all open dialogs.
     *
     * @return {@code null} to reload the page.
     */
    public String closeDialog() {
        dialog = ProfileEditDialog.NONE;
        return null;
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
     * @return The passwordNew.
     */
    public String getPasswordNew() {
        return passwordNew;
    }

    /**
     * @param passwordNew The passwordNew to set.
     */
    public void setPasswordNew(final String passwordNew) {
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
    public void setPasswordNewConfirm(final String passwordNewConfirm) {
        this.passwordNewConfirm = passwordNewConfirm;
    }

    /**
     * @return The new username.
     */
    public String getUsernameNew() {
        return usernameNew;
    }

    /**
     * @param usernameNew The new username to set.
     */
    public void setUsernameNew(final String usernameNew) {
        this.usernameNew = usernameNew;
    }

    /**
     * @return The new email.
     */
    public String getEmailNew() {
        return emailNew;
    }

    /**
     * @param emailNew The new email to set.
     */
    public void setEmailNew(final String emailNew) {
        this.emailNew = emailNew;
    }

    /**
     * @return The sanitized biography.
     */
    public String getSanitizedBio() {
        return sanitizedBio;
    }

    /**
     * @param sanitizedBio The new biography to set.
     */
    public void setSanitizedBio(final String sanitizedBio) {
        this.sanitizedBio = sanitizedBio;
    }

    /**
     * @return The uploaded avatar.
     */
    public Part getUploadedAvatar() {
        return uploadedAvatar;
    }

    /**
     * @param uploadedAvatar The uploaded avatar to set.
     */
    public void setUploadedAvatar(final Part uploadedAvatar) {
        this.uploadedAvatar = uploadedAvatar;
    }

    /**
     * Returns whether to delete the existing avatar.
     *
     * @return Whether to delete the existing avatar.
     */
    public boolean isDeleteAvatar() {
        return this.deleteAvatar;
    }

    /**
     * Sets whether to delete the existing avatar.
     *
     * @param deleteAvatar Whether to delete the existing avatar.
     */
    public void setDeleteAvatar(final boolean deleteAvatar) {
        this.deleteAvatar = deleteAvatar;
    }

    /**
     * @return The dialog.
     */
    public ProfileEditDialog getDialog() {
        return dialog;
    }

    /**
     * @param dialog The DialogType to set.
     */
    public void setDialog(final ProfileEditDialog dialog) {
        this.dialog = dialog;
    }

    /**
     * @return Whether the user is being created by an administrator.
     */
    public boolean isCreate() {
        return create;
    }

    /**
     * Sets whether the user is being created by an administrator.
     *
     * @param create Whether the user is being created by an administrator.
     */
    public void setCreate(final boolean create) {
        this.create = create;
    }

}
