package tech.bugger.control.validation;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.servlet.http.Part;
import tech.bugger.business.service.PostService;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.util.Constants;

/**
 * Validator for file uploads.
 */
@FacesValidator(value = "fileValidator", managed = true)
public class FileValidator implements Validator<Part> {

    /**
     * The post service for validating filenames.
     */
    private final PostService postService;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new file validator with the necessary dependencies.
     *
     * @param postService    The post service to use.
     * @param messagesBundle The resource bundle for feedback messages.
     */
    @Inject
    public FileValidator(final PostService postService, @RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.postService = postService;
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
        if (part.getSize() > Constants.MAX_ATTACHMENT_FILESIZE * Constants.MB_TO_BYTES) {
            String message = MessageFormat.format(messagesBundle.getString("image_validator.file_size_too_large"),
                    Constants.MAX_ATTACHMENT_FILESIZE);
            throw new ValidatorException(new FacesMessage(message));
        }
        if (!postService.isAttachmentNameValid(part.getSubmittedFileName())) {
            throw new ValidatorException(new FacesMessage(
                    messagesBundle.getString("file_validator.invalid_extension")));
        }

        try {
            part.getInputStream();
        } catch (IOException e) {
            throw new ValidatorException(new FacesMessage(
                    messagesBundle.getString("file_validator.file_corrupt")));
        }
    }

}
