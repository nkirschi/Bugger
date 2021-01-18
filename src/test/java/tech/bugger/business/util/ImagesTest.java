package tech.bugger.business.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import tech.bugger.business.exception.CorruptImageException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ImagesTest {

    @Test
    public void testConstructorAccess() throws NoSuchMethodException {
        Constructor<Images> constructor = Images.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Throwable e = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
    }

    @Test
    public void testGenerateThumbnail() throws IOException, CorruptImageException {
        byte[] bytes = ClassLoader.getSystemResourceAsStream("images/bugger.png").readAllBytes();
        assertNotNull(Images.generateThumbnail(bytes));
    }

    @Test
    public void testGenerateThumbnailImageNull() throws CorruptImageException {
        assertNull(Images.generateThumbnail(null));
    }

    @Test
    public void testGenerateThumbnailCorruptImage() {
        assertThrows(CorruptImageException.class,
                () -> Images.generateThumbnail(new byte[10])
        );
    }

    @Test
    public void testGenerateThumbnailIOException() {
        try (MockedStatic<ImageIO> imageIOMock = mockStatic(ImageIO.class)) {
            imageIOMock.when(() -> ImageIO.read(any(ByteArrayInputStream.class))).thenThrow(IOException.class);
            assertThrows(CorruptImageException.class, () -> Images.generateThumbnail(new byte[10]));
        }
    }

}
