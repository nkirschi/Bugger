package tech.bugger.business.internal;

import com.ocpsoft.pretty.PrettyContext;
import com.ocpsoft.pretty.faces.config.mapping.UrlMapping;
import tech.bugger.business.util.Registry;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.enterprise.inject.spi.CDI;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serial;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Checks requests on user authentication.
 */
public class TrespassListener implements PhaseListener {

    @Serial
    private static final long serialVersionUID = -6409448769566271889L;

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(TrespassListener.class);

    /**
     * The current application settings.
     */
    private final ApplicationSettings applicationSettings;

    /**
     * The dependency registry to get resource bundles from.
     */
    private final Registry registry;

    /**
     * Constructs a new trespass listener.
     */
    public TrespassListener() {
        super();
        applicationSettings = CDI.current().select(ApplicationSettings.class).get();
        registry = CDI.current().select(Registry.class).get();
    }

    /**
     * Returns the ID of the phase this listener is hooking in.
     *
     * @return The phase ID for this listener.
     */
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }

    /**
     * Takes actions before the associated phase.
     *
     * @param event The notification that the processing for the phase {@link #getPhaseId()} is about to begin.
     */
    @Override
    public void beforePhase(final PhaseEvent event) {
    }

    /**
     * Takes actions after the associated phase.
     *
     * @param event The notification that the processing for the phase {@link #getPhaseId()} has just been completed.
     */
    @Override
    public void afterPhase(final PhaseEvent event) {
        FacesContext fctx = event.getFacesContext();
        ExternalContext ectx = fctx.getExternalContext();

        // Disallow unknown views.
        UIViewRoot viewRoot = fctx.getViewRoot();
        if (viewRoot == null) {
            redirectToErrorPage(ectx);
        }
        String viewId = viewRoot.getViewId();
        if (viewId == null) {
            redirectToErrorPage(ectx);
        }

        UserSession session = CDI.current().select(UserSession.class).get();
        User user = session != null ? session.getUser() : null;
        Locale locale = session != null ? session.getLocale() : ectx.getRequestLocale();

        log.debug("Session:" + session);
        log.debug(user == null ? "User null" : user.toString());
        log.debug("Locale:" + locale);

        if (viewId.endsWith("admin.xhtml")) {
            if (user == null) {
                redirectToLoginPage(fctx, locale);
            } else if (!user.isAdministrator()) {
                redirectToErrorPage(ectx);
            }
            return;
        }

        boolean guestReading = applicationSettings.getConfiguration().isGuestReading();
        if (user == null && (viewId.startsWith("/view/restr") || (viewId.startsWith("/view/auth") && !guestReading))) {
            redirectToLoginPage(fctx, locale);
        }
    }


    /**
     * Retrieves and returns the URL to redirect to after login.
     *
     * @return The URL to redirect to after login.
     */
    private String getRedirectUrl(final ExternalContext ectx) {
        String base = ectx.getApplicationContextPath();
        HttpServletRequest request = (HttpServletRequest) ectx.getRequest();
        UrlMapping mapping = PrettyContext.getCurrentInstance().getCurrentMapping();
        String uri = mapping != null ? mapping.getPattern() : request.getRequestURI();
        String queryString = request.getQueryString();

        return URLEncoder.encode(base + uri + (queryString == null ? "" : '?' + queryString), StandardCharsets.UTF_8);
    }

    /**
     * Redirects the user to the login page which will then redirect to the requested page. A message that login is
     * necessary for this action will be displayed.
     *
     * @param fctx   The {@link FacesContext} to use for redirection and messages.
     * @param locale The {@link Locale} to use for the message.
     */
    private void redirectToLoginPage(final FacesContext fctx, final Locale locale) {
        ResourceBundle resourceBundle = registry.getBundle("labels", locale);
        FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_WARN, resourceBundle.getString("login_to_continue"), null);
        fctx.addMessage(null, message);
        ExternalContext ectx = fctx.getExternalContext();
        ectx.getFlash().setKeepMessages(true);
        try {
            ectx.redirect(ectx.getRequestContextPath() + "/login?url=" + getRedirectUrl(ectx));
        } catch (IOException e) {
            throw new InternalError("Could not redirect to error page.", e);
        }
    }

    /**
     * Redirects the user to a "not found" page.
     *
     * @param ectx The {@link ExternalContext} to use for redirection.
     */
    private void redirectToErrorPage(final ExternalContext ectx) {
        try {
            ectx.redirect(ectx.getRequestContextPath() + "/error");
        } catch (IOException e) {
            throw new InternalError("Could not redirect to error page.", e);
        }
    }

}
