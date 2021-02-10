package tech.bugger.business.internal;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class SessionIDChangerTest {

    private SessionIDChanger sessionIDChanger;

    @Mock
    private PhaseEvent phaseEvent;

    @Mock
    private FacesContext fctx;

    @Mock
    private PartialViewContext pctx;

    @Mock
    private ExternalContext ectx;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    public void setUp() {
        sessionIDChanger = new SessionIDChanger();
        lenient().doReturn(fctx).when(phaseEvent).getFacesContext();
        lenient().doReturn(pctx).when(fctx).getPartialViewContext();
        lenient().doReturn(ectx).when(fctx).getExternalContext();
        lenient().doReturn(request).when(ectx).getRequest();
    }

    @Test
    public void testGetPhaseId() {
        assertEquals(PhaseId.RESTORE_VIEW, sessionIDChanger.getPhaseId());
    }

    @Test
    public void testBeforePhase() {
        assertDoesNotThrow(() -> sessionIDChanger.beforePhase(phaseEvent));
    }

    @Test
    public void testAfterPhaseWhenSessionExists() {
        doReturn(mock(HttpSession.class)).when(ectx).getSession(false);
        sessionIDChanger.afterPhase(phaseEvent);
        verify(request).changeSessionId();
    }

    @Test
    public void testAfterPhaseWhenSessionNotExists() {
        lenient().doReturn(null).when(ectx).getSession(false);
        sessionIDChanger.afterPhase(phaseEvent);
        verify(request, never()).changeSessionId();
    }

    @Test
    public void testAfterPhaseWhenAjaxRequest() {
        doReturn(true).when(pctx).isAjaxRequest();
        sessionIDChanger.afterPhase(phaseEvent);
        verify(request, never()).changeSessionId();
    }

}