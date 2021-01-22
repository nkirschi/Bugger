package tech.bugger.persistence.gateway;

import java.io.InputStream;
import tech.bugger.global.transfer.Metadata;

/**
 * Gateway to meta information about the system.
 */
public interface MetadataGateway {

    /**
     * Retrieves the application metadata persisted in the data source.
     *
     * @return The application metadata, being {@code null} iff it is not present.
     */
    Metadata retrieveMetadata();

    /**
     * Initializes the data source schema using the given setup stream.
     *
     * @param is Stream of instructions for setting up the schema.
     */
    void initializeSchema(InputStream is);

}
