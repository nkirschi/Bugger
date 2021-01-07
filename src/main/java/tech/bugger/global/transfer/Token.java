package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * DTO representing a verification token.
 */
public class Token implements Serializable {

    @Serial
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

    /**
     * This token's value.
     */
    private String value;

    /**
     * This token's type.
     */
    private Type type;

    /**
     * This token's timestamp of creation.
     */
    private ZonedDateTime timestamp;

    /**
     * This token's meta information.
     */
    private String meta;

    /**
     * This token's associated {@link User}.
     */
    private User user;

    /**
     * Constructs a new verification token from the specified parameters.
     *
     * @param value     The token value.
     * @param type      The token type.
     * @param timestamp The token timestamp.
     * @param user      The associated user.
     */
    public Token(final String value, final Type type, final ZonedDateTime timestamp, final String meta,
                 final User user) {
        this.value = value;
        this.type = type;
        this.timestamp = timestamp;
        this.meta = meta;
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
    public void setValue(final String value) {
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
    public void setType(final Type type) {
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
    public void setTimestamp(final ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the token's meta information.
     *
     * @return The meta information.
     */
    public String getMeta() {
        return meta;
    }

    /**
     * Sets this token's meta information.
     *
     * @param meta The meta information to be set.
     */
    public void setMeta(String meta) {
        this.meta = meta;
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
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * Indicates whether some {@code other} token is semantically equal to this token.
     *
     * @param other The object to compare this token to.
     * @return {@code true} iff {@code other} is a semantically equivalent token.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Token)) {
            return false;
        }

        Token token = (Token) other;
        return value.equals(token.value)
                && type == token.type
                && timestamp.equals(token.timestamp)
                && user.equals(token.user);
    }

    /**
     * Calculates a hash code for this token for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this token.
     */
    @Override
    public int hashCode() {
        return Objects.hash(value, type, timestamp, user);
    }

    /**
     * Converts this token into a human-readable string representation.
     *
     * @return A human-readable string representation of this token.
     */
    @Override
    public String toString() {
        return "Token{"
                + "value='" + value + '\''
                + ", type=" + type
                + ", timestamp=" + timestamp
                + ", user=" + user
                + '}';
    }

}
