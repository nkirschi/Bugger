package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.util.List;

/**
 * Service providing methods related to posts and attachments. A {@code Feedback} event is fired, if unexpected
 * circumstances occur.
 */
@Dependent
public class PostService {

    private static final Log log = Log.forClass(PostService.class);

    @Inject
    NotificationService notificationService;

    @Inject
    @Any
    Event<Feedback> feedback;

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
     */
    public void createPost(Post post) {

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
