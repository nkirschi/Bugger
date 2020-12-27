package tech.bugger.persistence.gateway;

import java.io.InputStream;

/**
 * Gateway to meta information about the system.
 */
public interface MetadataGateway {

    /**
     * Retrieves the application version persisted in the data source.
     *
     * @return The application version, being {@code null} iff it is not present.
     */
    String retrieveVersion();

    /**
     * Initializes the data source schema using the given setup stream.
     *
     * @param is Stream of instructions for setting up the schema.
     */
    void initializeSchema(InputStream is);

}
