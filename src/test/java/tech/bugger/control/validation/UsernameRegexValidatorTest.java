package tech.bugger.control.validation;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class UsernameRegexValidatorTest {

    private UsernameRegexValidator usernameRegexValidator;

    @Mock
    private FacesContext fctx;

    @Mock
    private UIComponent comp;

    @BeforeEach
    public void setUp() {
        usernameRegexValidator = new UsernameRegexValidator(ResourceBundleMocker.mock(""));
    }

    @Test
    public void testValidateOnUsernameNotValidFormat() {
        assertThrows(ValidatorException.class, () -> usernameRegexValidator.validate(fctx, comp, "hyperspe`ed"));
    }

    @Test
    public void testValidateOnUsernameOkay() {
        assertDoesNotThrow(() -> usernameRegexValidator.validate(fctx, comp, "hyperspeeed"));
    }

}