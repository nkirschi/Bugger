package tech.bugger.control.validation;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.service.ProfileService;
import tech.bugger.global.transfer.User;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class EmailAssignedValidatorTest {

    private EmailAssignedValidator emailAssignedValidator;

    @Mock
    private FacesContext fctx;

    @Mock
    private UIComponent comp;

    @Mock
    private UIInput input;

    @Mock
    private ProfileService profileServiceMock;

    private final String onChange = "only-on-change";
    private final String email = "test@test.de";
    private final String otherEmail = "other@test.de";

    @BeforeEach
    public void setUp() {
        emailAssignedValidator = new EmailAssignedValidator(profileServiceMock, ResourceBundleMocker.mock(""));
    }

    @Test
    public void testValidateWithOnlyOnChangeAndChangeWhenUsernameTaken() {
        doReturn(Map.of(onChange, true)).when(input).getAttributes();
        doReturn(otherEmail).when(input).getValue();
        doReturn(new User()).when(profileServiceMock).getUserByEmail(email);
        assertThrows(ValidatorException.class, () -> emailAssignedValidator.validate(fctx, input, email));
    }

    @Test
    public void testValidateWithOnlyOnChangeAndChangeWhenUsernameNotTaken() {
        doReturn(Map.of(onChange, true)).when(input).getAttributes();
        doReturn(otherEmail).when(input).getValue();
        doReturn(null).when(profileServiceMock).getUserByEmail(email);
        assertDoesNotThrow(() -> emailAssignedValidator.validate(fctx, input, email));
    }

    @Test
    public void testValidateWithOnlyOnChangeAndNoChange() {
        doReturn(Map.of(onChange, true)).when(input).getAttributes();
        doReturn(email).when(input).getValue();
        assertDoesNotThrow(() -> emailAssignedValidator.validate(fctx, input, email));
    }

    @Test
    public void testValidateOnEmailExists() {
        doReturn(new User()).when(profileServiceMock).getUserByEmail(email);
        assertThrows(ValidatorException.class, () -> emailAssignedValidator.validate(fctx, comp, email));
    }

    @Test
    public void testValidateOnEmailOkay() {
        doReturn(null).when(profileServiceMock).getUserByEmail(email);
        assertDoesNotThrow(() -> emailAssignedValidator.validate(fctx, comp, email));
    }

}