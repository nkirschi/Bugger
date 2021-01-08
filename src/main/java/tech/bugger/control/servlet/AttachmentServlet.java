package tech.bugger.control.servlet;

import tech.bugger.business.service.PostService;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.util.Log;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;

/**
 * Custom servlet that serves post attachments.
 */
public class AttachmentServlet extends HttpServlet {

    @Serial
    private static final long serialVersionUID = -1911464315254552535L;

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(AttachmentServlet.class);

    /**
     * The post service providing attachments.
     */
    @Inject
    private PostService postService;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        // Retrieve the attachment ID from the request.
        int attachmentID = 0;
        try {
            attachmentID = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            log.debug("Invalid attachment ID given.");
            redirectToNotFoundPage(response);
            return;
        }

        // TODO: Check if user is allowed to download attachment.

        // Fetch attachment the requested attachment.
        Attachment attachment = postService.getAttachmentByID(attachmentID);
        if (attachment == null) {
            log.debug("Attachment with ID " + attachmentID + " not found.");
            redirectToNotFoundPage(response);
            return;
        }

        // Initialize servlet response.
        response.reset();
        // TODO: Attachment name might break this HEADER.
        response.setHeader("Content-disposition", "attachment; filename=" + attachment.getName());
        response.setContentType(attachment.getMimetype());
        response.setContentLength(attachment.getContent().get().length);

        // Write attachment content to response.
        try {
            response.getOutputStream().write(attachment.getContent().get());
        } catch (IOException e) {
            log.error("Could not write servlet response.", e);
        }
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
        doGet(request, response);
    }

    private void redirectToNotFoundPage(HttpServletResponse response) {
        try {
            // TODO: Redirect to our own error page.
            response.sendError(HttpServletResponse.SC_NOT_FOUND); // 404.
        } catch (IOException e) {
            throw new InternalError("Could not redirect to 404 page.");
        }
    }

}
