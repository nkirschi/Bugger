package tech.bugger.control.validation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.Part;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.util.Constants;

/**
 * Validator for input images.
 */
@FacesValidator(value = "imageValidator", managed = true)
public class ImageValidator implements Validator<Part> {

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new file validator with the necessary dependencies.
     *
     * @param messagesBundle The resource bundle for feedback messages.
     */
    @Inject
    public ImageValidator(@RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.messagesBundle = messagesBundle;
    }

    /**
     * Validates the given {@code part}.
     *
     * @param fctx      The current {@link FacesContext}.
     * @param component The affected input {@link UIComponent}
     * @param part      The file to validate.
     * @throws ValidatorException If validation fails.
     */
    @Override
    public void validate(final FacesContext fctx, final UIComponent component, final Part part) {
        if (part.getSize() > Constants.MAX_AVATAR_FILESIZE * Constants.MB_TO_BYTES) {
            String message = MessageFormat.format(messagesBundle.getString("file_validator.file_size_too_large"),
                    Constants.MAX_AVATAR_FILESIZE);
            throw new ValidatorException(new FacesMessage(message));
        }

        try {
            BufferedImage img = ImageIO.read(part.getInputStream());
            if (img == null) {
                throw new ValidatorException(new FacesMessage(
                        messagesBundle.getString("image_validator.image_corrupt")));
            }
            if (img.getWidth() < Constants.MIN_IMAGE_WIDTH || img.getHeight() < Constants.MIN_IMAGE_HEIGHT) {
                String message = MessageFormat.format(messagesBundle.getString("image_validator.image_too_small"),
                        Constants.MIN_IMAGE_WIDTH, Constants.MIN_IMAGE_HEIGHT);
                throw new ValidatorException(new FacesMessage(message));
            }
        } catch (IOException e) {
            throw new ValidatorException(new FacesMessage(
                    messagesBundle.getString("image_validator.image_corrupt")));
        }
    }

}
