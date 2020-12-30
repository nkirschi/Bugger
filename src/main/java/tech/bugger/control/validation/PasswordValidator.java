package tech.bugger.control.validation;

import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.util.Constants;

/**
 * Validator for password inputs.
 */
@FacesValidator(value = "passwordValidator", managed = true)
public class PasswordValidator implements Validator<String> {

    /**
     * The RegEx to use when validating a password's strength.
     */
    private static final Pattern REGEX = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])"
            + "(?=.*[!?+\\-*.:,;@#$%_äöüÄÖÜßẞ])"
            + ".{" + Constants.PASSWORD_MIN + "," + Constants.PASSWORD_MAX + "}$");

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new password validator with the necessary dependencies.
     *
     * @param messagesBundle The resource bundle for feedback messages.
     */
    @Inject
    public PasswordValidator(@RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.messagesBundle = messagesBundle;
    }

    /**
     * Validates the given {@code password}.
     *
     * @param fctx      The current {@link FacesContext}.
     * @param component The affected input {@link UIComponent}
     * @param password  The date to validate.
     * @throws ValidatorException If validation fails.
     */
    @Override
    public void validate(final FacesContext fctx, final UIComponent component, final String password) {
        if (!REGEX.matcher(password).matches()) {
            FacesMessage message = new FacesMessage(messagesBundle.getString("password_validator.password_too_weak"));
            throw new ValidatorException(message);
        }
    }

}
