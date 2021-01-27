package tech.bugger.control.servlet;

import java.io.IOException;
import java.io.Serial;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
            @Serial
            private static final long serialVersionUID = 6640097133536637613L;

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
    public void testConfigureClientCaching() {
        servlet.configureClientCaching(response);
        verify(response).setDateHeader(eq("Expires"), anyLong());
    }

}