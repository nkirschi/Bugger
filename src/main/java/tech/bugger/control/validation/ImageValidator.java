package tech.bugger.control.validation;

import tech.bugger.global.util.Log;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;

/**
 * Validator for input images.
 */
@FacesValidator(value = "imageValidator")
public class ImageValidator implements Validator<Part> {

    private static Log log = Log.forClass(ImageValidator.class);
    private static final int MAX_SIZE = 2 * 1024 * 1024;

    /**
     * Validates the given {@code part}.
     *
     * @param fctx      The current {@link FacesContext}.
     * @param component The affected input {@link UIComponent}
     * @param part      The image to validate.
     * @throws ValidatorException If validation fails.
     */
    @Override
    public void validate(FacesContext fctx, UIComponent component, Part part) {
    }

}
