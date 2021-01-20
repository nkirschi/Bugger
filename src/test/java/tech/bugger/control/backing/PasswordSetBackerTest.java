package tech.bugger.control.backing;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import javax.enterprise.event.Event;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class PasswordSetBackerTest {

    private PasswordSetBacker passwordSetBacker;

    private User testUser;

    private Token testToken;

    @Mock
    private NavigationHandler navHandler;

    @Mock
    private Application application;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserSession userSession;

    @Mock
    private ExternalContext ectx;

    @Mock
    private FacesContext fctx;

    @Mock
    private Event<Feedback> feedbackEvent;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().doReturn(navHandler).when(application).getNavigationHandler();
        lenient().doReturn(application).when(fctx).getApplication();
        lenient().doReturn(ectx).when(fctx).getExternalContext();
        lenient().doReturn(Locale.GERMAN).when(userSession).getLocale();
        passwordSetBacker = new PasswordSetBacker(authenticationService, userSession, fctx, feedbackEvent,
                ResourceBundleMocker.mock(""));
        testUser = new User();
        testToken = new Token("0123456789abcdef", Token.Type.REGISTER,
                OffsetDateTime.of(1999, 10, 3, 22, 13, 0, 0, ZoneOffset.UTC), "", testUser);
    }

    @Test
    public void testInitTokenTypeRegister() {
        doReturn(testToken).when(authenticationService).findToken(any());
        passwordSetBacker.init();
        verify(navHandler, never()).handleNavigation(any(), any(), any());
        assertEquals(testToken, passwordSetBacker.getToken());
    }

    @Test
    public void testInitTokenTypeForgotPassword() {
        testToken.setType(Token.Type.FORGOT_PASSWORD);
        doReturn(testToken).when(authenticationService).findToken(any());
        passwordSetBacker.init();
        verify(navHandler, never()).handleNavigation(any(), any(), any());
        assertEquals(testToken, passwordSetBacker.getToken());
    }

    @Test
    public void testInitTokenTypeChangeEmail() {
        testToken.setType(Token.Type.CHANGE_EMAIL);
        doReturn(testToken).when(authenticationService).findToken(any());
        passwordSetBacker.init();
        verify(navHandler).handleNavigation(any(), any(), any());
    }

    @Test
    public void testInitLoggedIn() {
        doReturn(testUser).when(userSession).getUser();
        passwordSetBacker.init();
        verify(navHandler).handleNavigation(any(), any(), any());
    }

    @Test
    public void testInitNoTokenFound() {
        doReturn(null).when(authenticationService).findToken(any());
        passwordSetBacker.init();
        verify(navHandler).handleNavigation(any(), any(), any());
    }

    @Test
    public void testSetUserPasswordSuccess() {
        doReturn(true).when(authenticationService).setPassword(any(), any(), any());
        passwordSetBacker.setToken(testToken);
        String redirect = passwordSetBacker.setUserPassword();
        assertAll(() -> assertEquals("pretty:home", redirect),
                () -> verify(userSession).setUser(any()));
    }

    @Test
    public void testSetUserPasswordFail() {
        doReturn(false).when(authenticationService).setPassword(any(), any(), any());
        passwordSetBacker.setToken(testToken);
        String redirect = passwordSetBacker.setUserPassword();
        assertAll(() -> assertNull(redirect),
                () -> verify(userSession, never()).setUser(any()));
    }

    @Test
    public void testIsTokenValidTypeRegister() {
        passwordSetBacker.setToken(testToken);
        assertTrue(passwordSetBacker.isValidToken());
    }

    @Test
    public void testIsTokenValidYesTypeForgotPassword() {
        testToken.setType(Token.Type.FORGOT_PASSWORD);
        passwordSetBacker.setToken(testToken);
        assertTrue(passwordSetBacker.isValidToken());
    }

    @Test
    public void testIsTokenValidTypeChangeEmail() {
        testToken.setType(Token.Type.CHANGE_EMAIL);
        passwordSetBacker.setToken(testToken);
        assertFalse(passwordSetBacker.isValidToken());
    }

    @Test
    public void testIsTokenValidNo() {
        passwordSetBacker.setToken(null);
        assertFalse(passwordSetBacker.isValidToken());
    }

}