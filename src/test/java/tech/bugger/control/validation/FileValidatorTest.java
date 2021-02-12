package tech.bugger.control.validation;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.service.PostService;
import tech.bugger.global.util.Constants;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileValidatorTest {

    private FileValidator fileValidator;

    @Mock
    private PostService postService;

    @Mock
    private FacesContext fctx;

    @Mock
    private UIComponent comp;

    @Mock
    private Part part;

    @BeforeEach
    public void setUp() {
        fileValidator = new FileValidator(postService, ResourceBundleMocker.mock(""));
    }

    @Test
    public void testValidateOnValidFile() {
        doReturn(Constants.MAX_ATTACHMENT_FILESIZE * Constants.MB_TO_BYTES - 1L).when(part).getSize();
        doReturn(true).when(postService).isAttachmentNameValid(any());
        assertDoesNotThrow(() -> fileValidator.validate(fctx, comp, part));
    }

    @Test
    public void testValidateOnTooLargeAttachment() {
        doReturn(Constants.MAX_ATTACHMENT_FILESIZE * Constants.MB_TO_BYTES + 1L).when(part).getSize();
        assertThrows(ValidatorException.class, () -> fileValidator.validate(fctx, comp, part));
    }

    @Test
    public void testValidateOnInvalidName() {
        doReturn(Constants.MAX_ATTACHMENT_FILESIZE * Constants.MB_TO_BYTES - 1L).when(part).getSize();
        doReturn(false).when(postService).isAttachmentNameValid(any());
        assertThrows(ValidatorException.class, () -> fileValidator.validate(fctx, comp, part));
    }

    @Test
    public void testValidateOnIOException() throws Exception {
        doReturn(Constants.MAX_ATTACHMENT_FILESIZE * Constants.MB_TO_BYTES - 1L).when(part).getSize();
        doReturn(true).when(postService).isAttachmentNameValid(any());
        doThrow(IOException.class).when(part).getInputStream();
        assertThrows(ValidatorException.class, () -> fileValidator.validate(fctx, comp, part));
    }

}