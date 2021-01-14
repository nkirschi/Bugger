package tech.bugger.persistence.util;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.LogExtension;
import tech.bugger.persistence.exception.OutOfConnectionsException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(LogExtension.class)
public class ConnectionPoolTest {
    private static final String DVR = "org.postgresql.Driver";
    private static final String URL = "jdbc:postgresql://localhost:42424/postgres";
    private static final Properties PROPS = new Properties();
    private static final int MIN_CONNS = 2;
    private static final int MAX_CONNS = 5;
    private static final int TIMEOUT = 1000; // should not cause trouble with in-memory DB ;-)
    private static EmbeddedPostgres pg;

    @BeforeAll
    public static void setUp() throws Exception {
        pg = EmbeddedPostgres.builder().setPort(42424).start();
        PROPS.load(ClassLoader.getSystemResourceAsStream("jdbc.properties"));
    }

    @AfterAll
    public static void tearDown() throws Exception {
        pg.close();
    }

    @Nested
    public class ConnectionPoolInitializationTest {

        @Test
        public void testConstructorWhenDvrNull() {
            assertThrows(IllegalArgumentException.class,
                         () -> new ConnectionPool(null, URL, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT));
        }

        @Test
        public void testConstructorWhenUrlNull() {
            assertThrows(IllegalArgumentException.class,
                         () -> new ConnectionPool(DVR, null, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT));
        }

        @Test
        public void testConstructorWhenUrlInvalid() {
            assertThrows(InternalError.class,
                         () -> new ConnectionPool(DVR, "invalid", PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT));
        }

        @Test
        public void testConstructorWhenPropsNull() {
            assertThrows(IllegalArgumentException.class,
                         () -> new ConnectionPool(DVR, URL, null, MIN_CONNS, MAX_CONNS, TIMEOUT));
        }

        @Test
        public void testConstructorWhenMinConnsNotPositive() {
            assertThrows(IllegalArgumentException.class,
                         () -> new ConnectionPool(DVR, URL, PROPS, 0, MAX_CONNS, TIMEOUT));
        }

        @Test
        public void testConstructorWhenMaxConnsNotPositive() {
            assertThrows(IllegalArgumentException.class,
                         () -> new ConnectionPool(DVR, URL, PROPS, MIN_CONNS, 0, TIMEOUT));
        }

        @Test
        public void testConstructorWhenTimeoutNegative() {
            assertThrows(IllegalArgumentException.class,
                         () -> new ConnectionPool(DVR, URL, PROPS, MIN_CONNS, MAX_CONNS, -42));
        }

        @Test
        public void testConstructorWhenDriverNotExisting() {
            assertThrows(InternalError.class,
                         () -> new ConnectionPool("nodriver", URL, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT));
        }

        @Test
        public void testConstructorSetsUpConnections() throws IllegalAccessException, NoSuchFieldException {
            ConnectionPool connectionPool = new ConnectionPool(DVR, URL, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT);
            Field field = ConnectionPool.class.getDeclaredField("availableConnections");
            field.setAccessible(true);
            Collection<Connection> availableConnections = (Collection<Connection>) field.get(connectionPool);
            assertEquals(2, availableConnections.size());
            connectionPool.shutdown();
        }
    }

    @Nested
    public class ConnectionPoolShutdownTest {
        private ConnectionPool connectionPool;
        private Collection<Connection> availableConnections;
        private Collection<Connection> usedConnections;

        @BeforeEach
        public void setUp() throws Exception {
            connectionPool = new ConnectionPool(DVR, URL, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT);

            Field field = ConnectionPool.class.getDeclaredField("availableConnections");
            field.setAccessible(true);
            availableConnections = (Collection<Connection>) field.get(connectionPool);

            field = ConnectionPool.class.getDeclaredField("usedConnections");
            field.setAccessible(true);
            usedConnections = (Collection<Connection>) field.get(connectionPool);
        }

        @Test
        public void testGetConnectionWhenAlreadyShutDown() {
            connectionPool.shutdown();
            assertThrows(IllegalStateException.class,
                         () -> connectionPool.getConnection());
        }

        @Test
        public void testReleaseConnectionWhenAlreadyShutDown() {
            connectionPool.shutdown();
            assertThrows(IllegalStateException.class,
                         () -> connectionPool.releaseConnection(null));
        }

        @Test
        public void testShutdownWhenConnectionsCorrupt() throws Exception {
            availableConnections.clear();
            usedConnections.clear();
            Connection connection = mock(Connection.class);
            doThrow(SQLException.class).when(connection).close();
            availableConnections.add(connection);
            connectionPool.shutdown();
            assertAll(
                    () -> assertEquals(0, availableConnections.size()),
                    () -> assertEquals(0, usedConnections.size())
            );
        }

        @Test
        public void testShutdownWhenAlreadyShutDown() {
            connectionPool.shutdown();
            assertDoesNotThrow(() -> connectionPool.shutdown());
        }
    }

    @Nested
    public class ConnectionPoolUsageTest {
        private ConnectionPool connectionPool;
        private Collection<Connection> availableConnections;
        private Collection<Connection> usedConnections;

        @BeforeEach
        public void setUp() throws Exception {
            connectionPool = new ConnectionPool(DVR, URL, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT);

            Field field = ConnectionPool.class.getDeclaredField("availableConnections");
            field.setAccessible(true);
            availableConnections = (Collection<Connection>) field.get(connectionPool);

            field = ConnectionPool.class.getDeclaredField("usedConnections");
            field.setAccessible(true);
            usedConnections = (Collection<Connection>) field.get(connectionPool);
        }

        @AfterEach
        public void tearDown() {
            connectionPool.shutdown();
        }

        @Test
        public void testGetConnectionWhenAvailable() {
            connectionPool.getConnection();
            assertAll(
                    () -> assertEquals(1, usedConnections.size()),
                    () -> assertEquals(1, availableConnections.size())
            );
        }

        @Test
        public void testGetConnectionWhenNotAvailableButIncreasable() {
            for (int i = 0; i < 3; i++) {
                connectionPool.getConnection();
            }
            assertAll(
                    () -> assertEquals(3, usedConnections.size()),
                    () -> assertEquals(1, availableConnections.size())
            );
        }

        @Test
        public void testGetConnectionWhenNotAvailableButIncreasableButCappedAtMaximum() {
            for (int i = 0; i < MAX_CONNS; i++) {
                connectionPool.getConnection();
            }
            assertAll(
                    () -> assertEquals(MAX_CONNS, usedConnections.size()),
                    () -> assertEquals(0, availableConnections.size())
            );
        }

        @Test
        public void testGetConnectionWhenTimeout() {
            for (int i = 0; i < MAX_CONNS; i++) {
                connectionPool.getConnection();
            }
            assertThrows(OutOfConnectionsException.class, () -> connectionPool.getConnection());
        }

        @Test
        public void testReleaseConnectionWhenConnectionIsNull() {
            assertThrows(IllegalArgumentException.class, () -> connectionPool.releaseConnection(null));
        }

        @Test
        public void testReleaseConnectionWhenConnectionIsForeign() {
            Connection connection = mock(Connection.class);
            assertThrows(IllegalArgumentException.class, () -> connectionPool.releaseConnection(connection));
        }

        @Test
        public void testReleaseConnectionWhenConnectionIsClosed() throws Exception {
            Connection connection = connectionPool.getConnection();
            connection.close();
            assertThrows(IllegalStateException.class, () -> connectionPool.releaseConnection(connection));
        }

        @Test
        public void testReleaseConnectionWhenConnectionIsCorrupt() throws Exception {
            Connection connection = mock(Connection.class);
            doThrow(SQLException.class).when(connection).isClosed();
            usedConnections.add(connection);
            assertThrows(IllegalStateException.class, () -> connectionPool.releaseConnection(connection));
        }

        @Test
        public void testReleaseConnectionWhenRollbackError() throws Exception {
            Connection connection = mock(Connection.class);
            doThrow(SQLException.class).when(connection).rollback();
            usedConnections.add(connection);
            assertDoesNotThrow(() -> connectionPool.releaseConnection(connection));
        }

        @Test
        public void testReleaseConnectionWhenConnectionIsFine() {
            Connection connection = connectionPool.getConnection();
            connectionPool.releaseConnection(connection);
            assertAll(
                    () -> assertEquals(0, usedConnections.size()),
                    () -> assertEquals(MIN_CONNS, availableConnections.size()),
                    () -> assertTrue(availableConnections.contains(connection))
            );
        }

        @Test
        public void testReleaseConnectionWhenBalancing() {
            for (int i = 0; i < 2; i++) {
                connectionPool.getConnection();
            }
            Connection connection = connectionPool.getConnection();
            connectionPool.releaseConnection(connection);
            assertAll(
                    () -> assertEquals(2, usedConnections.size()),
                    () -> assertEquals(1, availableConnections.size()),
                    () -> assertTrue(availableConnections.contains(connection))
            );
        }

        @Test
        public void testScenarioWithWait() throws Exception {
            List<Thread> threads = new ArrayList<>();
            for (int i = 0; i < MAX_CONNS; i++) {
                Thread thread = new Thread(() -> {
                    Connection connection = connectionPool.getConnection();
                    try {
                        Thread.sleep(TIMEOUT / 2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    connectionPool.releaseConnection(connection);
                });
                threads.add(thread);
            }

            Thread waiter = new Thread(() -> {
                Connection connection = connectionPool.getConnection();
                connectionPool.releaseConnection(connection);
            });

            for (Thread thread : threads) {
                thread.start();
            }
            Thread.sleep(TIMEOUT / 8);
            waiter.start();

            Thread.sleep(TIMEOUT / 4);
            waiter.interrupt(); // for full branch coverage :-)
            assertAll(
                    () -> assertEquals(MAX_CONNS, usedConnections.size()),
                    () -> assertEquals(0, availableConnections.size())
            );

            for (Thread thread : threads) {
                thread.join();
            }
            waiter.join();

            assertEquals(0, usedConnections.size());
        }

        @Test
        public void testDecreaseConnectionsJustForBranchCoverage() throws Exception {
            Method method = connectionPool.getClass().getDeclaredMethod("decreaseConnections", int.class);
            method.setAccessible(true);
            Throwable e = assertThrows(InvocationTargetException.class, () -> method.invoke(connectionPool, MAX_CONNS));
            assertEquals(IllegalArgumentException.class, e.getCause().getClass());
        }
    }
}