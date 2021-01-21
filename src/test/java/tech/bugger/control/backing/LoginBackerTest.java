package tech.bugger.control.backing;

import com.sun.faces.context.RequestParameterMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.AuthenticationService;
import tech.bugger.global.transfer.User;

import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private RequestParameterMap map;

    @Mock
    private NavigationHandler navHandler;

    @Mock
    private Application application;

    private User user;
    private final String home = "home";
    private final String profile = "/profile?u=admin";
    private final String profileEncoded = URLEncoder.encode(profile, StandardCharsets.UTF_8);

    @BeforeEach
    public void setup() {
        user = new User(12345, "Helgi", "v3rys3cur3", "salt", "algorithm", "helga@web.de", "Helga", "Brötchen",
                        new byte[0], null, "Hallo, ich bin die Helgi | Perfect | He/They/Her | vergeben | Abo =|= "
                                + "endorsement", Locale.GERMAN, User.ProfileVisibility.MINIMAL, OffsetDateTime.now(),
                        null, false);
        MockitoAnnotations.openMocks(this);
        loginBacker = new LoginBacker(authenticationService, session, fctx);
        loginBacker.setUsername(user.getUsername());
        loginBacker.setPassword(user.getPasswordHash());
        when(fctx.getExternalContext()).thenReturn(context);
        when(context.getRequestParameterMap()).thenReturn(map);
    }

    @Test
    public void testInit() {
        loginBacker.init();
        assertNull(loginBacker.getRedirectURL());
    }

    @Test
    public void testInitUserNotNull() {
        when(session.getUser()).thenReturn(user);
        when(fctx.getApplication()).thenReturn(application);
        when(application.getNavigationHandler()).thenReturn(navHandler);
        loginBacker.init();
        verify(navHandler).handleNavigation(any(), any(), anyString());
    }

    @Test
    public void testInitRedirectURL() {
        when(map.get(anyString())).thenReturn(home);
        loginBacker.init();
        assertEquals(home, loginBacker.getRedirectURL());
    }

    @Test
    public void testLoginRedirectURL() throws Exception {
        loginBacker.setRedirectURL(profileEncoded);
        when(authenticationService.authenticate(loginBacker.getUsername(), loginBacker.getPassword())).thenReturn(user);
        assertNull(loginBacker.login());
        verify(authenticationService).authenticate(any(), anyString());
        verify(context).redirect(profile);
    }

    @Test
    public void testLoginIOException() throws Exception {
        loginBacker.setRedirectURL(profile);
        doThrow(IOException.class).when(context).redirect(any());
        when(authenticationService.authenticate(loginBacker.getUsername(), loginBacker.getPassword())).thenReturn(user);
        assertEquals("pretty:home", loginBacker.login());
        verify(authenticationService).authenticate(any(), anyString());
    }

    @Test
    public void testLoginNoRedirectURL() throws Exception {
        loginBacker.setRedirectURL(null);
        when(authenticationService.authenticate(loginBacker.getUsername(), loginBacker.getPassword())).thenReturn(user);
        assertEquals("pretty:home", loginBacker.login());
        verify(authenticationService).authenticate(any(), anyString());
        verify(context, never()).redirect(profile);
    }

    @Test
    public void testLoginUserNull() {
        assertNull(loginBacker.login());
        verify(authenticationService).authenticate(any(), anyString());
    }

}
