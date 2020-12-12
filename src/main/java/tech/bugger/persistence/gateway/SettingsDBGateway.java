package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;
import tech.bugger.global.util.Log;

/**
 * A settings gateway that gives access to settings stored in a database.
 */
public class SettingsDBGateway implements SettingsGateway {

    private static final Log log = Log.forClass(SettingsDBGateway.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getAppConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Organization getOrganization() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAppConfig(Configuration config) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOrganization(Organization org) {
        // TODO Auto-generated method stub

    }

}
