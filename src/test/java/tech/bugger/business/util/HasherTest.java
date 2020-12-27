package tech.bugger.business.util;

import org.junit.jupiter.api.Test;
import tech.bugger.business.exception.CryptographyImpossibleException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HasherTest {
    
    private static final String INPUT = "Hash my darling, don't fear my darling.";
    private static final String SALT = "2f73616c7421"; // equals "/salt!" in UTF-8
    private static final String ALGO = "SHA3-512";
    public static final int NUM_BYTES = 42;

    @Test
    public void testConstructorAccess() throws NoSuchMethodException {
        Constructor<Hasher> constructor = Hasher.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Throwable e = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
    }

    @Test
    public void testHashWhenInputNull() {
        assertThrows(IllegalArgumentException.class, () -> Hasher.hash(null, SALT, ALGO));
    }

    @Test
    public void testHashWhenSaltNull() {
        assertThrows(IllegalArgumentException.class, () -> Hasher.hash(INPUT, null, ALGO));
    }

    @Test
    void testHashWhenSaltUnevenLength() {
        assertThrows(IllegalArgumentException.class, () -> Hasher.hash(INPUT, "a0f", ALGO));
    }

    @Test
    void testHashWhenSaltInvalidHexString() {
        assertThrows(IllegalArgumentException.class, () -> Hasher.hash(INPUT, "0x123ö", ALGO));
    }

    @Test
    void testHashWhenSaltAnotherInvalidHexString() {
        assertThrows(IllegalArgumentException.class, () -> Hasher.hash(INPUT, "äü", ALGO));
    }

    @Test
    public void testHashWhenAlgorithmNull() {
        assertThrows(IllegalArgumentException.class, () -> Hasher.hash(INPUT, SALT, null));
    }

    @Test
    public void testHashWhenAlgorithmNotAvailable() {
        assertThrows(CryptographyImpossibleException.class, () -> Hasher.hash(INPUT, SALT, "SCHA-512"));
    }

    @Test
    public void testHashWithoutSalt() {
        assertEquals("a2ab83ebe527d4d6336116d4af9335e91c0c59225f5a98ce24ff43065bbd222da25eb43b7a8cee3890bad7f46e624c6b0"
                + "fd892f94adfe71c7d2beb0b7fa0fa13", Hasher.hash(INPUT, "", ALGO));
    }

    @Test
    public void testHashWithSalt() {
        assertEquals("78ae52530f31daf36e222f970c7888bd75bc5ceca4025dcbf0352fbc196cc240b874918dc6e87a122bfcf190dc58c000e"
                + "b4a48449b30192ddb88a53fdf833b35", Hasher.hash(INPUT, SALT, ALGO));
    }

    @Test
    public void testGenerateRandomBytes() {
        String bytes = Hasher.generateRandomBytes(NUM_BYTES);
        assertAll(
                () -> assertTrue(bytes.matches("([0-9a-f]{2})*")),
                () -> assertEquals(NUM_BYTES * 2, bytes.length())
        );
    }
}