package tech.bugger.business.internal;

import com.ocpsoft.pretty.PrettyContext;
import com.ocpsoft.pretty.faces.config.mapping.UrlMapping;
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
     * Constructs a new trespass listener.
     */
    public TrespassListener() {
        super();
        applicationSettings = CDI.current().select(ApplicationSettings.class).get();
        session = CDI.current().select(UserSession.class).get();
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
        ExternalContext ctx = fctx.getExternalContext();

        // Is the user on the login page?
        boolean publicArea = false;

        User user = session.getUser();
        UIViewRoot viewRoot = fctx.getViewRoot();
        if (viewRoot != null) {
            String viewId = viewRoot.getViewId();
            if (viewId.endsWith("admin.xhtml")) {
                if (user == null) {
                    redirectToLoginPage(fctx);
                } else if (!user.isAdministrator()) {
                    redirectToErrorPage(fctx);
                }
                return;
            }

            if (user == null && !viewId.startsWith("/view/public")) {
                redirectToLoginPage(fctx);
            }




            System.out.println(viewId);




            publicArea = viewId.endsWith("login.xhtml");

            // starts with /view/auth/ --> ok
            // otherwise: login, show message "You have to login to perform this action"
        }

        // Is the user already authenticated?
        boolean loggedIn = sessionMap.containsKey("loggedin");

        if (!publicArea && !loggedIn) {
            // Illegal request.

            /*
            // Define error message.
            FacesMessage fmsg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Log in first.", null);
            fctx.addMessage(null, fmsg);

            // Let the faces messages of fctx also live in the next request. The
            // flash scope lives exactly for two subsequent requests.
            ctx.getFlash().setKeepMessages(true);

            // Redirect to the login page.
            NavigationHandler nav = fctx.getApplication().getNavigationHandler();
            nav.handleNavigation(fctx, null, "login.xhtml?faces-redirect=true");

            // Stop the life cycle for this request.
            fctx.responseComplete();
            */
        }
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
            ectx.redirect("/login?url=" + getRedirectUrl(ectx));
        } catch (IOException e) {
            throw new InternalError("Could not redirect to error page.", e);
        }
    }

    private void redirectToErrorPage(FacesContext fctx) {
        try {
            fctx.getExternalContext().redirect("/error");
        } catch (IOException e) {
            throw new InternalError("Could not redirect to error page.", e);
        }
    }

}
