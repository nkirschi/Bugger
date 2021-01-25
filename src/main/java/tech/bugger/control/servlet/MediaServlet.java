package tech.bugger.control.servlet;

import java.io.IOException;
import java.io.Serial;
import java.util.Date;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import tech.bugger.global.util.Log;

/**
 * Custom servlet that serves avatars and avatar thumbnails.
 */
public abstract class MediaServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = -4981163493957399645L;

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(AvatarServlet.class);

    /**
     * The time in milliseconds clients should cache content.
     */
    private static final int CACHE_AGE = 10 * 60 * 1000; // 10 minutes

    /**
     * Handles a media request.
     *
     * @param request  The request to handle.
     * @param response The response to return to the client.
     */
    protected abstract void handleRequest(HttpServletRequest request, HttpServletResponse response);

    /**
     * Handles a GET request.
     *
     * @param request  The request to handle.
     * @param response The response to return to the client.
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {
        handleRequest(request, response);
    }

    /**
     * Handles a POST request.
     *
     * @param request  The request to handle.
     * @param response The response to return to the client.
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) {
        handleRequest(request, response);
    }

    /**
     * Redirects the user to a page that indicates the requested content could not be found.
     *
     * @param response The response to perform the redirect.
     */
    protected void redirectToNotFoundPage(final HttpServletResponse response) {
        try {
            // TODO Redirect to our own error page.
            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
        } catch (IOException e) {
            log.warning("Could not redirect to 404 page.");
        }
    }

    /**
     * Defines client caching headers for the response.
     *
     * @param response The response to add headers to.
     */
    protected void configureClientCaching(final HttpServletResponse response) {
        long expiry = new Date().getTime() + CACHE_AGE;
        response.setDateHeader("Expires", expiry);
    }

}
