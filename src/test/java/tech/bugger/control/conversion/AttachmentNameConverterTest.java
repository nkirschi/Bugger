package tech.bugger.control.conversion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AttachmentNameConverterTest {
    private AttachmentNameConverter attachmentNameConverter;

    @BeforeEach
    public void setUp() {
        attachmentNameConverter = new AttachmentNameConverter();
    }

    @Test
    public void testGetAsObject() {
        assertThrows(UnsupportedOperationException.class, () -> attachmentNameConverter.getAsObject(null, null, null));
    }

    @Test
    public void testGetAsStringWhenPrefixTooLong() {
        assertEquals("abcdefghijklmnopqrstuvwx[...].abcdef",
                     attachmentNameConverter.getAsString(null, null, "abcdefghijklmnopqrstuvwxyz.abcdef"));
    }

    @Test
    public void testGetAsStringWhenSuffixTooLong() {
        assertEquals("abcdef.abcdefg[...]",
                     attachmentNameConverter.getAsString(null, null, "abcdef.abcdefghijklmnopqrstuvwxyz"));
    }

    @Test
    public void testGetAsStringWhenNoDot() {
        assertEquals("abcdefghijklmnopqrstuvwxyz012345[...]",
                     attachmentNameConverter.getAsString(null, null, "abcdefghijklmnopqrstuvwxyz0123456789"));
    }

    @Test
    public void testGetAsStringWhenShortEnough() {
        assertEquals("abcdefghijklmnopqrstuvw.xyz",
                     attachmentNameConverter.getAsString(null, null, "abcdefghijklmnopqrstuvw.xyz"));
    }

}