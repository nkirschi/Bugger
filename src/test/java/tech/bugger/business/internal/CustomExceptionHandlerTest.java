package tech.bugger.business.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.persistence.exception.StoreException;

import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomExceptionHandlerTest {

    @Mock
    private ExceptionHandler exceptionHandlerMock;

    @Mock
    private FacesContext fctx;

    @Mock
    private ExternalContext ectx;

    @Mock
    private Iterable<ExceptionQueuedEvent> exceptionQueuedEventIterable;

    @Mock
    private Iterator<ExceptionQueuedEvent> exceptionQueuedEventIterator;

    @Mock
    private ExceptionQueuedEvent exceptionQueuedEvent;

    @Mock
    private ExceptionQueuedEventContext exceptionQueuedEventContext;

    @Mock
    private Throwable throwable;

    @Mock
    private Application application;

    @Mock
    private ViewHandler viewHandler;

    @Mock
    private UIViewRoot viewRoot;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PartialViewContext pvctx;

    @Mock
    private ViewDeclarationLanguage viewDeclarationLanguage;

    @Mock
    private Map<String, Object> requestScope;

    private CustomExceptionHandler customExceptionHandler;

    @BeforeEach
    public void setup() {
        customExceptionHandler = new CustomExceptionHandler(exceptionHandlerMock);
        doReturn(exceptionQueuedEventIterable).when(exceptionHandlerMock).getUnhandledExceptionQueuedEvents();
        doReturn(exceptionQueuedEventIterator).when(exceptionQueuedEventIterable).iterator();
        lenient().doReturn(ectx).when(fctx).getExternalContext();
        lenient().doReturn(application).when(fctx).getApplication();
        lenient().doReturn(viewHandler).when(application).getViewHandler();
        lenient().doReturn(viewRoot).when(viewHandler).createView(any(), any());
        lenient().doReturn(response).when(ectx).getResponse();
        lenient().doReturn(pvctx).when(fctx).getPartialViewContext();
        lenient().doReturn(viewDeclarationLanguage).when(viewHandler).getViewDeclarationLanguage(any(), any());
        lenient().doReturn(requestScope).when(ectx).getRequestMap();
    }

    public void makeEvent() {
        lenient().doReturn(exceptionQueuedEvent).when(exceptionQueuedEventIterator).next();
        lenient().doReturn(exceptionQueuedEventContext).when(exceptionQueuedEvent).getContext();
        lenient().doReturn(throwable).when(exceptionQueuedEventContext).getException();
    }

    @Test
    public void testHandleExceptionWhenFacesContextIsNull() {
        assertDoesNotThrow(() -> customExceptionHandler.handleException(null));
    }

    @Test
    public void testHandleExceptionWhenResponseIsCommitted() {
        doReturn(true).when(ectx).isResponseCommitted();
        assertDoesNotThrow(() -> customExceptionHandler.handleException(fctx));
    }

    @Test
    public void testHandleExceptionWhenNoUnhandledEvents() {
        assertDoesNotThrow(() -> customExceptionHandler.handleException(fctx));
    }

    @Test
    public void testHandleExceptionWhenError404Exception() {
        throwable = new Error404Exception();
        makeEvent();
        doReturn(true, false).when(exceptionQueuedEventIterator).hasNext();
        assertDoesNotThrow(() -> customExceptionHandler.handleException(fctx));
        verify(requestScope).put("Show404", "yes");
        verify(ectx).setResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testHandleExceptionWhenNotError404Exception() {
        makeEvent();
        doReturn(true, false).when(exceptionQueuedEventIterator).hasNext();
        assertDoesNotThrow(() -> customExceptionHandler.handleException(fctx));
        verify(viewHandler).createView(fctx, "/WEB-INF/errorpages/500.xhtml");
        verify(requestScope).put("Show404", "no");
        verify(ectx).setResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testHandleExceptionWhenCauseIsError404Exception() {
        Error404Exception exception = new Error404Exception();
        doReturn(exception).when(throwable).getCause();
        makeEvent();
        doReturn(true, false).when(exceptionQueuedEventIterator).hasNext();
        assertDoesNotThrow(() -> customExceptionHandler.handleException(fctx));
        verify(requestScope).put("Show404", "yes");
        verify(ectx).setResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testHandleExceptionGoToCause() {
        throwable = mock(FacesException.class);
        StoreException exception = new StoreException();
        doReturn(exception).when(throwable).getCause();
        makeEvent();
        doReturn(true, false).when(exceptionQueuedEventIterator).hasNext();
        assertDoesNotThrow(() -> customExceptionHandler.handleException(fctx));
        verify(viewHandler).createView(fctx, "/WEB-INF/errorpages/500.xhtml");
        verify(ectx).setResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testHandleExceptionEmptyUnhandledEvents() {
        makeEvent();
        doReturn(true, true, false).when(exceptionQueuedEventIterator).hasNext();
        assertDoesNotThrow(() -> customExceptionHandler.handleException(fctx));
        verify(viewHandler).createView(fctx, "/WEB-INF/errorpages/500.xhtml");
        verify(ectx).setResponseStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(exceptionQueuedEventIterator, times(2)).next();
        verify(exceptionQueuedEventIterator, times(2)).remove();
    }

    @Test
    public void testHandleExceptionWhenIOException() throws Exception {
        makeEvent();
        doReturn(true, false).when(exceptionQueuedEventIterator).hasNext();
        doThrow(IOException.class).when(viewDeclarationLanguage).buildView(any(), any());
        assertThrows(FacesException.class, () -> customExceptionHandler.handleException(fctx));
    }

    @Test
    public void testHandleExceptionBackupHeaders() {
        makeEvent();
        doReturn(true, false).when(exceptionQueuedEventIterator).hasNext();
        List<String> headerNames = new ArrayList<>();
        headerNames.add("Hello there!");
        doReturn(headerNames).when(response).getHeaderNames();
        doReturn("General Kenobi!").when(response).getHeader("Hello there!");
        assertDoesNotThrow(() -> customExceptionHandler.handleException(fctx));
        verify(ectx).addResponseHeader("Hello there!", "General Kenobi!");
    }
}