package tech.bugger.control.backing;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
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
    private AuthenticationService authenticationService;

    @Mock
    private UserSession userSession;

    @Mock
    private ExternalContext ectx;

    @Mock
    private Event<Feedback> feedbackEvent;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().doReturn(Locale.GERMAN).when(userSession).getLocale();
        passwordSetBacker = new PasswordSetBacker(authenticationService, userSession, ectx, feedbackEvent,
                ResourceBundleMocker.mock(""));
        testUser = new User();
        testToken = new Token("0123456789abcdef", Token.Type.REGISTER,
                ZonedDateTime.of(1999, 10, 3, 22, 13, 0, 0, ZoneId.systemDefault()), testUser);
    }

    @Test
    public void testInitTokenTypeRegister() throws Exception {
        doReturn(testToken).when(authenticationService).findToken(any());
        passwordSetBacker.init();
        verify(ectx, never()).redirect("home.xhtml");
        assertEquals(testToken, passwordSetBacker.getToken());
    }

    @Test
    public void testInitTokenTypeForgotPassword() throws Exception {
        testToken.setType(Token.Type.FORGOT_PASSWORD);
        doReturn(testToken).when(authenticationService).findToken(any());
        passwordSetBacker.init();
        verify(ectx, never()).redirect("home.xhtml");
        assertEquals(testToken, passwordSetBacker.getToken());
    }

    @Test
    public void testInitTokenTypeChangeEmail() throws Exception {
        testToken.setType(Token.Type.CHANGE_EMAIL);
        doReturn(testToken).when(authenticationService).findToken(any());
        passwordSetBacker.init();
        verify(ectx, never()).redirect("home.xhtml");
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testInitLoggedIn() throws Exception {
        doReturn(testUser).when(userSession).getUser();
        passwordSetBacker.init();
        verify(ectx).redirect("home.xhtml");
    }

    @Test
    public void testInitLoggedInAndException() throws Exception {
        User copy = new User(testUser);
        doReturn(copy).when(userSession).getUser();
        doThrow(IOException.class).when(ectx).redirect(any());
        assertThrows(InternalError.class, () -> passwordSetBacker.init());
    }

    @Test
    public void testInitNoTokenFound() throws Exception {
        doReturn(null).when(authenticationService).findToken(any());
        passwordSetBacker.init();
        verify(ectx, never()).redirect("home.xhtml");
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSetUserPasswordSuccess() {
        doReturn(true).when(authenticationService).setPassword(any(), any(), any());
        passwordSetBacker.setToken(testToken);
        String redirect = passwordSetBacker.setUserPassword();
        assertEquals("home.xhtml", redirect);
    }

    @Test
    public void testSetUserPasswordFail() {
        doReturn(false).when(authenticationService).setPassword(any(), any(), any());
        passwordSetBacker.setToken(testToken);
        String redirect = passwordSetBacker.setUserPassword();
        assertNull(redirect);
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