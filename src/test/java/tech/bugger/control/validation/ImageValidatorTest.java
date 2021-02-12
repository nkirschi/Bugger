package tech.bugger.control.validation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.imageio.ImageIO;
import javax.servlet.http.Part;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.global.util.Constants;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageValidatorTest {

    private ImageValidator imageValidator;

    @Mock
    private FacesContext fctx;

    @Mock
    private UIComponent comp;

    @Mock
    private Part part;

    @Mock
    private BufferedImage img;

    @BeforeEach
    public void setUp() {
        imageValidator = new ImageValidator(ResourceBundleMocker.mock(""));
    }

    @Test
    public void testValidateOnValidImage() {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class)) {
            imageIO.when(() -> ImageIO.read(nullable(InputStream.class))).thenReturn(img);
            doReturn(Constants.MAX_AVATAR_FILESIZE * Constants.MB_TO_BYTES - 1L).when(part).getSize();
            doReturn(Constants.MIN_IMAGE_WIDTH + 1).when(img).getWidth();
            doReturn(Constants.MIN_IMAGE_HEIGHT + 1).when(img).getHeight();
            assertDoesNotThrow(() -> imageValidator.validate(fctx, comp, part));
        }
    }

    @Test
    public void testValidateOnTooBigImage() {
        doReturn(Constants.MAX_AVATAR_FILESIZE * Constants.MB_TO_BYTES + 1L).when(part).getSize();
        assertThrows(ValidatorException.class, () -> imageValidator.validate(fctx, comp, part));
    }

    @Test
    public void testValidateOnTooSmallWidth() {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class)) {
            imageIO.when(() -> ImageIO.read(nullable(InputStream.class))).thenReturn(img);
            doReturn(Constants.MAX_AVATAR_FILESIZE * Constants.MB_TO_BYTES - 1L).when(part).getSize();
            doReturn(Constants.MIN_IMAGE_WIDTH - 1).when(img).getWidth();
            assertThrows(ValidatorException.class, () -> imageValidator.validate(fctx, comp, part));
        }
    }

    @Test
    public void testValidateOnTooSmallHeight() {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class)) {
            imageIO.when(() -> ImageIO.read(nullable(InputStream.class))).thenReturn(img);
            doReturn(Constants.MAX_AVATAR_FILESIZE * Constants.MB_TO_BYTES - 1L).when(part).getSize();
            doReturn(Constants.MIN_IMAGE_WIDTH + 1).when(img).getWidth();
            doReturn(Constants.MIN_IMAGE_HEIGHT - 1).when(img).getHeight();
            assertThrows(ValidatorException.class, () -> imageValidator.validate(fctx, comp, part));
        }
    }

    @Test
    public void testValidateOnInvalidImage() {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class)) {
            imageIO.when(() -> ImageIO.read(nullable(InputStream.class))).thenReturn(null);
            doReturn(Constants.MAX_AVATAR_FILESIZE * Constants.MB_TO_BYTES - 1L).when(part).getSize();
            assertThrows(ValidatorException.class, () -> imageValidator.validate(fctx, comp, part));
        }
    }

    @Test
    public void testValidateOnIOException() {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class)) {
            imageIO.when(() -> ImageIO.read(nullable(InputStream.class))).thenThrow(IOException.class);
            assertThrows(ValidatorException.class, () -> imageValidator.validate(fctx, comp, part));
        }
    }

}