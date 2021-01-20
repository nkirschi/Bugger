package tech.bugger.global.transfer;

import java.util.Locale;
import tech.bugger.global.util.Constants;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

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

    /**
     * This user's unique ID.
     */
    private Integer id;

    /**
     * This user's username.
     */
    private String username;

    /**
     * The user's password hashed using the salt {@link #passwordSalt} and algorithm {@link #hashingAlgorithm}.
     */
    private String passwordHash;

    /**
     * The salt to use when hashing this user's password.
     */
    private String passwordSalt;

    /**
     * The hashing algorithm to use when hashing this user's password.
     */
    private String hashingAlgorithm;

    /**
     * This user's e-mail address.
     */
    private String emailAddress;

    /**
     * This user's first name.
     */
    private String firstName;

    /**
     * This user's last name.
     */
    private String lastName;

    /**
     * This user's avatar, loaded lazily.
     */
    private byte[] avatar;

    /**
     * This user's avatar thumbnail.
     */
    private byte[] avatarThumbnail;

    /**
     * This user's current biography in Markdown.
     */
    private String biography;

    /**
     * This user's preferred language.
     */
    private Locale preferredLanguage;

    /**
     * This user's profile visibility.
     */
    private ProfileVisibility profileVisibility;

    /**
     * This user's date of registration.
     */
    private ZonedDateTime registrationDate;

    /**
     * This user's forced voting weight or {@code null} if none.
     */
    private Integer forcedVotingWeight;

    /**
     * Whether this user is administrator or not.
     */
    private boolean administrator;

    /**
     * Constructs an empty user.
     */
    public User() {
        this(null, "", "", "", "", "", "", "", new byte[0], new byte[0],
                "", Locale.ENGLISH, User.ProfileVisibility.FULL, null, null, false);
    }

    /**
     * Constructs a new user from the specified parameters.
     *
     * @param id                 The ID of the user.
     * @param username           The username of the user.
     * @param passwordHash       The hashed password of the user.
     * @param passwordSalt       The salt used to hash the password of the user.
     * @param hashingAlgorithm   The algorithm used to hash the password of the user.
     * @param emailAddress       The e-mail address of the user.
     * @param firstName          The first name of the user.
     * @param lastName           The last name of the user.
     * @param avatar             The avatar of the user, loaded lazily.
     * @param avatarThumbnail    The avatar thumbnail of the user.
     * @param biography          The biography of the user.
     * @param preferredLanguage  The preferred language of the user.
     * @param profileVisibility  The profile visibility of the user.
     * @param registrationDate   The registration date of the user.
     * @param forcedVotingWeight The forced voting weight of the user.
     * @param administrator      The administrator status of the user.
     */
    public User(final Integer id, final String username, final String passwordHash, final String passwordSalt,
                final String hashingAlgorithm, final String emailAddress, final String firstName,
                final String lastName, final byte[] avatar, final byte[] avatarThumbnail,
                final String biography, final Locale preferredLanguage, final ProfileVisibility profileVisibility,
                final ZonedDateTime registrationDate, final Integer forcedVotingWeight, final boolean administrator) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.hashingAlgorithm = hashingAlgorithm;
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.avatarThumbnail = avatarThumbnail;
        this.biography = biography;
        this.preferredLanguage = preferredLanguage;
        this.profileVisibility = profileVisibility;
        this.registrationDate = registrationDate;
        this.forcedVotingWeight = forcedVotingWeight;
        this.administrator = administrator;
    }

    /**
     * Constructs a new user as deep clone of the given user.
     *
     * @param user The user to clone.
     */
    public User(final User user) {
        this(user.id, user.username, user.passwordHash, user.passwordSalt, user.hashingAlgorithm,
                user.emailAddress, user.firstName, user.lastName, user.avatar, user.avatarThumbnail.clone(),
                user.biography, user.preferredLanguage, user.profileVisibility, user.registrationDate,
                user.forcedVotingWeight, user.administrator);
    }

    /**
     * Returns the ID of this user.
     *
     * @return The user ID.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the ID of this user.
     *
     * @param id The user ID to be set.
     */
    public void setId(final Integer id) {
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
    public void setUsername(final String username) {
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
    public void setPasswordHash(final String passwordHash) {
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
    public void setPasswordSalt(final String passwordSalt) {
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
    public void setHashingAlgorithm(final String hashingAlgorithm) {
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
    public void setEmailAddress(final String emailAddress) {
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
    public void setFirstName(final String firstName) {
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
    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the avatar image of this user.
     *
     * @return The user's avatar.
     */
    public byte[] getAvatar() {
        return avatar;
    }

    /**
     * Sets the avatar image of this user.
     *
     * @param avatar The user's avatar to be set.
     */
    public void setAvatar(final byte[] avatar) {
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
    public void setAvatarThumbnail(final byte[] avatarThumbnail) {
        this.avatarThumbnail = avatarThumbnail;
    }

    /**
     * Returns whether the avatar thumbnail is non-empty.
     *
     * @return Whether the avatar thumbnail is non-empty.
     */
    public boolean isExistsAvatar() {
        return avatarThumbnail != null && avatarThumbnail.length > 0;
    }

    /**
     * Returns the biography of this user.
     *
     * @return The user's biography.
     */
    public String getBiography() {
        return biography;
    }

    /**
     * Returns the biography of this user.
     *
     * @param biography The user's biography to be set.
     */
    public void setBiography(final String biography) {
        this.biography = biography;
    }

    /**
     * Returns the preferred language of this user.
     *
     * @return The user's preferred language.
     */
    public Locale getPreferredLanguage() {
        return preferredLanguage;
    }

    /**
     * Sets the preferred language of this user.
     *
     * @param preferredLanguage The user's preferred language to be set.
     */
    public void setPreferredLanguage(final Locale preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    /**
     * Returns all language values as an array.
     *
     * @return All language values.
     */
    public Locale[] getPreferredLanguages() {
        return Constants.LANGUAGES;
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
    public void setProfileVisibility(final ProfileVisibility profileVisibility) {
        this.profileVisibility = profileVisibility;
    }

    /**
     * Returns all profile visibility values as an array.
     *
     * @return All profile visibility values.
     */
    public ProfileVisibility[] getProfileVisibilities() {
        return ProfileVisibility.values();
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
    public void setRegistrationDate(final ZonedDateTime registrationDate) {
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
    public void setForcedVotingWeight(final Integer forcedVotingWeight) {
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
    public void setAdministrator(final boolean administrator) {
        this.administrator = administrator;
    }

    /**
     * Indicates whether some {@code other} user is semantically equal to this user.
     *
     * @param other The object to compare this user to.
     * @return {@code true} iff {@code other} is a semantically equivalent user.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof User)) {
            return false;
        }

        User user = (User) other;
        return Objects.equals(id, user.id);
    }

    /**
     * Calculates a hash code for this user for hashing purposes, and to fulfill the {@link Object#equals(Object)}
     * contract.
     *
     * @return The hash code value of this user.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Converts this user into a human-readable string representation.
     *
     * @return A human-readable string representation of this user.
     */
    @Override
    public String toString() {
        return "User{"
                + "id=" + id
                + ", username='" + username + '\''
                + ", passwordHash='" + passwordHash + '\''
                + ", passwordSalt='" + passwordSalt + '\''
                + ", hashingAlgorithm='" + hashingAlgorithm + '\''
                + ", emailAddress='" + emailAddress + '\''
                + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", avatar=byte[" + (avatar == null ? 0 : avatar.length) + ']'
                + ", avatar=byte[" + (avatarThumbnail == null ? 0 : avatarThumbnail.length) + ']'
                + ", biography='" + biography + '\''
                + ", preferredLanguage=" + preferredLanguage
                + ", profileVisibility=" + profileVisibility
                + ", registrationDate=" + registrationDate
                + ", forcedVotingWeight=" + forcedVotingWeight
                + ", administrator=" + administrator
                + '}';
    }

}
