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

    private static final Log log = Log.forClass(PostDBGateway.class);

    private Connection conn;

    /**
     * Constructs a new post gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public PostDBGateway(Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Post getPostByID(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPost(Post post) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePost(Post post) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePost(Post post) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Post> getPostsOfReport(Report report, Selection selection) {
        // TODO Auto-generated method stub
        return null;
    }

}
