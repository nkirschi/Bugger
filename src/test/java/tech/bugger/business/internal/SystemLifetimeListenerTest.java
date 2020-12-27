package tech.bugger.business.internal;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.MetadataGateway;
import tech.bugger.persistence.util.ConnectionPool;
import tech.bugger.persistence.util.ConnectionPoolRegistry;
import tech.bugger.persistence.util.MailerRegistry;
import tech.bugger.persistence.util.PropertiesReader;
import tech.bugger.persistence.util.PropertiesReaderRegistry;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SystemLifetimeListenerTest {
    private SystemLifetimeListener systemLifetimeListener;

    private MockedStatic<Log> logMock;
    private PropertiesReaderRegistry propertiesReaderRegistry;
    private ConnectionPoolRegistry connectionPoolRegistry;
    private MailerRegistry mailerRegistry;
    private TransactionManager transactionManager;
    private Transaction tx;
    private MetadataGateway mg;

    private ServletContextEvent sce;
    private ServletContext sctx;

    private static EmbeddedPostgres pg;

    @BeforeAll
    public static void setUpAll() throws Exception {
        pg = EmbeddedPostgres.builder().setPort(42424).start();
    }

    @AfterAll
    public static void tearDownAll() throws Exception {
        pg.close();
    }

    @BeforeEach
    public void setUp() {
        logMock = mockStatic(Log.class);
        Log log = mock(Log.class);
        doNothing().when(log).debug(any());
        doNothing().when(log).info(any());
        doNothing().when(log).warning(any());
        doNothing().when(log).error(any());
        doNothing().when(log).debug(any(), any());
        doNothing().when(log).info(any(), any());
        doNothing().when(log).warning(any(), any());
        doNothing().when(log).error(any(), any());
        logMock.when(() -> Log.forClass(any())).thenReturn(log);

        propertiesReaderRegistry = mock(PropertiesReaderRegistry.class);
        connectionPoolRegistry = mock(ConnectionPoolRegistry.class);
        mailerRegistry = mock(MailerRegistry.class);
        transactionManager = mock(TransactionManager.class);

        systemLifetimeListener = new SystemLifetimeListener();
        systemLifetimeListener.setPropertiesReaderRegistry(propertiesReaderRegistry);
        systemLifetimeListener.setConnectionPoolRegistry(connectionPoolRegistry);
        systemLifetimeListener.setMailerRegistry(mailerRegistry);
        systemLifetimeListener.setTransactionManager(transactionManager);

        PropertiesReader propertiesReader = mock(PropertiesReader.class);
        when(propertiesReader.getString(any())).thenReturn("");
        when(propertiesReader.getString("DB_DRIVER")).thenReturn("org.postgresql.Driver");
        when(propertiesReader.getString("DB_URL")).thenReturn("jdbc:postgresql://localhost:42424/postgres");
        when(propertiesReader.getInt(any())).thenReturn(1);
        when(propertiesReader.getBoolean(any())).thenReturn(false);
        when(propertiesReaderRegistry.get(any())).thenReturn(propertiesReader);

        tx = mock(Transaction.class);
        mg = mock(MetadataGateway.class);
        when(mg.retrieveVersion()).thenReturn(null);
        doNothing().when(mg).initializeSchema(any());
        when(tx.newMetadataGateway()).thenReturn(mg);
        when(transactionManager.begin()).thenReturn(tx);


        sce = mock(ServletContextEvent.class);
        sctx = mock(ServletContext.class);
        when(sctx.getResourceAsStream(any())).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(sctx.getResourceAsStream("/WEB-INF/jdbc.properties"))
                .thenReturn(ClassLoader.getSystemResourceAsStream("jdbc.properties"));
        when(sce.getServletContext()).thenReturn(sctx);
    }

    @AfterEach
    public void tearDown() {
        logMock.close();
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
    public void testContextInitializedInitializesConfigReader() {
        systemLifetimeListener.contextInitialized(sce);
        verify(propertiesReaderRegistry).register(any(), any());
    }

    @Test
    public void testContextInitializedWhenConfigLoadingFails() throws IOException {
        InputStream badStream = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
        badStream.close();
        when(sctx.getResourceAsStream("/WEB-INF/config.properties")).thenReturn(badStream);
        assertThrows(InternalError.class, () -> systemLifetimeListener.contextInitialized(sce));
    }

    @Test
    public void testContextInitializedInitializesConnectionPool() {
        systemLifetimeListener.contextInitialized(sce);
        verify(connectionPoolRegistry).register(any(), any());
    }

    @Test
    public void testContextInitializedWhenConnectionPoolInitFails() throws IOException {
        InputStream badStream = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
        badStream.close();
        when(sctx.getResourceAsStream("/WEB-INF/jdbc.properties")).thenReturn(badStream);
        assertThrows(InternalError.class, () -> systemLifetimeListener.contextInitialized(sce));
    }

    @Test
    public void testContextInitializedInitializesDatabaseSchema() {
        systemLifetimeListener.contextInitialized(sce);
        verify(mg).initializeSchema(any());
    }
    
    @Test
    public void testContextInitializedWhenSchemaAlreadyPresent() {
        when(mg.retrieveVersion()).thenReturn("1.0");
        systemLifetimeListener.contextInitialized(sce);
        verify(mg, times(0)).initializeSchema(any());
    }

    @Test
    public void testContextInitializedWhenSchemaLoadingFails() {
        when(sctx.getResourceAsStream("/WEB-INF/setup.sql")).thenReturn(null);
        assertThrows(InternalError.class, () -> systemLifetimeListener.contextInitialized(sce));
    }

    @Test
    public void testContextInitializedWhenSchemaTransactionError() throws Exception{
        doThrow(TransactionException.class).when(tx).commit();
        assertThrows(InternalError.class, () -> systemLifetimeListener.contextInitialized(sce));
    }

    @Test
    public void testContextInitializedInitializesMailer() {
        systemLifetimeListener.contextInitialized(sce);
        verify(mailerRegistry).register(any(), any());
    }

    @Test
    public void testContextInitializedWhenMailerInitFails() throws IOException {
        InputStream badStream = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
        badStream.close();
        when(sctx.getResourceAsStream("/WEB-INF/mailing.properties")).thenReturn(badStream);
        assertThrows(InternalError.class, () -> systemLifetimeListener.contextInitialized(sce));
    }

    @Test
    public void testContextInitializedAddsShutdownHook() throws Exception {
        Runtime runtime = mock(Runtime.class);
        MockedStatic<Runtime> runtimeMock = mockStatic(Runtime.class);
        runtimeMock.when(Runtime::getRuntime).thenReturn(runtime);
        doNothing().when(runtime).addShutdownHook(any());
        systemLifetimeListener.contextInitialized(sce);
        verify(runtime).addShutdownHook(any());
        runtimeMock.close();
    }

    @Test
    public void testContextDestroyedShutsDownConnectionPool() {
        ConnectionPool connectionPool = mock(ConnectionPool.class);
        doNothing().when(connectionPool).shutdown();
        when(connectionPool.isShutDown()).thenReturn(false);
        when(connectionPoolRegistry.get(any())).thenReturn(connectionPool);
        systemLifetimeListener.contextInitialized(sce);
        systemLifetimeListener.contextDestroyed(sce);
        verify(connectionPool).shutdown();
    }

    @Test
    public void testContextDestroyedWhenConnectionPoolAlreadyShutDown() {
        ConnectionPool connectionPool = mock(ConnectionPool.class);
        doNothing().when(connectionPool).shutdown();
        when(connectionPool.isShutDown()).thenReturn(true);
        when(connectionPoolRegistry.get(any())).thenReturn(connectionPool);
        systemLifetimeListener.contextInitialized(sce);
        systemLifetimeListener.contextDestroyed(sce);
        verify(connectionPool, times(0)).shutdown();
    }

    private Map<Thread, Thread> getShutdownHooks() throws Exception {
        Class<?> clazz = Class.forName("java.lang.ApplicationShutdownHooks");
        Field field = clazz.getDeclaredField("hooks");
        field.setAccessible(true);
        return (Map<Thread, Thread>) field.get(null);
    }
}