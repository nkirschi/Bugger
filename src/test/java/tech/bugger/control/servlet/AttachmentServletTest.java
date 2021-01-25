package tech.bugger.control.servlet;

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.PostService;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class AttachmentServletTest {

    @InjectMocks
    private AttachmentServlet servlet;

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    private UserSession session;

    @Mock
    private PostService postService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Configuration configuration;

    @BeforeEach
    public void setUp() {
        doReturn(configuration).when(applicationSettings).getConfiguration();
        servlet = spy(servlet);
    }

    @Test
    public void testHandleRequestNoGuestReading() {
        doReturn(false).when(configuration).isGuestReading();
        doReturn(mock(User.class)).when(session).getUser();
        servlet.handleRequest(request, response);
        verify(servlet).redirectToNotFoundPage(response);
    }

    @Test
    public void testHandleRequestNoUser() {
        doReturn(false).when(configuration).isGuestReading();
        doReturn(null).when(session).getUser();
        servlet.handleRequest(request, response);
        verify(servlet).redirectToNotFoundPage(response);
    }

    @Test
    public void testHandleRequestInvalidParam() {
        doReturn(true).when(configuration).isGuestReading();
        doReturn("weißwurscht").when(request).getParameter("id");
        servlet.handleRequest(request, response);
        verify(servlet).redirectToNotFoundPage(response);
    }

    @Test
    public void testHandleRequestAttachmentNotFound() {
        doReturn(true).when(configuration).isGuestReading();
        doReturn("1234").when(request).getParameter("id");
        doReturn(null).when(postService).getAttachmentByID(1234);
        servlet.handleRequest(request, response);
        verify(servlet).redirectToNotFoundPage(response);
    }

    @Test
    public void testHandleRequestAttachmentContentNotFound() {
        doReturn(true).when(configuration).isGuestReading();
        doReturn("1234").when(request).getParameter("id");
        doReturn(mock(Attachment.class)).when(postService).getAttachmentByID(1234);
        doReturn(null).when(postService).getAttachmentContent(1234);
        servlet.handleRequest(request, response);
        verify(servlet).redirectToNotFoundPage(response);
    }

    @Test
    public void testHandleRequestWriteSuccessful() throws Exception {
        doReturn(true).when(configuration).isGuestReading();
        doReturn("1234").when(request).getParameter("id");
        doReturn(mock(Attachment.class)).when(postService).getAttachmentByID(1234);

        byte[] content = new byte[]{1, 2, 3, 4};
        doReturn(content).when(postService).getAttachmentContent(1234);
        ServletOutputStream os = mock(ServletOutputStream.class);
        doReturn(os).when(response).getOutputStream();

        servlet.handleRequest(request, response);
        verify(servlet).configureClientCaching(response);
        verify(response).setContentLength(content.length);
        verify(os).write(content);
    }

    @Test
    public void testHandleRequestWriteUnsuccessful() throws Exception {
        doReturn(true).when(configuration).isGuestReading();
        doReturn("1234").when(request).getParameter("id");
        doReturn(mock(Attachment.class)).when(postService).getAttachmentByID(1234);

        byte[] content = new byte[]{1, 2, 3, 4};
        doReturn(content).when(postService).getAttachmentContent(1234);
        ServletOutputStream os = mock(ServletOutputStream.class);
        doReturn(os).when(response).getOutputStream();
        doThrow(IOException.class).when(os).write(any());

        assertDoesNotThrow(() -> servlet.handleRequest(request, response));
    }

}