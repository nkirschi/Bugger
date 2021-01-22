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
import tech.bugger.global.transfer.Organization;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class LogoServletTest {

    @InjectMocks
    private LogoServlet servlet;

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Organization organization;

    @BeforeEach
    public void setUp() {
        doReturn(organization).when(applicationSettings).getOrganization();
        servlet = spy(servlet);
    }

    @Test
    public void testHandleRequestNoLogo() {
        doReturn(null).when(organization).getLogo();
        servlet.handleRequest(request, response);
        verify(servlet).redirectToNotFoundPage(response);
    }

    @Test
    public void testHandleRequestWriteSuccessful() throws Exception {
        byte[] logo = new byte[]{1, 2, 3, 4};
        doReturn(logo).when(organization).getLogo();

        ServletOutputStream os = mock(ServletOutputStream.class);
        doReturn(os).when(response).getOutputStream();

        servlet.handleRequest(request, response);
        verify(servlet).configureClientCaching(response);
        verify(os).write(logo);
    }

    @Test
    public void testHandleRequestWriteUnsuccessful() throws Exception {
        byte[] logo = new byte[]{1, 2, 3, 4};
        doReturn(logo).when(organization).getLogo();

        ServletOutputStream os = mock(ServletOutputStream.class);
        doReturn(os).when(response).getOutputStream();
        doThrow(IOException.class).when(os).write(any());

        assertDoesNotThrow(() -> servlet.handleRequest(request, response));
    }

}