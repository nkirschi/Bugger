package tech.bugger.global.util;

/**
 * Collection of application-wide constants.
 */
public final class Constants {

    private Constants() {
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

}
