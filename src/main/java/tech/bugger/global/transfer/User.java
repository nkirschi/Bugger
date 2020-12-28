package tech.bugger.global.transfer;

import tech.bugger.global.util.Lazy;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * DTO representing a user.
 */
public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = -5091686502934907535L;

    /**
     * Available profile visibility settings.
     */
    public enum ProfileVisibility {
        /**
         * Everything is visible.
         */
        FULL,

        /**
         * Only necessary data is visible.
         */
        MINIMAL
    }

    private int id;

    private String username;
    private String passwordHash;
    private String passwordSalt;
    private String hashingAlgorithm;

    private String emailAddress;
    private String firstName;
    private String lastName;
    private Lazy<byte[]> avatar;
    private byte[] avatarThumbnail;
    private String bigraphy;
    private Language preferredLanguage;
    private ProfileVisibility profileVisibility;

    private ZonedDateTime registrationDate;
    private Integer forcedVotingWeight;
    private boolean administrator;

    /**
     * Returns the ID of this user.
     *
     * @return The user ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of this user.
     *
     * @param id The user ID to be set.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the username of this user.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of this user.
     *
     * @param username The username to be set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password hash value of this user.
     *
     * @return The user password hash.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash value of this user.
     *
     * @param passwordHash The user password hash to be set.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Returns the password salt of this user.
     *
     * @return The user password salt.
     */
    public String getPasswordSalt() {
        return passwordSalt;
    }

    /**
     * Sets the password salt of this user.
     *
     * @param passwordSalt The user password salt to be set.
     */
    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    /**
     * Returns the hashing algorithm used for this user's password.
     *
     * @return The user password hashing algorithm.
     */
    public String getHashingAlgorithm() {
        return hashingAlgorithm;
    }

    /**
     * Sets the hashing algorithm used for this user's password.
     *
     * @param hashingAlgorithm The user password hashing algorithm to be set.
     */
    public void setHashingAlgorithm(String hashingAlgorithm) {
        this.hashingAlgorithm = hashingAlgorithm;
    }

    /**
     * Returns the e-mail address of this user.
     *
     * @return The user e-mail address.
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the e-mail address of this user.
     *
     * @param emailAddress The user e-mail address to be set.
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Returns the first name of this user.
     *
     * @return The user's first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of this user.
     *
     * @param firstName The user's first name to be set.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name of this user.
     *
     * @return The user's last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of this user.
     *
     * @param lastName The user's last name to be set.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the avatar image of this user.
     *
     * @return The user's avatar.
     */
    public Lazy<byte[]> getAvatar() {
        return avatar;
    }

    /**
     * Sets the avatar image of this user.
     *
     * @param avatar The user's avatar to be set.
     */
    public void setAvatar(Lazy<byte[]> avatar) {
        this.avatar = avatar;
    }

    /**
     * Returns the avatar thumbnail of this user.
     *
     * @return The user's avatar thumbnail.
     */
    public byte[] getAvatarThumbnail() {
        return avatarThumbnail;
    }

    /**
     * Sets the avatar thumbnail of this user.
     *
     * @param avatarThumbnail The user's avatar thumbnail to be set.
     */
    public void setAvatarThumbnail(byte[] avatarThumbnail) {
        this.avatarThumbnail = avatarThumbnail;
    }

    /**
     * Returns the biography of this user.
     *
     * @return The user's biography.
     */
    public String getBigraphy() {
        return bigraphy;
    }

    /**
     * Returns the biography of this user.
     *
     * @param bigraphy The user's biography to be set.
     */
    public void setBigraphy(String bigraphy) {
        this.bigraphy = bigraphy;
    }

    /**
     * Returns the preferred language of this user.
     *
     * @return The user's preferred language.
     */
    public Language getPreferredLanguage() {
        return preferredLanguage;
    }

    /**
     * Sets the preferred language of this user.
     *
     * @param preferredLanguage The user's preferred language to be set.
     */
    public void setPreferredLanguage(Language preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    /**
     * Returns the profile visibility level of this user.
     *
     * @return The user's profile visibility.
     */
    public ProfileVisibility getProfileVisibility() {
        return profileVisibility;
    }

    /**
     * Sets the profile visibility level of this user.
     *
     * @param profileVisibility The user's profile visibility to be set.
     */
    public void setProfileVisibility(ProfileVisibility profileVisibility) {
        this.profileVisibility = profileVisibility;
    }

    /**
     * Returns the registration date of this user.
     *
     * @return The user's registration date.
     */
    public ZonedDateTime getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Sets the registration date of this user.
     *
     * @param registrationDate The user's registration date to be set.
     */
    public void setRegistrationDate(ZonedDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * Returns the voting weight value to override the calculated voting weight.
     *
     * @return The forced voting weight of {@code null} if it is not overridden.
     */
    public Integer getForcedVotingWeight() {
        return forcedVotingWeight;
    }

    /**
     * Sets the voting weight value to override the calculated voting weight.
     *
     * @param forcedVotingWeight The forced voting weight to be set.
     */
    public void setForcedVotingWeight(Integer forcedVotingWeight) {
        this.forcedVotingWeight = forcedVotingWeight;
    }

    /**
     * Returns whether this user is an administrator.
     *
     * @return This user's administrator status.
     */
    public boolean isAdministrator() {
        return administrator;
    }

    /**
     * Sets whether this user is an administrator.
     *
     * @param administrator This user's administrator status to be set.
     */
    public void setAdministrator(boolean administrator) {
        this.administrator = administrator;
    }

    /**
     * Indicates whether some {@code other} user is semantically equal to this user.
     *
     * @param other The object to compare this user to.
     * @return {@code true} iff {@code other} is a semantically equivalent user.
     */
    @Override
    public boolean equals(Object other) {
        // TODO Auto-generated method stub
        return super.equals(other);
    }

    /**
     * Calculates a hash code for this user for hashing purposes, and to fulfil the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this user.
     */
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    /**
     * Converts this user into a human-readable string representation.
     *
     * @return A human-readable string representation of this user.
     */
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }
}
