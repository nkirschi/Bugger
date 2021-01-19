package tech.bugger.control.conversion;

import java.util.Locale;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Simple converter for displaying locales in the format [languageTag] and converting those back into {@link Locale}s.
 */
@FacesConverter("localeConverter")
public class LocaleConverter implements Converter<Locale> {

    /**
     * Converts a language tag into a {@link Locale}.
     *
     * @param fctx        The current {@link FacesContext}.
     * @param comp        The relevant GUI component.
     * @param languageTag The language tag to convert to a {@link Locale}.
     * @return The parsed {@link Locale}.
     */
    @Override
    public Locale getAsObject(final FacesContext fctx, final UIComponent comp, final String languageTag) {
        return Locale.forLanguageTag(languageTag);
    }

    /**
     * Converts a {@link Locale} into a string representation of format [languageTag].
     *
     * @param fctx   The current {@link FacesContext}.
     * @param comp   The relevant GUI component.
     * @param locale The time locale to convert.
     * @return The {@code locale} in the format [languageTag].
     */
    @Override
    public String getAsString(final FacesContext fctx, final UIComponent comp, final Locale locale) {
        return locale.getLanguage();
    }

}
