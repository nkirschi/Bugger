package tech.bugger.global.transfer;

import java.util.Objects;

/**
 * DTO representing application metdata.
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
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof Metadata)) {
            return false;
        }
        Metadata metadata = (Metadata) that;
        return version.equals(metadata.version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Metadata{" + "version='" + version + '\'' + '}';
    }

}
