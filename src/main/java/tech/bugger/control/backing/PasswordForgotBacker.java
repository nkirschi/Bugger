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
 * Backing Bean for the password forgot page.
 */
@RequestScoped
@Named
public class PasswordForgotBacker {

    private static final Log log = Log.forClass(PasswordForgotBacker.class);

    private User user;

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private UserSession session;

    @Inject
    private FacesContext fctx;

    /**
     * Initializes the password forgot page. If the user is already logged in, they are redirected to the home page.
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
     * Checks if username and email provided match up. If so, an e-mail with instructions on how to set a new password
     * is sent.
     */
    public void forgotPassword() {
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
