package tech.bugger.persistence.util;

import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.OutOfConnectionsException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Thread-safe singleton object pool of database connections.
 *
 * The pool dynamically manages a set of database connections that can be borrowed and returned by callers. Before being
 * used, the pool must be initialized with technical parameters for the database connection and performance aspects.
 */
public class ConnectionPool {
    private static final Log log = Log.forClass(ConnectionPool.class);
    private static final double DECREASE_THRESH = 0.7;

    private static ConnectionPool instance;

    private final Deque<Connection> availableConnections;
    private final Set<Connection> usedConnections;

    private int minConns;
    private int maxConns;
    private int timeout;

    private String jdbcURL;
    private Properties jdbcProps;
    private State state;

    private enum State {
        UNINITIALIZED, INITIALIZED, SHUT_DOWN
    }

    private ConnectionPool() {
        availableConnections = new ArrayDeque<>();
        usedConnections = new HashSet<>();
        state = State.UNINITIALIZED;
    }

    /**
     * Supplies the singleton connection pool object.
     *
     * @return The one and only instance of the connection pool.
     */
    public static ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    /**
     * Initializes the connection pool with technical parameters and sets up the initial connections.
     *
     * This initialization has to occur before any other method calls and can only be executed exactly once.
     *
     * @param jdbcDvr   The fully qualified class name of the JDBC database driver to use.
     * @param jdbcURL   The DBMS-specific JDBC URL for connecting to the database. For a list, see
     *                  https://vladmihalcea.com/jdbc-driver-connection-url-strings.
     * @param jdbcProps The DBMS-specific JDBC connection properties containing at least credentials.
     * @param minConns  The minimum amount of database connections to maintain.
     * @param maxConns  The maximum amount of database connections to maintain.
     * @param timeoutMs The maximum time in milliseconds to wait for receiving a connection.
     * @throws IllegalStateException if the connection pool has already been initialized.
     * @see <a href="https://docs.oracle.com/javase/tutorial/jdbc/basics/connecting.html">Connecting with JDBC</a>
     */
    public void init(String jdbcDvr, String jdbcURL, Properties jdbcProps, int minConns, int maxConns, int timeoutMs) {
        if (state == State.INITIALIZED) {
            throw new IllegalStateException("Connection Pool has already been initialized.");
        } else if (state == State.SHUT_DOWN) {
            throw new IllegalStateException("Connection pool has already been shut down.");
        } else if (jdbcDvr == null) {
            throw new IllegalArgumentException("Driver class must not be null.");
        } else if (jdbcURL == null) {
            throw new IllegalArgumentException("Database URL must not be null.");
        } else if (jdbcProps == null) {
            throw new IllegalArgumentException("Connection properties must not be null.");
        } else if (minConns < 1) {
            throw new IllegalArgumentException("Minimum number of connections must be a positive integer.");
        } else if (minConns > maxConns) {
            throw new IllegalArgumentException("Minimum number of connections must be leq maximum number.");
        } else if (timeoutMs < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative.");
        }

        try {
            Class.forName(jdbcDvr); // explicitly load the driver class
        } catch (ClassNotFoundException e) {
            log.error("JDBC Driver " + jdbcDvr + " not found.", e);
            throw new InternalError(e);
        }

        this.minConns = minConns;
        this.maxConns = maxConns;
        this.timeout = timeoutMs;
        this.jdbcURL = jdbcURL;
        this.jdbcProps = jdbcProps;
        this.state = State.INITIALIZED;

        increaseConnections(minConns);
    }

    /**
     * Requests a database connection for use.
     *
     * If no connections are currently available, the calling thread will wait until this is the case. Therefore any
     * received connections should be returned soon
     *
     * @return A free database connection that is ready to be used.
     * @throws IllegalStateException if the connection pool has not been initialized yet.
     */
    public synchronized Connection getConnection() {
        if (state == State.UNINITIALIZED) {
            throw new IllegalStateException("Connection pool has not yet been initialized.");
        } else if (state == State.SHUT_DOWN) {
            throw new IllegalStateException("Connection pool has already been shut down.");
        }

        if (availableConnections.isEmpty()) {
            int increasePotential = maxConns - usedConnections.size();
            if (increasePotential > 0) {
                increaseConnections(Math.min(usedConnections.size(), increasePotential));
            } else {
                awaitConnections();
            }
        }
        Connection connection = availableConnections.poll();
        usedConnections.add(connection);
        log.debug("Connection acquired.");
        return connection;
    }

    private void awaitConnections() {
        while (availableConnections.isEmpty()) {
            long t = System.currentTimeMillis();
            try {
                wait(timeout);
            } catch (InterruptedException e) {
                log.warning("Interrupted while waiting for a database connection.", e);
            }

            /*
             * Check for timeout. This is necessary as the thread might be awakened spuriously!
             */
            if (System.currentTimeMillis() - t >= timeout) { // timeout?
                log.error("Timeout while waiting for a database connection.");
                throw new OutOfConnectionsException("Out of database connections.");
            }
        }
    }

    /**
     * Passes a database connection that is no longer needed back to the pool.
     *
     * This enables a potential thread waiting for free connections to proceed.
     *
     * @param connection The connection to be reintegrated.
     * @throws IllegalStateException if the connection pool has not been initialized yet.
     */
    public synchronized void releaseConnection(final Connection connection) {
        if (state == State.UNINITIALIZED) {
            throw new IllegalStateException("Connection pool has not yet been initialized.");
        } else if (state == State.SHUT_DOWN) {
            throw new IllegalStateException("Connection pool has already been shut down.");
        } else if (connection == null) {
            throw new IllegalArgumentException("Connection to release must not be null.");
        }

        usedConnections.remove(connection);
        if (isClosed(connection)) {
            increaseConnections(1); // replace closed connection
            log.debug("Closed connection replaced.");
        } else {
            availableConnections.add(connection);
            log.debug("Connection released.");
        }
        balanceConnections();
        notifyAll();
    }

    private boolean isClosed(Connection connection) {
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            log.warning("Database error when checking connection close status.", e);
            return true; // treat corrupt connection as closed
        }
    }

    private void balanceConnections() {
        int used = usedConnections.size();
        int total = availableConnections.size() + used;
        if (total > minConns && (double) used / total < DECREASE_THRESH) {
            int decreaseAmount = availableConnections.size() / 2;
            decreaseConnections(decreaseAmount);
            log.debug("Decreased available connections by " + decreaseAmount + " due to low load.");
        }
    }

    /**
     * Shuts down the connection pool by releasing all resources.
     */
    public void shutdown() {
        if (state == State.SHUT_DOWN) {
            throw new IllegalStateException("Connection pool has already been shut down.");
        }

        log.debug("Shutting down connection pool.");
        availableConnections.addAll(usedConnections);
        decreaseConnections(availableConnections.size());
        state = State.SHUT_DOWN;
    }

    private void increaseConnections(int increaseAmount) {
        for (int i = 0; i < increaseAmount; i++) {
            try {
                availableConnections.add(DriverManager.getConnection(jdbcURL, jdbcProps));
                log.debug("Incrementing available database connections.");
            } catch (SQLException e) {
                log.error("Could not acquire a database connection.", e);
                throw new InternalError(e);
            }
        }
    }

    private void decreaseConnections(int decreaseAmount) {
        if (decreaseAmount > availableConnections.size()) {
            throw new IllegalArgumentException("Decrease amount must not be greater than number of connections.");
        }

        for (int i = 0; i < decreaseAmount; i++) {
            Connection conn = availableConnections.remove();
            try {
                conn.close();
            } catch (SQLException e) {
                log.warning("Failed to close connection.", e);
            }
        }
    }
}
