package tech.bugger.control.backing;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.util.MarkdownHandler;
import tech.bugger.business.util.Registry;
import tech.bugger.global.transfer.Language;

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
     * The current help bundle containing all the texts for help.
     */
    private final ResourceBundle helpBundle;

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
     * @param session  The current user session.
     * @param registry The current registry.
     */
    @Inject
    public FooterBacker(final UserSession session, final Registry registry) {
        this.session = session;
        this.helpBundle = registry.getBundle("help", session);
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
     *
     * @return The site to redirect to or {@code null} to stay on the same page.
     */
    public String toggleHelp() {
        helpDisplayed = !helpDisplayed;
        return null;
    }

    /**
     * Returns the current help key for loading the help from the properties.
     *
     * @param helpKey The help key of the currently loaded page.
     * @return The current help key.
     */
    public String getHelp(final String helpKey) {
        String mainHelp = helpBundle.getString("main");
        return MarkdownHandler.toHtml(mainHelp + "\n\n" + helpBundle.getString(helpKey));
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
