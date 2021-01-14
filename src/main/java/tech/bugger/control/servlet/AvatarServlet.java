package tech.bugger.control.servlet;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ProfileService;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;

/**
 * Custom servlet that serves avatars and avatar thumbnails.
 */
public class AvatarServlet extends MediaServlet {

    @Serial
    private static final long serialVersionUID = 3230525044134835918L;

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(AvatarServlet.class);

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
    private ProfileService profileService;

    /**
     * Handles a request for a user's avatar. Expects the user's ID and the type of avatar (full image or thumbnail) as
     * a request parameter.
     * <p>
     * Verifies if the client is authorized to view the avatar, retrieves it and writes the attachment or potential
     * errors to the response.
     *
     * @param request  The request to handle.
     * @param response The response to return to the client.
     */
    @Override
    protected void handleRequest(final HttpServletRequest request, final HttpServletResponse response) {
        if (!applicationSettings.getConfiguration().isGuestReading() && session.getUser() == null) {
            log.debug("Refusing to serve avatar picture to anonymous user.");
            redirectToNotFoundPage(response);
            return;
        }

        // Retrieve user ID and image type from the request.
        int userID;
        try {
            userID = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            log.debug("Invalid user ID given.");
            redirectToNotFoundPage(response);
            return;
        }
        boolean serveThumbnail = "thumbnail".equals(request.getParameter("type"));

        // Fetch the requested image.
        User user = profileService.getUser(userID);
        if (user == null) {
            log.debug("User with ID " + userID + " not found.");
            redirectToNotFoundPage(response);
            return;
        }
        byte[] image = serveThumbnail ? user.getAvatarThumbnail() : user.getAvatar().get();
        if (image == null) {
            log.debug("Avatar or thumbnail for user with ID " + userID + " not found.");
            redirectToNotFoundPage(response);
            return;
        }

        // Initialize servlet response.
        response.reset();
        configureClientCaching(response);

        // Write image to response.
        try {
            response.getOutputStream().write(image);
        } catch (IOException e) {
            log.error("Could not write servlet response.", e);
        }
    }

}
