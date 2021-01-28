package tech.bugger.business.service;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.AttachmentGateway;
import tech.bugger.persistence.gateway.UserGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.http.Part;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Service providing methods related to posts and attachments. A {@link Feedback} event is fired, if unexpected
 * circumstances occur.
 */
@ApplicationScoped
public class PostService {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(PostService.class);

    /**
     * Notification service used for sending notifications.
     */
    private final NotificationService notificationService;

    /**
     * The current application settings.
     */
    private final ApplicationSettings applicationSettings;

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
     * @param notificationService The notification service to use for sending notifications.
     * @param applicationSettings The application settings to use.
     * @param transactionManager  The transaction manager to use for creating transactions.
     * @param feedbackEvent       The feedback event to use for user feedback.
     * @param messagesBundle      The resource bundle for feedback messages.
     */
    @Inject
    public PostService(final NotificationService notificationService,
                       final ApplicationSettings applicationSettings,
                       final TransactionManager transactionManager,
                       final Event<Feedback> feedbackEvent,
                       final @RegistryKey("messages") ResourceBundle messagesBundle) {
        this.notificationService = notificationService;
        this.applicationSettings = applicationSettings;
        this.transactionManager = transactionManager;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
    }

    /**
     * Updates an existing post and notifies users about the change. Notifications are handled by the {@link
     * NotificationService}.
     *
     * @param post   The post to update.
     * @param report The report the {@code post} is in.
     * @return {@code true} iff updating the post succeeded.
     */
    public boolean updatePost(final Post post, final Report report) {
        try (Transaction tx = transactionManager.begin()) {
            post.getAuthorship().setModifiedDate(OffsetDateTime.now());
            tx.newPostGateway().update(post);
            AttachmentGateway attachmentGateway = tx.newAttachmentGateway();
            List<Attachment> newAttachments = post.getAttachments();
            List<Attachment> oldAttachments = attachmentGateway.getAttachmentsForPost(post);

            // Delete all existing attachments that do not occur in the edited post.
            for (Attachment oldAttachment : oldAttachments) {
                if (!newAttachments.contains(oldAttachment)) {
                    attachmentGateway.delete(oldAttachment);
                }
            }

            // Create all new attachments that do not yet exist in the post.
            for (Attachment newAttachment : newAttachments) {
                if (!oldAttachments.contains(newAttachment)) {
                    attachmentGateway.create(newAttachment);
                }
            }

            tx.commit();
        } catch (NotFoundException e) {
            log.error("Post to be updated could not be found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
            return false;
        } catch (TransactionException e) {
            log.error("Error while updating a post.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("update_failure"), Feedback.Type.ERROR));
            return false;
        }
        Notification notification = new Notification();
        notification.setType(Notification.Type.EDITED_POST);
        notification.setActuatorID(post.getAuthorship().getModifier().getId());
        notification.setTopicID(report.getTopicID());
        notification.setReportID(report.getId());
        notification.setPostID(post.getId());
        notification.setReportTitle(report.getTitle());
        notificationService.createNotification(notification);
        return true;
    }

    /**
     * Checks whether an attachment's name is valid according to the current application configuration.
     *
     * @param name The attachment name to check the validity of.
     * @return Whether the attachment name is valid.
     */
    public boolean isAttachmentNameValid(final String name) {
        return Arrays.stream(applicationSettings.getConfiguration().getAllowedFileExtensions().split(","))
                .anyMatch(suffix -> name.endsWith(suffix.trim()));
    }

    /**
     * Checks whether a list of attachments is allowed for a post according to the current application configuration.
     *
     * @param attachments The list of attachments to check the validity of.
     * @return Whether the list of attachments is valid.
     */
    public boolean isAttachmentListValid(final List<Attachment> attachments) {
        int maxAttachments = applicationSettings.getConfiguration().getMaxAttachmentsPerPost();
        if (attachments.size() > maxAttachments) {
            log.info("Trying to create post with too many attachments.");
            String message = MessageFormat.format(messagesBundle.getString("too_many_attachments"),
                    maxAttachments);
            feedbackEvent.fire(new Feedback(message, Feedback.Type.ERROR));
            return false;
        }

        if (attachments.size() != attachments.stream().map(Attachment::getName).distinct().count()) {
            log.info("Trying to create post where attachment names are not unique.");
            feedbackEvent.fire(new Feedback(messagesBundle.getString("attachment_names_not_unique"),
                    Feedback.Type.ERROR));
            return false;
        }

        if (!attachments.stream().map(Attachment::getName).allMatch(this::isAttachmentNameValid)) {
            log.info("Trying to create post with invalid attachment name.");
            feedbackEvent.fire(new Feedback(messagesBundle.getString("attachment_names_invalid"),
                    Feedback.Type.ERROR));
            return false;
        }

        return true;
    }

    /**
     * Creates a post using a given {@link Transaction}.
     *
     * @param post The post to be created.
     * @param tx   The transaction to use when creating the post.
     * @return {@code true} iff creating the post succeeded.
     */
    boolean createPostWithTransaction(final Post post, final Transaction tx) {
        boolean valid = isAttachmentListValid(post.getAttachments());
        if (valid) {
            tx.newPostGateway().create(post);
            AttachmentGateway attachmentGateway = tx.newAttachmentGateway();
            post.getAttachments().forEach(attachmentGateway::create);
        }
        return valid;
    }

    /**
     * Creates a new post for an existing report and notifies users about the creation. Notifications are handled by the
     * {@link NotificationService}.
     *
     * @param post   The post to be created.
     * @param report The report the {@code post} shall be in.
     * @return {@code true} iff creating the post succeeded.
     */
    public boolean createPost(final Post post, final Report report) {
        boolean success;
        try (Transaction tx = transactionManager.begin()) {
            success = createPostWithTransaction(post, tx);
            if (success) {
                tx.commit();
                log.info("Post created successfully.");
                feedbackEvent.fire(new Feedback(messagesBundle.getString("post_created"), Feedback.Type.INFO));
            }
        } catch (TransactionException e) {
            log.error("Error while creating a new post.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("create_failure"), Feedback.Type.ERROR));
            return false;
        }
        if (success) {
            Notification notification = new Notification();
            notification.setType(Notification.Type.NEW_POST);
            notification.setActuatorID(post.getAuthorship().getCreator().getId());
            notification.setTopicID(report.getTopicID());
            notification.setReportID(report.getId());
            notification.setPostID(post.getId());
            notification.setReportTitle(report.getTitle());
            notificationService.createNotification(notification);
        }
        return success;
    }

    /**
     * Checks whether an uploaded attachment can be added to a given post and, if so, adds it to the post.
     *
     * @param post The post to add the attachment to.
     * @param part The uploaded file to add as an attachment.
     */
    public void addAttachment(final Post post, final Part part) {
        if (post == null) {
            throw new IllegalArgumentException("Post must not be null.");
        }
        if (part == null) {
            throw new IllegalArgumentException("Part must not be null.");
        }

        byte[] content;
        try {
            content = part.getInputStream().readAllBytes();
        } catch (IOException e) {
            log.info("Uploaded attachment could not be read", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("attachment_invalid"), Feedback.Type.ERROR));
            return;
        }

        Attachment attachment = new Attachment();
        attachment.setName(part.getSubmittedFileName());
        attachment.setContent(content);
        attachment.setMimetype(part.getContentType());
        attachment.setPost(post.getId());

        List<Attachment> attachments = post.getAttachments();
        attachments.add(attachment);
        if (isAttachmentListValid(attachments)) {
            log.debug("Attachment '" + attachment.getName() + "' uploaded.");
        } else {
            attachments.remove(attachment);
        }
    }

    /**
     * Irreversibly deletes a post and its attachments. If {@code post} happens to be the last in its {@code report},
     * then the latter will be deleted as well.
     *
     * @param post   The post to be deleted.
     * @param report The report the {@code post} is in.
     */
    public void deletePost(final Post post, final Report report) {
        if (post == null) {
            log.error("Cannot delete post null.");
            throw new IllegalArgumentException("Post cannot be null.");
        }
        try (Transaction tx = transactionManager.begin()) {
            Post firstPost = tx.newPostGateway().getFirstPost(report);
            if (post.equals(firstPost)) {
                tx.newReportGateway().delete(report);
            } else {
                tx.newPostGateway().delete(post);
            }
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Post to be deleted " + post + " not found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error when deleting post " + post + ".", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
    }

    /**
     * Returns the post with the specified ID. If no such post exists, returns {@code null} and fires an event.
     *
     * @param id The ID of the post to be returned.
     * @return The post with the specified ID if it exists, {@code null} if no post with that ID exists.
     */
    public Post getPostByID(final int id) {
        try (Transaction tx = transactionManager.begin()) {
            Post post = tx.newPostGateway().find(id);
            tx.commit();
            return post;
        } catch (NotFoundException e) {
            log.debug("Post not found.", e);
            return null;
        } catch (TransactionException e) {
            log.error("Error while searching for post.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("lookup_failure"), Feedback.Type.ERROR));
            return null;
        }
    }

    /**
     * Returns the attachment with the specified ID.
     *
     * @param id The ID of the attachment to be returned.
     * @return The attachment with the specified ID if it exists, {@code null} if no attachment with that ID exists.
     */
    public Attachment getAttachmentByID(final int id) {
        try (Transaction tx = transactionManager.begin()) {
            Attachment attachment = tx.newAttachmentGateway().find(id);
            tx.commit();
            return attachment;
        } catch (NotFoundException e) {
            log.debug("Attachment not found.", e);
            return null;
        } catch (TransactionException e) {
            log.error("Error while retrieving attachment.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("lookup_failure"), Feedback.Type.ERROR));
            return null;
        }
    }

    /**
     * Returns the content of an attachment with the specified ID.
     *
     * @param id The ID of the attachment whose content to retrieve.
     * @return The content of the attachment if it was found, {@code null} otherwise.
     */
    public byte[] getAttachmentContent(final int id) {
        try (Transaction tx = transactionManager.begin()) {
            byte[] content = tx.newAttachmentGateway().findContent(id);
            tx.commit();
            return content;
        } catch (NotFoundException e) {
            log.debug("Attachment content not found.", e);
            return null;
        } catch (TransactionException e) {
            log.error("Error while searching for attachment content.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("lookup_failure"), Feedback.Type.ERROR));
            return null;
        }
    }

    /**
     * Checks if a user is allowed to modify (edit or delete) a certain post. Administrators can modify any post,
     * moderators can modify all posts within their moderated topic, regular users can modify their own posts as long as
     * they have not been banned from the topic the post belongs to. Anonymous users cannot modify any posts.
     *
     * @param user   The user in question.
     * @param post   The post in question.
     * @param report The report of the post in question.
     * @return {@code true} iff the user is allowed to modify the post.
     */
    public boolean isPrivileged(final User user, final Post post, final Report report) {
        if (user == null || post == null) {
            return false;
        } else if (user.isAdministrator()) {
            return true;
        }

        if (report == null) {
            return false;
        }

        try (Transaction tx = transactionManager.begin()) {
            Topic topic = tx.newTopicGateway().findTopic(report.getTopicID());
            UserGateway userGateway = tx.newUserGateway();
            if (userGateway.isBanned(user, topic)) {
                feedbackEvent.fire(new Feedback(messagesBundle.getString("user_banned"), Feedback.Type.ERROR));
                tx.commit();
                return false;
            } else if (userGateway.isModerator(user, topic) || user.equals(post.getAuthorship().getCreator())) {
                tx.commit();
                return true;
            }
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Unable to find an answer, if the user with id " + user.getId() + " is privileged for the "
                    + "post with id " + post.getId(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while trying to determine if the user with id " + user.getId() + " is privileged for "
                    + "the post with id " + post.getId(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("lookup_failure"), Feedback.Type.ERROR));
        }
        return false;
    }

}
