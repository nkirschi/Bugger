package tech.bugger.control.backing;

import java.lang.reflect.Field;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HeaderBackerTest {

    @InjectMocks
    private HeaderBacker headerBacker;

    @Mock
    private UserSession session;

    @Mock
    private FacesContext fctx;

    @Mock
    private ExternalContext ectx;

    private Field field;

    private User user;

    @BeforeEach
    public void setup() throws NoSuchFieldException {
        user = new User();
        lenient().doReturn(user).when(session).getUser();
        lenient().doReturn(ectx).when(fctx).getExternalContext();
        MockitoAnnotations.openMocks(this);
        field = headerBacker.getClass().getDeclaredField("displayMenu");
        field.setAccessible(true);
    }

    @Test
    public void testInit() throws IllegalAccessException, NoSuchFieldException {
        headerBacker.init();
        assertAll(
                () -> assertEquals(user, headerBacker.getUser()),
                () -> assertFalse(headerBacker.isDisplayMenu())
        );
    }

    @Test
    public void testToggleMenuActivate() throws IllegalAccessException {
        field.setBoolean(headerBacker, false);
        headerBacker.toggleMenu();
        assertTrue(headerBacker.isDisplayMenu());
    }

    @Test
    public void testToggleMenuDeactivate() throws IllegalAccessException {
        field.setBoolean(headerBacker, true);
        headerBacker.toggleMenu();
        assertFalse(headerBacker.isDisplayMenu());
    }

}
