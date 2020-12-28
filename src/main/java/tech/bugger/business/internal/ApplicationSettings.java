package tech.bugger.business.internal;

import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;
import tech.bugger.global.util.Log;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;

/**
 * Internal bean holding application-wide settings.
 */
@ApplicationScoped
@Named
public class ApplicationSettings implements Serializable {
    @Serial
    private static final long serialVersionUID = 215148767008692866L;
    private static final Log log = Log.forClass(ApplicationSettings.class);
    private Configuration configuration;
    private Organization organization;

    /**
     * Gets the configuration.
     *
     * @return The configuration.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration.
     *
     * @param configuration The configuration to set.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Gets the organization settings.
     *
     * @return The organization settings.
     */
    public Organization getOrganization() {
        return organization;
    }

    /**
     * Sets the organization settings.
     *
     * @param organization The organization settings to set.
     */
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

}
