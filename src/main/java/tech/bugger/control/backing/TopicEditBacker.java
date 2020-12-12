package tech.bugger.control.backing;

import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.util.Log;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Backing bean for the topic edit page.
 */
@RequestScoped
@Named
public class TopicEditBacker {

    private static final Log log = Log.forClass(TopicEditBacker.class);

    private int topicID;
    private Topic topic;

    @Inject
    private TopicService topicService;

    @Inject
    private FacesContext fctx;

    /**
     * Initializes the topic edit page. Also checks if the user is allowed to edit the topic. If not, acts as if the
     * page did not exist.
     */
    public void init() {
    }

    /**
     * Creates a FacesMessage to display if an event is fired in one of the injected services.
     *
     * @param feedback The feedback with details on what to display.
     */
    public void displayFeedback(@Observes @Any Feedback feedback) {
    }

    /**
     * Saves and applies the changes made.
     */
    public void saveChanges() {
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
    public void setTopicID(int topicID) {
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
    public void setTopic(Topic topic) {
        this.topic = topic;
    }

}
