package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;

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
