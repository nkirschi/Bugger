package tech.bugger.control.backing;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.TopicService;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

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
    private static final Log log = Log.forClass(TopicEditBacker.class);

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
    private final transient TopicService topicService;

    /**
     * The current faces context.
     */
    private final FacesContext fctx;

    /**
     * The current user session.
     */
    private final UserSession session;

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
            throw new Error404Exception();
        }
        ExternalContext ext = fctx.getExternalContext();
        User user = session.getUser();
        if (!user.isAdministrator()) {
            throw new Error404Exception();
        }
        if ((ext.getRequestParameterMap().containsKey("id")) && ext.getRequestParameterMap().get("id") != null) {
            try {
                topicID = Integer.parseInt(fctx.getExternalContext().getRequestParameterMap().get("id"));
            } catch (NumberFormatException e) {
                // Report ID parameter not valid.
                throw new Error404Exception();
            }
            topic = topicService.getTopicByID(topicID);
            create = false;
        } else {
            topic = new Topic();
            create = true;
        }
    }

    /**
     * Saves and applies the changes made.
     *
     * @return The page to navigate to.
     */
    public String saveChanges() throws IOException {
        boolean success;
        if (create) {
            success = topicService.createTopic(topic);
        } else {
            success = topicService.updateTopic(topic);
        }
        if (success) {
            log.debug(topic.toString());
            ExternalContext ectx = fctx.getExternalContext();
            ectx.redirect(ectx.getRequestContextPath() + "/topic?id=" + topic.getId());
        } else {
            throw new Error404Exception();
        }
        return "";
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
     * @param topic The topic to set.
     */
    public void setTopic(final Topic topic) {
        this.topic = topic;
    }

}
