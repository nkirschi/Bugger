package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.util.Log;

import java.sql.Connection;
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
    public Post findPost(final int id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPost(final Post post) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePost(final Post post) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePost(final Post post) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Post> selectPostsOfReport(final Report report, final Selection selection) {
        // TODO Auto-generated method stub
        return null;
    }

}
