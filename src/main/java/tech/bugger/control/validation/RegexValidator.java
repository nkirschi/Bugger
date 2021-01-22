package tech.bugger.control.validation;

import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import tech.bugger.business.util.RegistryKey;

/**
 * Validator for regular expression inputs.
 */
@FacesValidator(value = "regexValidator", managed = true)
public class RegexValidator implements Validator<String> {

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new regex validator with the necessary dependencies.
     *
     * @param messagesBundle The resource bundle for feedback messages.
     */
    @Inject
    public RegexValidator(@RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.messagesBundle = messagesBundle;
    }

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
            FacesMessage message = new FacesMessage(messagesBundle.getString("invalid_regex"));
            throw new ValidatorException(message, e);
        }
    }

}
