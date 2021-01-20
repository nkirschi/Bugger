package tech.bugger.business.internal;

import com.sun.faces.lifecycle.Phase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.util.Registry;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.TokenGateway;
import tech.bugger.persistence.gateway.UserGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class TrespassListenerTest {

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    private Registry registry;

    private Instance<UserSession> sessionInstance;

    @Mock
    private PhaseEvent event;

    @Mock
    private FacesContext fctx;

    @Mock
    private ExternalContext ectx;

    private TrespassListener trespassListener;

    @BeforeEach
    public void setUp() throws Exception {
        trespassListener = new TrespassListener();
        /*Field applicationSettingsField = TrespassListener.class.getDeclaredField("applicationSettings");
        applicationSettingsField.setAccessible(true);
        applicationSettingsField.set(trespassListener, applicationSettings);
        Field registryField = TrespassListener.class.getDeclaredField("applicationSettings");
        registryField.setAccessible(true);
        registryField.set(trespassListener, registry);*/

        /*try (MockedStatic<CDI> cdiMockedStatic = Mockito.mockStatic(CDI.class)) {
            CDI cdi = mock(CDI.class);
            cdiMockedStatic.when(CDI::current).thenReturn(cdi);
            Instance<ApplicationSettings> applicationSettingsInstance = mock(Instance.class);
            doReturn(applicationSettingsInstance).when(cdi).select(ApplicationSettings.class);
            doReturn(applicationSettings).when(applicationSettingsInstance).get();
            Instance<Registry> registryInstance = mock(Instance.class);
            doReturn(registryInstance).when(cdi).select(Registry.class);
            doReturn(registry).when(registryInstance).get();
            sessionInstance = mock(Instance.class);
            doReturn(sessionInstance).when(cdi).select(UserSession.class);
        }*/

        doReturn(fctx).when(event).getFacesContext();
        doReturn(ectx).when(fctx).getExternalContext();
    }

    @Test
    public void testGetPhaseId() {
        assertEquals(PhaseId.RESTORE_VIEW, trespassListener.getPhaseId());
    }

    @Test
    public void testAfterPhaseViewRootNull() throws Exception {
        doReturn(null).when(fctx).getViewRoot();
        trespassListener.afterPhase(event);
        verify(ectx).redirect(anyString());
    }

    @Test
    public void testAfterPhaseViewIdNull() throws Exception {
        UIViewRoot viewRoot = mock(UIViewRoot.class);
        doReturn(viewRoot).when(fctx).getViewRoot();
        doReturn(null).when(viewRoot).getViewId();
        trespassListener.afterPhase(event);
        verify(ectx).redirect(anyString());
    }

    @Test
    public void testAfterPhaseAdminUserNull() throws Exception {
        UIViewRoot viewRoot = mock(UIViewRoot.class);
        doReturn(viewRoot).when(fctx).getViewRoot();
        doReturn("/test/admin.xhtml").when(viewRoot).getViewId();
        doReturn(null).when(sessionInstance).get();
        trespassListener.afterPhase(event);
        verify(ectx).redirect(anyString());
    }

}