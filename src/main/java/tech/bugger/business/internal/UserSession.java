package tech.bugger.business.internal;

import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

/**
 * Internal bean holding details of the session of a user.
 */
@SessionScoped
@Named
public class UserSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 8943571923172893158L;
    private static final Log log = Log.forClass(UserSession.class);
    private User user;
    private Locale locale;

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
     * Gets the user.
     *
     * @return The user.
     */
    public User getUser() {
        // Until this is implemented, just return dummy topic.
        User user = new User();
        user.setId(1);
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user The user to set.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Gets the locale.
     *
     * @return The locale.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale.
     *
     * @param locale The locale to set.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

}
