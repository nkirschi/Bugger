package tech.bugger.business.internal;

import tech.bugger.global.util.Log;
import tech.bugger.persistence.util.ConfigReader;
import tech.bugger.persistence.util.ConnectionPool;
import tech.bugger.persistence.util.Mailer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.Properties;

/**
 * Listener observing application startup and shutdown in order to initialize and clean up critical resources.
 */
@WebListener
public class SystemLifetimeListener implements ServletContextListener {

    /**
     * Log instance for logging in this class.
     */
    private static final Log log = Log.forClass(SystemLifetimeListener.class);

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
     * Initializes necessary dependencies for the application to run.
     */
    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        ServletContext sctx = sce.getServletContext();

        initializeLogging(sctx);
        initializeAppConfig(sctx);
        initializeConnectionPool(sctx);
        initializeMailing(sctx);
        registerEmergencyHook();

        log.info("Application startup completed.");
    }

    /**
     * Terminates all permanent dependencies in order not to leave behind unclosed resources.
     */
    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        cleanUp();
        log.info("Application shutdown completed.");
    }

    private void initializeLogging(final ServletContext sctx) {
        try {
            Log.init(sctx.getResourceAsStream(LOGGING_CONFIG));
        } catch (IOException e) {
            throw new InternalError("Failed to initialize logger.", e);
        }
    }

    private void initializeAppConfig(final ServletContext sctx) {
        ConfigReader configReader = ConfigReader.getInstance();
        try {
            configReader.load(sctx.getResourceAsStream(APP_CONFIG));
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
        ConnectionPool.getInstance().init(
                ConfigReader.getInstance().getString("DB_DRIVER"),
                ConfigReader.getInstance().getString("DB_URL"),
                jdbcProperties,
                ConfigReader.getInstance().getInt("DB_MIN_CONNS"),
                ConfigReader.getInstance().getInt("DB_MAX_CONNS"),
                ConfigReader.getInstance().getInt("DB_TIMEOUT")
        );
    }

    private void initializeMailing(final ServletContext sctx) {
        try {
            Mailer.getInstance().configure(
                    sctx.getResourceAsStream(MAILING_CONFIG),
                    ConfigReader.getInstance().getString("MAIL_USER"),
                    ConfigReader.getInstance().getString("MAIL_PASS")
            );
        } catch (IOException e) {
            throw new InternalError("Failed to load mailing properties file.", e);
        }
    }

    private void registerEmergencyHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanUp)); // emergency cleanup hook
    }

    private void cleanUp() {
        ConnectionPool.getInstance().shutdown();
    }
}
