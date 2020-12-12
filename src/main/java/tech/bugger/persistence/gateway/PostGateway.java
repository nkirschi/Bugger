package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.persistence.exception.NotFoundException;

import java.util.List;

/**
 * A post gateway allows to query and modify a persistent storage of posts.
 */
public interface PostGateway {

    /**
     * Retrieves a post by its ID.
     *
     * @param id The ID of the post to look for.
     * @return The post identified by the ID.
     * @throws NotFoundException The post could not be found.
     */
    public Post getPostByID(int id) throws NotFoundException;

    /**
     * Retrieves the list of posts of a report that match the given selection criteria.
     *
     * @param report    The report whose posts to look for.
     * @param selection The search criteria to apply.
     * @return The list of posts of the report that match {@code selection}.
     * @throws NotFoundException The report could not be found.
     */
    public List<Post> getPostsOfReport(Report report, Selection selection) throws NotFoundException;

    /**
     * Inserts a post into the post storage.
     *
     * @param post The post to insert.
     */
    public void createPost(Post post);

    /**
     * Updates a post's attributes in the post storage.
     *
     * @param post The post to update.
     * @throws NotFoundException The post could not be found.
     */
    public void updatePost(Post post) throws NotFoundException;

    /**
     * Deletes a post from the post storage.
     *
     * @param post The post to delete.
     * @throws NotFoundException The post could not be found.
     */
    public void deletePost(Post post) throws NotFoundException;

}
