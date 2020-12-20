package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;
import tech.bugger.global.util.Log;

import java.sql.Connection;

/**
 * A settings gateway that gives access to settings stored in a database.
 */
public class SettingsDBGateway implements SettingsGateway {

    private static final Log log = Log.forClass(SettingsDBGateway.class);

    private Connection conn;

    /**
     * Constructs a new settings gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public SettingsDBGateway(Connection conn) {
        this.conn = conn;
    }

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
