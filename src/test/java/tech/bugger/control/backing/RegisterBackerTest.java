package tech.bugger.control.backing;

import java.lang.reflect.Field;
import java.util.Locale;
import javax.enterprise.event.Event;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.service.ProfileService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class RegisterBackerTest {

    private RegisterBacker registerBacker;

    private User testUser;

    @Mock
    private NavigationHandler navHandler;

    @Mock
    private Application application;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private ProfileService profileService;

    @Mock
    private UserSession userSession;

    @Mock
    private HttpServletRequest request;

    @Mock
    private FacesContext fctx;

    @Mock
    private ExternalContext ectx;

    @Mock
    private Event<Feedback> feedbackEvent;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().doReturn(request).when(ectx).getRequest();
        lenient().doReturn(ectx).when(fctx).getExternalContext();
        lenient().doReturn(Locale.GERMAN).when(userSession).getLocale();
        registerBacker = new RegisterBacker(authenticationService, profileService, userSession, fctx, feedbackEvent,
                ResourceBundleMocker.mock(""));
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
    public void testInitNoAccess() {
        doReturn(navHandler).when(application).getNavigationHandler();
        doReturn(application).when(fctx).getApplication();
        doReturn(testUser).when(userSession).getUser();
        registerBacker.init();
        verify(navHandler).handleNavigation(any(), any(), any());
    }

    @Test
    public void testRegister() {
        StringBuffer buffer = new StringBuffer("http://test.de/hello_there.xhtml?someparam=69420&other=kaykay");
        lenient().doReturn(buffer).when(request).getRequestURL();
        doReturn(true).when(profileService).createUser(any());
        doReturn(true).when(authenticationService).register(any(), any());
        assertNotNull(registerBacker.register());
        verify(profileService).createUser(any());
        verify(authenticationService).register(any(), any());
    }

    @Test
    public void testRegisterInvalidUrl() {
        StringBuffer buffer = new StringBuffer("i am not a link");
        doReturn(true).when(profileService).createUser(any());
        lenient().doReturn(buffer).when(request).getRequestURL();
        assertThrows(InternalError.class, () -> registerBacker.register());
    }

    @Test
    public void testRegisterFailCreateUser() {
        StringBuffer buffer = new StringBuffer("http://test.de/hello_there.xhtml?someparam=69420&other=kaykay");
        lenient().doReturn(buffer).when(request).getRequestURL();
        doReturn(false).when(profileService).createUser(any());
        lenient().doReturn(true).when(authenticationService).register(any(), any());
        assertNull(registerBacker.register());
        verify(profileService).createUser(any());
    }

    @Test
    public void testRegisterFailRegister() {
        StringBuffer buffer = new StringBuffer("http://test.de/hello_there.xhtml?someparam=69420&other=kaykay");
        lenient().doReturn(buffer).when(request).getRequestURL();
        doReturn(true).when(profileService).createUser(any());
        doReturn(false).when(authenticationService).register(any(), any());
        assertNull(registerBacker.register());
        verify(profileService).createUser(any());
        verify(authenticationService).register(any(), any());
    }

    @Test
    public void testRegisterFailCreateUserAndRegister() {
        StringBuffer buffer = new StringBuffer("http://test.de/hello_there.xhtml?someparam=69420&other=kaykay");
        lenient().doReturn(buffer).when(request).getRequestURL();
        doReturn(false).when(profileService).createUser(any());
        lenient().doReturn(false).when(authenticationService).register(any(), any());
        assertNull(registerBacker.register());
        verify(profileService).createUser(any());
    }

}