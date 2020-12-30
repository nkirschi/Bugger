package tech.bugger.global.transfer;

import java.util.Locale;

/**
 * Enumeration of supported languages.
 */
public enum Language {

    /**
     * The german language.
     */
    GERMAN,

    /**
     * The english language.
     */
    ENGLISH;

    /**
     * Returns the appropriate Language for the given locale with {@link #ENGLISH} as fallback.
     *
     * @param locale The locale to translate into the {@link Language} enum.
     * @return The appropriate {@link Language} to choose.
     */
    public static Language getLanguage(final Locale locale) {
        if (locale.equals(Locale.GERMAN)) {
            return GERMAN;
        } else {
            return ENGLISH;
        }
    }

}
