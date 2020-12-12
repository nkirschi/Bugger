package tech.bugger.global.transfer;

import tech.bugger.global.util.Lazy;

import java.io.Serializable;

/**
 * DTO representing a post attachment.
 */
public class Attachment implements Serializable {

    private static final long serialVersionUID = -2716524830405709705L;

    private int id;
    private String name;
    private Lazy<byte[]> content;
    private String mimetype;
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
     * @param title    The title of the attachment.
     * @param content  The data of the attachment.
     * @param mimetype The MIME type of the attachment.
     * @param post     The post the attachment belongs to.
     */
    public Attachment(int id, String title, Lazy<byte[]> content, String mimetype, Lazy<Post> post) {
        this.id = id;
        this.name = title;
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
    public void setId(int id) {
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
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the data of this attachment.
     *
     * @return The attachment data.
     */
    public Lazy<byte[]> getContent() {
        return content;
    }

    /**
     * Sets the data of this attachment.
     *
     * @param content The data to be set.
     */
    public void setContent(Lazy<byte[]> content) {
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
    public void setMimetype(String mimetype) {
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
    public void setPost(Lazy<Post> post) {
        this.post = post;
    }

    /**
     * Indicates whether some {@code other} attachment is semantically equal to this attachment.
     *
     * @param other The object to compare this attachment to.
     * @return {@code true} iff {@code other} is a semantically equivalent attachment.
     */
    @Override
    public boolean equals(Object other) {
        // TODO Auto-generated method stub
        return super.equals(other);
    }

    /**
     * Calculates a hash code for this attachment for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this attachment.
     */
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    /**
     * Converts this attachment into a human-readable string representation.
     *
     * @return A human-readable string representation of this attachment.
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }
}
