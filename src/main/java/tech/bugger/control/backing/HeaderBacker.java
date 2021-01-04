package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.global.transfer.User;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;


/**
 * Backing Bean for the header.
 */
@SessionScoped
@Named
public class HeaderBacker implements Serializable {
    @Serial
    private static final long serialVersionUID = 7342292657804667855L;

    /**
     * The user behind the current UserSession.
     */
    private User user;

    /**
     * The current UserSession.
     */
    @Inject
    private UserSession session;

    /**
     * {@code true} if the Menu should be displayed, {@code false} otherwise.
     */
    private boolean displayMenu;

    /**
     * Initializes the User for the header and makes sure the headerMenu is closed.
     */
    @PostConstruct
    void init() {
        user = session.getUser();
        closeMenu();
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
     * @return {@code Null} to reload the page.
     */
    final public String toggleMenu() {
        if (displayMenu) {
            closeMenu();
        } else {
            openMenu();
        }
        return null;
    }

    /**
     * @return {@code true} if the Menu should be displayed, {@code false} otherwise.
     */
    final public boolean isDisplayMenu() {
        return displayMenu;
    }

    private void closeMenu() {
        displayMenu = false;
    }

    private void openMenu() {
        displayMenu = true;
    }
}
