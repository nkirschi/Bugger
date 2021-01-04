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
public class PasswordRegexValidatorTest {

    private PasswordRegexValidator passwordRegexValidator;

    @Mock
    private FacesContext fctx;

    @Mock
    private UIComponent comp;

    @BeforeEach
    public void setUp() {
        passwordRegexValidator = new PasswordRegexValidator(ResourceBundleMocker.mock(""));
    }

    @Test
    public void testValidateOnTooShortPassword() {
        assertThrows(ValidatorException.class, () -> passwordRegexValidator.validate(fctx, comp, "t3st!"));
    }

    @Test
    public void testValidateOnTooLongPassword() {
        assertThrows(ValidatorException.class, () -> passwordRegexValidator.validate(fctx, comp, "Hello1am4verystr0ngpassword!hello1am4verystr0ngpassword!hello1am4verystr0ngpassword!hello1am4verystr0ngpassword!hello1am4verystr0ngpassworD!"));
    }

    @Test
    public void testValidateOnTooWeakPassword() {
        assertThrows(ValidatorException.class, () -> passwordRegexValidator.validate(fctx, comp, "hello1am4wEakpassword"));
    }

    @Test
    public void testValidateOnGoodPassword() {
        assertDoesNotThrow(() -> passwordRegexValidator.validate(fctx, comp, "Hello1am4verystr0ngpassword!"));
    }

}