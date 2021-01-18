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
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

/**
 * Backing Bean for the password set page.
 */
@RequestScoped
@Named
public class PasswordSetBacker {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(PasswordSetBacker.class);

    /**
     * The service providing access methods for authentication related procedures.
     */
    private final AuthenticationService authenticationService;

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
     * The token being used to set a password.
     */
    private Token token;

    /**
     * The currently typed password.
     */
    private String password;

    /**
     * The currently typed (repeated) confirmation password.
     */
    private String passwordConfirmation;

    /**
     * Constructs a new password set page backing bean with the necessary dependencies.
     *
     * @param authenticationService The authentication service to use.
     * @param session               The current {@link UserSession}.
     * @param fctx                  The current faces context.
     * @param feedbackEvent         The feedback event to use for user feedback.
     * @param messagesBundle        The resource bundle for feedback messages.
     */
    @Inject
    public PasswordSetBacker(final AuthenticationService authenticationService, final UserSession session,
                             final FacesContext fctx, final Event<Feedback> feedbackEvent,
                             @RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.authenticationService = authenticationService;
        this.session = session;
        this.fctx = fctx;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
    }

    /**
     * Initializes the page for setting a new password. Checks if the token for setting a new password is still valid.
     * If the user is already logged in, they are redirected to the home page.
     */
    @PostConstruct
    void init() {
        if (session.getUser() != null) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:home");
            return;
        }

        token = authenticationService.findToken(fctx.getExternalContext().getRequestParameterMap().get("token"));
        log.debug("Showing Password-Set page with token " + token + '.');

        if (!isValidToken()) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:home");
        }
    }

    /**
     * Sets the user's password to the new value in {@code password} and logs in the user.
     *
     * @return The site to redirect to or {@code null}.
     */
    public String setUserPassword() {
        User user = token.getUser();
        if (authenticationService.setPassword(user, password, token.getValue())) {
            feedbackEvent.fire(new Feedback(messagesBundle.getString("password_set.success"), Feedback.Type.INFO));
            session.setUser(user);
            return "pretty:home";
        }
        return null;
    }

    /**
     * Returns the current token.
     *
     * @return The current token.
     */
    public Token getToken() {
        return token;
    }

    /**
     * Sets a new token.
     *
     * @param token The token to set.
     */
    public void setToken(final Token token) {
        this.token = token;
    }

    /**
     * Returns the current password.
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets a new password.
     *
     * @param password The password to set.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Returns the current (repeated) confirmation password.
     *
     * @return The (repeated) confirmation password.
     */
    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    /**
     * Sets a new (repeated) confirmation password.
     *
     * @param passwordConfirmation The (repeated) confirmation password to set.
     */
    public void setPasswordConfirmation(final String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    /**
     * Returns whether the supplied token is valid.
     *
     * @return Whether the supplied token is valid.
     */
    public boolean isValidToken() {
        return token != null
                && (token.getType() == Token.Type.REGISTER || token.getType() == Token.Type.FORGOT_PASSWORD);
    }

}
