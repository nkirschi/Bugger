package tech.bugger.control.servlet;

import java.io.IOException;
import java.io.Serial;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.util.Log;

/**
 * Custom servlet that serves the organization logo.
 */
public class LogoServlet extends MediaServlet {

    @Serial
    private static final long serialVersionUID = -1911464315254552535L;

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(LogoServlet.class);

    /**
     * The current application settings.
     */
    @Inject
    private ApplicationSettings applicationSettings;

    /**
     * Handles a request for the organization logo.
     *
     * @param request  The request to handle.
     * @param response The response to return to the client.
     */
    @Override
    protected void handleRequest(final HttpServletRequest request, final HttpServletResponse response) {
        byte[] logo = applicationSettings.getOrganization().getLogo();
        if (logo == null) {
            log.debug("Organization logo does not exist.");
            throw new Error404Exception("Organization logo does not exist.");
        }

        // Initialize servlet response.
        response.reset();
        configureClientCaching(response);

        // Write image to response.
        try {
            response.getOutputStream().write(logo);
        } catch (IOException e) {
            log.error("Could not write servlet response.", e);
        }
    }

}
