package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A settings gateway that gives access to settings stored in a database.
 */
public class SettingsDBGateway implements SettingsGateway {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(SettingsDBGateway.class);

    /**
     * Database connection used by this gateway.
     */
    private final Connection conn;

    /**
     * Constructs a new settings gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public SettingsDBGateway(final Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getConfiguration() throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM system_settings;")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Configuration(rs.getBoolean("guest_reading"), rs.getBoolean("closed_report_posting"),
                        rs.getString("user_email_format"), rs.getString("allowed_file_extensions"),
                        rs.getInt("max_attachments_per_post"), rs.getString("voting_weight_definition"));
            } else {
                throw new NotFoundException("System configuration not found in database.");
            }
        } catch (SQLException e) {
            log.error("Error while loading system configuration", e);
            throw new StoreException("Error while loading system configuration.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConfiguration(final Configuration configuration) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE system_settings "
                + "SET guest_reading = ?, closed_report_posting = ?, user_email_format = ?,"
                + " allowed_file_extensions = ?, max_attachments_per_post = ?, voting_weight_definition = ?"
                + "WHERE id = 0;")) {

            new StatementParametrizer(stmt)
                    .bool(configuration.isGuestReading())
                    .bool(configuration.isClosedReportPosting())
                    .string(configuration.getUserEmailFormat())
                    .string(configuration.getAllowedFileExtensions())
                    .integer(configuration.getMaxAttachmentsPerPost())
                    .string(configuration.getVotingWeightDefinition())
                    .toStatement().executeUpdate();
        } catch (SQLException e) {
            log.error("Error while updating organization data.", e);
            throw new StoreException("Error while updating organization data.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Organization getOrganization() throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM system_settings;")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Organization(rs.getString("organization_name"), rs.getBytes("organization_logo"),
                        rs.getString("organization_theme"), rs.getString("organization_privacy_policy"),
                        rs.getString("organization_imprint"));
            } else {
                throw new NotFoundException("Organization data not found in database.");
            }
        } catch (SQLException e) {
            log.error("Error while loading organization data.", e);
            throw new StoreException("Error while loading organization data.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOrganization(final Organization organization) {
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE system_settings "
                + "SET organization_name = ?, organization_logo = ?, organization_theme = ?, "
                + "organization_privacy_policy = ?, organization_imprint = ?"
                + "WHERE id = 0;")) {

            new StatementParametrizer(stmt)
                    .string(organization.getName())
                    .bytes(organization.getLogo())
                    .string(organization.getTheme())
                    .string(organization.getImprint())
                    .string(organization.getPrivacyPolicy())
                    .toStatement().executeUpdate();
        } catch (SQLException e) {
            log.error("Error while updating organization data.", e);
            throw new StoreException("Error while updating organization data.", e);
        }
    }

}
