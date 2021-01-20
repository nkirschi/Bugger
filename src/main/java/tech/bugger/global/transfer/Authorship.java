package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * DTO representing content authorship metadata.
 * <p>
 * Instances are indented to be standalone objects but rather thought of to be associated with some content object.
 */
public class Authorship implements Serializable {

    @Serial
    private static final long serialVersionUID = -1621253242478497728L;

    /**
     * The User listed as the original author of the content.
     */
    private User creator;

    /**
     * The Point in Time where the creation of the content happened.
     */
    private OffsetDateTime creationDate;

    /**
     * The user listed as the last modifier of the content.
     */
    private User modifier;

    /**
     * The Point in Time where the last modification of the content happened.
     */
    private OffsetDateTime modifiedDate;

    /**
     * Constructs a new authorship metadata object from the specified parameters.
     *
     * @param creator      The creator of the associated content.
     * @param creationDate The creation date of the associated content.
     * @param modifier     The last modifier of the associated content.
     * @param modifiedDate The last modification date of the associated content.
     */
    public Authorship(final User creator, final OffsetDateTime creationDate, final User modifier,
                      final OffsetDateTime modifiedDate) {
        this.creator = creator;
        this.creationDate = creationDate;
        this.modifier = modifier;
        this.modifiedDate = modifiedDate;
    }

    /**
     * Constructs a new authorship as deep clone of the given authorship.
     *
     * @param authorship The authorship to clone.
     */
    public Authorship(final Authorship authorship) {
        this(authorship.creator, authorship.creationDate, authorship.modifier, authorship.modifiedDate);
    }

    /**
     * Returns the user who has created the associated content.
     *
     * @return The original creator.
     */
    public User getCreator() {
        return creator;
    }

    /**
     * Sets the user who has created the associated content.
     *
     * @param creator The original creator.
     */
    public void setCreator(final User creator) {
        this.creator = creator;
    }

    /**
     * Returns the user who last modified the associated content.
     *
     * @return The user responsible for the last modification.
     */
    public User getModifier() {
        return modifier;
    }

    /**
     * Sets the user who last modified the associated content.
     *
     * @param modifier The user responsible for the last modification.
     */
    public void setModifier(final User modifier) {
        this.modifier = modifier;
    }

    /**
     * Returns the date when the associated content was created.
     *
     * @return The original creation date.
     */
    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the date when the associated content was created.
     *
     * @param creationDate The original creation date.
     */
    public void setCreationDate(final OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Returns the date when the associated content was last modified.
     *
     * @return The last modification date.
     */
    public OffsetDateTime getModifiedDate() {
        return modifiedDate;
    }

    /**
     * Sets the date when the associated content was last modified.
     *
     * @param modifiedDate The last modification date.
     */
    public void setModifiedDate(final OffsetDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /**
     * Indicates whether some {@code other} authorship is semantically equal to this authorship.
     *
     * @param other The object to compare this authorship to.
     * @return {@code true} iff {@code other} is a semantically equivalent authorship.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Authorship)) {
            return false;
        }
        Authorship o = (Authorship) other;
        return creator == o.creator
                && creationDate == o.creationDate
                && modifier == o.modifier
                && modifiedDate == o.modifiedDate;
    }

    /**
     * Calculates a hash code for this authorship for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this authorship.
     */
    @Override
    public int hashCode() {
        return Objects.hash(creator, creationDate, modifier, modifiedDate);
    }

    /**
     * Converts this authorship into a human-readable string representation.
     *
     * @return A human-readable string representation of this authorship.
     */
    @Override
    public String toString() {
        return "Authorship{"
                + "creator=" + creator
                + ", creationDate=" + creationDate
                + ", modifier=" + modifier
                + ", modifiedDate='" + modifiedDate
                + '}';
    }

}
