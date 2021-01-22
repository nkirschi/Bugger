package tech.bugger.control.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.global.transfer.Organization;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class MediaServletTest {

    private MediaServlet servlet;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        servlet = spy(new MediaServlet() {
            @Override
            protected void handleRequest(HttpServletRequest request, HttpServletResponse response) {
            }
        });
    }

    @Test
    public void testDoGet() {
        servlet.doGet(request, response);
        verify(servlet).handleRequest(request, response);
    }

    @Test
    public void testDoPost() {
        servlet.doPost(request, response);
        verify(servlet).handleRequest(request, response);
    }

    @Test
    public void testRedirectToNotFoundPage() throws Exception {
        servlet.redirectToNotFoundPage(response);
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testRedirectToNotFoundPageError() throws Exception {
        doThrow(IOException.class).when(response).sendError(anyInt());
        assertDoesNotThrow(() -> servlet.redirectToNotFoundPage(response));
    }

    @Test
    public void testConfigureClientCaching() {
        servlet.configureClientCaching(response);
        verify(response).setDateHeader(eq("Expires"), anyLong());
    }

}