package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * DTO representing content authorship metadata.
 *
 * Instances are indented to be standalone objects but rather thought of to be associated with some content object.
 */
public class Authorship implements Serializable {
    @Serial
    private static final long serialVersionUID = -1621253242478497728L;

    private User creator;
    private ZonedDateTime creationDate;
    private User modifier;
    private ZonedDateTime modifiedDate;

    /**
     * Constructs a new authorship metadata object from the specified parameters.
     *
     * @param creator      The creator of the associated content.
     * @param creationDate The creation date of the associated content.
     * @param modifier     The last modifier of the associated content.
     * @param modifiedDate The last modification date of the associated content.
     */
    public Authorship(User creator, ZonedDateTime creationDate, User modifier, ZonedDateTime modifiedDate) {
        this.creator = creator;
        this.creationDate = creationDate;
        this.modifier = modifier;
        this.modifiedDate = modifiedDate;
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
    public void setCreator(User creator) {
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
    public void setModifier(User modifier) {
        this.modifier = modifier;
    }

    /**
     * Returns the date when the associated content was created.
     *
     * @return The original creation date.
     */
    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the date when the associated content was created.
     *
     * @param creationDate The original creation date.
     */
    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Returns the date when the associated content was last modified.
     *
     * @return The last modification date.
     */
    public ZonedDateTime getModifiedDate() {
        return modifiedDate;
    }

    /**
     * Sets the date when the associated content was last modified.
     *
     * @param modifiedDate The last modification date.
     */
    public void setModifiedDate(ZonedDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /**
     * Indicates whether some {@code other} authorship is semantically equal to this authorship.
     *
     * @param other The object to compare this authorship to.
     * @return {@code true} iff {@code other} is a semantically equivalent authorship.
     */
    @Override
    public boolean equals(Object other) {
        // TODO Auto-generated method stub
        return super.equals(other);
    }

    /**
     * Calculates a hash code for this authorship for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this authorship.
     */
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    /**
     * Converts this authorship into a human-readable string representation.
     *
     * @return A human-readable string representation of this authorship.
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

}
