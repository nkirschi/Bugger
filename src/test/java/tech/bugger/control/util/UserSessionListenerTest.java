package tech.bugger.control.util;

import java.util.Locale;
import javax.faces.context.ExternalContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.business.internal.UserSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserSessionListenerTest {

    private UserSessionListener listener;

    private UserSession session;

    @Mock
    ExternalContext externalContext;

    @BeforeEach
    public void setup() {
        session = new UserSession();
        listener = new UserSessionListener();
        listener.setExternalContext(externalContext);
        listener.setUserSession(session);
    }

    @Test
    public void testSessionCreatedEnglish() {
        doReturn(Locale.ENGLISH).when(externalContext).getRequestLocale();
        listener.sessionCreated(null);
        assertEquals(Locale.ENGLISH, session.getLocale());
    }

    @Test
    public void testSessionCreatedGerman() {
        doReturn(Locale.GERMAN).when(externalContext).getRequestLocale();
        listener.sessionCreated(null);
        assertEquals(Locale.GERMAN, session.getLocale());
    }

}