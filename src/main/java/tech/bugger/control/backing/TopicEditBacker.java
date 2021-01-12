package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.MarkdownHandler;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

/**
 * Backing bean for the topic edit page.
 */
@ViewScoped
@Named
public class TopicEditBacker implements Serializable {

    @Serial
    private static final long serialVersionUID = -129399064226820566L;

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final
    Log log = Log.forClass(TopicEditBacker.class);

    /**
     * The ID of the Topic to edit.
     */
    private int topicID;

    /**
     * The Topic to edit.
     */
    private Topic topic;

    /**
     * Whether the user is being created by an administrator or not.
     */
    private boolean create;

    /**
     * The Topic Service user to edit topics.
     */
    private transient TopicService topicService;

    /**
     * The current faces context.
     */
    private FacesContext fctx;

    /**
     * The current user session.
     */
    private UserSession session;

    /**
     * Topic description sanitized for display.
     */
    private String sanitizedDescription;

    @Inject
    TopicEditBacker(final TopicService topicService, final FacesContext fctx, final UserSession session) {
        this.topicService = topicService;
        this.fctx = fctx;
        this.session = session;
    }

    /**
     * Initializes the topic edit page. Also checks if the user is allowed to edit the topic. If not, acts as if the
     * page did not exist.
     */
    @PostConstruct
    public void init() {
        if (session.getUser() == null) {
            redirectTo404Page();
        }
        ExternalContext ext = fctx.getExternalContext();
        User user = session.getUser();
        if (!user.isAdministrator()) {
            redirectTo404Page();
            return;
        }
        if ((ext.getRequestParameterMap().containsKey("id")) && ext.getRequestParameterMap().get("id") != null) {
            try {
                topicID = Integer.parseInt(fctx.getExternalContext().getRequestParameterMap().get("id"));
            } catch (NumberFormatException e) {
                // Report ID parameter not valid.
                redirectTo404Page();
                return;
            }
            topic = topicService.getTopicByID(topicID);
            sanitizedDescription = MarkdownHandler.toHtml(topic.getDescription());
            create = false;
        } else {
            topic = new Topic();
            sanitizedDescription = "";
            create = true;
        }
    }

    /**
     * Creates a FacesMessage to display if an event is fired in one of the injected services.
     *
     * @param feedback The feedback with details on what to display.
     */
    public void displayFeedback(@Observes @Any final Feedback feedback) {
    }

    /**
     * Saves and applies the changes made.
     *
     * @return The page to navigate to.
     */
    public String saveChanges() throws IOException {
        boolean success = false;
        topic.setDescription(sanitizedDescription);
        if (create) {
            success = topicService.createTopic(topic);
        } else {
            success = topicService.updateTopic(topic);
        }
        if (success) {
            log.debug(topic.toString());
            ExternalContext ext = fctx.getExternalContext();
            ext.redirect(ext.getRequestContextPath() + "/faces/view/public/topic.xhtml?id=" + topic.getId());
        } else {
            redirectTo404Page();
        }
        return "";
    }

    /**
     * Redirects the user to a 404 page.
     */
    private void redirectTo404Page() {
        // This will be subject to change when the error page is implemented.
        try {
            ExternalContext ectx = fctx.getExternalContext();
            ectx.redirect(ectx.getRequestContextPath() + "/faces/view/public/error.xhtml");
        } catch (IOException e) {
            throw new InternalError("Redirection to error page failed.");
        }
    }

    /**
     * @return The topicID.
     */
    public int getTopicID() {
        return topicID;
    }

    /**
     * @param topicID The topicID to set.
     */
    public void setTopicID(final int topicID) {
        this.topicID = topicID;
    }

    /**
     * @return The topic.
     */
    public Topic getTopic() {
        return topic;
    }

    /**
     * @return The sanitized Description.
     */
    public String getSanitizedDescription() {
        return sanitizedDescription;
    }

    /**
     * @param sanitizedDescription The sanitizedDescription to set.
     */
    public void setSanitizedDescription(final String sanitizedDescription) {
        this.sanitizedDescription = sanitizedDescription;
    }

    /**
     * @param topic The topic to set.
     */
    public void setTopic(final Topic topic) {
        this.topic = topic;
    }

}
