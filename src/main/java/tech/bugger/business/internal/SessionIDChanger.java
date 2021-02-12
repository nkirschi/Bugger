package tech.bugger.business.internal;

import java.io.Serial;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

/**
 * Phase listener changing the current session ID on every request to prevent session hijacking.
 */
public class SessionIDChanger implements PhaseListener {

    @Serial
    private static final long serialVersionUID = -3371104134499525455L;

    /**
     * Performs nothing.
     *
     * @param phaseEvent The event fired before the phase {@link #getPhaseId()}.
     */
    @Override
    public void beforePhase(final PhaseEvent phaseEvent) {
        // nop
    }

    /**
     * Changes the HTTP session ID upon request initialization, except for AJAX request as this yields unexpected and
     * unpredictable behavior.
     *
     * @param phaseEvent The event fired after the phase {@link #getPhaseId()}.
     */
    @Override
    public void afterPhase(final PhaseEvent phaseEvent) {
        FacesContext fctx = phaseEvent.getFacesContext();
        if (fctx.getPartialViewContext().isAjaxRequest()) {
            return;
        }

        ExternalContext ectx = fctx.getExternalContext();
        if (ectx.getSession(false) != null) {
            ((HttpServletRequest) ectx.getRequest()).changeSessionId();
        }
    }

    /**
     * Returns the phase before rendering the response.
     *
     * @return {@link PhaseId#RESTORE_VIEW}.
     */
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }

}
