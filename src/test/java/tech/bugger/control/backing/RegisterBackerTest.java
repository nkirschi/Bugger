package tech.bugger.control.backing;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;
import javax.faces.context.ExternalContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.service.ProfileService;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegisterBackerTest {

    private RegisterBacker registerBacker;

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
        registerBacker = new RegisterBacker(authenticationService, profileService, userSession, ectx);
        testUser = new User();
    }

    @Test
    public void testInit() throws Exception {
        registerBacker.init();

        Field f = RegisterBacker.class.getDeclaredField("user");
        f.setAccessible(true);
        User internalUser = (User) f.get(registerBacker);

        assertAll(() -> assertEquals(Language.GERMAN, internalUser.getPreferredLanguage()),
                () -> assertEquals("", registerBacker.getUser().getUsername()),
                () -> assertEquals("", registerBacker.getUser().getEmailAddress()),
                () -> assertEquals("", registerBacker.getUser().getFirstName()),
                () -> assertEquals("", registerBacker.getUser().getLastName()));
    }

    @Test
    public void testInitNoAccess() throws Exception {
        User copy = new User(testUser);
        doReturn(copy).when(userSession).getUser();
        registerBacker.init();
        verify(ectx).redirect(any());
    }

    @Test
    public void testInitNoAccessAndException() throws Exception {
        User copy = new User(testUser);
        doReturn(copy).when(userSession).getUser();
        doThrow(IOException.class).when(ectx).redirect(any());
        assertThrows(InternalError.class, () -> registerBacker.init());
    }

    @Test
    public void testRegister() {
        doReturn(true).when(profileService).createUser(any());
        doReturn(true).when(authenticationService).register(any());
        assertNotNull(registerBacker.register());
        verify(profileService).createUser(any());
        verify(authenticationService).register(any());
    }

    @Test
    public void testRegisterFailCreateUser() {
        doReturn(false).when(profileService).createUser(any());
        lenient().doReturn(true).when(authenticationService).register(any());
        assertNull(registerBacker.register());
        verify(profileService).createUser(any());
    }

    @Test
    public void testRegisterFailRegister() {
        doReturn(true).when(profileService).createUser(any());
        doReturn(false).when(authenticationService).register(any());
        assertNull(registerBacker.register());
        verify(profileService).createUser(any());
        verify(authenticationService).register(any());
    }

    @Test
    public void testRegisterFailCreateUserAndRegister() {
        doReturn(false).when(profileService).createUser(any());
        lenient().doReturn(false).when(authenticationService).register(any());
        assertNull(registerBacker.register());
        verify(profileService).createUser(any());
    }

}