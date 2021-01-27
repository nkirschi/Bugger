package tech.bugger.control.servlet;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletContext;
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
import tech.bugger.business.service.ProfileService;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class AvatarServletTest {

    @InjectMocks
    private AvatarServlet servlet;

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    private UserSession session;

    @Mock
    private ProfileService profileService;

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
    public void testHandleRequestNoParams() {
        doReturn(true).when(configuration).isGuestReading();
        doReturn(null).when(request).getParameter("id");
        doReturn(null).when(request).getParameter("u");
        servlet.handleRequest(request, response);
        verify(servlet).redirectToNotFoundPage(response);
    }

    @Test
    public void testHandleRequestNoDefaultImage() throws Exception {
        doReturn(true).when(configuration).isGuestReading();
        doReturn("1234").when(request).getParameter("id");
        doReturn("thumbnail").when(request).getParameter("type");

        ServletContext sctx = mock(ServletContext.class);
        doReturn(sctx).when(servlet).getServletContext();
        InputStream is = mock(InputStream.class);
        doReturn(is).when(sctx).getResourceAsStream(any());
        doThrow(IOException.class).when(is).readAllBytes();

        User user = new User();
        user.setId(1234);
        user.setAvatarThumbnail(new byte[0]);
        doReturn(user).when(profileService).getUser(1234);

        assertDoesNotThrow(() -> servlet.handleRequest(request, response));
        verify(servlet).redirectToNotFoundPage(response);
    }

    @Test
    public void testHandleRequestWriteSuccessful() throws Exception {
        doReturn(true).when(configuration).isGuestReading();
        lenient().doReturn("admin").when(request).getParameter("u");
        lenient().doReturn("thumbnail").when(request).getParameter("type");

        byte[] thumbnail = new byte[]{1, 2, 3, 4};
        User user = new User();
        user.setId(1234);
        user.setAvatarThumbnail(thumbnail);
        lenient().doReturn(user).when(profileService).getUserByUsername("admin");

        ServletOutputStream os = mock(ServletOutputStream.class);
        doReturn(os).when(response).getOutputStream();

        servlet.handleRequest(request, response);
        verify(servlet).configureClientCaching(response);
        verify(os).write(thumbnail);
    }

    @Test
    public void testHandleRequestWriteUnsuccessful() throws Exception {
        doReturn(true).when(configuration).isGuestReading();
        lenient().doReturn("admin").when(request).getParameter("u");
        lenient().doReturn(null).when(request).getParameter("type");

        User user = new User();
        user.setId(1234);
        lenient().doReturn(user).when(profileService).getUserByUsername("admin");

        ServletContext sctx = mock(ServletContext.class);
        doReturn(sctx).when(servlet).getServletContext();
        InputStream is = mock(InputStream.class);
        doReturn(is).when(sctx).getResourceAsStream(any());
        byte[] defaultAvatar = new byte[]{1, 2, 3, 4};
        doReturn(defaultAvatar).when(is).readAllBytes();

        doReturn(new byte[0]).when(profileService).getAvatarForUser(1234);
        ServletOutputStream os = mock(ServletOutputStream.class);
        doReturn(os).when(response).getOutputStream();
        doThrow(IOException.class).when(os).write(defaultAvatar);

        assertDoesNotThrow(() -> servlet.handleRequest(request, response));
    }

    @Test
    public void testHandleRequestWriteUnsuccessfulReturnsNull() throws Exception {
        doReturn(true).when(configuration).isGuestReading();
        lenient().doReturn("admin").when(request).getParameter("u");
        lenient().doReturn(null).when(request).getParameter("type");

        User user = new User();
        user.setId(1234);
        lenient().doReturn(user).when(profileService).getUserByUsername("admin");

        ServletContext sctx = mock(ServletContext.class);
        doReturn(sctx).when(servlet).getServletContext();
        InputStream is = mock(InputStream.class);
        doReturn(is).when(sctx).getResourceAsStream(any());
        byte[] defaultAvatar = new byte[]{1, 2, 3, 4};
        doReturn(defaultAvatar).when(is).readAllBytes();

        doReturn(null).when(profileService).getAvatarForUser(1234);
        ServletOutputStream os = mock(ServletOutputStream.class);
        doReturn(os).when(response).getOutputStream();
        doThrow(IOException.class).when(os).write(defaultAvatar);

        assertDoesNotThrow(() -> servlet.handleRequest(request, response));
    }

}