package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;

/**
 * A settings gateway allows to query and modify persistently stored application configuration and organization
 * settings.
 */
public interface SettingsGateway {

    /**
     * Retrieves the current application configuration.
     *
     * @return The current application configuration.
     */
    public Configuration getAppConfig();

    /**
     * Retrieves the current organization settings.
     *
     * @return The current organization settings.
     */
    public Organization getOrganization();

    /**
     * Persistently updates the application configuration.
     *
     * @param config The new application configuration.
     */
    public void setAppConfig(Configuration config);

    /**
     * Persistently updates the organization settings.
     *
     * @param org The new organization settings.
     */
    public void setOrganization(Organization org);
}
