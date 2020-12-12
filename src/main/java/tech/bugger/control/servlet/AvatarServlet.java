package tech.bugger.control.servlet;

import tech.bugger.global.util.Log;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Custom servlet that serves avatars and avatar thumbnails.
 */
public class AvatarServlet extends HttpServlet {

    private static final Log log = Log.forClass(AvatarServlet.class);
    private static final long serialVersionUID = 3230525044134835918L;

    /**
     * Handles a request for a user's avatar. Expects the user's ID and the type of avatar (full image or thumbnail) as
     * a request parameter.
     * <p>
     * Verifies if the client is authorized to view the avatar, retrieves it and writes the attachment or potential
     * errors to the response.
     * <p>
     * Called by the server when a GET request occurs.
     *
     * @param request  The request to handle.
     * @param response The response to return to the client.
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) {
    }

    /**
     * Handles a request for a user's avatar. Expects the user's ID and the type of avatar (full image or thumbnail) as
     * a request parameter.
     * <p>
     * Verifies if the client is authorized to view the avatar, retrieves it and writes the attachment or potential
     * errors to the response.
     * <p>
     * Called by the server when a POST request occurs.
     *
     * @param request  The request to handle.
     * @param response The response to return to the client.
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) {
    }
}
