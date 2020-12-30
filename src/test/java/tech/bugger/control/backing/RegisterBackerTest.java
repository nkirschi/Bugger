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
import tech.bugger.global.util.Lazy;

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


        testUser = new User(null, "", "", "", "", "", "", "", new Lazy<>(new byte[0]), new byte[0], "",
                Language.ENGLISH, User.ProfileVisibility.FULL, null, null, false);
    }

    @Test
    public void testInit() throws Exception {
        registerBacker.init();

        Field f = RegisterBacker.class.getDeclaredField("user");
        f.setAccessible(true);
        User internalUser = (User) f.get(registerBacker);

        assertAll(() -> assertEquals(Language.GERMAN, internalUser.getPreferredLanguage()),
                () -> assertEquals("", registerBacker.getUsername()),
                () -> assertEquals("", registerBacker.getEmailAddress()),
                () -> assertEquals("", registerBacker.getFirstName()),
                () -> assertEquals("", registerBacker.getLastName()));
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
        registerBacker.register();
        verify(profileService).createUser(any());
        verify(authenticationService).register(any());
    }

}