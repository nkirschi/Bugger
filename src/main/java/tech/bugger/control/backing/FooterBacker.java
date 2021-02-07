package tech.bugger.control.backing;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.util.MarkdownHandler;
import tech.bugger.business.util.Registry;
import tech.bugger.global.util.Constants;

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
    private final UserSession session;

    /**
     * The current registry which to retrieve resource bundles from.
     */
    private final Registry registry;

    /**
     * The currently selected language.
     */
    private Locale language;

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
    public FooterBacker(final UserSession session,
                        final Registry registry) {
        this.session = session;
        this.registry = registry;
        language = session.getLocale();
    }

    /**
     * Reloads the language. The change is effective for the whole session.
     */
    public void changeLanguage() {
        session.setLocale(language);
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
     * @param helpKey    The help key of the currently loaded page.
     * @param helpSuffix The help suffix for the additional, role-specific help text.
     * @return The current help key.
     */
    public String getHelp(final String helpKey, final String helpSuffix) {
        String extra = "";
        if (helpSuffix != null && !helpSuffix.isEmpty()) {
            extra = "\n\n" + MarkdownHandler.toHtml(registry.getBundle("help", session.getLocale())
                    .getString(helpKey + helpSuffix));
        }
        return MarkdownHandler.toHtml(registry.getBundle("help", session.getLocale()).getString(helpKey)) + extra;
    }

    /**
     * Yields the available languages to select.
     *
     * @return An array of supported website languages.
     */
    public Locale[] getAvailableLanguages() {
        return Constants.LANGUAGES;
    }

    /**
     * Returns the currently selected language.
     *
     * @return The selected language.
     */
    public Locale getLanguage() {
        return session.getLocale();
    }

    /**
     * Sets the currently selected language.
     *
     * @param language The language to set.
     */
    public void setLanguage(final Locale language) {
        this.language = language;
        session.setLocale(language);
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
