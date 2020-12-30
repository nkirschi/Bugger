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
import tech.bugger.business.service.ProfileService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsernameValidatorTest {

    private UsernameValidator usernameValidator;

    @Mock
    private FacesContext fctx;

    @Mock
    private UIComponent comp;

    @Mock
    private ProfileService profileServiceMock;

    @BeforeEach
    public void setUp() {
        usernameValidator = new UsernameValidator(profileServiceMock, ResourceBundleMocker.mock(""));
    }

    @Test
    public void testValidateOnUsernameNotValidFormat() {
        assertThrows(ValidatorException.class, () -> usernameValidator.validate(fctx, comp, "hyperspe`ed"));
    }

    @Test
    public void testValidateOnUsernameExists() {
        doReturn(true).when(profileServiceMock).isUsernameAssigned("hyperspeeed");
        assertThrows(ValidatorException.class, () -> usernameValidator.validate(fctx, comp, "hyperspeeed"));
    }

    @Test
    public void testValidateOnUsernameOkay() {
        doReturn(false).when(profileServiceMock).isUsernameAssigned("hyperspeeed");
        assertDoesNotThrow(() -> usernameValidator.validate(fctx, comp, "hyperspeeed"));
    }

}