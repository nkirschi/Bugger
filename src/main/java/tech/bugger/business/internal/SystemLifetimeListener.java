package tech.bugger.business.internal;

import tech.bugger.global.util.Log;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Performs certain tasks after startup and before shutdown.
 */
@WebListener
public class SystemLifetimeListener implements ServletContextListener {
    private static Log log = Log.forClass(SystemLifetimeListener.class);

    /**
     * Performs necessary tasks after startup.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    /**
     * Performs necessary tasks before shutdown.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
