package tech.bugger.control.servlet;

import tech.bugger.global.util.Log;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serial;

/**
 * Custom servlet that serves post attachments.
 */
public class AttachmentServlet extends HttpServlet {

    private static final Log log = Log.forClass(AttachmentServlet.class);
    @Serial
    private static final long serialVersionUID = -1911464315254552535L;

    /**
     * Handles a request for a post attachment. Expects the attachment's ID as a request parameter.
     * <p>
     * Verifies if the client is authorized to view the attachment, retrieves it and writes the attachment or potential
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
     * Handles a request for a post attachment. Expects the attachment's ID as a request parameter.
     * <p>
     * Verifies if the client is authorized to view the attachment, retrieves it and writes the attachment or potential
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
