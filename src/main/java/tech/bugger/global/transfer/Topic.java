package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO representing a topic.
 */
public class Topic implements Serializable {

    @Serial
    private static final long serialVersionUID = 6600990552933685863L;

    /**
     * The unique ID of a topic.
     */
    private int id;

    /**
     * The title.
     */
    private String title;

    /**
     * The description.
     */
    private String description;

    /**
     * Constructs a new topic from the specified parameters.
     *
     * @param id          The topic ID.
     * @param title       The topic title.
     * @param description The topic description.
     */
    public Topic(final int id, final String title, final String description) {
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
    public void setId(final int id) {
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
    public void setTitle(final String title) {
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
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Indicates whether some {@code other} topic is semantically equal to this topic.
     *
     * @param other The object to compare this topic to.
     * @return {@code true} iff {@code other} is a semantically equivalent topic.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Topic)) {
            return false;
        }
        Topic topic = (Topic) other;
        return this.id == topic.id;
    }

    /**
     * Calculates a hash code for this topic for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this topic.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts this topic into a human-readable string representation.
     *
     * @return A human-readable string representation of this topic.
     */
    @Override
    public String toString() {
        return "Topic{" + "ID = " + id + ", title = " + title + ", description = "
                + String.format("%.100s", description) + '}';
    }

}
