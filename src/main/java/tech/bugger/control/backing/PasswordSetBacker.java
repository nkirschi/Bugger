package tech.bugger.control.backing;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
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
     * The currently typed repeated password.
     */
    private String passwordRepeat;

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
     * Constructs a new register page backing bean with the necessary dependencies.
     *
     * @param authenticationService The authentication service to use.
     * @param session               The current {@link UserSession}.
     * @param ectx                  The current external context.
     */
    @Inject
    public PasswordSetBacker(final AuthenticationService authenticationService, final UserSession session,
                             final ExternalContext ectx) {
        this.authenticationService = authenticationService;
        this.session = session;
        this.ectx = ectx;
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

        token = authenticationService.getTokenByValue(ectx.getRequestParameterMap().get("token"));
        log.debug("Showing Password-Set page with token " + token + '.');
    }

    /**
     * Sets the user's password to the new value in {@code password}.
     *
     * @return The site to redirect to or {@code null}.
     */
    public String setUserPassword() {
        if (authenticationService.setPassword(token.getUser(), password, token.getValue())) {
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
     * Returns the current repeated password.
     *
     * @return The repeated password.
     */
    public String getPasswordRepeat() {
        return passwordRepeat;
    }

    /**
     * Sets a new repeated password.
     *
     * @param passwordRepeat The repeated password to set.
     */
    public void setPasswordRepeat(final String passwordRepeat) {
        this.passwordRepeat = passwordRepeat;
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
        return token != null;
    }

}
