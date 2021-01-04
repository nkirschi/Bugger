package tech.bugger.control.backing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.global.transfer.User;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import java.lang.reflect.Field;


@ExtendWith(MockitoExtension.class)
public class HeaderBackerTest {

    private final HeaderBacker headerBacker = new HeaderBacker();

    @Mock
    UserSession session;

    @Test
    public void testInit() throws IllegalAccessException, NoSuchFieldException {
        Field field = headerBacker.getClass().getDeclaredField("session");
        field.setAccessible(true);
        field.set(headerBacker, session);
        User user = new User();
        doReturn(user).when(session).getUser();
        headerBacker.init();
        assertAll(
                () -> assertEquals(user, headerBacker.getUser()),
                () -> assertFalse(headerBacker.isDisplayMenu())
        );
    }

    @Test
    public void testToggleMenuActivate() throws IllegalAccessException, NoSuchFieldException {
        Field field = headerBacker.getClass().getDeclaredField("displayMenu");
        field.setAccessible(true);
        field.setBoolean(headerBacker, false);
        headerBacker.toggleMenu();
        assertTrue(headerBacker.isDisplayMenu());
    }

    @Test
    public void testToggleMenuDeactivate() throws IllegalAccessException, NoSuchFieldException {
        Field field = headerBacker.getClass().getDeclaredField("displayMenu");
        field.setAccessible(true);
        field.setBoolean(headerBacker, true);
        headerBacker.toggleMenu();
        assertFalse(headerBacker.isDisplayMenu());
    }
}
