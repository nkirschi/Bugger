package tech.bugger.business.util;

import tech.bugger.business.exception.CorruptImageException;
import tech.bugger.global.util.Log;

/**
 * Utility class for image processing.
 */
public class Images {

    private static final Log log = Log.forClass(Images.class);

    /**
     * Generate a thumbnail from the given image.
     *
     * @param image The concerned image as byte representation.
     * @return The generated thumbnail.
     * @throws CorruptImageException If {@code image} is not a valid image.
     */
    public static byte[] generateThumbnail(byte[] image) throws CorruptImageException {
        return null;
    }
}
