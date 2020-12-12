package tech.bugger.control.validation;

import tech.bugger.global.util.Log;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.time.LocalDate;

/**
 * Validator for time inputs.
 */
@FacesValidator(value = "dateValidator")
public class DateValidator implements Validator<LocalDate> {

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
    public void validate(FacesContext fctx, UIComponent component, LocalDate date) {

    }

}
