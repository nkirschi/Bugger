package tech.bugger.control.validation;

import tech.bugger.business.service.PostService;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.util.Log;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.servlet.http.Part;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Validator for file uploads.
 */
@FacesValidator(value = "fileValidator", managed = true)
public class FileValidator implements Validator<Part> {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(FileValidator.class);

    /**
     * The maximum file size in megabytes allowed for uploaded files.
     */
    private static final int MAX_FILE_SIZE = 10;

    /**
     * The post service for validating filenames.
     */
    private PostService postService;

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
    public void validate(FacesContext fctx, UIComponent component, Part part) {
        if (part.getSize() > MAX_FILE_SIZE * 1000 * 1000) {
            String message = MessageFormat.format(messagesBundle.getString("image_validator.file_size_too_large"),
                    MAX_FILE_SIZE);
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
