package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;
import tech.bugger.persistence.exception.NotFoundException;

/**
 * A settings gateway allows to query and modify persistently stored application configuration and organization
 * settings.
 */
public interface SettingsGateway {

    /**
     * Persistently updates the application configuration.
     *
     * @param config The new application configuration.
     */
    void setConfiguration(Configuration config);

    /**
     * Persistently updates the organization settings.
     *
     * @param org The new organization settings.
     */
    void setOrganization(Organization org);

    /**
     * Retrieves the current application configuration.
     *
     * @return The current application configuration.
     */
    Configuration getConfiguration() throws NotFoundException;

    /**
     * Retrieves the current organization settings.
     *
     * @return The current organization settings.
     */
    Organization getOrganization() throws NotFoundException;

}
