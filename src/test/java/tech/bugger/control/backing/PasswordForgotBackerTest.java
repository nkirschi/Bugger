package tech.bugger.control.backing;

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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.service.ProfileService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class PasswordForgotBackerTest {

    private PasswordForgotBacker backer;

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

    private User testUser1;

    private User testUser2;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().doReturn(request).when(ectx).getRequest();
        lenient().doReturn(ectx).when(fctx).getExternalContext();
        lenient().doReturn(Locale.GERMAN).when(userSession).getLocale();
        backer = new PasswordForgotBacker(authenticationService, profileService, userSession, fctx, feedbackEvent,
                ResourceBundleMocker.mock(""));
        testUser1 = new User();
        testUser2 = new User();
    }

    @Test
    public void testInitLoggedIn() {
        doReturn(navHandler).when(application).getNavigationHandler();
        doReturn(application).when(fctx).getApplication();
        doReturn(testUser1).when(userSession).getUser();
        backer.init();
        verify(navHandler).handleNavigation(any(), any(), any());
    }

    @Test
    public void testInit() {
        doReturn(null).when(userSession).getUser();
        backer.init();
        assertNotNull(backer.getUser());
    }

    @Test
    public void testForgotPasswordWrongEmail() {
        testUser1.setId(100);
        testUser2.setId(101);
        backer.setUser(testUser1);
        doReturn(testUser1).when(profileService).getUserByEmail(any());
        doReturn(testUser2).when(profileService).getUserByUsername(any());
        assertNull(backer.forgotPassword());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testForgotPasswordUserNotFound() {
        testUser1.setId(100);
        testUser2.setId(101);
        backer.setUser(testUser1);
        doReturn(null).when(profileService).getUserByEmail(any());
        doReturn(testUser2).when(profileService).getUserByUsername(any());
        assertNull(backer.forgotPassword());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testForgotPasswordWhenError() {
        try (MockedStatic<AuthenticationService> serviceMock = mockStatic(AuthenticationService.class)) {
            serviceMock.when(() -> AuthenticationService.getApplicationPath(any())).thenReturn("https://bugger.tech");
            testUser1.setId(100);
            backer.setUser(testUser1);
            doReturn(true).when(authenticationService).forgotPassword(any(), any());
            doReturn(testUser1).when(profileService).getUserByEmail(any());
            doReturn(testUser1).when(profileService).getUserByUsername(any());
            assertNotNull(backer.forgotPassword());
            verify(feedbackEvent).fire(any());
        }
    }

    @Test
    public void testForgotPasswordSuccess() {
        try (MockedStatic<AuthenticationService> serviceMock = mockStatic(AuthenticationService.class)) {
            serviceMock.when(() -> AuthenticationService.getApplicationPath(any())).thenReturn("https://bugger.tech");
            testUser1.setId(100);
            backer.setUser(testUser1);
            doReturn(false).when(authenticationService).forgotPassword(any(), any());
            doReturn(testUser1).when(profileService).getUserByEmail(any());
            doReturn(testUser1).when(profileService).getUserByUsername(any());
            assertNull(backer.forgotPassword());
        }
    }

}