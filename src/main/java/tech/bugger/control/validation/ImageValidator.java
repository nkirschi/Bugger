package tech.bugger.control.validation;

import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.util.Log;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.http.Part;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Validator for input images.
 */
@FacesValidator(value = "imageValidator", managed = true)
public class ImageValidator implements Validator<Part> {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(FileValidator.class);

    /**
     * The maximum file size in megabytes allowed for uploaded images.
     */
    private static final int MAX_FILE_SIZE = 2;

    /**
     * The minimum width images are required to have.
     */
    private static final int MIN_WIDTH = 128;

    /**
     * The minimum height images are required to have.
     */
    private static final int MIN_HEIGHT = 128;

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
        if (part.getSize() > MAX_FILE_SIZE * 1000 * 1000) {
            String message = MessageFormat.format(messagesBundle.getString("file_validator.file_size_too_large"),
                    MAX_FILE_SIZE);
            throw new ValidatorException(new FacesMessage(message));
        }

        try {
            BufferedImage img = ImageIO.read(part.getInputStream());
            if (img == null) {
                throw new ValidatorException(new FacesMessage(
                        messagesBundle.getString("image_validator.image_corrupt")));
            }
            if (img.getWidth() < MIN_WIDTH || img.getHeight() < MIN_HEIGHT) {
                String message = MessageFormat.format(messagesBundle.getString("image_validator.image_too_small"),
                        MIN_WIDTH, MIN_HEIGHT);
                throw new ValidatorException(new FacesMessage(message));
            }
        } catch (IOException e) {
            throw new ValidatorException(new FacesMessage(
                    messagesBundle.getString("image_validator.image_corrupt")));
        }
    }

}
