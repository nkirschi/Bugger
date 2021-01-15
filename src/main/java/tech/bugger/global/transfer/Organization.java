package tech.bugger.global.transfer;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * DTO representing an organization.
 */
public class Organization implements Serializable {

    @Serial
    private static final long serialVersionUID = 2775773770560723398L;

    /**
     * Organization name.
     */
    private String name;

    /**
     * Organization logo.
     */
    private byte[] logo;

    /**
     * Organization theme.
     */
    private String theme;

    /**
     * Organization imprint.
     */
    private String imprint;

    /**
     * Organization privacy policy.
     */
    private String privacyPolicy;

    /**
     * Organization support information.
     */
    private String supportInfo;

    /**
     * Constructs a new organization from the specified parameters.
     *
     * @param name          The organization name.
     * @param logo          The organization logo.
     * @param theme         The organization theme.
     * @param imprint       The organization imprint.
     * @param privacyPolicy The organization privacy policy.
     * @param supportInfo   The organization support information.
     */
    public Organization(final String name, final byte[] logo, final String theme,
                        final String imprint, final String privacyPolicy, final String supportInfo) {
        this.name = name;
        this.logo = logo;
        this.theme = theme;
        this.imprint = imprint;
        this.privacyPolicy = privacyPolicy;
        this.supportInfo = supportInfo;
    }

    /**
     * Constructs a new organization from the given organization.
     *
     * @param organization The organization to clone.
     */
    public Organization(final Organization organization) {
        this(organization.name, organization.logo.clone(), organization.theme,
             organization.imprint, organization.privacyPolicy, organization.supportInfo);
    }

    /**
     * Returns the name of this organization.
     *
     * @return The organization name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this organization.
     *
     * @param name The organization name to be set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the logo of this organization.
     *
     * @return The organization logo.
     */
    public byte[] getLogo() {
        return logo;
    }

    /**
     * Returns the logo of this organization.
     *
     * @param logo The organization logo to be set.
     */
    public void setLogo(final byte[] logo) {
        this.logo = logo;
    }

    /**
     * Returns whether the organization logo is non-empty.
     *
     * @return Whether the organization logo is non-empty.
     */
    public boolean isExistsLogo() {
        return logo != null && logo.length > 0;
    }

    /**
     * Returns the theme of this organization.
     *
     * @return The organization theme.
     */
    public String getTheme() {
        return theme;
    }

    /**
     * Sets the theme of this organization.
     *
     * @param theme The organization theme to be set.
     */
    public void setTheme(final String theme) {
        this.theme = theme;
    }

    /**
     * Returns the imprint of the organization.
     *
     * @return The organization imprint.
     */
    public String getImprint() {
        return imprint;
    }

    /**
     * Sets the imprint of the organization.
     *
     * @param imprint The organization imprint to be set.
     */
    public void setImprint(final String imprint) {
        this.imprint = imprint;
    }

    /**
     * Returns the privacy policy of the organization.
     *
     * @return The organization privacy policy.
     */
    public String getPrivacyPolicy() {
        return privacyPolicy;
    }

    /**
     * Sets the privacy policy of the organization.
     *
     * @param privacyPolicy The organization privacy policy to be set.
     */
    public void setPrivacyPolicy(final String privacyPolicy) {
        this.privacyPolicy = privacyPolicy;
    }

    /**
     * Returns the support contact information of the organization.
     *
     * @return The organization support information.
     */
    public String getSupportInfo() {
        return supportInfo;
    }

    /**
     * Sets the support contact information of the organization.
     *
     * @param supportInfo The organization support information to be set.
     */
    public void setSupportInfo(final String supportInfo) {
        this.supportInfo = supportInfo;
    }

    /**
     * Indicates whether some {@code other} organization is semantically equal to this organization.
     *
     * @param other The object to compare this organization to.
     * @return {@code true} iff {@code other} is a semantically equivalent organization.
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Organization)) {
            return false;
        }
        Organization that = (Organization) other;
        return name.equals(that.name)
                && Arrays.equals(logo, that.logo)
                && theme.equals(that.theme)
                && imprint.equals(that.imprint)
                && privacyPolicy.equals(that.privacyPolicy)
                && supportInfo.equals(that.supportInfo);
    }

    /**
     * Calculates a hash code for this organization for hashing purposes, and to fulfil the {@link
     * Object#equals(Object)} contract.
     *
     * @return The hash code value of this organization.
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(name, theme, imprint, privacyPolicy, supportInfo);
        result = 31 * result + Arrays.hashCode(logo);
        return result;
    }

    /**
     * Converts this organization into a human-readable string representation.
     *
     * @return A human-readable string representation of this organization.
     */
    @Override
    public String toString() {
        return "Organization{"
                + "name='" + name + '\''
                + ", logo=byte[" + logo.length + "]"
                + ", theme='" + theme + '\''
                + ", imprint='" + imprint + '\''
                + ", privacyPolicy='" + privacyPolicy + '\''
                + '}';
    }

}
