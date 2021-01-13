package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.service.ProfileService;
import tech.bugger.business.util.MarkdownHandler;
import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

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
     * Path to the default avatar.
     */
    private static final String DEFAULT_AVATAR = "/resources/images/bugger.png";

    /**
     * The type of popup dialog to be rendered on the profile page.
     */
    enum DialogType {
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
     * The default avatar to use for user that do not have their own avatar set.
     */
    private Lazy<byte[]> defaultAvatar;

    /**
     * The user's sanitized biography.
     */
    private String sanitizedBio;

    /**
     * The type of popup dialog to be rendered.
     */
    private DialogType dialog;

    /**
     * Whether the user is being created by an administrator or not.
     */
    private boolean create;

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
     * The authentication service providing the business logic for authentication.
     */
    @Inject
    private transient AuthenticationService authenticationService;

    /**
     * Initializes the profile edit page. Also checks if the user is allowed to modify this profile.
     */
    @PostConstruct
    void init() {
        ExternalContext ext = fctx.getExternalContext();
        if (ext.getRequestParameterMap().containsKey("token")) {
            updateUserEmail(authenticationService.findToken(ext.getRequestParameterMap().get("token")));
            log.debug("Updating the user's email address with the given token.");
        }

        if (session.getUser() == null) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:home");
            return;
        }
        dialog = DialogType.NONE;

        if (ext.getRequestParameterMap().containsKey("c") && session.getUser().isAdministrator()) {
            create = true;
            user = new User();
            log.debug("Creating new user.");
        } else if (ext.getRequestParameterMap().containsKey("e")) {
            user = findUser(ext.getRequestParameterMap().get("e"));
            log.debug("Using the edit key to find the user in the database.");
        } else {
            user = profileService.getUser(session.getUser().getId());
            log.debug("Using the session user's id to find the user in the database.");
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
        defaultAvatar = new Lazy<>(() -> {
            try {
                ServletContext sctx = (ServletContext) ext.getContext();
                return sctx.getResourceAsStream(DEFAULT_AVATAR).readAllBytes();
            } catch (IOException e) {
                log.warning("Default avatar could not be loaded", e);
                return new byte[0];
            }
        });
    }

    /**
     * Finds the {@link User} based on the given {@code id}.
     *
     * @param id The id passed in the RequestParameterMap.
     * @return The {@link User} if they exist and the {@code id} could be parsed, or {@code null} if not.
     */
    private User findUser(final String id) {
        try {
            int userID = Integer.parseInt(id);
            return profileService.getUser(userID);
        } catch (NumberFormatException e) {
            return null;
        }
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
     * @return {@code null} to reload the page.
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
            user.setEmailAddress(emailNew);
            profileService.createUser(user);
        } else {
            if (!emailNew.equals(user.getEmailAddress()) && !updateEmail(user, emailNew)) {
                closeDialog();
                return null;
            }

            if (profileService.updateUser(user)) {
                closeDialog();
            }
        }

        return null;
    }

    /**
     * Generates a new token with which the user's email address can be updated. An e-mail to finalize the process
     * is sent to the given email address if the provided data checks out.
     *
     * @param user The user whose email is to be updated.
     * @param email The user's new email address.
     * @return Whether the operation was successful or not.
     */
    private boolean updateEmail(final User user, final String email) {
        URL url;
        try {
            url = new URL(((HttpServletRequest) fctx.getExternalContext().getRequest()).getRequestURL().toString());
        } catch (MalformedURLException e) {
            log.debug("The user with id " + user.getId() + " tried to update their email with an invalid URL.", e);
            throw new InternalError("URL is invalid.", e);
        }

        String domain = String.format("%s://%s%s", url.getProtocol(), url.getAuthority(),
                fctx.getExternalContext().getApplicationContextPath());
        User updateUser = new User(user);

        return authenticationService.updateEmail(updateUser, domain, email);
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
                session.invalidateSession();
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
        Lazy<byte[]> image = deleteAvatar ? defaultAvatar : profileService.uploadAvatar(uploadedAvatar);
        if (image != null) {
            byte[] thumbnail = profileService.generateThumbnail(image.get());
            if (thumbnail != null) {
                user.setAvatar(image);
                user.setAvatarThumbnail(thumbnail);
                return true;
            }
        }
        uploadedAvatar = null;
        return false;
    }

    /**
     * Opens the delete profile dialog.
     */
    public void openDeleteDialog() {
        dialog = DialogType.DELETE;
    }

    /**
     * Opens the dialog that is displayed if the user wants to save the changes made to the given profile.
     */
    public void openChangeDialog() {
        if (uploadedAvatar != null || deleteAvatar) {
            if (!uploadAvatar()) {
                return;
            }
        }
        dialog = DialogType.UPDATE;
    }

    /**
     * Opens the dialog that is displayed if the user wants a preview of the biography.
     */
    public void openPreviewDialog() {
        if (uploadedAvatar != null || deleteAvatar) {
            if (!uploadAvatar()) {
                return;
            }
        }
        if (user.getBiography() != null) {
            sanitizedBio = MarkdownHandler.toHtml(user.getBiography());
        }
        dialog = DialogType.PREVIEW;
    }

    /**
     * Closes all open dialogs.
     */
    public void closeDialog() {
        dialog = DialogType.NONE;
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
<<<<<<< HEAD
     * @return The uploaded avatar.
=======
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
     * @return The tempAvatar.
>>>>>>> master
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
     * Returns the default avatar.
     *
     * @return The default avatar.
     */
    public Lazy<byte[]> getDefaultAvatar() {
        return defaultAvatar;
    }

    /**
     * Sets the default avatar.
     *
     * @param defaultAvatar The default avatar to set.
     */
    public void setDefaultAvatar(final Lazy<byte[]> defaultAvatar) {
        this.defaultAvatar = defaultAvatar;
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
    public DialogType getDialog() {
        return dialog;
    }

    /**
     * @param dialog The DialogType to set.
     */
    public void setDialog(final DialogType dialog) {
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
