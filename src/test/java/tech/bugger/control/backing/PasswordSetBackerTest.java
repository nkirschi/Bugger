package tech.bugger.control.backing;

import java.lang.reflect.Field;
import java.util.Locale;
import javax.faces.context.ExternalContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.service.ProfileService;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class PasswordSetBackerTest {

    private PasswordSetBacker passwordSetBacker;

    private User testUser;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private ProfileService profileService;

    @Mock
    private UserSession userSession;

    @Mock
    private ExternalContext ectx;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().doReturn(Locale.GERMAN).when(userSession).getLocale();
        passwordSetBacker = new PasswordSetBacker(authenticationService, profileService, userSession, ectx);
        testUser = new User();
    }

    @Test
    public void testInit() throws Exception {
        doReturn(1).when(authenticationService).getUserIdForToken(any());
        doReturn(testUser).when(profileService).getUser(anyInt());
        passwordSetBacker.init();

        Field f = PasswordSetBacker.class.getDeclaredField("user");
        f.setAccessible(true);
        User user = (User) f.get(passwordSetBacker);

        verify(ectx, never()).redirect("home.xhtml");
        assertEquals(testUser, user);
    }

    @Test
    public void testInitLoggedIn() throws Exception {
        doReturn(testUser).when(userSession).getUser();
        passwordSetBacker.init();
        verify(ectx).redirect("home.xhtml");
    }

    @Test
    public void testInitNoUserFound() throws Exception {
        doReturn(null).when(authenticationService).getUserIdForToken(any());
        passwordSetBacker.init();
        verify(ectx, never()).redirect("home.xhtml");
    }

    @Test
    public void testSetUserPasswordSuccess() {
        doReturn(true).when(authenticationService).setPassword(any(), any(), any());
        String redirect = passwordSetBacker.setUserPassword();
        assertEquals("home.xhtml", redirect);
    }

    @Test
    public void testSetUserPasswordFail() {
        doReturn(false).when(authenticationService).setPassword(any(), any(), any());
        String redirect = passwordSetBacker.setUserPassword();
        assertNull(redirect);
    }

    @Test
    public void testIsTokenValidYes() throws Exception {
        Field f = PasswordSetBacker.class.getDeclaredField("user");
        f.setAccessible(true);
        f.set(passwordSetBacker, testUser);

        assertTrue(passwordSetBacker.isValidToken());
    }

    @Test
    public void testIsTokenValidNo() throws Exception {
        Field f = PasswordSetBacker.class.getDeclaredField("user");
        f.setAccessible(true);
        f.set(passwordSetBacker, null);

        assertFalse(passwordSetBacker.isValidToken());
    }

}