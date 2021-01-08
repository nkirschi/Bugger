package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Post gateway that gives access to posts stored in a database.
 */
public class PostDBGateway implements PostGateway {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(PostDBGateway.class);

    /**
     * Database connection used by this gateway.
     */
    private Connection conn;

    /**
     * Constructs a new post gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public PostDBGateway(final Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Post find(final int id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(final Post post) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO post (content, created_by, last_modified_by, report)"
                        + "VALUES (?, ?, ?, ?);",
                PreparedStatement.RETURN_GENERATED_KEYS
        )) {
            User creator = post.getAuthorship().getCreator();
            User modifier = post.getAuthorship().getModifier();
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .string(post.getContent())
                    .object(creator == null ? null : creator.getId(), Types.INTEGER)
                    .object(modifier == null ? null : modifier.getId(), Types.INTEGER)
                    .integer(post.getReport().get().getId())
                    .toStatement();
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                post.setId(generatedKeys.getInt("id"));
            } else {
                log.error("Error while retrieving new post ID.");
                throw new StoreException("Error while retrieving new post ID.");
            }
        } catch (SQLException e) {
            log.error("Error while creating post.", e);
            throw new StoreException("Error while creating post.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final Post post) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Post post) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Post> getPostsOfReport(final Report report, final Selection selection) {
        // TODO Auto-generated method stub
        return null;
    }

}
