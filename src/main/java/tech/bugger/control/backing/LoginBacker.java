package tech.bugger.control.backing;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

/**
 * Backing Bean for the login page.
 */
@RequestScoped
@Named
public class LoginBacker {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(LoginBacker.class);

    /**
     * The URL to which the user will be redirected after log in, if present.
     */
    private String redirectURL;

    /**
     * The user's username input.
     */
    private String username;

    /**
     * The user's password input.
     */
    private String password;

    /**
     * The service providing access to workflows regarding authentication.
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
     * The current external context.
     */
    private final ExternalContext ectx;

    /**
     * Constructs a new login page backing bean with the necessary dependencies.
     *
     * @param authenticationService The authentication service to use.
     * @param session               The current {@link UserSession}.
     * @param fctx                  The current faces context.
     * @param ectx                  The current external context.
     */
    @Inject
    public LoginBacker(final AuthenticationService authenticationService,
                       final UserSession session,
                       final FacesContext fctx,
                       final ExternalContext ectx) {
        this.authenticationService = authenticationService;
        this.session = session;
        this.fctx = fctx;
        this.ectx = ectx;
    }

    /**
     * Initializes the login page. If a user is already logged in, they are redirected to the home page.
     */
    @PostConstruct
    void init() {
        if (session.getUser() != null) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:home");
        }

        redirectURL = ectx.getRequestParameterMap().get("url");
        log.debug("Will try to redirect to " + redirectURL);
    }

    /**
     * Logs in the user and returns them to the page they were on before. If there is no such page they are redirected
     * to the home page instead. Also changes the language to the user's preferred language.
     *
     * @return the name of the page to be redirected to.
     */
    public String login() {
        User user = authenticationService.authenticate(username, password);
        if (user == null) {
            return null;
        }
        session.setUser(user);
        session.setLocale(user.getPreferredLanguage());

        if (redirectURL != null) {
            try {
                String url = URLDecoder.decode(redirectURL, StandardCharsets.UTF_8);
                log.debug("Redirecting user to " + url);
                ectx.redirect(url);
                return null;
            } catch (IOException e) {
                // Ignore the exception and just go to the home page
            }
        }
        return "pretty:home";
    }

    /**
     * @return The redirectURL.
     */
    public String getRedirectURL() {
        return redirectURL;
    }

    /**
     * @param redirectURL The redirectURL to set.
     */
    public void setRedirectURL(final String redirectURL) {
        this.redirectURL = redirectURL;
    }

    /**
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username the user enters in the login form..
     *
     * @param username The user's username.
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the un-hashed password the user enters in the login form..
     *
     * @param password The user's password.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

}
