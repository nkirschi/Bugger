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

/**
 * Validator for username inputs.
 */
@FacesValidator(value = "usernameRegexValidator", managed = true)
public class UsernameRegexValidator implements Validator<String> {

    /**
     * The RegEx to use when validating a username.
     */
    private static final Pattern REGEX = Pattern.compile("^([a-zA-Z0-9_äöüÄÖÜ]){4,16}$");

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new username validator with the necessary dependencies.
     *
     * @param messagesBundle The resource bundle for feedback messages.
     */
    @Inject
    public UsernameRegexValidator(@RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.messagesBundle = messagesBundle;
    }

    /**
     * Validates the given {@code username}'s validity in terms of matching the {@link #REGEX}.
     *
     * @param fctx      The current {@link FacesContext}.
     * @param component The affected input {@link UIComponent}
     * @param username  The regular expression to validate.
     * @throws ValidatorException If validation fails.
     */
    @Override
    public void validate(final FacesContext fctx, final UIComponent component, final String username) {
        if (!REGEX.matcher(username).matches()) {
            FacesMessage message = new FacesMessage(messagesBundle.getString("username_validator.format_wrong"));
            throw new ValidatorException(message);
        }
    }

}
