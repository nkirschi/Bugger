package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.global.transfer.Language;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

/**
 * Backing Bean for the footer.
 */
@SessionScoped
@Named
public class FooterBacker implements Serializable {

    @Serial
    private static final long serialVersionUID = 4849101262721339096L;

    /**
     * The current UserSession.
     */
    private final UserSession session;

    /**
     * The currently selected language.
     */
    private Language language;

    /**
     * Whether the help is displayed for the current page.
     */
    private boolean helpDisplayed;

    /**
     * Constructs a new footer backing bean with the necessary dependencies.
     *
     * @param session The current user session.
     */
    @Inject
    public FooterBacker(final UserSession session) {
        this.session = session;
        language = Language.of(session.getLocale());
    }

    /**
     * Changes language. The change is effective for the whole session.
     */
    public void changeLanguage() {
        session.setLocale(
                switch (language) {
                    case ENGLISH -> Locale.ENGLISH;
                    case GERMAN -> Locale.GERMAN;
                }
        );
    }

    /**
     * Toggles the help popup.
     */
    public void toggleHelp() {
        helpDisplayed = !helpDisplayed;
    }

    /**
     * Yields the available languages to select.
     *
     * @return An array of supported website languages.
     */
    public Language[] getAvailableLanguages() {
        return Language.values();
    }

    /**
     * Returns the currently selected language.
     *
     * @return The selected language.
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Sets the currently selected language.
     *
     * @param language The language to set.
     */
    public void setLanguage(final Language language) {
        this.language = language;
    }

    /**
     * Returns whether the help is displayed for the current page.
     *
     * @return {@code true} iff the help popup is displayed.
     */
    public boolean isHelpDisplayed() {
        return helpDisplayed;
    }

    /**
     * Sets whether the help is displayed for the current page.
     *
     * @param helpDisplayed Whether the help popup shall be displayed.
     */
    public void setHelpDisplayed(final boolean helpDisplayed) {
        this.helpDisplayed = helpDisplayed;
    }

}
