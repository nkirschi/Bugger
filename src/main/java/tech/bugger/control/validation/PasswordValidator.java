package tech.bugger.control.validation;

import tech.bugger.global.util.Log;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * Validator for password inputs.
 */
@FacesValidator(value = "passwordValidator")
public class PasswordValidator implements Validator<String> {

    private static Log log = Log.forClass(PasswordValidator.class);
    private static final String REGEX = null;

    /**
     * Validates the given {@code password}.
     *
     * @param fctx      The current {@link FacesContext}.
     * @param component The affected input {@link UIComponent}
     * @param password  The date to validate.
     * @throws ValidatorException If validation fails.
     */
    @Override
    public void validate(FacesContext fctx, UIComponent component, String password) {
    }
}
