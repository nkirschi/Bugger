package tech.bugger.global.transfer;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * DTO representing a verification token.
 */
public class Token implements Serializable {

    private static final long serialVersionUID = -8499689805678436803L;

    /**
     * The available verification token types.
     */
    public enum Type {
        /**
         * A token for registration.
         */
        REGISTER,

        /**
         * A token for resetting the password.
         */
        FORGOT_PASSWORD,

        /**
         * A token for changing the e-mail address.
         */
        CHANGE_EMAIL
    }

    private String value;
    private Type type;
    private ZonedDateTime timestamp;
    private User user;

    /**
     * Constructs a new verification token from the specified parameters.
     *
     * @param value     The token value.
     * @param type      The token type.
     * @param timestamp The token timestamp.
     * @param user      The associated user.
     */
    public Token(String value, Type type, ZonedDateTime timestamp, User user) {
        this.value = value;
        this.type = type;
        this.timestamp = timestamp;
        this.user = user;
    }

    /**
     * Returns the value of this token.
     *
     * @return The token value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of this token.
     *
     * @param value The token value to be set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the type of this token.
     *
     * @return The token type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of this token.
     *
     * @param type The token type to be set.
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Returns the time this token was created.
     *
     * @return The token creation time.
     */
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the time this token was created.
     *
     * @param timestamp The token creation time to be set.
     */
    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the user this token is associated with.
     *
     * @return The associated user.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user this token is associated with.
     *
     * @param user The associated user to be set.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Indicates whether some {@code other} token is semantically equal to this token.
     *
     * @param other The object to compare this token to.
     * @return {@code true} iff {@code other} is a semantically equivalent token.
     */
    @Override
    public boolean equals(Object other) {
        // TODO Auto-generated method stub
        return super.equals(other);
    }

    /**
     * Calculates a hash code for this token for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this token.
     */
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    /**
     * Converts this token into a human-readable string representation.
     *
     * @return A human-readable string representation of this token.
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

}
