package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.global.util.Log;
import tech.bugger.global.util.Pagitable;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
    public void delete(final Post post) throws NotFoundException {
        if (post == null) {
            log.error("Cannot delete post null.");
            throw new IllegalArgumentException("Post cannot be null.");
        }

        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM post WHERE id = ? RETURNING *;")) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(post.getId()).toStatement();
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                if (rs.getInt("id") != post.getId()) {
                    throw new InternalError("Wrong post deleted! Please investigate! Expected: " + post + ", actual: "
                            + rs.getInt("id"));
                }
            } else {
                log.error("Post to delete " + post + " not found.");
                throw new NotFoundException("Post to delete " + post + " not found.");
            }
        } catch (SQLException e) {
            log.error("Error when deleting post " + post + ".", e);
            throw new StoreException("Error when deleting post " + post + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Post getFirstPost(final Report report) throws NotFoundException {
        if (report == null) {
            log.error("Cannot find first post of report null.");
            throw new IllegalArgumentException("Report cannot be null.");
        } else if (report.getId() == null) {
            log.error("Cannot find first post of report with ID null.");
            throw new IllegalArgumentException("Report ID cannot be null.");
        }

        String sql = "SELECT * FROM post WHERE report = ? ORDER BY created_at ASC LIMIT 1;";
        Post firstPost;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(report.getId()).toStatement();
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                // TODO authorship, attachments
                firstPost = new Post(rs.getInt("id"), rs.getString("content"), new Lazy<>(report), null, null);
            } else {
                log.error("Could not find first post of report " + report + ".");
                throw new NotFoundException("Could not find first post of report " + report + ".");
            }
        } catch (SQLException e) {
            log.error("Error when retrieving first post of report " + report + ".", e);
            throw new StoreException("Error when retrieving first post of report " + report + ".", e);
        }
        return firstPost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Post> selectPostsOfReport(final Report report, final Selection selection) {
        if (report == null) {
            log.error("Error when selecting posts of report null.");
            throw new IllegalArgumentException("Report cannot be null.");
        } else if (report.getId() == null) {
            log.error("Error when selecting posts of report with ID null.");
            throw new IllegalArgumentException("Report ID cannot be null.");
        } else if (selection == null) {
            log.error("Error when selecting posts of report " + report + " with selection null");
            throw new IllegalArgumentException("Selection cannot be null.");
        }

        String sql = "SELECT p.id AS p_id, p.content AS p_content, p.created_at AS p_created_at,"
                + " p.created_by AS p_created_by, p.last_modified_at AS p_last_modified_at,"
                + " p.last_modified_by AS p_last_modified_by, p.report AS p_report,"
                + " author.id AS author_id, author.username AS author_username,"
                + " author.password_hash AS author_password_hash, author.password_salt AS author_password_salt,"
                + " author.hashing_algorithm AS author_hashing_algorithm,"
                + " author.email_address AS author_email_address, author.first_name AS author_first_name,"
                + " author.last_name AS author_last_name, author.avatar AS author_avatar,"
                + " author.avatar_thumbnail AS author_avatar_thumbnail, author.biography AS author_biography,"
                + " author.preferred_language AS author_preferred_language,"
                + " author.profile_visibility AS author_profile_visibility,"
                + " author.registered_at AS author_registered_at,"
                + " author.forced_voting_weight AS author_forced_voting_weight, author.is_admin AS author_is_admin,"
                + " modifier.id AS modifier_id, modifier.username AS modifier_username,"
                + " modifier.password_hash AS modifier_password_hash, modifier.password_salt AS modifier_password_salt,"
                + " modifier.hashing_algorithm AS modifier_hashing_algorithm,"
                + " modifier.email_address AS modifier_email_address, modifier.first_name AS modifier_first_name,"
                + " modifier.last_name AS modifier_last_name, modifier.avatar AS modifier_avatar,"
                + " modifier.avatar_thumbnail AS modifier_avatar_thumbnail, modifier.biography AS modifier_biography,"
                + " modifier.preferred_language AS modifier_preferred_language,"
                + " modifier.profile_visibility AS modifier_profile_visibility,"
                + " modifier.registered_at AS modifier_registered_at,"
                + " modifier.forced_voting_weight AS modifier_forced_voting_weight,"
                + " modifier.is_admin AS modifier_is_admin"
                + " FROM post AS p"
                + " LEFT JOIN \"user\" AS author ON p.created_by = author.id"
                + " LEFT JOIN \"user\" AS modifier ON p.last_modified_by = modifier.id"
                + " WHERE p.report = ?"
                + " ORDER BY p.? ?"
                + " LIMIT ?"
                + " OFFSET ?;";
        List<Post> selectedPosts = new ArrayList<>(Math.max(0, selection.getTotalSize()));
        Lazy<Report> reportLazy = new Lazy<>(report);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(report.getId())
                    .string(selection.getSortedBy())
                    .string(selection.isAscending() ? "ASC" : "DESC")
                    .integer(Pagitable.getItemLimit(selection))
                    .integer(Pagitable.getItemOffset(selection)).toStatement();
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                User author = null;
                if (rs.getInt("p_created_by") != 0) {
                    author = parseUserFromResultSetWithPrefix("author_", rs);
                }
                User modifier = null;
                if (rs.getInt("p_last_modified_by") != 0) {
                    modifier = parseUserFromResultSetWithPrefix("modifier_", rs);
                }
                ZonedDateTime creationDate = null;
                if (rs.getTimestamp("p_created_at") != null) {
                    creationDate = rs.getTimestamp("p_created_at").toInstant().atZone(ZoneId.systemDefault());
                }
                ZonedDateTime modificationDate = null;
                if (rs.getTimestamp("p_last_modified_at") != null) {
                    modificationDate = rs.getTimestamp("p_last_modified_at").toInstant().atZone(ZoneId.systemDefault());
                }
                Authorship authorship = new Authorship(author, creationDate, modifier, modificationDate);
                selectedPosts.add(new Post(rs.getInt("p_id"), rs.getString("p_content"), reportLazy, authorship, null));
            }
        } catch (SQLException e) {
            log.error("Error when selecting posts of report " + report + " with selection " + selection + ".", e);
            throw new StoreException("Error when selecting posts of report " + report + " with selection " + selection
                    + ".", e);
        }

        return selectedPosts;
    }

    private User parseUserFromResultSetWithPrefix(final String prefix, final ResultSet rs) throws SQLException {
        String langStr = rs.getString(prefix + "preferred_language").toUpperCase();
        Language lang = Language.ENGLISH;

        if (!langStr.isBlank()) {
            lang = Language.valueOf(langStr);
        }

        return new User(rs.getInt(prefix + "id"), rs.getString(prefix + "username"),
                rs.getString(prefix + "password_hash"), rs.getString(prefix + "password_salt"),
                rs.getString(prefix + "hashing_algorithm"), rs.getString(prefix + "email_address"),
                rs.getString(prefix + "first_name"), rs.getString(prefix + "last_name"),
                new Lazy<>(rs.getBytes(prefix + "avatar")), rs.getBytes(prefix + "avatar_thumbnail"),
                rs.getString(prefix + "biography"), lang,
                User.ProfileVisibility.valueOf(rs.getString(prefix + "profile_visibility").toUpperCase()),
                rs.getTimestamp(prefix + "registered_at").toLocalDateTime().atZone(ZoneId.systemDefault()),
                rs.getObject(prefix + "forced_voting_weight", Integer.class), rs.getBoolean(prefix + "is_admin"));
    }

}
