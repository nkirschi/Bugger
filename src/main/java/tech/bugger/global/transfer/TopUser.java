package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO representing a top user entry.
 */
public final class TopUser implements Serializable {

    @Serial
    private static final long serialVersionUID = -2911767883152502483L;

    /**
     * The username of the top user.
     */
    private final String username;

    /**
     * The total relevance this top user received on created reports.
     */
    private final int earnedRelevance;

    /**
     * Constructs a new top user from the given parameters.
     *
     * @param username        The username of the top user.
     * @param earnedRelevance The total relevance this top user received on created reports.
     */
    public TopUser(final String username, final int earnedRelevance) {
        this.username = username;
        this.earnedRelevance = earnedRelevance;
    }

    /**
     * Returns the username of the top user.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the total relevance this top user received on created reports.
     *
     * @return The amount of earned relevance.
     */
    public int getEarnedRelevance() {
        return earnedRelevance;
    }

    /**
     * Indicates whether some {@code other} top user is semantically equal to this top user.
     *
     * @param other The object to compare this top user to.
     * @return {@code true} iff {@code other} is a semantically equivalent top user.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TopUser)) {
            return false;
        }
        TopUser topUser = (TopUser) other;
        return earnedRelevance == topUser.earnedRelevance && username.equals(topUser.username);
    }

    /**
     * Calculates a hash code for this top user for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this top user.
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, earnedRelevance);
    }

    /**
     * Converts this top user into a human-readable string representation.
     *
     * @return A human-readable string representation of this top user.
     */
    @Override
    public String toString() {
        return "TopUser{"
                + "username='" + username + '\''
                + ", earnedRelevance=" + earnedRelevance
                + '}';
    }

}
