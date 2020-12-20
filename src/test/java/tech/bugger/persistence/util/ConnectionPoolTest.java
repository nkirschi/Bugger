package tech.bugger.persistence.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tech.bugger.persistence.exception.OutOfConnectionsException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class ConnectionPoolTest {
    private static final String DVR = "org.postgresql.Driver";
    private static final String URL = "jdbc:postgresql://bueno.fim.uni-passau.de:5432/sep20g02t";
    private static final Properties PROPS = new Properties();
    private static final int MIN_CONNS = 2;
    private static final int MAX_CONNS = 5;
    private static final int TIMEOUT = 5000;

    @Nested
    public class ConnectionPoolShutdownTest {
        private ConnectionPool connectionPool;
        private Collection<Connection> availableConnections;
        private Collection<Connection> usedConnections;

        @BeforeEach
        public void init() throws IOException, NoSuchFieldException, IllegalAccessException {
            connectionPool = ConnectionPool.getInstance();
            PROPS.load(ClassLoader.getSystemResourceAsStream("jdbc-test.properties"));
            Field field = ConnectionPool.class.getDeclaredField("availableConnections");
            field.setAccessible(true);
            availableConnections = (Collection<Connection>) field.get(connectionPool);
            field = ConnectionPool.class.getDeclaredField("usedConnections");
            field.setAccessible(true);
            usedConnections = (Collection<Connection>) field.get(connectionPool);
        }

        @AfterEach
        public void reset() throws IllegalAccessException, NoSuchFieldException {
            Field instance = ConnectionPool.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        }

        @Test
        public void testInitWhenAlreadyShutDown() {
            connectionPool.shutdown();
            assertThrows(IllegalStateException.class,
                    () -> connectionPool.init(DVR, URL, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT));
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
        public void testShutdownWhenAlreadyShutDown() {
            connectionPool.shutdown();
            assertThrows(IllegalStateException.class,
                    () -> connectionPool.shutdown());
        }

        @Test
        public void testShutdownWhenConnectionsCorrupt() throws SQLException {
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
    }

    static {
        try {
            PROPS.load(ClassLoader.getSystemResourceAsStream("jdbc-test.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetInstance() {
        ConnectionPool po = ConnectionPool.getInstance();
        ConnectionPool ol = ConnectionPool.getInstance();
        assertSame(po, ol, "Should always return the same singleton instance.");
    }

    @Nested
    public class ConnectionPoolInitializationTest {

        private ConnectionPool connectionPool;

        @BeforeEach
        public void init() throws IOException {
            connectionPool = ConnectionPool.getInstance();
            PROPS.load(ClassLoader.getSystemResourceAsStream("jdbc-test.properties"));
        }

        @AfterEach
        public void reset() throws IllegalAccessException, NoSuchFieldException {
            connectionPool.shutdown();
            Field instance = ConnectionPool.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
        }

        @Test
        public void testInitWhenAlreadyInitialized() {
            connectionPool.init(DVR, URL, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT);
            assertThrows(IllegalStateException.class,
                    () -> connectionPool.init(DVR, URL, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT));
        }

        @Test
        public void testInitWhenDvrNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> connectionPool.init(null, URL, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT));
        }

        @Test
        public void testInitWhenUrlNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> connectionPool.init(DVR, null, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT));
        }

        @Test
        public void testInitWhenUrlInvalid() {
            assertThrows(InternalError.class,
                    () -> connectionPool.init(DVR, "invalid", PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT));
        }

        @Test
        public void testInitWhenPropsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> connectionPool.init(DVR, URL, null, MIN_CONNS, MAX_CONNS, TIMEOUT));
        }

        @Test
        public void testInitWhenMinConnsNotPositive() {
            assertThrows(IllegalArgumentException.class,
                    () -> connectionPool.init(DVR, URL, PROPS, 0, MAX_CONNS, TIMEOUT));
        }

        @Test
        public void testInitWhenMaxConnsNotPositive() {
            assertThrows(IllegalArgumentException.class,
                    () -> connectionPool.init(DVR, URL, PROPS, MIN_CONNS, 0, TIMEOUT));
        }

        @Test
        public void testInitWhenTimeoutNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> connectionPool.init(DVR, URL, PROPS, MIN_CONNS, MAX_CONNS, -42));
        }

        @Test
        public void testInitWhenDriverNotExisting() {
            assertThrows(InternalError.class,
                    () -> connectionPool.init("nodriver", URL, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT));
        }

        @Test
        public void testInitSetsUpConnections() throws IllegalAccessException, NoSuchFieldException {
            connectionPool.init(DVR, URL, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT);
            Field field = ConnectionPool.class.getDeclaredField("availableConnections");
            field.setAccessible(true);
            Collection<Connection> availableConnections = (Collection<Connection>) field.get(connectionPool);
            assertEquals(2, availableConnections.size());
        }

        @Test
        public void testGetConnectionIfNotInitialized() {
            assertThrows(IllegalStateException.class, () -> connectionPool.getConnection());
        }

        @Test
        public void testReleaseConnectionIfNotInitialized() {
            assertThrows(IllegalStateException.class, () -> connectionPool.releaseConnection(null));
        }
    }


    @Nested
    public class ConnectionPoolUsageTest {
        private ConnectionPool connectionPool;
        private Collection<Connection> availableConnections;
        private Collection<Connection> usedConnections;

        @BeforeEach
        public void init() throws IllegalAccessException, NoSuchFieldException {
            connectionPool = ConnectionPool.getInstance();
            connectionPool.init(DVR, URL, PROPS, MIN_CONNS, MAX_CONNS, TIMEOUT);
            Field field = ConnectionPool.class.getDeclaredField("availableConnections");
            field.setAccessible(true);
            availableConnections = (Collection<Connection>) field.get(connectionPool);
            field = ConnectionPool.class.getDeclaredField("usedConnections");
            field.setAccessible(true);
            usedConnections = (Collection<Connection>) field.get(connectionPool);
        }

        @AfterEach
        public void reset() throws IllegalAccessException, NoSuchFieldException {
            ConnectionPool.getInstance().shutdown();
            Field instance = ConnectionPool.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
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
        public void testGetConnectionWhenTimeout() throws InterruptedException {
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
        public void testReleaseConnectionWhenConnectionIsClosed() throws SQLException {
            Connection connection = connectionPool.getConnection();
            connection.close();
            connectionPool.releaseConnection(connection);
            assertAll(
                    () -> assertEquals(0, usedConnections.size()),
                    () -> assertEquals(MIN_CONNS, availableConnections.size()),
                    () -> assertFalse(availableConnections.contains(connection))
            );
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
        public void testReleaseConnectionWhenConnectionCorrupt() throws SQLException {
            Connection connection = mock(Connection.class);
            doThrow(SQLException.class).when(connection).isClosed();
            connectionPool.releaseConnection(connection);
            assertAll(
                    () -> assertEquals(0, usedConnections.size()),
                    () -> assertEquals(2, availableConnections.size()),
                    () -> assertFalse(availableConnections.contains(connection))
            );
        }

        @Test
        public void testScenarioWithWaitAndAllFine() throws InterruptedException {
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
        public void testDecreaseConnectionsJustForBranchCoverage() throws NoSuchMethodException {
            Method method = connectionPool.getClass().getDeclaredMethod("decreaseConnections", int.class);
            method.setAccessible(true);
            assertThrows(InvocationTargetException.class, () -> method.invoke(connectionPool, 3));
        }
    }
}