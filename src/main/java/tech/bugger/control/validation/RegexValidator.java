package tech.bugger.control.validation;

import tech.bugger.business.util.RegistryKey;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Validator for regular expression inputs.
 */
@FacesValidator(value = "regexValidator", managed = true)
public class RegexValidator implements Validator<String> {

    /**
     * Resource bundle for feedback messages.
     */
    @Inject
    @RegistryKey("messages")
    private ResourceBundle resourceBundle;

    /**
     * Validates the given {@code regex}.
     *
     * @param fctx      The current {@link FacesContext}.
     * @param component The affected input {@link UIComponent}
     * @param regex     The regular expression to validate.
     * @throws ValidatorException If validation fails.
     */
    @Override
    public void validate(final FacesContext fctx, final UIComponent component, final String regex) {
        try {
            Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            FacesMessage message = new FacesMessage(resourceBundle.getString("validation.invalid_regex"));
            throw new ValidatorException(message, e);
        }
    }

}
