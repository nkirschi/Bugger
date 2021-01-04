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
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.global.transfer.Configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class EmailRegexValidatorTest {

    private EmailRegexValidator emailRegexValidator;

    @Mock
    private FacesContext fctx;

    @Mock
    private UIComponent comp;

    @Mock
    private Configuration configurationMock;

    @Mock
    private ApplicationSettings settingsMock;

    @BeforeEach
    public void setUp() {
        doReturn(".+@.+").when(configurationMock).getUserEmailFormat();
        doReturn(configurationMock).when(settingsMock).getConfiguration();
        emailRegexValidator = new EmailRegexValidator(settingsMock, ResourceBundleMocker.mock(""));
    }

    @Test
    public void testValidateOnEmailNotValidFormat() {
        assertThrows(ValidatorException.class, () -> emailRegexValidator.validate(fctx, comp, "testtest.de"));
    }

    @Test
    public void testValidateOnEmailOkay() {
        assertDoesNotThrow(() -> emailRegexValidator.validate(fctx, comp, "test@test.de"));
    }

}