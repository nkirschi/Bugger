package tech.bugger.control.conversion;

import tech.bugger.business.util.RegistryKey;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Simple one-way converter for displaying time instances with the system time zone.
 */
@FacesConverter(value = "offsetDateTimeConverter", managed = true)
public class OffsetDateTimeConverter implements Converter<OffsetDateTime> {

    /**
     * The resource bundle for UI labels.
     */
    private final ResourceBundle labelsBundle;

    /**
     * Constructs a new converter for {@link OffsetDateTime}s.
     * @param labelsBundle The resource bundle for UI labels.
     */
    @Inject
    public OffsetDateTimeConverter(@RegistryKey("labels") final ResourceBundle labelsBundle) {
        this.labelsBundle = labelsBundle;
    }

    /**
     * Converts a string into an {@link OffsetDateTime}. This operation is NOT supported.
     *
     * @throws UnsupportedOperationException as this converter is only meant for displaying.
     */
    @Override
    public OffsetDateTime getAsObject(final FacesContext fctx, final UIComponent comp, final String s) {
        throw new UnsupportedOperationException("This is an output converter only.");
    }

    /**
     * Converts an {@link OffsetDateTime} into a string representation of suitable format.
     *
     * @param fctx     The current {@link FacesContext}.
     * @param comp     The relevant GUI component.
     * @param dateTime The time duration to convert.
     * @return The {@code duration} in the format [days]:[hours in day] or [hours]:[minutes in hour].
     */
    @Override
    public String getAsString(final FacesContext fctx, final UIComponent comp, final OffsetDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(labelsBundle.getString("date_time_pattern"));
        return dateTime.atZoneSameInstant(ZoneId.systemDefault()).format(formatter);
    }

}
