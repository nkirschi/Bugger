package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Backing Bean for the error page.
 */
@RequestScoped
@Named
public class ErrorBacker {

    private static final Log log = Log.forClass(ErrorBacker.class);

    private String adminMail;
    private String title;
    private String description;
    private String stackTrace;

    @Inject
    private UserSession session;

    @Inject
    private FacesContext fctx;

    /**
     * Initializes the error page.
     */
    @PostConstruct
    public void init() {

    }

    /**
     * Creates a FacesMessage to display if an event is fired in one of the injected services.
     *
     * @param feedback The feedback with details on what to display.
     */
    public void displayFeedback(@Observes @Any Feedback feedback) {
    }

    /**
     * @return The adminMail.
     */
    public String getAdminMail() {
        return adminMail;
    }

    /**
     * @param adminMail The adminMail to set.
     */
    public void setAdminMail(String adminMail) {
        this.adminMail = adminMail;
    }

    /**
     * @return The title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return The stackTrace.
     */
    public String getStackTrace() {
        return stackTrace;
    }

    /**
     * @param stackTrace The stackTrace to set.
     */
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

}
