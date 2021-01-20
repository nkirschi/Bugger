package tech.bugger.global.transfer;

import tech.bugger.global.util.Lazy;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO representing a post attachment.
 */
public class Attachment implements Serializable {

    @Serial
    private static final long serialVersionUID = -2716524830405709705L;

    /**
     * The ID of the attachment.
     */
    private int id;

    /**
     * The filename of the attachment.
     */
    private String name;

    /**
     * The content of the attachment, loaded lazily.
     */
    private byte[] content;

    /**
     * The media type (MIME type) of the attachment.
     */
    private String mimetype;

    /**
     * The post of the attachment, loaded lazily.
     */
    private Lazy<Post> post;

    /**
     * Constructs an empty attachment.
     */
    public Attachment() {

    }

    /**
     * Constructs a new attachment from the specified parameters.
     *
     * @param id       The ID of the attachment.
     * @param name     The filename of the attachment.
     * @param content  The data of the attachment.
     * @param mimetype The MIME type of the attachment.
     * @param post     The post the attachment belongs to.
     */
    public Attachment(final int id, final String name, final byte[] content, final String mimetype,
                      final Lazy<Post> post) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.mimetype = mimetype;
        this.post = post;
    }

    /**
     * Returns the ID of the attachment.
     *
     * @return The attachment ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the attachment.
     *
     * @param id The ID to be set.
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Returns the title of the post.
     *
     * @return The post title.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the title of the attachment.
     *
     * @param name The title to be set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the data of this attachment.
     *
     * @return The attachment data.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Sets the data of this attachment.
     *
     * @param content The data to be set.
     */
    public void setContent(final byte[] content) {
        this.content = content;
    }

    /**
     * Returns the MIME type of this attachment.
     *
     * @return The attachment MIME type.
     */
    public String getMimetype() {
        return mimetype;
    }

    /**
     * Sets the MIME type of this attachment.
     *
     * @param mimetype The attachment MIME type to be set.
     */
    public void setMimetype(final String mimetype) {
        this.mimetype = mimetype;
    }

    /**
     * Returns the post this attachment belongs to.
     *
     * @return The associated post.
     */
    public Lazy<Post> getPost() {
        return post;
    }

    /**
     * Sets the post this attachment belongs to.
     *
     * @param post The post to be set.
     */
    public void setPost(final Lazy<Post> post) {
        this.post = post;
    }

    /**
     * Indicates whether some {@code other} attachment is semantically equal to this attachment.
     *
     * @param other The object to compare this attachment to.
     * @return {@code true} iff {@code other} is a semantically equivalent attachment.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Attachment)) {
            return false;
        }
        Attachment that = (Attachment) other;
        return id == that.id;
    }

    /**
     * Calculates a hash code for this attachment for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this attachment.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts this attachment into a human-readable string representation.
     *
     * @return A human-readable string representation of this attachment.
     */
    @Override
    public String toString() {
        String postId = post.isPresent() ? String.valueOf(post.get().getId()) : "<absent>";
        return "Attachment{"
                + "id='" + id + '\''
                + ", name='" + name + '\''
                + ", content=byte[" + (content != null ? content.length : 0) + "]"
                + ", mimetype='" + mimetype + '\''
                + ", post='##" + postId + '\''
                + '}';
    }

}
