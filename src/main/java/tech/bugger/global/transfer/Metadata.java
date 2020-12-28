package tech.bugger.global.transfer;

import java.util.Objects;

/**
 * DTO representing application metadata.
 */
public class Metadata {

    /**
     * The application version.
     */
    private String version;

    /**
     * Constructs a new metadata container.
     *
     * @param version The application version.
     */
    public Metadata(final String version) {
        this.version = version;
    }

    /**
     * Returns the application version.
     *
     * @return The application version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the application version.
     *
     * @param version The application version.
     */
    public void setVersion(final String version) {
        this.version = version;
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
        if (!(other instanceof Metadata)) {
            return false;
        }
        Metadata metadata = (Metadata) other;
        return version.equals(metadata.version);
    }

    /**
     * Calculates a hash code for this organization for hashing purposes, and to fulfil the {@link
     * Object#equals(Object)} contract.
     *
     * @return The hash code value of this organization.
     */
    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    /**
     * Converts this organization into a human-readable string representation.
     *
     * @return A human-readable string representation of this organization.
     */
    @Override
    public String toString() {
        return "Metadata{" + "version='" + version + '\'' + '}';
    }

}
