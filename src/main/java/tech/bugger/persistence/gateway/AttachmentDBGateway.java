package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Attachment gateway that gives access to post attachments stored in a database.
 */
public class AttachmentDBGateway implements AttachmentGateway {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(AttachmentDBGateway.class);

    /**
     * Database connection used by this gateway.
     */
    private Connection conn;

    /**
     * Constructs a new attachment gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public AttachmentDBGateway(final Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(final Attachment attachment) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO attachment (name, content, mimetype, post)"
                        + "VALUES (?, ?, ?, ?);",
                PreparedStatement.RETURN_GENERATED_KEYS
        )) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .string(attachment.getName())
                    .bytes(attachment.getContent().get())
                    .string(attachment.getMimetype())
                    .integer(attachment.getPost().get().getId())
                    .toStatement();
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                attachment.setId(generatedKeys.getInt("id"));
            } else {
                log.error("Error while retrieving new attachment ID.");
                throw new StoreException("Error while retrieving new attachment ID.");
            }
        } catch (SQLException e) {
            log.error("Error while creating attachment.", e);
            throw new StoreException("Error while creating attachment.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final Attachment attachment) throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE attachment "
                        + "SET name = ?, content = ?, mimetype = ?, post = ? "
                        + "WHERE id = ?;"
        )) {
            int rowsAffected = new StatementParametrizer(stmt)
                    .string(attachment.getName())
                    .bytes(attachment.getContent().get())
                    .string(attachment.getMimetype())
                    .integer(attachment.getPost().get().getId())
                    .integer(attachment.getId())
                    .toStatement().executeUpdate();
            if (rowsAffected == 0) {
                log.error("Attachment to be updated could not be found.");
                throw new NotFoundException("Attachment to be updated could not be found.");
            }
        } catch (SQLException e) {
            log.error("Error while updating attachment.", e);
            throw new StoreException("Error while updating attachment.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Attachment attachment) throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM attachment "
                        + "WHERE id = ?;"
        )) {
            int rowsAffected = new StatementParametrizer(stmt)
                    .integer(attachment.getId())
                    .toStatement().executeUpdate();
            if (rowsAffected == 0) {
                log.error("Attachment to be deleted could not be found.");
                throw new NotFoundException("Attachment to be deleted could not be found.");
            }
        } catch (SQLException e) {
            log.error("Error while deleting attachment.", e);
            throw new StoreException("Error while deleting attachment.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Attachment getContentByID(final int id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Attachment> getAttachmentsForPost(final Post post) {
        // TODO Auto-generated method stub
        return null;
    }

}
