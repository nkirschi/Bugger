package tech.bugger.control.servlet;

import java.io.IOException;
import java.io.Serial;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.PostService;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.util.Log;

/**
 * Custom servlet that serves post attachments.
 */
public class AttachmentServlet extends MediaServlet {

    @Serial
    private static final long serialVersionUID = -2411022287149244216L;

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(AttachmentServlet.class);

    /**
     * The current application settings.
     */
    @Inject
    private ApplicationSettings applicationSettings;

    /**
     * The current user session.
     */
    @Inject
    private UserSession session;


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
     *
     * @param request  The request to handle.
     * @param response The response to return to the client.
     */
    @Override
    protected void handleRequest(final HttpServletRequest request, final HttpServletResponse response) {
        if (!applicationSettings.getConfiguration().isGuestReading() && session.getUser() == null) {
            log.debug("Refusing to serve attachment to anonymous user.");
            throw new Error404Exception("Attachment could not be found.");
        }

        // Retrieve the attachment ID from the request.
        int attachmentID;
        try {
            attachmentID = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            log.debug("Invalid attachment ID given.");
            throw new Error404Exception("Attachment could not be found.");
        }

        // Fetch the requested attachment.
        Attachment attachment = postService.getAttachmentByID(attachmentID);
        if (attachment == null) {
            log.debug("Attachment with ID " + attachmentID + " not found.");
            throw new Error404Exception("Attachment could not be found.");
        }
        byte[] content = postService.getAttachmentContent(attachmentID);
        if (content == null) {
            log.debug("Content of attachment with ID " + attachmentID + " not found.");
            throw new Error404Exception("Attachment could not be found.");
        }

        // Initialize servlet response.
        response.reset();
        configureClientCaching(response);

        response.setHeader("Content-Disposition", "attachment; filename=\"" + attachment.getName() + '\"');
        response.setContentType(attachment.getMimetype());
        response.setContentLength(content.length);

        // Write attachment content to response.
        try {
            response.getOutputStream().write(content);
        } catch (IOException e) {
            log.warning("Could not write servlet response.", e);
        }
    }

}
