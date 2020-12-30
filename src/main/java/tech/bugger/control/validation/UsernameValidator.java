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
import tech.bugger.business.service.ProfileService;
import tech.bugger.business.util.RegistryKey;

/**
 * Validator for username inputs.
 */
@FacesValidator(value = "usernameValidator", managed = true)
public class UsernameValidator implements Validator<String> {

    /**
     * The RegEx to use when validating a username.
     */
    private static final Pattern REGEX = Pattern.compile("^([a-zA-Z0-9_äöüÄÖÜ])+$");

    /**
     * The profile service for user interactions.
     */
    private final ProfileService profileService;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new username validator with the necessary dependencies.
     *
     * @param profileService The profile service for user interactions.
     * @param messagesBundle The resource bundle for feedback messages.
     */
    @Inject
    public UsernameValidator(final ProfileService profileService,
                             @RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.profileService = profileService;
        this.messagesBundle = messagesBundle;
    }

    /**
     * Validates the given {@code username}.
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
        } else if (profileService.isUsernameAssigned(username)) {
            FacesMessage message = new FacesMessage(messagesBundle.getString("username_validator.already_exists"));
            throw new ValidatorException(message);
        }
    }

}
