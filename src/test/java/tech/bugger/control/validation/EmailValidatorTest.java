package tech.bugger.control.validation;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.service.ProfileService;
import tech.bugger.global.transfer.Configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailValidatorTest {

    private EmailValidator emailValidator;

    @Mock
    private FacesContext fctx;

    @Mock
    private UIComponent comp;

    @Mock
    private Configuration configurationMock;

    @Mock
    private ApplicationSettings settingsMock;

    @Mock
    private ProfileService profileServiceMock;

    @BeforeEach
    public void setUp() {
        doReturn(".+@.+").when(configurationMock).getUserEmailFormat();
        doReturn(configurationMock).when(settingsMock).getConfiguration();
        emailValidator = new EmailValidator(settingsMock, profileServiceMock, ResourceBundleMocker.mock(""));
    }

    @Test
    public void testValidateOnEmailNotValidFormat() {
        assertThrows(ValidatorException.class, () -> emailValidator.validate(fctx, comp, "testtest.de"));
    }

    @Test
    public void testValidateOnEmailExists() {
        doReturn(true).when(profileServiceMock).isEmailAssigned("test@test.de");
        assertThrows(ValidatorException.class, () -> emailValidator.validate(fctx, comp, "test@test.de"));
    }

    @Test
    public void testValidateOnEmailOkay() {
        doReturn(false).when(profileServiceMock).isEmailAssigned("test@test.de");
        assertDoesNotThrow(() -> emailValidator.validate(fctx, comp, "test@test.de"));
    }

}