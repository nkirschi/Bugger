package tech.bugger.control.conversion;

import java.time.Duration;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Simple one-way converter for displaying time durations in the format [hours]:[minutes in hour].
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
     * Converts a {@link Duration} into a string representation of format [hours]:[minutes in hour].
     *
     * @param fctx     The current {@link FacesContext}.
     * @param comp     The relevant GUI component.
     * @param duration The time duration to convert.
     * @return The {@code duration} in the format [hours]:[minutes in hour].
     */
    @Override
    public String getAsString(final FacesContext fctx, final UIComponent comp, final Duration duration) {
        return String.format("%dh:%02dm", duration.toHours(), duration.toMinutesPart());
    }

}
