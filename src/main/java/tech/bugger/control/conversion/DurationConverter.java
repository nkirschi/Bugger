package tech.bugger.control.conversion;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.time.Duration;

/**
 * Simple one-way converter for displaying time durations.
 */
@FacesConverter("durationConverter")
public class DurationConverter implements Converter<Duration> {

    /**
     * Converts a string into a {@link Duration}. This operation is NOT supported.
     *
     * @throws UnsupportedOperationException as this converter is only meant for displaying.
     */
    @Override
    public Duration getAsObject(final FacesContext fctx, final UIComponent comp, final String s) {
        throw new UnsupportedOperationException("This is an output converter only.");
    }

    /**
     * Converts a {@link Duration} into a string representation of suitable format.
     *
     * @param fctx     The current {@link FacesContext}.
     * @param comp     The relevant GUI component.
     * @param duration The time duration to convert.
     * @return The {@code duration} in the format [days]:[hours in day] or [hours]:[minutes in hour].
     */
    @Override
    public String getAsString(final FacesContext fctx, final UIComponent comp, final Duration duration) {
        if (duration.toDays() >= 1) {
            return String.format("%dd:%02dh", duration.toDays(), duration.toHoursPart());
        } else {
            return String.format("%dh:%02dm", duration.toHours(), duration.toMinutesPart());
        }
    }

}
