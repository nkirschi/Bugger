package tech.bugger.control.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.ResourceBundleMocker;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class RegexValidatorTest {

    private RegexValidator regexValidator;

    @Mock
    private FacesContext fctx;

    @Mock
    private UIComponent comp;

    @BeforeEach
    public void setUp() {
        regexValidator = new RegexValidator(ResourceBundleMocker.mock(""));
    }

    @Test
    public void testValidateOnValidRegex() {
        assertDoesNotThrow(() -> regexValidator.validate(fctx, comp, ".+@.+"));
    }

    @Test
    public void testValidateOnInvalidRegex() {
        assertThrows(ValidatorException.class, () -> regexValidator.validate(fctx, comp, "@{-1}"));
    }

}