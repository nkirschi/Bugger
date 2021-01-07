package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;

/**
 * DTO representing a topic.
 */
public class Topic implements Serializable {
    @Serial
    private static final long serialVersionUID = 6600990552933685863L;

    private int id;
    private String title;
    private String description;

    /**
     * Constructs a new topic from the specified parameters.
     *
     * @param id          The topic ID.
     * @param title       The topic title.
     * @param description The topic description.
     */
    public Topic(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    /**
     * Returns the ID of this topic.
     *
     * @return The topic ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of this topic.
     *
     * @param id The topic ID to be set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the title of this topic.
     *
     * @return The topic title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of this topic.
     *
     * @param title The topic title to be set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the description of this topic.
     *
     * @return The topic description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this topic.
     *
     * @param description The topic description to be set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Indicates whether some {@code other} topic is semantically equal to this topic.
     *
     * @param other The object to compare this topic to.
     * @return {@code true} iff {@code other} is a semantically equivalent topic.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Topic)) {
            return false;
        }
        Topic that = (Topic) other;
        return id == that.id;

    }

    /**
     * Calculates a hash code for this topic for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this topic.
     */
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    /**
     * Converts this topic into a human-readable string representation.
     *
     * @return A human-readable string representation of this topic.
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

}
