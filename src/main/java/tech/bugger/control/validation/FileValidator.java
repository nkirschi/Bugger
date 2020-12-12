package tech.bugger.control.validation;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.global.util.Log;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.servlet.http.Part;

/**
 * Validator for file uploads.
 */
@FacesValidator(value = "fileValidator")
public class FileValidator implements Validator<Part> {

    private static final Log log = Log.forClass(FileValidator.class);
    private static final int MAX_FILESIZE = 2; // in MB

    @Inject
    private ApplicationSettings applicationSettings;

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

    }
}
