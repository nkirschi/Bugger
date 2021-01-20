package tech.bugger.persistence.gateway;

import com.ocpsoft.pretty.faces.util.StringUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.global.util.Pagitable;
import tech.bugger.persistence.exception.DuplicateException;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.SelfReferenceException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

/**
 * Report gateway that gives access to reports stored in a database.
 */
public class ReportDBGateway implements ReportGateway {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(ReportDBGateway.class);

    /**
     * Database connection used by this gateway.
     */
    private final Connection conn;

    /**
     * User gateway used for finding users.
     */
    private final UserGateway userGateway;

    /**
     * Constructs a new report gateway with the given database connection.
     *
     * @param conn        The database connection to use for the gateway.
     * @param userGateway The user gateway to use.
     */
    public ReportDBGateway(final Connection conn, final UserGateway userGateway) {
        this.conn = conn;
        this.userGateway = userGateway;
    }

    static Report getReportFromResultSet(final ResultSet rs, final UserGateway userGateway)
            throws SQLException, NotFoundException {
        Report report = new Report();
        report.setId(rs.getInt("id"));
        report.setTitle(rs.getString("title"));
        report.setType(Report.Type.valueOf(rs.getString("type")));
        report.setSeverity(Report.Severity.valueOf(rs.getString("severity")));
        report.setVersion(rs.getString("version"));
        report.setTopicID(rs.getInt("topic"));
        report.setDuplicateOf(rs.getInt("duplicate_of"));
        OffsetDateTime closed = null;
        if (rs.getObject("closed_at", OffsetDateTime.class) != null) {
            closed = rs.getObject("closed_at", OffsetDateTime.class);
        }
        report.setClosingDate(closed);
        report.setAuthorship(getAuthorshipFromResultSet(rs, userGateway));
        return report;
    }

    private Report extractRelevanceFromResultSet(final Report report, final ResultSet rs) throws SQLException {
        int relevance = rs.getInt("relevance");
        boolean relevanceOverwritten = false;
        if (rs.getObject("forced_relevance", Integer.class) != null) {
            relevance = rs.getInt("forced_relevance");
            relevanceOverwritten = true;
        }
        report.setRelevance(relevance);
        report.setRelevanceOverwritten(relevanceOverwritten);
        return report;
    }

    /**
     * Parses the given {@link ResultSet} and returns the corresponding {@link Authorship}.
     *
     * @param rs          The {@link ResultSet} to parse.
     * @param userGateway The {@link UserGateway} to use for fetching users.
     * @return The parsed {@link Authorship}.
     * @throws SQLException      Some parsing error occurred.
     * @throws NotFoundException A user could not be found.
     */
    static Authorship getAuthorshipFromResultSet(final ResultSet rs, final UserGateway userGateway)
            throws SQLException, NotFoundException {
        Integer creatorID = rs.getObject("created_by", Integer.class);
        User creator = creatorID == null ? null : userGateway.getUserByID(creatorID);
        OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
        Integer modifierID = rs.getObject("last_modified_by", Integer.class);
        User modifier = modifierID == null ? null : userGateway.getUserByID(modifierID);
        OffsetDateTime modifiedAt = rs.getObject("last_modified_at", OffsetDateTime.class);
        return new Authorship(creator, createdAt, modifier, modifiedAt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countPosts(final Report report) {
        if (report == null) {
            log.error("Cannot count posts of report null.");
            throw new IllegalArgumentException("Report cannot be null.");
        } else if (report.getId() == null) {
            log.error("Cannot count posts of report with ID null.");
            throw new IllegalArgumentException("Report ID must not be null.");
        }

        int count = 0;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM post WHERE report = ?;")) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(report.getId()).toStatement();
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException e) {
            log.error("Error when counting posts of report " + report + ".", e);
            throw new StoreException("Error when counting posts of report " + report + ".", e);
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Report find(final int id) throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM report AS r"
                        + " LEFT OUTER JOIN report_relevance AS v ON r.id = v.report"
                        + " WHERE id = ?;"
        )) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .integer(id)
                    .toStatement().executeQuery();
            if (rs.next()) {
                int relevance = rs.getInt("relevance");
                boolean relevanceOverwritten = false;
                if (rs.getObject("forced_relevance", Integer.class) != null) {
                    relevance = rs.getInt("forced_relevance");
                    relevanceOverwritten = true;
                }

                Integer duplicateOf = rs.getObject("duplicate_of", Integer.class);
                OffsetDateTime closingDate = rs.getObject("closed_at", OffsetDateTime.class);

                return new Report(
                        id,
                        rs.getString("title"),
                        Report.Type.valueOf(rs.getString("type")),
                        Report.Severity.valueOf(rs.getString("severity")),
                        rs.getString("version"),
                        getAuthorshipFromResultSet(rs, userGateway),
                        closingDate,
                        duplicateOf,
                        relevance,
                        relevanceOverwritten,
                        rs.getInt("topic")
                );
            } else {
                throw new NotFoundException("Report could not be found.");
            }
        } catch (SQLException e) {
            throw new StoreException("Error while searching for report.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getSelectedReports(final Topic topic, final Selection selection, final boolean showOpenReports,
                                           final boolean showClosedReports) {
        if (!showOpenReports && !showClosedReports) {
            return Collections.emptyList();
        }

        List<Report> selectedReports = new ArrayList<>();
        String filter = "";
        if (showOpenReports && !showClosedReports) {
            filter = " AND closed_at IS NULL";
        } else if (!showOpenReports) {
            filter = " AND closed_at IS NOT NULL";
        }

        String sql = "SELECT * FROM report AS r"
                + " LEFT OUTER JOIN report_relevance AS v ON r.id = v.report"
                + " WHERE topic = ? " + filter
                + " ORDER BY " + selection.getSortedBy() + (selection.isAscending() ? " ASC" : " DESC")
                + " LIMIT ? OFFSET ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .integer(topic.getId())
                    .integer(Pagitable.getItemLimit(selection))
                    .integer(Pagitable.getItemOffset(selection))
                    .toStatement().executeQuery();

            while (rs.next()) {
                selectedReports.add(extractRelevanceFromResultSet(getReportFromResultSet(rs, userGateway), rs));
            }
            log.debug("Found " + selectedReports.size() + " reports!");
        } catch (SQLException | NotFoundException e) {
            log.error("Error while searching for reports in topic with id " + topic.getId(), e);
            throw new StoreException("Error while searching reports in topic with id " + topic.getId(), e);
        }
        return selectedReports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countDuplicates(final Report report) throws NotFoundException {
        if (report == null) {
            log.error("Cannot count duplicates of report null.");
            throw new IllegalArgumentException("Report cannot be null.");
        } else if (report.getId() == null) {
            log.error("Cannot count duplicates of report with ID null.");
            throw new IllegalArgumentException("Report ID must not be null.");
        }

        int count;

        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM report r1 "
                + "JOIN report r2 ON r1.duplicate_of = r2.id WHERE r2.id = ?")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .integer(report.getId())
                    .toStatement().executeQuery();

            if (rs.next()) {
                count = rs.getInt("count");
            } else {
                log.error("Report could not be found when counting its duplicates!");
                throw new NotFoundException("Report could not be found when counting its duplicates!");
            }
        } catch (SQLException e) {
            log.error("Error when counting posts of report " + report + '.', e);
            throw new StoreException("Error when counting posts of report " + report + '.', e);
        }

        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> selectDuplicates(final Report report, final Selection selection) {
        List<Report> selectedDuplicates = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement("SELECT r1.* FROM report r1"
                + " JOIN report r2 ON r1.duplicate_of = r2.id WHERE r2.id = ?"
                + " ORDER BY r1.id " + (selection.isAscending() ? "ASC" : "DESC")
                + " LIMIT ? OFFSET ?")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .integer(report.getId())
                    .integer(Pagitable.getItemLimit(selection))
                    .integer(Pagitable.getItemOffset(selection))
                    .toStatement().executeQuery();

            while (rs.next()) {
                selectedDuplicates.add(getReportFromResultSet(rs, userGateway));
            }
            log.debug("Found " + selectedDuplicates.size() + " duplicates!");
        } catch (SQLException | NotFoundException e) {
            log.error("Error while searching for duplicates of report with id " + report.getId(), e);
            throw new StoreException("Error while searching duplicates of report with id " + report.getId(), e);
        }

        return selectedDuplicates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(final Report report) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO report (title, type, severity, version, created_by, last_modified_by, topic)"
                        + "VALUES (?, ?::report_type, ?::report_severity, ?, ?, ?, ?);",
                PreparedStatement.RETURN_GENERATED_KEYS
        )) {
            User creator = report.getAuthorship().getCreator();
            User modifier = report.getAuthorship().getModifier();
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .string(report.getTitle())
                    .string(report.getType().name())
                    .string(report.getSeverity().name())
                    .string(report.getVersion())
                    .object(creator == null ? null : creator.getId(), Types.INTEGER)
                    .object(modifier == null ? null : modifier.getId(), Types.INTEGER)
                    .integer(report.getTopicID())
                    .toStatement();
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                report.setId(generatedKeys.getInt("id"));
            } else {
                log.error("Error while retrieving new report ID.");
                throw new StoreException("Error while retrieving new report ID.");
            }
        } catch (SQLException e) {
            log.error("Error while creating report.", e);
            throw new StoreException("Error while creating report.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final Report report) throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE report "
                        + "SET title = ?, type = ?::report_type, severity = ?::report_severity, "
                        + "    version = ?, last_modified_by = ?, topic = ?"
                        + "WHERE id = ?;"
        )) {
            User modifier = report.getAuthorship().getModifier();
            int rowsAffected = new StatementParametrizer(stmt)
                    .string(report.getTitle())
                    .string(report.getType().name())
                    .string(report.getSeverity().name())
                    .string(report.getVersion())
                    .object(modifier == null ? null : modifier.getId(), Types.INTEGER)
                    .object(report.getTopicID(), Types.INTEGER)
                    .integer(report.getId())
                    .toStatement().executeUpdate();
            if (rowsAffected == 0) {
                throw new NotFoundException("Report to be updated could not be found.");
            }
        } catch (SQLException e) {
            throw new StoreException("Error while updating report.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Report report) throws NotFoundException {
        if (report == null) {
            log.error("Cannot delete report null.");
            throw new IllegalArgumentException("Report cannot be null.");
        } else if (report.getId() == null) {
            log.error("Cannot delete report with ID null");
            throw new IllegalArgumentException("Report ID cannot be null.");
        }

        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM report WHERE id = ? RETURNING *;")) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(report.getId()).toStatement();
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                if (rs.getInt("id") != report.getId()) {
                    throw new InternalError("Wrong report deleted! Please investigate! Expected: " + report
                            + ", actual ID: " + rs.getInt("id"));
                }
            } else {
                log.error("Report to delete " + report + " not found.");
                throw new NotFoundException("Report to delete " + report + " not found.");
            }
        } catch (SQLException e) {
            log.error("Error when deleting report " + report + ".", e);
            throw new StoreException("Error when deleting report " + report + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeReport(final Report report) throws NotFoundException {
        if (report == null) {
            log.error("Cannot close report null.");
            throw new IllegalArgumentException("Report cannot be null.");
        } else if (report.getId() == null) {
            log.error("Cannot close report with ID null.");
            throw new IllegalArgumentException("Report ID cannot be null.");
        } else if (report.getClosingDate() == null) {
            log.error("Cannot close report with closing date null.");
            throw new IllegalArgumentException("Report closing date cannot be null.");
        }

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE report SET closed_at = ? WHERE id = ?;")) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .object(report.getClosingDate())
                    .integer(report.getId()).toStatement();
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                log.error("Report to close " + report + " cannot be found.");
                throw new NotFoundException("Report to close " + report + " cannot be found.");
            }
        } catch (SQLException e) {
            log.error("Error when closing report " + report + ".", e);
            throw new StoreException("Error when closing report " + report + ".", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openReport(final Report report) throws NotFoundException {
        if (report == null) {
            log.error("Cannot open report null.");
            throw new IllegalArgumentException("Report cannot be null.");
        } else if (report.getId() == null) {
            log.error("Cannot open report with ID null.");
            throw new IllegalArgumentException("Report ID cannot be null.");
        }

        String sql = "UPDATE report SET closed_at = null WHERE id = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(report.getId()).toStatement();
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                log.error("Report to open " + report + " cannot be found.");
                throw new NotFoundException("Report to open " + report + " cannot be found.");
            }
        } catch (SQLException e) {
            log.error("Error when opening report " + report + ".", e);
            throw new StoreException("Error when opening report " + report + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markDuplicate(final Report duplicate, final int originalID)
            throws NotFoundException, SelfReferenceException {
        if (duplicate == null) {
            log.error("Cannot mark report null as duplicate.");
            throw new IllegalArgumentException("Report cannot be null.");
        } else if (duplicate.getId() == null) {
            log.error("Cannot mark report with ID null as duplicate.");
            throw new IllegalArgumentException("Report ID cannot be null.");
        } else if (duplicate.getId() == originalID) {
            log.error("Cannot mark report " + duplicate + " as original report of itself.");
            throw new SelfReferenceException("Reports cannot mark themselves as original report.");
        }

        String sql = "UPDATE report SET duplicate_of = ? WHERE id = ? OR duplicate_of = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int affectedRows = new StatementParametrizer(stmt)
                    .integer(originalID)
                    .integer(duplicate.getId())
                    .integer(duplicate.getId())
                    .toStatement().executeUpdate();
            if (affectedRows == 0) {
                log.error("Report to mark as duplicate " + duplicate + " cannot be found.");
                throw new NotFoundException("Report to mark as duplicate " + duplicate + " cannot be found.");
            }
            duplicate.setDuplicateOf(originalID);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                // 23503 states that "insert value of foreign key is invalid", hence the original is non-existent.
                log.error("Couldn't find original report when marking report as duplicate.", e);
                throw new NotFoundException(e);
            } else {
                log.error("Error when marking report " + duplicate + " as duplicate.", e);
                throw new StoreException("Error when marking report " + duplicate + " as duplicate.", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unmarkDuplicate(final Report report) throws NotFoundException {
        if (report == null) {
            log.error("Cannot unmark report null as duplicate.");
            throw new IllegalArgumentException("Report cannot be null.");
        } else if (report.getId() == null) {
            log.error("Cannot unmark report with ID null as duplicate.");
            throw new IllegalArgumentException("Report ID cannot be null.");
        }

        String sql = "UPDATE report SET duplicate_of = NULL WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int affectedRows = new StatementParametrizer(stmt)
                    .integer(report.getId())
                    .toStatement().executeUpdate();
            if (affectedRows == 0) {
                log.error("Report to unmark as duplicate " + report + " cannot be found.");
                throw new NotFoundException("Report to unmark as duplicate " + report + " cannot be found.");
            }
            report.setDuplicateOf(null);
        } catch (SQLException e) {
            log.error("Error when unmarking report " + report + " as duplicate.", e);
            throw new StoreException("Error when unmarking report " + report + " as duplicate.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void overwriteRelevance(final Report report, final Integer relevance) {
        String sql = "UPDATE report SET forced_relevance = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int affectedRows = new StatementParametrizer(stmt)
                    .object(relevance)
                    .integer(report.getId())
                    .toStatement().executeUpdate();
            if (affectedRows == 0) {
                log.error("Report to edit relevance " + report + " cannot be found.");
                throw new NotFoundException("Report to edit relevance " + report + " cannot be found.");
            }
        } catch (SQLException | NotFoundException e) {
            log.error("Error when editing relevance of report " + report + ".", e);
            throw new StoreException("Error when editing relevance of report " + report + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasUpvoted(final User user, final Report report) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM relevance_vote "
                + "WHERE voter = ? AND report = ?;")) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(user.getId())
                    .integer(report.getId())
                    .toStatement();
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("weight") > 0;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new StoreException("Error when searching for upvote on " + report + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDownvoted(final User user, final Report report) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM relevance_vote "
                + "WHERE voter = ? AND report = ?;")) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(user.getId())
                    .integer(report.getId()).toStatement();
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("weight") < 0;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new StoreException("Error when searching for downvote on " + report + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upvote(final Report report, final User user, final Integer votingWeight) throws DuplicateException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO relevance_vote (voter, report, weight)"
                        + "VALUES (?, ?, ?)"
        )) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(user.getId())
                    .integer(report.getId())
                    .integer(votingWeight)
                    .toStatement();
            statement.executeUpdate();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                // 23505 states that "foreign key constraint violated", hence the user has already voted.
                log.error("Couldn't find user when inserting token into database.", e);
                throw new DuplicateException(e);
            } else {
                log.error("Error while casting upvote.", e);
                throw new StoreException("Error while casting upvote.", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void downvote(final Report report, final User user, final Integer votingWeight) throws DuplicateException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO relevance_vote (voter, report, weight)"
                        + "VALUES (?, ?, ?);"
        )) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(user.getId())
                    .integer(report.getId())
                    .integer(-votingWeight)
                    .toStatement();
            statement.executeUpdate();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                // 23505 states that "foreign key constraint violated", hence the user has already voted.
                log.error("Couldn't find user when inserting token into database.", e);
                throw new DuplicateException(e);
            } else {
                log.error("Error while casting downvote.", e);
                throw new StoreException("Error while casting downvote.", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeVote(final Report report, final User user) {
        if (report == null) {
            log.error("Cannot delete report null.");
            throw new IllegalArgumentException("Report cannot be null.");
        } else if (report.getId() == null) {
            log.error("Cannot delete report with ID null");
            throw new IllegalArgumentException("Report ID cannot be null.");
        }
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM relevance_vote "
                + "WHERE voter = ? AND report = ?")) {
            new StatementParametrizer(stmt)
                    .integer(user.getId())
                    .integer(report.getId())
                    .toStatement().executeUpdate();
        } catch (SQLException e) {
            log.error("Error when deleting vote on report " + report + ".", e);
            throw new StoreException("Error when deleting vote on report " + report + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int findReportOfPost(final int postID) throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT report FROM post WHERE id = ?;")) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(postID).toStatement();
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("report");
            } else {
                throw new NotFoundException("The report containing post with ID " + postID + "could not be found.");
            }
        } catch (SQLException e) {
            throw new StoreException("Error when finding report containing post with ID " + postID + ".");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> selectSubscribedReports(final User user, final Selection selection) {
        if (selection == null) {
            log.error("Cannot select subscribed reports when selection is null.");
            throw new IllegalArgumentException("Selection cannot be null.");
        } else if (StringUtils.isBlank(selection.getSortedBy())) {
            log.error("Cannot select subscribed reports when sorted by is blank.");
            throw new IllegalArgumentException("Cannot sort by nothing.");
        } else if (user == null) {
            log.error("Cannot select subscribed reports when user is null.");
            throw new IllegalArgumentException("User cannot be null.");
        } else if (user.getId() == null) {
            log.error("Cannot select subscribed reports when user ID is null.");
            throw new IllegalArgumentException("User ID cannot be null.");
        }

        String sql = "SELECT * FROM report_subscription AS s"
                + " LEFT OUTER JOIN report r ON s.report = r.id"
                + " WHERE s.subscriber = ?"
                + " ORDER BY " + selection.getSortedBy() + (selection.isAscending() ? " ASC" : " DESC")
                + " LIMIT ? OFFSET ?;";
        List<Report> selectedReports =
                new ArrayList<>(Math.min(Pagitable.getItemLimit(selection), Math.max(0, selection.getTotalSize())));
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(user.getId())
                    .integer(Pagitable.getItemLimit(selection))
                    .integer(Pagitable.getItemOffset(selection)).toStatement();
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Report r;
                try {
                    r = getReportFromResultSet(rs, userGateway);
                } catch (NotFoundException e) {
                    throw new InternalError("User not found! Who thought it was a good idea to let the helper "
                            + "methods throw a NotFoundException?!", e);
                }
                selectedReports.add(r);
            }
        } catch (SQLException e) {
            log.error("Error while selecting subscribed reports for user " + user + " with " + selection + ".", e);
            throw new StoreException("Error while selecting subscribed reports for user " + user + " with " + selection
                    + ".", e);
        }
        return selectedReports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int countSubscribedReports(final User user) {
        if (user == null) {
            log.error("Cannot count subscribed reports when user is null.");
            throw new IllegalArgumentException("User cannot be null.");
        } else if (user.getId() == null) {
            log.error("Cannot count subscribed reports when user ID is null.");
            throw new IllegalArgumentException("User ID cannot be null.");
        }

        String sql = "SELECT COUNT(*) AS count FROM report_subscription WHERE subscriber = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .integer(user.getId()).toStatement();
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            } else {
                throw new InternalError("Could not count the number of subscribed reports.");
            }
        } catch (SQLException e) {
            log.error("Error while retrieving number of subscribed reports for user " + user + ".", e);
            throw new StoreException("Error while retrieving number of subscribed reports for user " + user + ".", e);
        }
    }

}
