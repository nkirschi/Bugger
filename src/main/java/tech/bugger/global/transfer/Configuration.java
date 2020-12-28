package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * DTO representing application configuration data.
 */
public class Configuration implements Serializable {

    @Serial
    private static final long serialVersionUID = -8910178508236757385L;

    /**
     * Whether guests are granted reading permissions.
     */
    private boolean guestReading;

    /**
     * Whether posting in closed reports is permitted.
     */
    private boolean closedReportPosting;

    /**
     * Mandatory format regex for user e-mails.
     */
    private String userEmailFormat;

    /**
     * Allowed extensions for uploaded files as comma-separated list.
     */
    private String allowedFileExtensions;

    /**
     * Maximum allowed number of post attachments.
     */
    private int maxAttachmentsPerPost;

    /**
     * Definition of user voting weights as comma-separated list.
     */
    private String votingWeightDefinition;

    /**
     * Constructs a new configuration data object from the specified parameters.
     *
     * @param guestReading           Whether guests have reading permissions.
     * @param closedReportPosting    Whether posting in closed reports is allowed.
     * @param userEmailFormat        The required format of email addresses.
     * @param allowedFileExtensions  The allowed file extensions as comma-separated list.
     * @param maxAttachmentsPerPost  The maximum allowed number of post attachments.
     * @param votingWeightDefinition The definition of a user's voting weight as map giving the minimally required
     *                               total
     */
    public Configuration(final boolean guestReading, final boolean closedReportPosting, final String userEmailFormat,
                         final String allowedFileExtensions, final int maxAttachmentsPerPost,
                         final String votingWeightDefinition) {
        this.guestReading = guestReading;
        this.closedReportPosting = closedReportPosting;
        this.userEmailFormat = userEmailFormat;
        this.allowedFileExtensions = allowedFileExtensions;
        this.maxAttachmentsPerPost = maxAttachmentsPerPost;
        this.votingWeightDefinition = votingWeightDefinition;
    }

    /**
     * Returns whether guests are granted reading privileges.
     *
     * @return Whether guests have reading permissions.
     */
    public boolean isGuestReading() {
        return guestReading;
    }

    /**
     * Sets whether guests are granted reading privileges.
     *
     * @param guestReading Whether guests have reading permissions.
     */
    public void setGuestReading(final boolean guestReading) {
        this.guestReading = guestReading;
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
    public void setClosedReportPosting(final boolean closedReportPosting) {
        this.closedReportPosting = closedReportPosting;
    }

    /**
     * Returns the mandatory format email addresses of users must have.
     *
     * @return The required format of email addresses.
     */
    public String getUserEmailFormat() {
        return userEmailFormat;
    }

    /**
     * Sets the mandatory format email addresses of users must have.
     *
     * @param userEmailFormat The required format of email addresses.
     */
    public void setUserEmailFormat(final String userEmailFormat) {
        this.userEmailFormat = userEmailFormat;
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
    public void setAllowedFileExtension(final String allowedFiletypes) {
        this.allowedFileExtensions = allowedFiletypes;
    }

    /**
     * Returns the maximum number of attachments a post might have.
     *
     * @return The maximum allowed number of post attachments.
     */
    public int getMaxAttachmentsPerPost() {
        return maxAttachmentsPerPost;
    }

    /**
     * Sets the maximum number of attachments a post might have.
     *
     * @param maxAttachmentsPerPost The maximum allowed number of post attachments.
     */
    public void setMaxAttachmentsPerPost(final int maxAttachmentsPerPost) {
        this.maxAttachmentsPerPost = maxAttachmentsPerPost;
    }

    /**
     * Returns the definition of a user's voting weight.
     *
     * @return The voting weight definition as map giving the minimally required total number of posts for a certain
     *         voting power level.
     */
    public String getVotingWeightDefinition() {
        return votingWeightDefinition;
    }

    /**
     * Sets the definition of a user's voting weight.
     *
     * @param votingWeightDefinition The voting weight definition as map giving the minimally required total number of
     *                               posts for a certain voting power level.
     */
    public void setVotingWeightDefinition(final String votingWeightDefinition) {
        this.votingWeightDefinition = votingWeightDefinition;
    }

    /**
     * Indicates whether some {@code other} configuration is semantically equal to this configuration.
     *
     * @param other The object to compare this configuration to.
     * @return {@code true} iff {@code other} is a semantically equivalent configuration.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Configuration)) {
            return false;
        }
        Configuration that = (Configuration) other;
        return guestReading == that.guestReading
                && closedReportPosting == that.closedReportPosting
                && maxAttachmentsPerPost == that.maxAttachmentsPerPost
                && userEmailFormat.equals(that.userEmailFormat)
                && allowedFileExtensions.equals(that.allowedFileExtensions)
                && votingWeightDefinition.equals(that.votingWeightDefinition);
    }

    /**
     * Calculates a hash code for this configuration for hashing purposes, and to fulfil the {@link
     * Object#equals(Object)} contract.
     *
     * @return The hash code value of this configuration.
     */
    @Override
    public int hashCode() {
        return Objects.hash(guestReading, closedReportPosting, userEmailFormat, allowedFileExtensions,
                maxAttachmentsPerPost, votingWeightDefinition);
    }

    /**
     * Converts this configuration into a human-readable string representation.
     *
     * @return A human-readable string representation of this configuration.
     */
    @Override
    public String toString() {
        return "Configuration{"
                + "guestMode=" + guestReading
                + ", closedReportPosting=" + closedReportPosting
                + ", emailFormat='" + userEmailFormat + "'"
                + ", allowedFileExtensions='" + allowedFileExtensions + "'"
                + ", maxAttachmentsPerPost=" + maxAttachmentsPerPost
                + ", votingWeightDefinition=" + votingWeightDefinition
                + '}';
    }

}
