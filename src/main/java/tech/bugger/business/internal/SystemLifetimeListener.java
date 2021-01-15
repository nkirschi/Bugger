package tech.bugger.business.internal;

import tech.bugger.business.util.PriorityExecutor;
import tech.bugger.business.util.Registry;
import tech.bugger.global.transfer.Metadata;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.MetadataGateway;
import tech.bugger.persistence.util.ConnectionPool;
import tech.bugger.persistence.util.Mailer;
import tech.bugger.persistence.util.PropertiesReader;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Listener observing application startup and shutdown in order to initialize and clean up critical resources.
 */
@WebListener
public class SystemLifetimeListener implements ServletContextListener {

    /**
     * Path to the application configuration relative to the application root.
     */
    private static final String APP_CONFIG = "/WEB-INF/config.properties";

    /**
     * Path to the JDBC configuration relative to the application root.
     */
    private static final String JDBC_CONFIG = "/WEB-INF/jdbc.properties";

    /**
     * Path to the logging configuration relative to the application root.
     */
    private static final String LOGGING_CONFIG = "/WEB-INF/logging.properties";

    /**
     * Path to the mailing configuration relative to the application root.
     */
    private static final String MAILING_CONFIG = "/WEB-INF/mailing.properties";

    /**
     * Path to the database setup script relative to the application root.
     */
    private static final String DB_SETUP_SCRIPT = "/WEB-INF/setup.sql";

    /**
     * Maximum time in ms to wait for remaining mailing task execution completion.
     */
    private static final long TASK_TERMINATION_TIMEOUT_MILLIS = Long.MAX_VALUE;

    /**
     * Rate in minutes at which maintenance tasks are periodically run.
     */
    private static final long MAINTENANCE_PERIODICITY_MINUTES = 60;

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private Log log;

    /**
     * Registry to be maintained.
     */
    private Registry registry;

    /**
     * Transaction manager for issuing transactions.
     */
    private TransactionManager transactionManager;

    /**
     * Periodic executor for maintenance tasks.
     */
    private ScheduledExecutorService maintenanceExecutor;

    /**
     * Shutdown hook for cleaning up database connections.
     */
    private Thread databaseShutdownHook;

    /**
     * Shutdown hook for clearing mailing queues.
     */
    private Thread mailQueueShutdownHook;

    /**
     * Shutdown hook for clearing maintenance tasks.
     */
    private Thread maintenanceShutdownHook;

    /**
     * Initializes necessary resources for the application to run.
     */
    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        ServletContext sctx = sce.getServletContext();

        initializeLogging(sctx);
        initializeAppConfig(sctx);
        initializeConnectionPool(sctx);
        initializeDatabaseSchema(sctx);
        initializeMailing(sctx);
        registerPriorityExecutors();
        registerShutdownHooks();
        scheduleMaintenanceTasks();

        log.info("Application startup completed.");
    }

    /**
     * Terminates all permanent dependencies in order not to leave behind unclosed resources.
     */
    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        deregisterShutdownHooks(); // hooks not needed due to regular shutdown

        cleanUpDatabaseConnections();
        terminateMaintenanceTasks(false);
        terminateMailingTasks(false);

        log.info("Application shutdown completed.");
    }

    private void initializeLogging(final ServletContext sctx) {
        try {
            Log.init(sctx.getResourceAsStream(LOGGING_CONFIG));
            log = Log.forClass(SystemLifetimeListener.class);
        } catch (IOException e) {
            throw new InternalError("Failed to initialize logger.", e);
        }
    }

    private void initializeAppConfig(final ServletContext sctx) {
        try {
            registry.registerPropertiesReader("config", new PropertiesReader(sctx.getResourceAsStream(APP_CONFIG)));
        } catch (IOException e) {
            log.error("Config file could not be loaded.", e);
            throw new InternalError("Failed to load app configuration file.", e);
        }
    }

    private void initializeConnectionPool(final ServletContext sctx) {
        Properties jdbcProperties = new Properties();
        try {
            jdbcProperties.load(sctx.getResourceAsStream(JDBC_CONFIG));
        } catch (IOException e) {
            throw new InternalError("Failed to load JDBC properties file.", e);
        }
        PropertiesReader configReader = registry.getPropertiesReader("config");
        registry.registerConnectionPool("db", new ConnectionPool(
                configReader.getString("DB_DRIVER"),
                configReader.getString("DB_URL"),
                jdbcProperties,
                configReader.getInt("DB_MIN_CONNS"),
                configReader.getInt("DB_MAX_CONNS"),
                configReader.getInt("DB_TIMEOUT")
        ));
    }

    private void initializeDatabaseSchema(final ServletContext sctx) {
        InputStream is = sctx.getResourceAsStream(DB_SETUP_SCRIPT);
        if (is == null) {
            throw new InternalError("Failed to load database setup script.");
        }
        Transaction tx = transactionManager.begin();
        try (tx) {
            MetadataGateway mg = tx.newMetadataGateway();
            Metadata metadata = mg.retrieveMetadata();
            if (metadata == null) { // no schema present
                mg.initializeSchema(is);
                log.info("Installed database schema.");
            } else {
                String version = metadata.getVersion();
                log.info("Found database schema version: " + version);
                // if (version.equals("1.0")) {
                //     for future versions: update schema here
                // }
            }
            tx.commit();
        } catch (TransactionException e) {
            log.error("Database setup script could not be applied.", e);
            tx.abort();
            throw new InternalError(e);
        }
    }

    private void initializeMailing(final ServletContext sctx) {
        try {
            PropertiesReader configReader = registry.getPropertiesReader("config");
            registry.registerMailer("main", new Mailer(
                    sctx.getResourceAsStream(MAILING_CONFIG),
                    configReader.getString("MAIL_USER"),
                    configReader.getString("MAIL_PASS")
            ));
        } catch (IOException e) {
            throw new InternalError("Failed to load mailing properties file.", e);
        }
    }

    private void registerPriorityExecutors() {
        PropertiesReader configReader = registry.getPropertiesReader("config");
        registry.registerPriorityExecutor("mails", new PriorityExecutor(
                configReader.getInt("MAIL_INITIAL_CAP"),
                configReader.getInt("MAIL_CORE_THREADS"),
                configReader.getInt("MAIL_MAX_THREADS"),
                configReader.getInt("MAIL_IDLE_TIMEOUT")
        ));
    }

    private void registerShutdownHooks() {
        databaseShutdownHook = new Thread(this::cleanUpDatabaseConnections);
        Runtime.getRuntime().addShutdownHook(databaseShutdownHook);

        mailQueueShutdownHook = new Thread(() -> terminateMailingTasks(true));
        Runtime.getRuntime().addShutdownHook(mailQueueShutdownHook);

        maintenanceShutdownHook = new Thread(() -> terminateMaintenanceTasks(true));
        Runtime.getRuntime().addShutdownHook(maintenanceShutdownHook);
    }

    private void deregisterShutdownHooks() {
        Runtime.getRuntime().removeShutdownHook(databaseShutdownHook);
        Runtime.getRuntime().removeShutdownHook(mailQueueShutdownHook);
        Runtime.getRuntime().removeShutdownHook(maintenanceShutdownHook);
    }

    private void cleanUpDatabaseConnections() {
        registry.getConnectionPool("db").shutdown();
    }

    private void terminateMaintenanceTasks(final boolean immediately) {
        if (immediately) {
            maintenanceExecutor.shutdownNow();
        } else {
            maintenanceExecutor.shutdown();
        }
        try {
            if (maintenanceExecutor.awaitTermination(TASK_TERMINATION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                log.info("Successfully terminated all running and scheduled maintenance tasks.");
            } else {
                log.warning("Timeout while terminating maintenance tasks.");
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for maintenance tasks to finish.", e);
        }
    }

    private void terminateMailingTasks(final boolean immediately) {
        PriorityExecutor mailingExecutor = registry.getPriorityExecutor("mails");
        try {
            boolean completed = immediately
                    ? mailingExecutor.kill(TASK_TERMINATION_TIMEOUT_MILLIS)
                    : mailingExecutor.shutdown(TASK_TERMINATION_TIMEOUT_MILLIS);

            if (completed) {
                log.info("Successfully terminated all running and queued mailing tasks.");
            } else {
                log.warning("Timeout while terminating mailing tasks.");
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for mailing tasks to finish.", e);
        }
    }

    private void scheduleMaintenanceTasks() {
        maintenanceExecutor = new ScheduledThreadPoolExecutor(1);
        maintenanceExecutor.scheduleAtFixedRate(new PeriodicCleaner(transactionManager), 0,
                                                MAINTENANCE_PERIODICITY_MINUTES, TimeUnit.HOURS);
    }

    /**
     * Sets the registry for the whole application.
     *
     * @param registry The registry to set.
     */
    @Inject
    public void setRegistry(final Registry registry) {
        this.registry = registry;
    }

    /**
     * Sets the transaction manager for transaction management.
     *
     * @param transactionManager The transaction manager to set.
     */
    @Inject
    public void setTransactionManager(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

}
