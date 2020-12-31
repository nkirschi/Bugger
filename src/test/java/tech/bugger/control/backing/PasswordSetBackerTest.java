package tech.bugger.control.backing;

import java.io.IOException;
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
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        passwordSetBacker.init();
    }

    @Test
    public void testInitLoggedIn() throws Exception {
        passwordSetBacker.init();
    }

    @Test
    public void testInitNoAccessAndException() throws Exception {
        User copy = new User(testUser);
        doReturn(copy).when(userSession).getUser();
        doThrow(IOException.class).when(ectx).redirect(any());
        assertThrows(InternalError.class, () -> passwordSetBacker.init());
    }

}