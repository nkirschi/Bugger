package tech.bugger.global.util;

/**
 * Collection of application-wide constants.
 */
public final class Constants {

    /**
     * Prevents instantiation of this utility class.
     */
    private Constants() {
        throw new UnsupportedOperationException(); // for reflection abusers
    }

    /**
     * The minimum length of usernames.
     */
    public static final int USERNAME_MIN = 4;

    /**
     * The maximum length of usernames.
     */
    public static final int USERNAME_MAX = 16;

    /**
     * The minimum length of passwords.
     */
    public static final int PASSWORD_MIN = 8;

    /**
     * The maximum length of passwords.
     */
    public static final int PASSWORD_MAX = 128;

    /**
     * The maximum length of inputs in small or normal text boxes.
     */
    public static final int SMALL_FIELD = 50;

    /**
     * The maximum length of inputs in large text boxes.
     */
    public static final int LARGE_FIELD = 100;

    /**
     * The maximum length of inputs in small or normal text areas.
     */
    public static final int SMALL_AREA = 10_000;

    /**
     * The maximum length of inputs in large text areas.
     */
    public static final int LARGE_AREA = 100_000;

    /**
     * Regular expression for a comma-separated list of integers.
     */
    public static final String INTEGER_LIST_REGEX = "^\\d+(,\\d+)*$";

    /**
     * Regular expression for a comma-separated list of file extensions.
     */
    public static final String EXTENSION_LIST_REGEX = "^\\.[^,]+(,\\s?\\.[^,\\s\\.]+)*$";

    /**
     * The maximum filesize for attachments in megabytes.
     */
    public static final int MAX_ATTACHMENT_FILESIZE = 10;

    /**
     * The maximum filesize for profile avatars in megabytes.
     */
    public static final int MAX_AVATAR_FILESIZE = 2;

    /**
     * The number of bytes in a megabyte.
     */
    public static final int MB_TO_BYTES = 1_000_000;

    /**
     * Symbol to use when no meaningful value can be displayed.
     */
    public static final String NO_VALUE_INDICATOR = "-";

    /**
     * Maximum amount of decimal places to display to the user.
     */
    public static final int MAX_FRACTION_DIGITS = 2;

}
