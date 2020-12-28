package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Metadata;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.StoreException;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * {@inheritDoc}.
 */
public class MetadataDBGateway implements MetadataGateway {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(MetadataGateway.class);

    /**
     * Database connection used by this gateway.
     */
    private final Connection conn;

    /**
     * Constructs a new metadata gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public MetadataDBGateway(final Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Metadata retrieveMetadata() {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT version FROM metadata;")) {
            if (conn.getMetaData().getTables(null, null, "metadata", new String[]{"TABLE"}).next()) {
                log.debug("Found metadata table in database.");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return new Metadata(rs.getString("version"));
                }
            } else {
                log.debug("Metadata table does not exist.");
            }
        } catch (SQLException e) {
            log.error("Could not retrieve schema version from database.", e);
            throw new StoreException(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeSchema(final InputStream is) {
        Scanner scanner = new Scanner(is);
        scanner.useDelimiter(";(?=(?:[^$]*\\$\\$[^$]*\\$\\$)*[^$]*\\Z)");
        try (Statement stmt = conn.createStatement()) {
            while (scanner.hasNext()) {
                stmt.addBatch(scanner.next());
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            log.error("Database schema setup failed. Cannot continue.", e);
            throw new StoreException("Schema initialization failed.", e);
        }
    }

}
