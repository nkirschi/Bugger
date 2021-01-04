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
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.util.RegistryKey;

/**
 * Validator for e-mail address inputs.
 */
@FacesValidator(value = "emailRegexValidator", managed = true)
public class EmailRegexValidator implements Validator<String> {

    /**
     * The current application settings.
     */
    private final ApplicationSettings applicationSettings;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new e-mail address validator with the necessary dependencies.
     *
     * @param applicationSettings The current application settings.
     * @param messagesBundle      The resource bundle for feedback messages.
     */
    @Inject
    public EmailRegexValidator(final ApplicationSettings applicationSettings,
                               @RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.applicationSettings = applicationSettings;
        this.messagesBundle = messagesBundle;
    }

    /**
     * Validates the given {@code email}.
     *
     * @param fctx      The current {@link FacesContext}.
     * @param component The affected input {@link UIComponent}
     * @param email     The email address to validate.
     * @throws ValidatorException If validation fails.
     */
    @Override
    public void validate(final FacesContext fctx, final UIComponent component, final String email) {
        Pattern pattern = Pattern.compile(applicationSettings.getConfiguration().getUserEmailFormat());
        if (!pattern.matcher(email).matches()) {
            FacesMessage message = new FacesMessage(messagesBundle.getString("email_validator.format_wrong"));
            throw new ValidatorException(message);
        }
    }

}
