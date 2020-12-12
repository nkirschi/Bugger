package tech.bugger.control.validation;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.global.util.Log;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

/**
 * Validator for e-mail address inputs.
 */
@FacesValidator(value = "emailValidator")
public class EmailValidator implements Validator<String> {

    private static Log log = Log.forClass(EmailValidator.class);

    @Inject
    private ApplicationSettings applicationSettings;

    /**
     * Validates the given {@code email}.
     *
     * @param fctx      The current {@link FacesContext}.
     * @param component The affected input {@link UIComponent}
     * @param email     The email address to validate.
     * @throws ValidatorException If validation fails.
     */
    @Override
    public void validate(FacesContext fctx, UIComponent component, String email) {

    }

}
