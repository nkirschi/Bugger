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
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailAssignedValidatorTest {

    private EmailAssignedValidator emailAssignedValidator;

    @Mock
    private FacesContext fctx;

    @Mock
    private UIComponent comp;

    @Mock
    private ProfileService profileServiceMock;

    @BeforeEach
    public void setUp() {
        emailAssignedValidator = new EmailAssignedValidator(profileServiceMock, ResourceBundleMocker.mock(""));
    }

    @Test
    public void testValidateOnEmailExists() {
        doReturn(new User()).when(profileServiceMock).getUserByEmail("test@test.de");
        assertThrows(ValidatorException.class, () -> emailAssignedValidator.validate(fctx, comp, "test@test.de"));
    }

    @Test
    public void testValidateOnEmailOkay() {
        doReturn(null).when(profileServiceMock).getUserByEmail("test@test.de");
        assertDoesNotThrow(() -> emailAssignedValidator.validate(fctx, comp, "test@test.de"));
    }

}