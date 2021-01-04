package tech.bugger.control.validation;

import java.util.Map;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.service.ProfileService;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsernameAssignedValidatorTest {

    private UsernameAssignedValidator usernameAssignedValidator;

    @Mock
    private FacesContext fctx;

    @Mock
    private UIInput comp;

    @Mock
    private ProfileService profileServiceMock;

    @BeforeEach
    public void setUp() {
        usernameAssignedValidator = new UsernameAssignedValidator(profileServiceMock, ResourceBundleMocker.mock(""));
    }

    @Test
    public void testValidateWithOnlyChangeAndChange() {
        doReturn(Map.of("onlyOnChange", true)).when(comp).getAttributes();
        doReturn("someusername").when(comp).getValue();
        doReturn(new User()).when(profileServiceMock).getUserByUsername("hyperspeeed");
        assertThrows(ValidatorException.class, () -> usernameAssignedValidator.validate(fctx, comp, "hyperspeeed"));
    }

    @Test
    public void testValidateWithOnlyChangeAndNoChange() {
        doReturn(Map.of("onlyOnChange", true)).when(comp).getAttributes();
        doReturn("hyperspeeed").when(comp).getValue();
        assertDoesNotThrow(() -> usernameAssignedValidator.validate(fctx, comp, "hyperspeeed"));
    }

    @Test
    public void testValidateOnUsernameExists() {
        doReturn(Map.of()).when(comp).getAttributes();
        doReturn(new User()).when(profileServiceMock).getUserByUsername("hyperspeeed");
        assertThrows(ValidatorException.class, () -> usernameAssignedValidator.validate(fctx, comp, "hyperspeeed"));
    }

    @Test
    public void testValidateOnUsernameOkay() {
        doReturn(Map.of()).when(comp).getAttributes();
        doReturn(null).when(profileServiceMock).getUserByUsername("hyperspeeed");
        assertDoesNotThrow(() -> usernameAssignedValidator.validate(fctx, comp, "hyperspeeed"));
    }

}