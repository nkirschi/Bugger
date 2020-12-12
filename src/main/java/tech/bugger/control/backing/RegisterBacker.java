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
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Backing bean for the register page.
 */
@RequestScoped
@Named
public class RegisterBacker {

    private static final Log log = Log.forClass(RegisterBacker.class);

    private User user;

    @Inject
    private transient AuthenticationService authenticationService;

    @Inject
    private UserSession session;

    /**
     * Initializes the register page. If the user is already logged in, redirects them to the home page.
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
     * Registers a new user. An e-mail to finalize the process is sent to their address if the provided data checks
     * out.
     */
    public void register() {

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
