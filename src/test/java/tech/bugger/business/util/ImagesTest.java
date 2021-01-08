package tech.bugger.business.util;

import org.junit.jupiter.api.Test;
import tech.bugger.business.exception.CorruptImageException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ImagesTest {

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
    public void testGenerateThumbnailCorruptImage() throws CorruptImageException {
        assertThrows(CorruptImageException.class,
                () -> Images.generateThumbnail(new byte[10])
        );
    }
}
