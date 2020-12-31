package tech.bugger.control.util;

import java.io.Serial;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import tech.bugger.business.internal.UserSession;
import tech.bugger.global.util.Log;

@Eager
@SessionScoped
public class UserSessionListener implements HttpSessionListener, Serializable {

    @Serial
    private static final long serialVersionUID = 6264897807639983293L;

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(UserSessionListener.class);

    /**
     * The {@link UserSession} to manage.
     */
    private UserSession userSession;

    /**
     * The current {@link ExternalContext}.
     */
    private ExternalContext ectx;

    /**
     * Initializes a new user session by setting a default locale.
     *
     * @param event The event being handled.
     */
    @Override
    public void sessionCreated(final HttpSessionEvent event) {
        userSession.setLocale(ectx.getRequestLocale());
    }

    /**
     * Sets a new {@link ExternalContext} for reference.
     *
     * @param ectx The {@link ExternalContext} to set.
     */
    @Inject
    public void setExternalContext(final ExternalContext ectx) {
        this.ectx = ectx;
    }

    /**
     * Sets a new {@link UserSession} that should be managed.
     *
     * @param userSession The new {@link UserSession} to set.
     */
    @Inject
    public void setUserSession(final UserSession userSession) {
        this.userSession = userSession;
    }

}
