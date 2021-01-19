package tech.bugger.business.internal;

import com.ocpsoft.pretty.PrettyContext;
import com.ocpsoft.pretty.faces.config.mapping.UrlMapping;
import tech.bugger.business.util.Registry;
import tech.bugger.global.transfer.User;

import javax.enterprise.inject.spi.CDI;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serial;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
     * The current user session.
     */
    private final UserSession session;

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
        session = CDI.current().select(UserSession.class).get();
        registry = CDI.current().select(Registry.class).get();
        System.out.println("Constructing phase listener");
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
        FacesContext fctx = event.getFacesContext();
        UIViewRoot viewRoot = fctx.getViewRoot();
        if (viewRoot == null) {
            redirectToErrorPage(fctx);
        }

        User user = session != null ? session.getUser() : null;
        String viewId = viewRoot.getViewId();

        if (viewId.endsWith("admin.xhtml")) {
            if (user == null) {
                redirectToLoginPage(fctx);
            } else if (!user.isAdministrator()) {
                redirectToErrorPage(fctx);
            }
            return;
        }

        if (!viewId.startsWith("/view/public")
                && (user == null || !applicationSettings.getConfiguration().isGuestReading())) {
            redirectToLoginPage(fctx);
        }
    }

    /**
     * Takes actions after the associated phase.
     *
     * @param event The notification that the processing for the phase {@link #getPhaseId()} has just been completed.
     */
    @Override
    public void afterPhase(final PhaseEvent event) {
    }


    /**
     * Retrieves and returns the URL to redirect to after login.
     *
     * @return The URL to redirect to after login.
     */
    private String getRedirectUrl(ExternalContext ectx) {
        String base = ectx.getApplicationContextPath();
        HttpServletRequest request = (HttpServletRequest) ectx.getRequest();
        UrlMapping mapping = PrettyContext.getCurrentInstance().getCurrentMapping();
        String uri = mapping != null ? mapping.getPattern() : request.getRequestURI();
        String queryString = request.getQueryString();

        return URLEncoder.encode(base + uri + (queryString == null ? "" : '?' + queryString), StandardCharsets.UTF_8);
    }

    private void redirectToLoginPage(FacesContext fctx) {
        // TODO
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "GAGAGAGA.", null);
        fctx.addMessage(null, message);
        try {
            ExternalContext ectx = fctx.getExternalContext();
            ectx.getFlash().setKeepMessages(true);
            ectx.redirect(ectx.getRequestContextPath() + "/login?url=" + getRedirectUrl(ectx));
        } catch (IOException e) {
            throw new InternalError("Could not redirect to error page.", e);
        }
    }

    private void redirectToErrorPage(FacesContext fctx) {
        try {
            ExternalContext ectx = fctx.getExternalContext();
            ectx.redirect(ectx.getRequestContextPath() + "/error");
        } catch (IOException e) {
            throw new InternalError("Could not redirect to error page.", e);
        }
    }

}
