package tech.bugger.control.validation;

import java.time.LocalDate;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import tech.bugger.global.util.Log;

/**
 * Validator for time inputs.
 */
@FacesValidator(value = "dateValidator")
public class DateValidator implements Validator<LocalDate> {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static Log log = Log.forClass(DateValidator.class);

    /**
     * Validates the given {@code date}.
     *
     * @param fctx      The current {@link FacesContext}.
     * @param component The affected input {@link UIComponent}
     * @param date      The date to validate.
     * @throws ValidatorException If validation fails.
     */
    @Override
    public void validate(final FacesContext fctx, final UIComponent component, final LocalDate date) {
        throw new ValidatorException(new FacesMessage("Implement me! I am only here for CheckStyle purposes"));
    }

}
