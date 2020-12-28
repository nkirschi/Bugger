package tech.bugger.business.internal;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import java.io.Serial;

/**
 * Checks requests on user authentication.
 */
public class TrespassListener implements PhaseListener {
    @Serial
    private static final long serialVersionUID = 1L;

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
    public void beforePhase(PhaseEvent event) {
    }

    /**
     * Takes actions after the associated phase.
     *
     * @param event The notification that the processing for the phase {@link #getPhaseId()} has just been completed.
     */
    @Override
    public void afterPhase(PhaseEvent event) {

    }
}