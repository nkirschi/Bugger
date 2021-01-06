package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.service.ProfileService;
import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
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
        DELETE
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
    private Part tempAvatar;

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
    private ExternalContext ext;

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
    private void init() {
        if (ext.getRequestParameterMap().containsKey("token")) {
            updateUserEmail(authenticationService.findToken(ext.getRequestParameterMap().get("token")));
        }

        if (session.getUser() == null) {
            try {
                ext.redirect("home.xhtml");
            } catch (IOException e) {
                throw new InternalError("Error while redirecting.", e);
            }
        }
        dialog = DialogType.NONE;

        if (ext.getRequestParameterMap().containsKey("c") && session.getUser().isAdministrator()) {
            create = true;
            user = new User();
        } else if (ext.getRequestParameterMap().containsKey("e")) {
            user = findUser(ext.getRequestParameterMap().get("e"));
        } else {
            user = profileService.getUser(session.getUser().getId());
        }

        if (user == null) {
            try {
                ext.redirect("error.xhtml");
            } catch (IOException e) {
                throw new InternalError("Error while redirecting.", e);
            }
        } else {
            emailNew = user.getEmailAddress();
            usernameNew = user.getUsername();
        }
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
     * Applies and saves the changes made.
     */
    public void saveChanges() {
        boolean successful;
        if (create) {
            if (!profileService.matchingPassword(session.getUser(), password)) {
                return;
            }
            successful = profileService.createUser(user);
        } else {
            if (!emailNew.equals(user.getEmailAddress())) {
                if (!updateEmail(user, emailNew)) {
                    return;
                }
            }
            successful = profileService.updateUser(user);
        }

        if (successful) {
            try {
                ext.redirect("profile.xhtml");
            } catch (IOException e) {
                throw new InternalError("Error while redirecting.", e);
            }
        }
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
        URL currentUrl;
        try {
            currentUrl = new URL(((HttpServletRequest) ext.getRequest()).getRequestURL().toString());
        } catch (MalformedURLException e) {
            throw new InternalError("URL is invalid.", e);
        }

        String domain = String.format("%s://%s", currentUrl.getProtocol(), currentUrl.getAuthority());
        User updateUser = new User(user);
        updateUser.setEmailAddress(email);

        return authenticationService.updateEmail(updateUser, domain);
    }

    /**
     * Finalizes updating a user's email address using the {@link Token} to update the user information in the database.
     *
     * @param token The token containing the user's new email address.
     */
    private void updateUserEmail(final Token token) {
        if (token != null && token.getType() == Token.Type.CHANGE_EMAIL) {
            profileService.updateUser(token.getUser());
        } else {
            log.error("The token " + token + " was invalid for updating the email address.");
            try {
                ext.redirect("error.xhtml");
            } catch (IOException e) {
                throw new InternalError("Error while redirecting.", e);
            }
        }
    }

    /**
     * Opens the delete profile dialog.
     */
    public void openDeleteDialog() {
        dialog = DialogType.DELETE;
    }

    /**
     * Closes the delete profile dialog.
     */
    public void closeDeleteDialog() {
        dialog = DialogType.NONE;
    }

    /**
     * Opens the dialog that is displayed if an administrator edits another user's profile.
     */
    public void openChangeDialog() {
        dialog = DialogType.UPDATE;
    }

    /**
     * Closes the dialog that is displayed if an administrator edits another user's profile.
     */
    public void closeChangeDialog() {
        dialog = DialogType.NONE;
    }

    /**
     * Irreversibly deletes the user, logs them out and redirects to the home page. Their created reports and posts will
     * still remain.
     */
    public void delete() {
        if (!profileService.matchingPassword(session.getUser(), password)) {
            return;
        }

        if (profileService.deleteUser(user)) {
            if (user.equals(session.getUser())) {
                session.invalidateSession();
            }

            try {
                ext.redirect("home.xhtml");
            } catch (IOException e) {
                throw new InternalError("Error while redirecting.", e);
            }
        }
    }

    /**
     * Irreversibly deletes the user's avatar.
     */
    public void deleteAvatar() {
        //TODO needs to be changed to default pictures in Milestone 2
        user.setAvatar(new Lazy<>(null));
        user.setAvatarThumbnail(new byte[0]);
    }

    /**
     * Converts the uploaded avatar in {@code tempAvatar} to a {@code byte[]} and puts it into the user.
     */
    public void uploadAvatar() {
        Lazy<byte[]> image = (profileService.uploadAvatar(tempAvatar));
        if (image.isPresent()) {
            user.setAvatar(image);
            byte[] thumbnail = (profileService.generateThumbnail(image.get()));
            //TODO needs to be changed to default thumbnail in Milestone 2
            user.setAvatarThumbnail(thumbnail == null ? new byte[0] : thumbnail);
        } else {
            //TODO needs to be changed to default pictures in Milestone 2
            user.setAvatar(new Lazy<>(new byte[0]));
            user.setAvatarThumbnail(new byte[0]);
        }
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
     * @return The tempAvatar.
     */
    public Part getTempAvatar() {
        return tempAvatar;
    }

    /**
     * @param tempAvatar The tempAvatar to set.
     */
    public void setTempAvatar(final Part tempAvatar) {
        this.tempAvatar = tempAvatar;
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

}
