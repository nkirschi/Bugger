package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * DTO representing a post.
 */
public class Post implements Serializable {

    @Serial
    private static final long serialVersionUID = -4665707287087752126L;

    /**
     * The post ID.
     */
    private int id;

    /**
     * The post content.
     */
    private String content;

    /**
     * The associated report ID.
     */
    private Integer report;

    /**
     * The manipulation metadata.
     */
    private Authorship authorship;

    /**
     * The post attachments.
     */
    private List<Attachment> attachments;

    /**
     * Whether the current user is privileged to modify this post.
     */
    private boolean privileged;

    /**
     * Constructs a new post from the specified parameters.
     *
     * @param id          The post ID.
     * @param content     The post content.
     * @param report      The associated report ID.
     * @param authorship  The manipulation metadata.
     * @param attachments The post attachments.
     */
    public Post(final int id,
                final String content,
                final Integer report,
                final Authorship authorship,
                final List<Attachment> attachments) {
        this.id = id;
        this.content = content;
        this.report = report;
        this.authorship = authorship;
        this.attachments = attachments;
    }

    /**
     * Returns the ID of this post.
     *
     * @return The post ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of this post.
     *
     * @param id The post ID to be set.
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Returns the content of this post.
     *
     * @return The post content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of this post.
     *
     * @param content The post content to be set.
     */
    public void setContent(final String content) {
        this.content = content;
    }

    /**
     * Returns the report ID associated with this post.
     *
     * @return The associated report ID.
     */
    public Integer getReport() {
        return report;
    }

    /**
     * Sets the report ID associated with this post.
     *
     * @param report The associated report ID to be set.
     */
    public void setReport(final Integer report) {
        this.report = report;
    }

    /**
     * Returns the manipulation metadata of this post.
     *
     * @return The post authorship metadata.
     */
    public Authorship getAuthorship() {
        return authorship;
    }

    /**
     * Sets the manipulation metadata of this post.
     *
     * @param authorship The post authorship metadata to be set.
     */
    public void setAuthorship(final Authorship authorship) {
        this.authorship = authorship;
    }

    /**
     * Returns the attachments of this post.
     *
     * @return The post attachments.
     */
    public List<Attachment> getAttachments() {
        return attachments;
    }

    /**
     * Sets the attachments of this post.
     *
     * @param attachments The post attachments to be set.
     */
    public void setAttachments(final List<Attachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * Indicates whether some {@code other} post is semantically equal to this post.
     *
     * @param other The object to compare this post to.
     * @return {@code true} iff {@code other} is a semantically equivalent post.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Post)) {
            return false;
        }
        Post that = (Post) other;
        return id == that.id;
    }

    /**
     * Calculates a hash code for this post for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this post.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts this post into a human-readable string representation.
     *
     * @return A human-readable string representation of this post.
     */
    @Override
    public String toString() {
        return "Post{"
                + "id='" + id + '\''
                + ", content='" + String.format("%.20s", content) + '\''
                + ", report='#" + report + '\''
                + ", authorship='" + authorship + '\''
                + ", attachments='" + attachments + '\''
                + '}';
    }

}
