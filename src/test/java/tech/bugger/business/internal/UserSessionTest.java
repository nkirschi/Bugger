package tech.bugger.business.internal;

import java.util.Locale;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserSessionTest {

    private UserSession session;

    private MockedStatic<FacesContext> fctxStatic;

    @Mock
    private FacesContext fctx;

    @Mock
    private ExternalContext ectx;

    @BeforeEach
    public void setup() {
        fctxStatic = mockStatic(FacesContext.class);
        when(FacesContext.getCurrentInstance()).thenReturn(fctx);
        doReturn(ectx).when(fctx).getExternalContext();
        session = new UserSession();
    }

    @AfterEach
    public void tearDown() {
        fctxStatic.close();
    }

    @Test
    public void testInitGerman() {
        doReturn(Locale.GERMAN).when(ectx).getRequestLocale();
        session.init();
        assertEquals(Locale.GERMAN, session.getLocale());
    }

    @Test
    public void testInitEnglish() {
        doReturn(Locale.ENGLISH).when(ectx).getRequestLocale();
        session.init();
        assertEquals(Locale.ENGLISH, session.getLocale());
    }

}