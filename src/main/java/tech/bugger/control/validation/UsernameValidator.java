package tech.bugger.control.validation;

import tech.bugger.global.util.Log;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * Validator for username inputs.
 */
@FacesValidator(value = "usernameValidator")
public class UsernameValidator implements Validator<String> {

    private static Log log = Log.forClass(UsernameValidator.class);
    private static final String REGEX = ""; // TODO: RegEx hinschreiben

    /**
     * Validates the given {@code username}.
     *
     * @param fctx      The current {@link FacesContext}.
     * @param component The affected input {@link UIComponent}
     * @param username  The regular expression to validate.
     * @throws ValidatorException If validation fails.
     */
    @Override
    public void validate(FacesContext fctx, UIComponent component, String username) {
    }
}
