package tech.bugger.control.conversion;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Simple one-way converter for displaying attachment names.
 */
@FacesConverter("attachmentNameConverter")
public class AttachmentNameConverter implements Converter<String> {

    /**
     * The length over which a file name is trimmed.
     */
    public static final int NAME_TRIM_LENGTH = 24;

    /**
     * The length over which a file extension is trimmed.
     */
    public static final int EXT_TRIM_LENGTH = 8;

    /**
     * The trim dots.
     */
    public static final String DOTS = "[...]";

    /**
     * Converts a string into an attachment name. This operation does not make sense and thus is NOT supported.
     *
     * @throws UnsupportedOperationException as this converter is only meant for displaying.
     */
    @Override
    public String getAsObject(final FacesContext fctx, final UIComponent comp, final String s) {
        throw new UnsupportedOperationException("This is an output converter only.");
    }

    /**
     * Converts an attachment name into a string representation of suitable format.
     *
     * @param fctx           The current {@link FacesContext}.
     * @param comp           The relevant GUI component.
     * @param attachmentName The attachment name to convert.
     * @return The attachment name, possibly shortened.
     */
    @Override
    public String getAsString(final FacesContext fctx, final UIComponent comp, final String attachmentName) {
        int length = attachmentName.length();
        if (length > NAME_TRIM_LENGTH + EXT_TRIM_LENGTH) {
            int dotIndex = attachmentName.lastIndexOf('.');
            if (dotIndex == -1) {
                return attachmentName.substring(0, NAME_TRIM_LENGTH + EXT_TRIM_LENGTH) + DOTS;
            }
            String prefix = attachmentName.substring(0, dotIndex);
            String suffix = attachmentName.substring(dotIndex);
            if (prefix.length() > NAME_TRIM_LENGTH) {
                prefix = prefix.substring(0, NAME_TRIM_LENGTH) + DOTS;
            }
            if (suffix.length() > EXT_TRIM_LENGTH) {
                suffix = suffix.substring(0, EXT_TRIM_LENGTH) + DOTS;
            }
            return prefix + suffix;
        }
        return attachmentName;
    }

}
