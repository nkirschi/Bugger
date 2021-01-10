package tech.bugger.business.util;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import tech.bugger.business.exception.CorruptImageException;

/**
 * Utility class for image processing.
 */
public final class Images {

    /**
     * Prevents instantiation of this utility class.
     */
    private Images() {
        throw new UnsupportedOperationException(); // for reflection abusers
    }

    /**
     * The width of the thumbnail.
     */
    private static final int WIDTH = 100;

    /**
     * The height of the thumbnail.
     */
    private static final int HEIGHT = 100;

    /**
     * Generate a thumbnail from the given image.
     *
     * @param image The concerned image as byte representation.
     * @return The generated thumbnail.
     * @throws CorruptImageException If {@code image} is not a valid image.
     */
    public static byte[] generateThumbnail(final byte[] image) throws CorruptImageException {
        if (image == null) {
            return null;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(image);
        try {
            BufferedImage img = ImageIO.read(in);

            if (img == null) {
                throw new CorruptImageException("The image could not be converted to a thumbnail.");
            }

            Image scaledImage = img.getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
            BufferedImage imageBuff = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            imageBuff.getGraphics().drawImage(scaledImage, 0, 0, null);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            ImageIO.write(imageBuff, "jpg", buffer);

            return buffer.toByteArray();
        } catch (IOException e) {
            throw new CorruptImageException("The image could not be converted to a thumbnail.", e);
        }
    }

}
