package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.AttachmentGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Service providing methods related to posts and attachments. A {@code Feedback} event is fired, if unexpected
 * circumstances occur.
 */
@Dependent
public class PostService {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(PostService.class);

    @Inject
    NotificationService notificationService;

    /**
     * Transaction manager used for creating transactions.
     */
    private final TransactionManager transactionManager;

    /**
     * Feedback Event for user feedback.
     */
    private final Event<Feedback> feedbackEvent;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new post service with the given dependencies.
     *
     * @param transactionManager The transaction manager to use for creating transactions.
     * @param feedbackEvent      The feedback event to use for user feedback.
     * @param messagesBundle     The resource bundle for feedback messages.
     */
    @Inject
    public PostService(final TransactionManager transactionManager, final Event<Feedback> feedbackEvent,
                       final @RegistryKey("messages") ResourceBundle messagesBundle) {
        this.transactionManager = transactionManager;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
    }

    /**
     * Updates an existing post and notifies users about the change. Notifications are handled by the {@code
     * NotificationService}.
     *
     * @param post The post to update.
     */
    public void updatePost(Post post) {
    }

    /**
     * Creates a new post for an existing report and notifies users about the creation. Notifications are handled by the
     * {@code NotificationService}.
     *
     * @param post The post to be created.
     * @return {@code true} iff creating the post succeeded.
     */
    public boolean createPost(Post post) {
        // Notifications will be dealt with when implementing the subscriptions feature.
        try (Transaction tx = transactionManager.begin()) {
            tx.newPostGateway().create(post);
            AttachmentGateway attachmentGateway = tx.newAttachmentGateway();
            post.getAttachments().forEach(attachmentGateway::create);
            tx.commit();
            log.info("Post created successfully.");
            return true;
        } catch (TransactionException e) {
            log.error("Error while creating a new post.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("create_failure"), Feedback.Type.ERROR));
            return false;
        }
    }

    /**
     * Irreversibly deletes a post and its attachments.
     *
     * @param post The post to be deleted.
     */
    public void deletePost(Post post) {

    }

    /**
     * Returns the post with the specified ID. If no such post exists, returns {@code null} and fires an event.
     *
     * @param id The ID of the post to be returned.
     * @return The post with the specified ID if it exists, {@code null} if no post with that ID exists.
     */
    public Post getPostByID(int id) {
        return null;
    }

    /**
     * Returns the attachments of one particular post.
     *
     * @param post The post in question.
     * @return A list of attachments that may be empty.
     */
    public List<Attachment> getAttachmentsForPost(Post post) {
        return null;
    }

    /**
     * Creates a new attachment in the data storage.
     *
     * @param attachment The attachment to be created.
     */
    public void createAttachment(Attachment attachment) {

    }

    /**
     * Create several new attachments at once.
     *
     * @param attachments The list of attachments to be created.
     */
    public void createMultipleAttachments(List<Attachment> attachments) {

    }

    /**
     * Checks if a user is allowed to modify a certain post. Administrators can modify any post, moderators can modify
     * all posts within their moderated topic, regular users can modify their own posts as long as they have not been
     * banned from the topic the post belongs to. Anonymous users cannot modify any posts.
     *
     * @param user The user in question.
     * @param post The post in question.
     * @return {@code true} if the user is allowed to modify the post, {@code false} otherwise.
     */
    public boolean isPrivileged(User user, Post post) {
        return false;
    }
}
