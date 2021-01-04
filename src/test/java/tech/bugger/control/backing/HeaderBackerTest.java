package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.global.transfer.User;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import java.lang.reflect.Field;


@ExtendWith(MockitoExtension.class)
public class HeaderBackerTest {

    @InjectMocks
    private HeaderBacker headerBacker;

    @Mock
    private UserSession session;

    private Field field;

    @BeforeEach
    public void setup() throws NoSuchFieldException {
        MockitoAnnotations.openMocks(this);
        field = headerBacker.getClass().getDeclaredField("displayMenu");
        field.setAccessible(true);
    }

    @Test
    public void testInit() throws IllegalAccessException, NoSuchFieldException {
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
        field.setBoolean(headerBacker, false);
        headerBacker.toggleMenu();
        assertTrue(headerBacker.isDisplayMenu());
    }

    @Test
    public void testToggleMenuDeactivate() throws IllegalAccessException, NoSuchFieldException {
        field.setBoolean(headerBacker, true);
        headerBacker.toggleMenu();
        assertFalse(headerBacker.isDisplayMenu());
    }
}
