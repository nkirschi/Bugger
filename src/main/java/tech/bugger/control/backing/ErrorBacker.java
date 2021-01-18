package tech.bugger.control.backing;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.UserSession;
import tech.bugger.global.util.Log;

/**
 * Backing Bean for the error page.
 */
@RequestScoped
@Named
public class ErrorBacker {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(ErrorBacker.class);

    /**
     * The current admin email.
     */
    private String adminMail;

    /**
     * The current title.
     */
    private String title;

    /**
     * The current description.
     */
    private String description;

    /**
     * The current stack trace.
     */
    private String stackTrace;

    /**
     * The current user session.
     */
    @Inject
    private UserSession session;

    /**
     * The current faces context.
     */
    @Inject
    private FacesContext fctx;

    /**
     * Initializes the error page.
     */
    @PostConstruct
    public void init() {
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
    public void setAdminMail(final String adminMail) {
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
    public void setTitle(final String title) {
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
    public void setDescription(final String description) {
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
    public void setStackTrace(final String stackTrace) {
        this.stackTrace = stackTrace;
    }

}
