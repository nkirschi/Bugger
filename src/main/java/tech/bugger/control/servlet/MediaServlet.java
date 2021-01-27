package tech.bugger.control.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serial;
import java.util.Date;

/**
 * Custom servlet that serves avatars and avatar thumbnails.
 */
public abstract class MediaServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = -4981163493957399645L;

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
     * Defines client caching headers for the response.
     *
     * @param response The response to add headers to.
     */
    protected void configureClientCaching(final HttpServletResponse response) {
        long expiry = new Date().getTime() + CACHE_AGE;
        response.setDateHeader("Expires", expiry);
    }

}
