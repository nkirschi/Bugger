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
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.util.Log;

/**
 * Validator for checking whether two input fields have the same content.
 */
@FacesValidator(value = "matchingFieldValidator", managed = true)
public class MatchingFieldValidator implements Validator<String> {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(MatchingFieldValidator.class);

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new matching field validator with the necessary dependencies.
     *
     * @param messagesBundle The resource bundle for feedback messages.
     */
    @Inject
    public MatchingFieldValidator(@RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.messagesBundle = messagesBundle;
    }

    /**
     * Validates if the given value is equal to a value inside the component passed through as {@code <f:attribute>}
     * with name {@code otherInput}.
     *
     * @param fctx      The current {@link FacesContext}.
     * @param component The affected input {@link UIComponent}
     * @param value     The first value to validate.
     * @throws ValidatorException If validation fails.
     */
    @Override
    public void validate(final FacesContext fctx, final UIComponent component, final String value) {
        // Find the actual JSF component with the ID.
        UIInput otherInput = (UIInput) component.getAttributes().get("otherInput");
        if (otherInput == null) {
            log.error("Attribute 'otherInput' couldn't be found!");
            throw new IllegalArgumentException("Unable to find other component to validate.");
        }

        // Get its value, i.e. the entered text of the other field.
        String other = (String) otherInput.getValue();
        other = other == null ? (String) otherInput.getSubmittedValue() : other;

        // Check if the initial and other text are equal.
        if (!Objects.equals(other, value)) {
            FacesMessage message = new FacesMessage(
                    messagesBundle.getString("matching_field_validator.passwords_must_match"));
            throw new ValidatorException(message);
        }
    }

}
