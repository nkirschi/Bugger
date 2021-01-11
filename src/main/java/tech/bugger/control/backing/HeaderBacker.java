package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.global.transfer.User;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;


/**
 * Backing Bean for the header.
 */
@RequestScoped
@Named
public class HeaderBacker implements Serializable {

    @Serial
    private static final long serialVersionUID = 7342292657804667855L;

    /**
     * The user behind the current UserSession.
     */
    private User user;

    /**
     * The current user session..
     */
    private final UserSession session;

    /**
     * {@code true} if the Menu should be displayed, {@code false} otherwise.
     */
    private boolean displayMenu;

    /**
     * The current {@link FacesContext} of the application.
     */
    private final FacesContext fctx;

    /**
     * Constructs a new header backing bean.
     *
     * @param session The currently active {@link UserSession}.
     * @param fctx    The current {@link FacesContext} of the application.
     */
    @Inject
    public HeaderBacker(final UserSession session, final FacesContext fctx) {
        this.session = session;
        this.fctx = fctx;
    }

    /**
     * Initializes the User for the header and makes sure the headerMenu is closed.
     */
    @PostConstruct
    void init() {
        user = session.getUser();
        displayMenu = Boolean.parseBoolean(fctx.getExternalContext().getRequestParameterMap().get("d"));
    }

    /**
     * Takes the user to the search page with the current {@code searchQuery} already typed in.
     *
     * @return The location to redirect to.
     */
    public String search() {
        return null;
    }

    /**
     * @return The user.
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user The user to set.
     */
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * Activates/Deactivates the menu.
     *
     * @return {@code null} to reload the page.
     */
    public String toggleMenu() {
        if (displayMenu) {
            closeMenu();
        } else {
            openMenu();
        }
        return null;
    }

    /**
     * Determine alert class for messages.
     *
     * @return The determined alert class.
     */
    public String determineAlertClass() {
        if (!fctx.getMessageList(null).isEmpty()) {
            FacesMessage.Severity maxSeverity = fctx.getMessageList().stream().map(FacesMessage::getSeverity)
                                                    .max(FacesMessage.Severity::compareTo).orElseThrow();
            if (maxSeverity.equals(FacesMessage.SEVERITY_ERROR)) {
                return " alert-danger";
            } else if (maxSeverity.equals(FacesMessage.SEVERITY_WARN)) {
                return " alert-warning";
            } else if (maxSeverity.equals(FacesMessage.SEVERITY_INFO)) {
                return " alert-success";
            } else {
                return " alert-primary";
            }
        }
        return "";
    }

    /**
     * Returns the current year in the gregorian calendar.
     *
     * @return The current year as integer.
     */
    public int getCurrentYear() {
        return LocalDate.now().getYear();
    }

    /**
     * @return {@code true} if the Menu should be displayed, {@code false} otherwise.
     */
    public boolean isDisplayMenu() {
        return displayMenu;
    }

    private void closeMenu() {
        displayMenu = false;
    }

    private void openMenu() {
        displayMenu = true;
    }

}
