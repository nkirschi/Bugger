package tech.bugger.control.validation;

import java.util.Objects;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import tech.bugger.business.service.ProfileService;
import tech.bugger.business.util.RegistryKey;

/**
 * Validator for e-mail address inputs.
 */
@FacesValidator(value = "emailAssignedValidator", managed = true)
public class EmailAssignedValidator implements Validator<String> {

    /**
     * The profile service for user interactions.
     */
    private final ProfileService profileService;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new e-mail address validator with the necessary dependencies.
     *
     * @param profileService The profile service for user interactions.
     * @param messagesBundle The resource bundle for feedback messages.
     */
    @Inject
    public EmailAssignedValidator(final ProfileService profileService,
                                  @RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.profileService = profileService;
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
        // Read whether this validator should only be run when this field has been changed from ID.
        boolean onlyOnChange = component.getAttributes().containsKey("only-on-change");
        if (onlyOnChange && Objects.equals(((UIInput) component).getValue(), email)) {
            // Don't validate as inputs have not changed.
            return;
        }

        if (profileService.getUserByEmail(email) != null) {
            FacesMessage message = new FacesMessage(messagesBundle.getString("email_validator_already_exists"));
            throw new ValidatorException(message);
        }
    }

}
