package tech.bugger.control.util;

import javax.faces.context.ExternalContext;
import javax.inject.Inject;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import tech.bugger.business.internal.UserSession;

@WebListener
public class UserSessionListener implements HttpSessionListener {

    /**
     * The {@link UserSession} to manage.
     */
    private final UserSession userSession;

    /**
     * The current {@link ExternalContext}.
     */
    private final ExternalContext ectx;

    /**
     * Constructs a new user session listener managing the current {@link UserSession}.
     *
     * @param userSession The {@link UserSession} to manage.
     * @param ectx        The current {@link ExternalContext}.
     */
    @Inject
    public UserSessionListener(final UserSession userSession, final ExternalContext ectx) {
        this.userSession = userSession;
        this.ectx = ectx;
    }

    /**
     * Initializes a new user session by setting a default locale.
     *
     * @param event The event being handled.
     */
    public void sessionCreated(final HttpSessionEvent event) {
        userSession.setLocale(ectx.getRequestLocale());
    }

    /**
     * Discards the currently managed user session.
     *
     * @param event The event being handled.
     */
    public void sessionDestroyed(final HttpSessionEvent event) {
    }

}
