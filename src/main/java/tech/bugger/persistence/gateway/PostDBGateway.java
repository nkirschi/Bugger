package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.util.Log;

import java.util.List;

/**
 * Post gateway that gives access to posts stored in a database.
 */
public class PostDBGateway implements PostGateway {

    private static final Log log = Log.forClass(PostDBGateway.class);

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
