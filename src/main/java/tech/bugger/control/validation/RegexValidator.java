package tech.bugger.control.validation;

import tech.bugger.global.util.Log;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * Validator for regular expression inputs.
 */
@FacesValidator(value = "regexValidator")
public class RegexValidator implements Validator<String> {

    private static Log log = Log.forClass(RegexValidator.class);

    /**
     * Validates the given {@code regex}.
     *
     * @param fctx      The current {@link FacesContext}.
     * @param component The affected input {@link UIComponent}
     * @param regex     The regular expression to validate.
     * @throws ValidatorException If validation fails.
     */
    @Override
    public void validate(FacesContext fctx, UIComponent component, String regex) {
    }
}
