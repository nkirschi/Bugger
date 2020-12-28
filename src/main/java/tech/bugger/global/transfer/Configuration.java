package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * DTO representing application configuration data.
 */
public class Configuration implements Serializable {
    @Serial
    private static final long serialVersionUID = -8910178508236757385L;

    private boolean guestMode;
    private boolean closedReportPosting;
    private String emailFormat;
    private String allowedFileExtensions;
    private int maxAllowedAttachments;
    private Map<Integer, Integer> votingWeightDefinition;

    /**
     * Constructs an empty configuration object.
     */
    public Configuration() {

    }

    /**
     * Constructs a new configuration data object from the specified parameters.
     *
     * @param guestMode              Whether guests have reading permissions.
     * @param closedReportPosting    Whether posting in closed reports is allowed.
     * @param emailFormat            The required format of email addresses.
     * @param allowedFileExtensions  The allowed file extensions as comma-separated list.
     * @param maxAllowedAttachments  The maximum allowed number of post attachments.
     * @param votingWeightDefinition The definition of a user's voting weight as map giving the minimally required total
     *                               number of posts for a certain voting power level.
     */
    public Configuration(boolean guestMode, boolean closedReportPosting, String emailFormat,
                         String allowedFileExtensions, int maxAllowedAttachments,
                         Map<Integer, Integer> votingWeightDefinition) {
        this.guestMode = guestMode;
        this.closedReportPosting = closedReportPosting;
        this.emailFormat = emailFormat;
        this.allowedFileExtensions = allowedFileExtensions;
        this.maxAllowedAttachments = maxAllowedAttachments;
        this.votingWeightDefinition = votingWeightDefinition;
    }

    /**
     * Returns whether guests are granted reading privileges.
     *
     * @return Whether guests have reading permissions.
     */
    public boolean isGuestMode() {
        return guestMode;
    }

    /**
     * Sets whether guests are granted reading privileges.
     *
     * @param guestMode Whether guests have reading permissions.
     */
    public void setGuestMode(boolean guestMode) {
        this.guestMode = guestMode;
    }

    /**
     * Returns whether users are allowed to post in closed reports.
     *
     * @return Whether posting in closed reports is allowed.
     */
    public boolean isClosedReportPosting() {
        return closedReportPosting;
    }

    /**
     * Sets whether users are allowed to post in closed reports.
     *
     * @param closedReportPosting Whether posting in closed reports is allowed.
     */
    public void setClosedReportPosting(boolean closedReportPosting) {
        this.closedReportPosting = closedReportPosting;
    }

    /**
     * Returns the mandatory format email addresses of users must have.
     *
     * @return The required format of email addresses.
     */
    public String getEmailFormat() {
        return emailFormat;
    }

    /**
     * Sets the mandatory format email addresses of users must have.
     *
     * @param emailFormat The required format of email addresses.
     */
    public void setEmailFormat(String emailFormat) {
        this.emailFormat = emailFormat;
    }

    /**
     * Returns the possible file extensions uploaded files may have.
     *
     * @return The allowed file extensions as comma-separated list.
     */
    public String getAllowedFileExtensions() {
        return allowedFileExtensions;
    }

    /**
     * Sets the possible file extensions uploaded files may have.
     *
     * @param allowedFiletypes The allowed file extensions as comma-separated list.
     */
    public void setAllowedFileExtension(String allowedFiletypes) {
        this.allowedFileExtensions = allowedFiletypes;
    }

    /**
     * Returns the maximum number of attachments a post might have.
     *
     * @return The maximum allowed number of post attachments.
     */
    public int getMaxAllowedAttachments() {
        return maxAllowedAttachments;
    }

    /**
     * Sets the maximum number of attachments a post might have.
     *
     * @param maxAllowedAttachments The maximum allowed number of post attachments.
     */
    public void setMaxAllowedAttachments(int maxAllowedAttachments) {
        this.maxAllowedAttachments = maxAllowedAttachments;
    }

    /**
     * Returns the definition of a user's voting weight.
     *
     * @return The voting weight definition as map giving the minimally required total number of posts for a certain
     *         voting power level.
     */
    public Map<Integer, Integer> getVotingWeightDefinition() {
        return votingWeightDefinition;
    }

    /**
     * Sets the definition of a user's voting weight.
     *
     * @param votingWeightDefinition The voting weight definition as map giving the minimally required total number of
     *                               posts for a certain voting power level.
     */
    public void setVotingWeightDefinition(Map<Integer, Integer> votingWeightDefinition) {
        this.votingWeightDefinition = votingWeightDefinition;
    }

    /**
     * Indicates whether some {@code other} configuration is semantically equal to this configuration.
     *
     * @param other The object to compare this configuration to.
     * @return {@code true} iff {@code other} is a semantically equivalent configuration.
     */
    @Override
    public boolean equals(Object other) {
        // TODO Auto-generated method stub
        return super.equals(other);
    }


    /**
     * Calculates a hash code for this configuration for hashing purposes, and to fulfil the {@link
     * Object#equals(Object)} contract.
     *
     * @return The hash code value of this configuration.
     */
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    /**
     * Converts this configuration into a human-readable string representation.
     *
     * @return A human-readable string representation of this configuration.
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

}
