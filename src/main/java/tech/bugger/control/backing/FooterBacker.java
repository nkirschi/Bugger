package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.global.transfer.Language;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

/**
 * Backing Bean for the footer.
 */
@RequestScoped
@Named
public class FooterBacker implements Serializable {

    @Serial
    private static final long serialVersionUID = 4849101262721339096L;

    /**
     * The current UserSession.
     */
    private UserSession session;

    /**
     * The current selected language.
     */
    private Language language;

    /**
     * Whether the help is displayed for the current page.
     */
    private boolean helpDisplayed;

    @Inject
    public FooterBacker(final UserSession session) {
        this.session = session;
        // TODO this is ugly. Maybe consider storing Locale#getLanguageTag in DB, reversable via Locale#forLanguageTag
        if (Locale.ENGLISH.getLanguage().equals(session.getLocale().getLanguage())) {
            language = Language.ENGLISH;
        } else if (Locale.GERMAN.getLanguage().equals(session.getLocale().getLanguage())) {
            language = Language.GERMAN;
        }
    }

    /**
     * Changes language. The change is effective for the whole session.
     */
    public void changeLanguage() {
        session.setLocale(switch (language) {
            case ENGLISH -> Locale.ENGLISH;
            case GERMAN -> Locale.GERMAN;
        });
    }

    /**
     * Toggles the help popup.
     */
    public void toggleHelp() {
        helpDisplayed = !helpDisplayed;
    }

    public Language[] getAvailableLanguages() {
        return Language.values();
    }

    /**
     * @return The language.
     */
    public Language getLanguage() {
        return language;
    }

    /**
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
