package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;

@ExtendWith(LogExtension.class)
public class LoginBackerTest {

    @InjectMocks
    private LoginBacker loginBacker;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserSession session;

    @Mock
    private FacesContext fctx;

    @Mock
    private ExternalContext context;

    @Mock
    private HttpServletRequest request;

    private User user;
    private final String home = "home";
    private final String profile = "profile";

    @BeforeEach
    public void setup() {
        user = new User(12345, "Helgi", "v3rys3cur3", "salt", "algorithm", "helga@web.de", "Helga", "Brötchen",
                new Lazy<>(new byte[0]), null, "Hallo, ich bin die Helgi | Perfect | He/They/Her | vergeben | Abo =|= "
                + "endorsement", Language.GERMAN, User.ProfileVisibility.MINIMAL, ZonedDateTime.now(), null, false);
        MockitoAnnotations.openMocks(this);
        loginBacker = new LoginBacker(authenticationService, session, fctx);
        loginBacker.setUsername(user.getUsername());
        loginBacker.setPassword(user.getPasswordHash());
        when(fctx.getExternalContext()).thenReturn(context);
        when(fctx.getExternalContext().getRequest()).thenReturn(request);
    }

    @Test
    public void testInit() {
        when(session.getLocale()).thenReturn(Locale.GERMAN);
        loginBacker.init();
        assertAll(
                () -> assertEquals("", loginBacker.getRedirectURL()),
                () -> assertEquals(Language.GERMAN, loginBacker.getUser().getPreferredLanguage())
        );
    }

    @Test
    public void testInitNoPreferredLanguage() {
        when(session.getLocale()).thenReturn(null);
        loginBacker.init();
        assertEquals(Language.ENGLISH, loginBacker.getUser().getPreferredLanguage());
    }

    @Test
    public void testInitUserNotNull() throws IOException {
        when(session.getUser()).thenReturn(user);
        loginBacker.init();
        verify(context, times(1)).redirect(any());
    }

    @Test
    public void testInitUserIOException() throws IOException {
        when(session.getUser()).thenReturn(user);
        doThrow(IOException.class).when(context).redirect(any());
        assertThrows(InternalError.class,
                () -> loginBacker.init()
        );
        verify(context, times(1)).redirect(any());
    }

    @Test
    public void testInitRedirectURL() {
        when(request.getParameter(any())).thenReturn(home);
        loginBacker.init();
        assertEquals(home, loginBacker.getRedirectURL());
    }

    @Test
    public void testLogin() {
        loginBacker.setRedirectURL("");
        when(authenticationService.authenticate(loginBacker.getUsername(), loginBacker.getPassword())).thenReturn(user);
        assertAll(
                () -> assertEquals(home, loginBacker.login()),
                () -> assertEquals(user, loginBacker.getUser())
        );
        verify(authenticationService, times(1)).authenticate(any(), anyString());
    }

    @Test
    public void testLoginRedirectURL() {
        loginBacker.setRedirectURL(profile);
        when(authenticationService.authenticate(loginBacker.getUsername(), loginBacker.getPassword())).thenReturn(user);
        assertAll(
                () -> assertEquals(profile, loginBacker.login()),
                () -> assertEquals(user, loginBacker.getUser())
        );
        verify(authenticationService, times(1)).authenticate(any(), anyString());
    }

    @Test
    public void testLoginUserNull() {
        when(authenticationService.authenticate(any(), anyString())).thenReturn(null);
        assertAll(
                () -> assertNull(loginBacker.login())
        );
        verify(authenticationService, times(1)).authenticate(any(), anyString());
    }
}
