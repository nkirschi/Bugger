package tech.bugger.business.util;

import java.util.logging.Logger;

/**
 * Utility class for cryptographic hashing.
 */
public final class Hasher {
    private static final Logger logger = Logger.getLogger(Hasher.class.getName());

    private Hasher() {
    }

    /**
     * Hash the given string with the given algorithm and salt.
     *
     * @param input The string to be hashed and salted.
     * @param salt  The salt to be applied before hashing.
     * @param algo  The hashing algorithm to use. Available algorithms are described in the  <a href=
     *              "https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#MessageDigest">
     *              MessageDigest section</a>
     * @return The hash value of {@code string} under the given parameters as hexadecimal string.
     */
    public static String hash(final String input, final String salt, final String algo) {
        return null;
    }

    /**
     * Generate a random salt with the given length.
     *
     * @param numBytes The desired length of the salt in bytes.
     * @return Hexadecimal representation of the salt.
     */
    public static String generateSalt(int numBytes) {
        return null;
    }
}