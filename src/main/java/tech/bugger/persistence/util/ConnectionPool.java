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
 * Thread-safe object pool of database connections.
 *
 * The pool dynamically manages a set of database connections that can be borrowed and returned by callers. Before being
 * used, the pool must be initialized with technical parameters for the database connection and performance aspects.
 */
public final class ConnectionPool {

    /**
     * Log instance for logging in this class.
     */
    private static final Log log = Log.forClass(ConnectionPool.class);

    /**
     * Percentage of used connections below which the load is considered low.
     */
    private static final double DECREASE_THRESH = 0.7;

    /**
     * The connections available for usage.
     */
    private final Deque<Connection> availableConnections;

    /**
     * The connections currently in use.
     */
    private final Set<Connection> usedConnections;

    /**
     * The minimum total number of connections to maintain.
     */
    private final int minConnections;

    /**
     * The maximum allowed total number of connections.
     */
    private final int maxConnections;

    /**
     * Maximum time in milliseconds to wait on receiving a connection.
     */
    private final int timeoutMillis;

    /**
     * The DBMS-specific JDBC URL for connecting to the database.
     */
    private final String jdbcURL;

    /**
     * The DBMS-specific JDBC connection properties containing at least the credentials.
     */
    private final Properties jdbcProperties;

    /**
     * Flag indicating whether the connection pool is shut down and not available for use anymore.
     */
    private boolean shutDown;

    /**
     * Constructs a connection pool with the given technical parameters and sets up the initial connections.
     *
     * This initialization has to occur before any other method calls and can only be executed exactly once.
     *
     * @param jdbcDriver     The fully qualified class name of the JDBC database driver to use.
     * @param jdbcURL        The DBMS-specific JDBC URL for connecting to the database. For a list, see
     *                       https://vladmihalcea.com/jdbc-driver-connection-url-strings.
     * @param jdbcProperties The DBMS-specific JDBC connection properties containing at least the credentials.
     * @param minConnections The minimum amount of database connections to maintain.
     * @param maxConnections The maximum amount of database connections to maintain.
     * @param timeoutMillis  The maximum time in milliseconds to wait for receiving a connection.
     * @throws IllegalStateException if the connection pool has already been initialized.
     * @see <a href="https://docs.oracle.com/javase/tutorial/jdbc/basics/connecting.html">Connecting with JDBC</a>
     */
    public ConnectionPool(final String jdbcDriver, final String jdbcURL, final Properties jdbcProperties,
                          final int minConnections, final int maxConnections, final int timeoutMillis) {
        if (jdbcDriver == null) {
            throw new IllegalArgumentException("Driver class must not be null.");
        } else if (jdbcURL == null) {
            throw new IllegalArgumentException("Database URL must not be null.");
        } else if (jdbcProperties == null) {
            throw new IllegalArgumentException("Connection properties must not be null.");
        } else if (minConnections < 1) {
            throw new IllegalArgumentException("Minimum number of connections must be a positive integer.");
        } else if (minConnections > maxConnections) {
            throw new IllegalArgumentException("Minimum number of connections must be <= maximum number.");
        } else if (timeoutMillis < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative.");
        }

        try {
            Class.forName(jdbcDriver); // explicitly load the driver class
        } catch (ClassNotFoundException e) {
            log.error("JDBC Driver " + jdbcDriver + " not found.", e);
            throw new InternalError(e);
        }

        this.minConnections = minConnections;
        this.maxConnections = maxConnections;
        this.timeoutMillis = timeoutMillis;
        this.jdbcURL = jdbcURL;
        this.jdbcProperties = jdbcProperties;

        int meanConnections = (int) Math.sqrt(minConnections * maxConnections);
        availableConnections = new ArrayDeque<>(meanConnections);
        usedConnections = new HashSet<>(meanConnections);

        increaseConnections(minConnections);
    }

    /**
     * Requests a database connection for use.
     *
     * If no connections are currently available, the calling thread will wait until this is the case. Therefore any
     * received connections should be returned soon
     *
     * @return A free database connection that is ready to be used.
     * @throws IllegalStateException if the connection pool has already been shut down.
     */
    public synchronized Connection getConnection() {
        if (shutDown) {
            throw new IllegalStateException("Connection pool has already been shut down.");
        }

        if (availableConnections.isEmpty()) {
            int increasePotential = maxConnections - usedConnections.size();
            if (increasePotential > 0) {
                increaseConnections(Math.min(usedConnections.size(), increasePotential));
            } else {
                awaitConnections();
            }
        }
        Connection connection = availableConnections.remove();
        usedConnections.add(connection);
        log.debug("Connection " + connection.hashCode() + " acquired.");
        return connection;
    }

    private void awaitConnections() {
        while (availableConnections.isEmpty()) {
            long t = System.currentTimeMillis();
            try {
                wait(timeoutMillis);
            } catch (InterruptedException e) {
                log.warning("Interrupted while waiting for a database connection.", e);
            }

            /*
             * Check for timeout. This is necessary as the thread might be awakened spuriously!
             */
            if (System.currentTimeMillis() - t >= timeoutMillis) { // timeout?
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
     * @throws IllegalStateException if the connection pool has already been shut down.
     */
    public synchronized void releaseConnection(final Connection connection) {
        if (shutDown) {
            throw new IllegalStateException("Connection pool has already been shut down.");
        } else if (connection == null) {
            throw new IllegalArgumentException("Connection to release must not be null.");
        } else if (!usedConnections.contains(connection)) {
            throw new IllegalArgumentException("Connection does not belong to this pool.");
        } else if (isClosed(connection)) {
            throw new IllegalStateException("Connection to release is already closed.");
        }

        rollback(connection);

        usedConnections.remove(connection);
        availableConnections.add(connection);
        log.debug("Connection " + connection.hashCode() + " released.");

        balanceConnections();
        notifyAll();
    }

    private boolean isClosed(final Connection connection) {
        try {
            return connection.isClosed();
        } catch (SQLException e) {
            log.warning("Database error when checking connection close status.", e);
            return true; // treat corrupt connection as closed
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.warning("Error when defensively rolling back connection to be released.", e);
        }
    }

    private void balanceConnections() {
        int used = usedConnections.size();
        int total = availableConnections.size() + used;
        if (total > minConnections && (double) used / total < DECREASE_THRESH) {
            int decreaseAmount = availableConnections.size() / 2;
            decreaseConnections(decreaseAmount);
            log.debug("Decreased available connections by " + decreaseAmount + " due to low load.");
        }
    }

    /**
     * Shuts down the connection pool by releasing all resources.
     *
     * @throws IllegalStateException if the connection pool has already been shut down.
     */
    public void shutdown() {
        if (!shutDown) {
            log.debug("Shutting down connection pool.");
            availableConnections.addAll(usedConnections);
            decreaseConnections(availableConnections.size());
            shutDown = true;
        } else {
            log.debug("Connection pool already shut down. Doing nothing.");
        }
    }

    private void increaseConnections(final int increaseAmount) {
        log.debug("Increasing available database connections by " + increaseAmount + ".");
        for (int i = 0; i < increaseAmount; i++) {
            try {
                availableConnections.add(DriverManager.getConnection(jdbcURL, jdbcProperties));
            } catch (SQLException e) {
                log.error("Could not acquire a database connection.", e);
                throw new InternalError(e);
            }
        }
    }

    private void decreaseConnections(final int decreaseAmount) {
        if (decreaseAmount > availableConnections.size()) {
            throw new IllegalArgumentException("Decrease amount must not be greater than number of connections.");
        }

        log.debug("Decreasing available database connections by " + decreaseAmount + ".");
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
