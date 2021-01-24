package tech.bugger.business.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import tech.bugger.business.exception.CryptographyImpossibleException;
import tech.bugger.global.util.Log;

/**
 * Utility class for cryptographic hashing.
 */
public final class Hasher {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(Hasher.class);

    /**
     * Cryptographically secure random generator.
     */
    private static final SecureRandom random = new SecureRandom();

    /**
     * Cache of hashing algorithm instances by name.
     */
    private static final Map<String, MessageDigest> algorithms = new HashMap<>();

    /**
     * The hexadecimal basis.
     */
    public static final int HEX_RADIX = 16;

    /**
     * Number of bits in half a byte.
     */
    public static final int HALF_BYTE = 4;

    /**
     * Prevents instantiation of this utility class.
     */
    private Hasher() {
        throw new UnsupportedOperationException(); // for reflection abusers
    }

    /**
     * Hashes the given string with the given salt using the provided algorithm.
     * <p>
     * The procedure can be described as {@code algo(input|salt)} where the | character means concatenation.
     *
     * @param input The string to be hashed and salted.
     * @param salt  The salt to be appended before hashing as hexadecimal string of format {@code ([0-9a-f]{2})*}.
     * @param algo  The hashing algorithm to use. Available algorithms are described in the
     *              <a href="https://docs.oracle.com/en/java/javase/14/docs/specs/security/standard-names.html">
     *              Standard Algorithm Names Section</a> of the Java Security API documentation in the subsection
     *              "{@link MessageDigest} algorithms".
     * @return The hash value of {@code input} as hexadecimal string of format {@code ([0-9a-f]{2})*}.
     */
    public static String hash(final String input, final String salt, final String algo) {
        if (input == null) {
            throw new IllegalArgumentException("Input to hash must not be null.");
        } else if (salt == null) {
            throw new IllegalArgumentException("Salt must not be null.");
        } else if (algo == null) {
            throw new IllegalArgumentException("Hashing algorithm must not be null.");
        }

        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] saltBytes = hexToBytes(salt);
        MessageDigest algorithm = getAlgorithm(algo);
        algorithm.update(inputBytes);
        algorithm.update(saltBytes);
        return bytesToHex(algorithm.digest());
    }

    /**
     * Generates the given number of random bytes in hexadecimal representation.
     *
     * @param numBytes The desired number of bytes to randomly generate.
     * @return {@code numBytes} random bytes as hexadecimal string of format {@code ([0-9a-f]{2})*}.
     */
    public static String generateRandomBytes(final int numBytes) {
        byte[] bytes = new byte[numBytes];
        random.nextBytes(bytes);
        return bytesToHex(bytes);
    }

    private static MessageDigest getAlgorithm(final String algo) {
        if (!algorithms.containsKey(algo)) {
            try {
                algorithms.put(algo, MessageDigest.getInstance(algo));
            } catch (NoSuchAlgorithmException e) {
                log.error(algo + " not supported!", e);
                throw new CryptographyImpossibleException(e);
            }
        }
        return algorithms.get(algo);
    }

    private static String bytesToHex(final byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(final String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length!");
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int upperByte = Character.digit(hex.charAt(2 * i), HEX_RADIX);
            int lowerByte = Character.digit(hex.charAt(2 * i + 1), HEX_RADIX);
            if (upperByte == -1 || lowerByte == -1) {
                throw new IllegalArgumentException("Hex string must only contain [0-9a-f]!");
            }
            bytes[i] = (byte) ((upperByte << HALF_BYTE) + lowerByte);
        }
        return bytes;
    }

}
