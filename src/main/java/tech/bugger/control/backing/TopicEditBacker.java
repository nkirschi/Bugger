package tech.bugger.control.backing;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.TopicService;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Topic;
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
     * The ID of the topic to edit.
     */
    private int topicID;

    /**
     * The topic to edit.
     */
    private Topic topic;

    /**
     * Whether to create a new topic or edit an existing one.
     */
    private boolean create;

    /**
     * The topic service user to edit topics.
     */
    private final transient TopicService topicService;

    /**
     * The current external context.
     */
    private final ExternalContext ectx;

    /**
     * The current user session.
     */
    private final UserSession session;

    /**
     * Constructs a new topic page backing bean with the necessary dependencies.
     *
     * @param topicService The topic service to use.
     * @param ectx         The current {@link ExternalContext} of the application.
     * @param session      The current {@link UserSession}.
     */
    @Inject
    public TopicEditBacker(final TopicService topicService,
                           final ExternalContext ectx,
                           final UserSession session) {
        this.topicService = topicService;
        this.ectx = ectx;
        this.session = session;
    }

    /**
     * Initializes the topic edit page. Also checks if the user is allowed to edit the topic. If not, acts as if the
     * page did not exist.
     */
    @PostConstruct
    void init() {
        if (!session.getUser().isAdministrator()) {
            throw new Error404Exception();
        }
        if (ectx.getRequestParameterMap().containsKey("id") && ectx.getRequestParameterMap().get("id") != null) {
            try {
                topicID = Integer.parseInt(ectx.getRequestParameterMap().get("id"));
            } catch (NumberFormatException e) {
                // Topic ID parameter not valid.
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
            success = topicService.createTopic(topic, session.getUser());
        } else {
            success = topicService.updateTopic(topic);
        }
        if (success) {
            log.debug(topic.toString());
            ectx.redirect(ectx.getRequestContextPath() + "/topic?id=" + topic.getId());
        } else {
            throw new Error404Exception();
        }
        return null;
    }

    /**
     * Returns whether to create a new topic or edit an existing one.
     *
     * @return Whether to create a new topic or edit an existing one.
     */
    public boolean isCreate() {
        return create;
    }

    /**
     * Sets whether to create a new topic or edit an existing one.
     * @param create Whether to create a new topic or edit an existing one.
     */
    public void setCreate(final boolean create) {
        this.create = create;
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
