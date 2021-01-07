package tech.bugger.global.transfer;

import tech.bugger.global.util.Lazy;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * DTO representing a post.
 */
public class Post implements Serializable {
    @Serial
    private static final long serialVersionUID = -4665707287087752126L;

    private int id;
    private String content;
    private Lazy<Report> report;
    private Authorship authorship;
    private List<Attachment> attachments;

    /**
     * Constructs a new post from the specified parameters.
     *
     * @param id          The post ID.
     * @param content     The post content.
     * @param report      The associated report.
     * @param authorship  The manipulation metadata.
     * @param attachments The post attachments.
     */
    public Post(int id, String content, Lazy<Report> report, Authorship authorship, List<Attachment> attachments) {
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
    public void setId(int id) {
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
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the report associated with this post.
     *
     * @return The associated report.
     */
    public Lazy<Report> getReport() {
        return report;
    }

    /**
     * Sets the report associated with this post.
     *
     * @param report The associated report to be set.
     */
    public void setReport(Lazy<Report> report) {
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
    public void setAuthorship(Authorship authorship) {
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
    public void setAttachments(List<Attachment> attachments) {
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
        Post post = (Post) other;
        return this.id == post.id;
    }

    /**
     * Calculates a hash code for this post for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this post.
     */
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    /**
     * Converts this post into a human-readable string representation.
     *
     * @return A human-readable string representation of this post.
     */
    @Override
    public String toString() {
        return "Post{ID = " + id + "}";
    }

}
