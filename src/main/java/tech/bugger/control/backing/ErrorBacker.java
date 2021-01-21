package tech.bugger.control.backing;

import tech.bugger.control.exception.Error404Exception;
import tech.bugger.control.util.JFConfig;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.StoreException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Backing Bean for the error page.
 */
@RequestScoped
@Named
public class ErrorBacker {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(ErrorBacker.class);

    /**
     * Initializes the error page.
     */
    @PostConstruct
    void init() {
        log.debug("init called on error backer.");
    }

    /**
     * Throws a StoreException for testing purposes.
     *
     * @return Never returns.
     */
    public String throwStoreException() {
        throw new StoreException("Test exception thrown from error backer.");
    }

    /**
     * Throws an IOException for testing purposes.
     *
     * @return Never returns.
     */
    public String throwIOException() throws IOException {
        throw new IOException("Test exception thrown from error backer.");
    }

    /**
     * Throws an Error404Exception for testing purposes.
     *
     * @return Never returns.
     */
    public String throw404Exception() {
        throw new Error404Exception("Test exception thrown from error backer.");
    }

    /**
     * Returns the stack trace of the given exception as a String.
     *
     * @param exception The given exception.
     * @return The stack trace as a String.
     */
    public static String stackTrace(final Exception exception) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        exception.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    /**
     * Returns a link leading to the home page.
     *
     * @return The link leading to the home page.
     */
    public static String goHome() {
        log.debug("go home called in error backer");
        return JFConfig.getApplicationPath(FacesContext.getCurrentInstance().getExternalContext()) + "/";
    }

}
