package tech.bugger.control.backing;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.service.ProfileService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

/**
 * Backing bean for the register page.
 */
@RequestScoped
@Named
public class RegisterBacker {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(RegisterBacker.class);

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
     * The user that is being created.
     */
    private User user;

    /**
     * Constructs a new register page backing bean with the necessary dependencies.
     *
     * @param authenticationService The authentication service to use.
     * @param profileService        The profile service to use.
     * @param session               The current {@link UserSession}.
     * @param fctx                  The current faces context.
     * @param feedbackEvent         The feedback event to use for user feedback.
     * @param messagesBundle        The resource bundle for feedback messages.
     */
    @Inject
    public RegisterBacker(final AuthenticationService authenticationService, final ProfileService profileService,
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
     * Initializes the register page. If the user is already logged in, redirects them to the home page.
     */
    @PostConstruct
    void init() {
        if (session.getUser() != null) {
            fctx.getApplication().getNavigationHandler().handleNavigation(fctx, null, "pretty:base");
        }

        user = new User();
        user.setPreferredLanguage(Language.of(session.getLocale()));
    }

    /**
     * Registers a new user. An e-mail to finalize the process is sent to their address if the provided data checks out.
     *
     * @return The site to redirect to.
     */
    public String register() {
        ExternalContext ectx = fctx.getExternalContext();
        URL currentUrl;
        try {
            currentUrl = new URL(((HttpServletRequest) ectx.getRequest()).getRequestURL().toString());
        } catch (MalformedURLException e) {
            throw new InternalError("URL is invalid.", e);
        }

        String path = String.format("%s://%s%s", currentUrl.getProtocol(), currentUrl.getAuthority(),
                ectx.getApplicationContextPath());
        if (profileService.createUser(user) && authenticationService.register(user, path)) {
            log.debug("Registration for user " + user + " successful.");
            feedbackEvent.fire(new Feedback(messagesBundle.getString("register.success"), Feedback.Type.INFO));
            return "pretty:base";
        }
        return null;
    }

    /**
     * Returns the current {@link User} to register.
     *
     * @return The current {@link User} to register.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets a new {@link User} to register.
     *
     * @param user The new {@link User} to register.
     */
    public void setUser(final User user) {
        this.user = user;
    }

}
