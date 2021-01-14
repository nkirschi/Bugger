package tech.bugger.control.backing;

import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.service.ProfileService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

/**
 * Backing Bean for the password forgot page.
 */
@RequestScoped
@Named
public class PasswordForgotBacker {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(PasswordForgotBacker.class);

    /**
     * The service providing access to workflows regarding authentication.
     */
    private final transient AuthenticationService authenticationService;

    /**
     * The service providing access to workflows regarding profiles and user management.
     */
    private final transient ProfileService profileService;

    /**
     * The current user session.
     */
    private final UserSession session;

    /**
     * The current faces context.
     */
    private final FacesContext fctx;

    /**
     * Feedback Event for user feedback.
     */
    private final Event<Feedback> feedbackEvent;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * The {@link User} that has forgotten its password.
     */
    private User user;

    /**
     * Constructs a new password forgot page backing bean with the necessary dependencies.
     *
     * @param authenticationService The authentication service to use.
     * @param profileService        The profile service to use.
     * @param session               The current {@link UserSession}.
     * @param fctx                  The current faces context.
     * @param feedbackEvent         The feedback event to use for user feedback.
     * @param messagesBundle        The resource bundle for feedback messages.
     */
    @Inject
    public PasswordForgotBacker(final AuthenticationService authenticationService, final ProfileService profileService,
                                final UserSession session, final FacesContext fctx, final Event<Feedback> feedbackEvent,
                                @RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.authenticationService = authenticationService;
        this.profileService = profileService;
        this.session = session;
        this.fctx = fctx;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
    }

    /**
     * Initializes the password forgot page. If the user is already logged in, they are redirected to the home page.
     */
    @PostConstruct
    void init() {
        if (session.getUser() != null) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:home");
            return;
        }

        user = new User();
    }

    /**
     * Checks if username and email provided match up. If so, an e-mail with instructions on how to set a new password
     * is sent.
     *
     * @return The site to redirect to.
     */
    public String forgotPassword() {
        User userByEmail = profileService.getUserByEmail(user.getEmailAddress());
        User userByUsername = profileService.getUserByUsername(user.getUsername());

        if (userByEmail != null && userByEmail.equals(userByUsername)) {
            if (authenticationService.forgotPassword(userByEmail,
                    AuthenticationService.getApplicationPath(fctx.getExternalContext()))) {

                log.debug("Password forgot action for user " + user + " successful.");
                feedbackEvent.fire(new Feedback(messagesBundle.getString("password_forgot_success"),
                        Feedback.Type.INFO));
                return "pretty:home";
            }
        } else {
            log.debug("User " + userByEmail + " != " + userByUsername + " wanted to request a password change.");
            feedbackEvent.fire(new Feedback(messagesBundle.getString("password_forgot_user_not_found"),
                    Feedback.Type.ERROR));
        }
        return null;
    }

    /**
     * Returns the {@link User} whose password shall be reset.
     *
     * @return The {@link User} whose password shall be reset.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets a new {@link User} whose password shall be reset.
     *
     * @param user The new {@link User} to set.
     */
    public void setUser(final User user) {
        this.user = user;
    }

}
