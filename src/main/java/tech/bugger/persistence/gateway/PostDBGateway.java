package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.StoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        String sql = "SELECT * FROM post AS p"
                + " LEFT JOIN \"user\" AS author ON p.created_by = author.id"
                + " LEFT JOIN \"user\" AS modifier ON p.last_modified_by = modifier.id"
                + " WHERE p.report = " + report.getId()
                + " GROUP BY p.id"
                + " ORDER BY p." + selection.getSortedBy() + (selection.isAscending() ? " ASC" : " DESC")
                + " LIMIT " + selection.getPageSize().getSize()
                + " OFFSET " + selection.getCurrentPage() * selection.getPageSize().getSize() + ";";

        List<Post> selectedPosts = new ArrayList<>(Math.max(0, selection.getTotalSize()));
        Lazy<Report> reportLazy = new Lazy<>(report);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User author = parseUserFromResultSetWithPrefix("author.", rs);
                User modifier = null;
                if (rs.getInt("p.last_modified_by") != 0) {
                    modifier = parseUserFromResultSetWithPrefix("modifier.", rs);
                }
                ZonedDateTime creationDate = null;
                if (rs.getTimestamp("p.created_at") != null) {
                    creationDate = rs.getTimestamp("p.created_at").toInstant().atZone(ZoneId.systemDefault());
                }
                ZonedDateTime modificationDate = null;
                if (rs.getTimestamp("p.last_modified_at") != null) {
                    modificationDate = rs.getTimestamp("p.last_modified_at").toInstant().atZone(ZoneId.systemDefault());
                }
                Authorship authorship = new Authorship(author, creationDate, modifier, modificationDate);
                selectedPosts.add(new Post(rs.getInt("p.id"), rs.getString("p.content"), reportLazy, authorship, null));
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
