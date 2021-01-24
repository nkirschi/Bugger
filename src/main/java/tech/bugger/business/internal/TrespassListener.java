package tech.bugger.business.internal;

import com.ocpsoft.pretty.PrettyContext;
import java.io.IOException;
import java.io.Serial;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.enterprise.inject.spi.CDI;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import tech.bugger.business.util.Registry;
import tech.bugger.global.transfer.User;

/**
 * Checks requests on user authentication.
 */
public class TrespassListener implements PhaseListener {

    @Serial
    private static final long serialVersionUID = -6409448769566271889L;

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
            return;
        }
        String viewId = viewRoot.getViewId();
        if (viewId == null) {
            redirectToErrorPage(ectx);
            return;
        }

        // sometimes there strangely is no HTTP session and everything breaks
        if (ectx.getSession(true) == null) {
            // Should never happen...
            return;
        }

        UserSession session = CDI.current().select(UserSession.class).get();
        User user = session != null ? session.getUser() : null;
        Locale locale = session != null ? session.getLocale() : ectx.getRequestLocale();

        if (viewId.endsWith("admin.xhtml") && (user == null || !user.isAdministrator())) {
            redirectToErrorPage(ectx);
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
     * @param ectx The {@link ExternalContext} of the current request.
     * @return The URL to redirect to after login.
     */
    private String getRedirectUrl(final ExternalContext ectx) {
        PrettyContext pctx = PrettyContext.getCurrentInstance();
        String url = ectx.getRequestContextPath() + pctx.getRequestURL() + pctx.getRequestQueryString();
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
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
            throw new InternalError("Could not redirect to login page.", e);
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
