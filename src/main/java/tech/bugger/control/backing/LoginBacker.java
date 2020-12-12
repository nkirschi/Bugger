package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Backing Bean for the login page.
 */
@RequestScoped
@Named
public class LoginBacker {

    private static final Log log = Log.forClass(LoginBacker.class);

    private User user;
    private String redirectURL;

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private UserSession session;

    @Inject
    private FacesContext fctx;

    /**
     * Initializes the login page. If a user is already logged in, they are redirected to the home page.
     */
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
     * Logs in the user and returns them to the page they were on before. If there is no such page they are redirected
     * to the home page instead. Also changes the language to the user's preferred language.
     *
     * @return the name of the page to be redirected to.
     */
    public String login() {
        return null;
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
    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
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

}