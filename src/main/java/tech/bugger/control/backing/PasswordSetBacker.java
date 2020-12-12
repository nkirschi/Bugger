package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Backing Bean for the password set page.
 */
@RequestScoped
@Named
public class PasswordSetBacker {

    private static final Log log = Log.forClass(PasswordSetBacker.class);

    private User user;
    private String password;
    private String token;

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private UserSession session;

    @Inject
    private FacesContext fctx;

    /**
     * Initializes the page for setting a new password. Checks if the token for setting a new password is still valid.
     * If the user is already logged in, they are redirected to the home page.
     */
    @PostConstruct
    public void init() {

    }

    /**
     * Creates a FacesMessage to display if an event is fired in one of the injected services.
     *
     * @param feedback The feedback with details on what to display.
     */
    public void displayFeedback(@Observes @Any Feedback feedback) {

    }

    /**
     * Sets the user's password to the new value in {@code password}.
     */
    public void setUserPassword() {

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
     * @return The token.
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token The token to set.
     */
    public void setToken(String token) {
        this.token = token;
    }

}
