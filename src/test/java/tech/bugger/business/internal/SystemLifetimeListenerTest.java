package tech.bugger.business.internal;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import tech.bugger.business.util.PriorityExecutor;
import tech.bugger.business.util.PriorityExecutorRegistry;
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
    private SystemLifetimeListener systemLifetimeListenerMock;

    private MockedStatic<Log> logStaticMock;
    private MockedStatic<Runtime> runtimeStaticMock;

    private PropertiesReaderRegistry propertiesReaderRegistryMock;
    private ConnectionPoolRegistry connectionPoolRegistryMock;
    private MailerRegistry mailerRegistryMock;
    private PriorityExecutorRegistry priorityExecutorRegistryMock;
    private TransactionManager transactionManagerMock;
    private ConnectionPool connectionPoolMock;
    private PriorityExecutor priorityExecutorMock;
    private Transaction transactionMock;
    private MetadataGateway metadataGatewayMock;
    private Runtime runtimeMock;

    private ServletContextEvent sceMock;
    private ServletContext sctxMock;

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
        logStaticMock = mockStatic(Log.class);
        Log log = mock(Log.class);
        doNothing().when(log).debug(any());
        doNothing().when(log).info(any());
        doNothing().when(log).warning(any());
        doNothing().when(log).error(any());
        doNothing().when(log).debug(any(), any());
        doNothing().when(log).info(any(), any());
        doNothing().when(log).warning(any(), any());
        doNothing().when(log).error(any(), any());
        logStaticMock.when(() -> Log.forClass(any())).thenReturn(log);

        propertiesReaderRegistryMock = mock(PropertiesReaderRegistry.class);
        connectionPoolRegistryMock = mock(ConnectionPoolRegistry.class);
        mailerRegistryMock = mock(MailerRegistry.class);
        transactionManagerMock = mock(TransactionManager.class);
        priorityExecutorRegistryMock = mock(PriorityExecutorRegistry.class);

        systemLifetimeListenerMock = new SystemLifetimeListener();
        systemLifetimeListenerMock.setPropertiesReaderRegistry(propertiesReaderRegistryMock);
        systemLifetimeListenerMock.setConnectionPoolRegistry(connectionPoolRegistryMock);
        systemLifetimeListenerMock.setMailerRegistry(mailerRegistryMock);
        systemLifetimeListenerMock.setTransactionManager(transactionManagerMock);
        systemLifetimeListenerMock.setPriorityExecutorRegistry(priorityExecutorRegistryMock);

        PropertiesReader propertiesReader = mock(PropertiesReader.class);
        when(propertiesReader.getString(any())).thenReturn("");
        when(propertiesReader.getString("DB_DRIVER")).thenReturn("org.postgresql.Driver");
        when(propertiesReader.getString("DB_URL")).thenReturn("jdbc:postgresql://localhost:42424/postgres");
        when(propertiesReader.getInt(any())).thenReturn(1);
        when(propertiesReader.getBoolean(any())).thenReturn(false);
        when(propertiesReaderRegistryMock.get(any())).thenReturn(propertiesReader);

        connectionPoolMock = mock(ConnectionPool.class);
        doNothing().when(connectionPoolMock).shutdown();
        when(connectionPoolMock.isShutDown()).thenReturn(false);
        when(connectionPoolRegistryMock.get(any())).thenReturn(connectionPoolMock);

        priorityExecutorMock = mock(PriorityExecutor.class);
        doNothing().when(priorityExecutorMock).shutdown();
        when(priorityExecutorRegistryMock.get(any())).thenReturn(priorityExecutorMock);

        transactionMock = mock(Transaction.class);
        metadataGatewayMock = mock(MetadataGateway.class);
        when(metadataGatewayMock.retrieveVersion()).thenReturn(null);
        when(transactionMock.newMetadataGateway()).thenReturn(metadataGatewayMock);
        when(transactionManagerMock.begin()).thenReturn(transactionMock);

        runtimeMock = mock(Runtime.class);
        runtimeStaticMock = mockStatic(Runtime.class);
        runtimeStaticMock.when(Runtime::getRuntime).thenReturn(runtimeMock);

        sceMock = mock(ServletContextEvent.class);
        sctxMock = mock(ServletContext.class);
        when(sctxMock.getResourceAsStream(any())).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(sctxMock.getResourceAsStream("/WEB-INF/jdbc.properties"))
                .thenReturn(ClassLoader.getSystemResourceAsStream("jdbc.properties"));
        when(sceMock.getServletContext()).thenReturn(sctxMock);
    }

    @AfterEach
    public void tearDown() {
        logStaticMock.close();
        runtimeStaticMock.close();
    }

    @Test
    public void testContextInitializedInitializesLog() {
        systemLifetimeListenerMock.contextInitialized(sceMock);
        logStaticMock.verify(() -> Log.init(any()));
    }

    @Test
    public void testContextInitializedWhenLogInitFails() {
        logStaticMock.when(() -> Log.init(any())).thenThrow(IOException.class);
        assertThrows(InternalError.class, () -> systemLifetimeListenerMock.contextInitialized(sceMock));
    }

    @Test
    public void testContextInitializedInitializesConfigReader() {
        systemLifetimeListenerMock.contextInitialized(sceMock);
        verify(propertiesReaderRegistryMock).register(any(), any());
    }

    @Test
    public void testContextInitializedWhenConfigLoadingFails() throws IOException {
        InputStream badStream = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
        badStream.close();
        when(sctxMock.getResourceAsStream("/WEB-INF/config.properties")).thenReturn(badStream);
        assertThrows(InternalError.class, () -> systemLifetimeListenerMock.contextInitialized(sceMock));
    }

    @Test
    public void testContextInitializedInitializesConnectionPool() {
        systemLifetimeListenerMock.contextInitialized(sceMock);
        verify(connectionPoolRegistryMock).register(any(), any());
    }

    @Test
    public void testContextInitializedWhenConnectionPoolInitFails() throws IOException {
        InputStream badStream = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
        badStream.close();
        when(sctxMock.getResourceAsStream("/WEB-INF/jdbc.properties")).thenReturn(badStream);
        assertThrows(InternalError.class, () -> systemLifetimeListenerMock.contextInitialized(sceMock));
    }

    @Test
    public void testContextInitializedInitializesDatabaseSchema() {
        systemLifetimeListenerMock.contextInitialized(sceMock);
        verify(metadataGatewayMock).initializeSchema(any());
    }
    
    @Test
    public void testContextInitializedWhenSchemaAlreadyPresent() {
        when(metadataGatewayMock.retrieveVersion()).thenReturn("1.0");
        systemLifetimeListenerMock.contextInitialized(sceMock);
        verify(metadataGatewayMock, times(0)).initializeSchema(any());
    }

    @Test
    public void testContextInitializedWhenSchemaLoadingFails() {
        when(sctxMock.getResourceAsStream("/WEB-INF/setup.sql")).thenReturn(null);
        assertThrows(InternalError.class, () -> systemLifetimeListenerMock.contextInitialized(sceMock));
    }

    @Test
    public void testContextInitializedWhenSchemaTransactionError() throws Exception{
        doThrow(TransactionException.class).when(transactionMock).commit();
        assertThrows(InternalError.class, () -> systemLifetimeListenerMock.contextInitialized(sceMock));
    }

    @Test
    public void testContextInitializedInitializesMailer() {
        systemLifetimeListenerMock.contextInitialized(sceMock);
        verify(mailerRegistryMock).register(any(), any());
    }

    @Test
    public void testContextInitializedWhenMailerInitFails() throws IOException {
        InputStream badStream = new BufferedInputStream(new ByteArrayInputStream(new byte[0]));
        badStream.close();
        when(sctxMock.getResourceAsStream("/WEB-INF/mailing.properties")).thenReturn(badStream);
        assertThrows(InternalError.class, () -> systemLifetimeListenerMock.contextInitialized(sceMock));
    }

    @Test
    public void testContextInitializedInitializesPriorityExecutor() {
        systemLifetimeListenerMock.contextInitialized(sceMock);
        verify(priorityExecutorRegistryMock).register(any(), any());
    }

    @Test
    public void testContextInitializedAddsShutdownHooks() {
        systemLifetimeListenerMock.contextInitialized(sceMock);
        verify(runtimeMock, times(2)).addShutdownHook(any());
    }

    @Test
    public void testContextDestroyedShutsDownConnectionPool() {
        systemLifetimeListenerMock.contextInitialized(sceMock);
        systemLifetimeListenerMock.contextDestroyed(sceMock);
        verify(connectionPoolMock).shutdown();
    }

    @Test
    public void testContextDestroyedWhenConnectionPoolAlreadyShutDown() {
        when(connectionPoolMock.isShutDown()).thenReturn(true);
        systemLifetimeListenerMock.contextInitialized(sceMock);
        systemLifetimeListenerMock.contextDestroyed(sceMock);
        verify(connectionPoolMock, times(0)).shutdown();
    }

    @Test
    public void testContextDestroyedShutsDownPriorityExecutor() {
        systemLifetimeListenerMock.contextInitialized(sceMock);
        systemLifetimeListenerMock.contextDestroyed(sceMock);
        verify(priorityExecutorMock).shutdown();
    }

    @Test
    public void testContextDestroyedDeregistersShutdownHooks() {
        systemLifetimeListenerMock.contextInitialized(sceMock);
        systemLifetimeListenerMock.contextDestroyed(sceMock);
        verify(runtimeMock, times(2)).removeShutdownHook(any());
    }
}