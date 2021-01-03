package tech.bugger.control.backing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

public class HeaderBackerTest {

    HeaderBacker headerBacker = new HeaderBacker();

    @Test
    public void testToggleMenuActivate() {

        if (!headerBacker.isDisplayMenu()) {
            headerBacker.toggleMenu();
        }
        assertEquals(true, headerBacker.isDisplayMenu());
    }

    @Test
    public void testToggleMenuDeactivate() {

        if (headerBacker.isDisplayMenu()) {
            headerBacker.toggleMenu();
        }
        assertEquals(false, headerBacker.isDisplayMenu());
    }
}
