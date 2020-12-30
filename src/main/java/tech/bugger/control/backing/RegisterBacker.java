package tech.bugger.control.backing;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.service.ProfileService;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
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
     * The current external context.
     */
    private final ExternalContext externalContext;

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
     * @param externalContext       The current external context.
     */
    @Inject
    public RegisterBacker(final AuthenticationService authenticationService, final ProfileService profileService,
                          final UserSession session, final ExternalContext externalContext) {
        this.authenticationService = authenticationService;
        this.profileService = profileService;
        this.session = session;
        this.externalContext = externalContext;
    }

    /**
     * Initializes the register page. If the user is already logged in, redirects them to the home page.
     */
    @PostConstruct
    public void init() {
        if (session.getUser() != null) {
            try {
                externalContext.redirect("home.xhtml");
            } catch (IOException e) {
                throw new InternalError("Error while redirecting.", e);
            }
        }

        user = new User(null, "", "", "", "", "", "", "", new Lazy<>(new byte[0]), new byte[0], "",
                Language.getLanguage(session.getLocale()), User.ProfileVisibility.FULL, null, null, false);
    }

    /**
     * Registers a new user. An e-mail to finalize the process is sent to their address if the provided data checks out.
     */
    public void register() {
        profileService.createUser(user);
        authenticationService.register(user);
    }

    /**
     * Sets a new username.
     *
     * @param username The new username.
     */
    public void setUsername(final String username) {
        user.setUsername(username);
    }

    /**
     * Returns the current username.
     *
     * @return The current username.
     */
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Sets a new e-mail address.
     *
     * @param emailAddress The new e-mail address.
     */
    public void setEmailAddress(final String emailAddress) {
        user.setEmailAddress(emailAddress);
    }

    /**
     * Returns the current e-mail address.
     *
     * @return The current e-mail address.
     */
    public String getEmailAddress() {
        return user.getEmailAddress();
    }

    /**
     * Sets a new first name.
     *
     * @param firstName The new first name.
     */
    public void setFirstName(final String firstName) {
        user.setFirstName(firstName);
    }

    /**
     * Returns the current first name.
     *
     * @return The current first name.
     */
    public String getFirstName() {
        return user.getFirstName();
    }

    /**
     * Sets a new last name.
     *
     * @param lastName The new last name.
     */
    public void setLastName(final String lastName) {
        user.setLastName(lastName);
    }

    /**
     * Returns the current last name.
     *
     * @return The current last name.
     */
    public String getLastName() {
        return user.getLastName();
    }

}
