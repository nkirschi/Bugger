package tech.bugger.control.validation;

import java.util.Map;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(LogExtension.class)
public class MatchingFieldValidatorTest {

    private MatchingFieldValidator matchingFieldValidator;

    @Mock
    private FacesContext fctx;

    @Mock
    private UIComponent comp;

    @Mock
    private UIInput otherInputMock;

    @BeforeEach
    public void setUp() {
        matchingFieldValidator = new MatchingFieldValidator(ResourceBundleMocker.mock(""));
    }

    @Test
    public void testValidateOnNoField() {
        Map<String, Object> attributes = Map.of();
        doReturn(attributes).when(comp).getAttributes();
        assertThrows(IllegalArgumentException.class, () -> matchingFieldValidator.validate(fctx, comp, "test"));
    }

    @Test
    public void testValidateNotEqual() {
        doReturn("test").when(otherInputMock).getValue();
        Map<String, Object> attributes = Map.of("otherInput", otherInputMock);
        doReturn(attributes).when(comp).getAttributes();
        assertThrows(ValidatorException.class, () -> matchingFieldValidator.validate(fctx, comp, "tat"));
    }

    @Test
    public void testValidateEqual() {
        doReturn("test").when(otherInputMock).getValue();
        Map<String, Object> attributes = Map.of("otherInput", otherInputMock);
        doReturn(attributes).when(comp).getAttributes();
        assertDoesNotThrow(() -> matchingFieldValidator.validate(fctx, comp, "test"));
    }

    @Test
    public void testValidateValueNotLocally() {
        doReturn(null).when(otherInputMock).getValue();
        doReturn("test").when(otherInputMock).getSubmittedValue();
        Map<String, Object> attributes = Map.of("otherInput", otherInputMock);
        doReturn(attributes).when(comp).getAttributes();
        assertDoesNotThrow(() -> matchingFieldValidator.validate(fctx, comp, "test"));
    }

}