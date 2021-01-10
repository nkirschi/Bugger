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
    public Post find(int id) throws NotFoundException;

    /**
     * Retrieves the list of posts of a report that match the given selection criteria.
     *
     * @param report    The report whose posts to look for.
     * @param selection The search criteria to apply.
     * @return The list of posts of the report that match {@code selection}.
     * @throws NotFoundException The report could not be found.
     */
    List<Post> selectPostsOfReport(Report report, Selection selection) throws NotFoundException;

    /**
     * Inserts a post into the post storage.
     *
     * Sets the ID of {@ode post} that was assigned upon insertion by the post storage.
     *
     * @param post The post to insert.
     */
    public void create(Post post);

    /**
     * Updates a post's attributes in the post storage.
     *
     * @param post The post to update.
     * @throws NotFoundException The post could not be found.
     */
    public void update(Post post) throws NotFoundException;

    /**
     * Deletes a post from the post storage.
     *
     * @param post The post to delete.
     * @throws NotFoundException The post could not be found.
     */
    void delete(Post post) throws NotFoundException;

    /**
     * Retrieves the first post of the given report.
     *
     * @param report The report in question.
     * @return The first post of the report.
     */
    Post getFirstPost(Report report) throws NotFoundException;

}
