package tech.bugger.global.transfer;

/**
 * DTO representing a user with additional attributes.
 */
public class SearchedUser {

    /**
     * The users forced VotingWeight
     */
    private Integer forcedVotingWeight;

    /**
     * The users calculated VotingWeight
     */
    private int calculatedVotingWeight;

    /**
     * This user's number of posts
     */
    private int numPosts;

    /**
     * This user's username.
     */
    private String username;

    /**
     * This user's first name.
     */
    private String firstName;

    /**
     * This user's last name.
     */
    private String lastName;

    /**
     * Whether this user is administrator or not.
     */
    private boolean administrator;

    /**
     * Constructs a new searched user.
     *
     * @param username The username of the searched user.
     * @param firstName The first name of the searched user.
     * @param lastName The last name of the searched user.
     * @param administrator The administrator status of the searched user
     * @param visibility The visibility of the searched users profile.
     * @param forcedVotingWeight The forced voting weight for the searched user.
     * @param numPosts The number of posts used to calculate the voting weight for the searched user.
     */
    public SearchedUser(final String username, final String firstName, final String lastName, final boolean administrator,
                        final User.ProfileVisibility visibility, final Integer forcedVotingWeight, final int numPosts) {
        this.username = username;
        if (visibility == User.ProfileVisibility.FULL) {
            this.firstName = firstName;
            this.lastName = lastName;
        } else {
            this.firstName = "";
            this.lastName = "";
        }
        this.administrator = administrator;
        this.numPosts = numPosts;
        this.forcedVotingWeight = forcedVotingWeight;
    }

    /**
     * @return The voting weight resulting from calculated and forced voting weight.
     */
    public int getVotingWeight() {
        if (forcedVotingWeight != null) {
            return forcedVotingWeight;
        } else {
            return calculatedVotingWeight;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isAdministrator() {
        return administrator;
    }

    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    public void setCalculatedVotingWeight(int calculatedVotingWeight) {
        this.calculatedVotingWeight = calculatedVotingWeight;
    }

    public void setForcedVotingWeight(Integer forcedVotingWeight) {
        this.forcedVotingWeight = forcedVotingWeight;
    }

    public int getNumPosts() {
        return numPosts;
    }

    public void setNumPosts(int numPosts) {
        this.numPosts = numPosts;
    }
}
