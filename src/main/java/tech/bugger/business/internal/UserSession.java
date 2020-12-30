package tech.bugger.business.internal;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

/**
 * Internal bean holding details of the session of a user.
 */
@SessionScoped
@Named
public class UserSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 8943571923172893158L;

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(UserSession.class);

    /**
     * The currently logged in user.
     */
    private User user;

    /**
     * The currently selected locale.
     */
    private Locale locale;

    /**
     * Initializes this user session by setting a default locale.
     */
    @PostConstruct
    public void init() {
        locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
    }

    /**
     * Invalidates the session.
     */
    public void invalidateSession() {
    }

    /**
     * Gets the user or {@code null} if he is not logged in.
     *
     * @return The user or {@code null} if he is not logged in.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the current user.
     *
     * @param user The user to set.
     */
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * Gets the currently preferred locale.
     *
     * @return The locale.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the preferred locale.
     *
     * @param locale The preferred locale to set.
     */
    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

}
