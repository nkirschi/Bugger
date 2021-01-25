package tech.bugger.control.util;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import tech.bugger.business.util.Feedback;

/**
 * Observer for service feedback messages.
 */
public class FeedbackObserver {

    /**
     * The current {@link FacesContext}.
     */
    private final FacesContext fctx;

    /**
     * Constructs a new feedback observer.
     *
     * @param fctx A reference to the {@link FacesContext}.
     */
    @Inject
    public FeedbackObserver(final FacesContext fctx) {
        this.fctx = fctx;
    }

    /**
     * Creates a FacesMessage to display if an event is fired in one of the injected services.
     *
     * @param feedback The feedback with details on what to display.
     */
    public void displayFeedback(final @Observes @Any Feedback feedback) {
        FacesMessage.Severity severity = parseSeverity(feedback.getType());
        if (severity == null) {
            throw new IllegalArgumentException("Unknown severity '" + feedback.getType() + "'.");
        }
        fctx.addMessage(null, new FacesMessage(severity, feedback.getMessage(), null));
    }

    private FacesMessage.Severity parseSeverity(final Feedback.Type type) {
        return switch (type) {
            case ERROR -> FacesMessage.SEVERITY_ERROR;
            case WARNING -> FacesMessage.SEVERITY_WARN;
            case INFO -> FacesMessage.SEVERITY_INFO;
        };
    }

}
