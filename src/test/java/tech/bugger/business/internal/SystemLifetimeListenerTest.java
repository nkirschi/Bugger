package tech.bugger.business.internal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.util.ConfigReader;
import tech.bugger.persistence.util.ConnectionPool;
import tech.bugger.persistence.util.Mailer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SystemLifetimeListenerTest {
    private SystemLifetimeListener systemLifetimeListener;

    private MockedStatic<Log> logMock;

    private MockedStatic<ConfigReader> configReaderMock;

    private MockedStatic<ConnectionPool> connectionPoolMock;

    private MockedStatic<Mailer> mailerMock;

    private ConfigReader configReader;
    private ConnectionPool connectionPool;
    private Mailer mailer;

    private ServletContextEvent sce;
    private ServletContext sctx;

    @BeforeEach
    public void init() {
        systemLifetimeListener = new SystemLifetimeListener();

        logMock = mockStatic(Log.class);
        configReaderMock = mockStatic(ConfigReader.class);
        connectionPoolMock = mockStatic(ConnectionPool.class);
        mailerMock = mockStatic(Mailer.class);

        sce = mock(ServletContextEvent.class);
        sctx = mock(ServletContext.class);
        when(sctx.getResourceAsStream(any())).thenReturn(new ByteArrayInputStream(new byte[]{}));
        when(sce.getServletContext()).thenReturn(sctx);

        configReader = mock(ConfigReader.class);
        when(configReader.getString(any())).thenReturn(null);
        when(configReader.getString("DB_DRIVER")).thenReturn("org.postgresql.Driver");
        when(configReader.getString("DB_URL")).thenReturn("jdbc:postgresql://bueno.fim.uni-passau.de:5432/sep20g02t");
        when(configReader.getInt(any())).thenReturn(42);
        when(configReader.getBoolean(any())).thenReturn(false);
        configReaderMock.when(ConfigReader::getInstance).thenReturn(configReader);

        connectionPool = mock(ConnectionPool.class);
        doNothing().when(connectionPool).init(any(), any(), any(), anyInt(), anyInt(), anyInt());
        doNothing().when(connectionPool).shutdown();
        connectionPoolMock.when(ConnectionPool::getInstance).thenReturn(connectionPool);

        mailer = mock(Mailer.class);
        mailerMock.when(Mailer::getInstance).thenReturn(mailer);
    }

    @AfterEach
    public void reset() {
        logMock.close();
        configReaderMock.close();
        connectionPoolMock.close();
        mailerMock.close();
    }

    @Test
    public void testContextInitializedInitializesLog() {
        systemLifetimeListener.contextInitialized(sce);
        logMock.verify(() -> Log.init(any()));
    }

    @Test
    public void testContextInitializedWhenLogInitFails() {
        logMock.when(() -> Log.init(any())).thenThrow(IOException.class);
        assertThrows(InternalError.class, () -> systemLifetimeListener.contextInitialized(sce));
    }

    @Test
    public void testContextInitializedInitializesConfigReader() throws IOException {
        systemLifetimeListener.contextInitialized(sce);
        verify(configReader).load(any());
    }

    @Test
    public void testContextInitializedWhenConfigLoadingFails() throws IOException {
        doThrow(IOException.class).when(configReader).load(any());
        assertThrows(InternalError.class, () -> systemLifetimeListener.contextInitialized(sce));
    }

    @Test
    public void testContextInitializedInitializesConnectionPool() {
        systemLifetimeListener.contextInitialized(sce);
        verify(connectionPool).init(any(), any(), any(), anyInt(), anyInt(), anyInt());
    }

    @Test
    public void testContextInitializedWhenConnectionPoolInitFails() throws IOException {
        InputStream mockStream = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
        mockStream.close();
        when(sctx.getResourceAsStream("/WEB-INF/jdbc.properties")).thenReturn(mockStream);
        assertThrows(InternalError.class, () -> systemLifetimeListener.contextInitialized(sce));
    }

    @Test
    public void testContextInitializedInitializesMailer() throws IOException {
        systemLifetimeListener.contextInitialized(sce);
        verify(mailer).configure(any(), any(), any());
    }

    @Test
    public void testContextInitializedWhenMailerInitFails() throws IOException {
        doThrow(IOException.class).when(mailer).configure(any(), any(), any());
        assertThrows(InternalError.class, () -> systemLifetimeListener.contextInitialized(sce));
    }

    @Test
    public void testContextInitializedAddsShutdownHook() throws IllegalAccessException,
            NoSuchFieldException, ClassNotFoundException {
        systemLifetimeListener.contextInitialized(sce);
        assertFalse(getShutdownHooks().isEmpty());
    }

    @Test
    public void testContextDestroyedShutsDownConnectionPool() {
        systemLifetimeListener.contextDestroyed(sce);
        verify(connectionPool).shutdown();
    }

    private Map<Thread, Thread> getShutdownHooks() throws ClassNotFoundException, NoSuchFieldException,
            IllegalAccessException {
        Class clazz = Class.forName("java.lang.ApplicationShutdownHooks");
        Field field = clazz.getDeclaredField("hooks");
        field.setAccessible(true);
        return (Map<Thread, Thread>) field.get(null);
    }
}