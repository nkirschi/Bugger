package tech.bugger.control.backing;

import java.io.IOException;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Token;
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
     * The currently typed password.
     */
    private String password;

    /**
     * The currently typed (repeated) confirmation password.
     */
    private String passwordConfirmation;

    /**
     * The token being used to set a password.
     */
    private Token token;

    /**
     * The service providing access methods for authentication related procedures.
     */
    private final AuthenticationService authenticationService;

    /**
     * The current user session.
     */
    private final UserSession session;

    /**
     * The current external context.
     */
    private final ExternalContext ectx;

    /**
     * Feedback Event for user feedback.
     */
    private final Event<Feedback> feedbackEvent;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new register page backing bean with the necessary dependencies.
     *
     * @param authenticationService The authentication service to use.
     * @param session               The current {@link UserSession}.
     * @param ectx                  The current external context.
     * @param feedbackEvent         The feedback event to use for user feedback.
     * @param messagesBundle        The resource bundle for feedback messages.
     */
    @Inject
    public PasswordSetBacker(final AuthenticationService authenticationService, final UserSession session,
                             final ExternalContext ectx, final Event<Feedback> feedbackEvent,
                             @RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.authenticationService = authenticationService;
        this.session = session;
        this.ectx = ectx;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
    }

    /**
     * Initializes the page for setting a new password. Checks if the token for setting a new password is still valid.
     * If the user is already logged in, they are redirected to the home page.
     */
    @PostConstruct
    public void init() {
        if (session.getUser() != null) {
            try {
                ectx.redirect("home.xhtml");
            } catch (IOException e) {
                throw new InternalError("Error while redirecting.", e);
            }
        }

        token = authenticationService.findToken(ectx.getRequestParameterMap().get("token"));
        if (isValidToken()) {
            log.debug("Showing Password-Set page with token " + token + '.');
        } else {
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        }
    }

    /**
     * Sets the user's password to the new value in {@code password}.
     *
     * @return The site to redirect to or {@code null}.
     */
    public String setUserPassword() {
        if (authenticationService.setPassword(token.getUser(), password, token.getValue())) {
            feedbackEvent.fire(new Feedback(messagesBundle.getString("password_set.success"), Feedback.Type.INFO));
            return "home.xhtml";
        }
        return null;
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
     * Returns whether the supplied token is valid.
     *
     * @return Whether the supplied token is valid.
     */
    public boolean isValidToken() {
        return token != null
                && (token.getType() == Token.Type.REGISTER || token.getType() == Token.Type.FORGOT_PASSWORD);
    }

}
