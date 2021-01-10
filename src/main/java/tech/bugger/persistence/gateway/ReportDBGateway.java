package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                "SELECT * FROM report WHERE id = ?;"
        )) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .integer(id)
                    .toStatement().executeQuery();
            if (rs.next()) {
                Integer creatorID = rs.getObject("created_by", Integer.class);
                User creator = creatorID == null ? null : userGateway.getUserByID(creatorID);
                ZonedDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime()
                        .atZone(ZoneId.systemDefault());
                Integer modifierID = rs.getObject("last_modified_by", Integer.class);
                User modifier = modifierID == null ? null : userGateway.getUserByID(modifierID);
                ZonedDateTime modifiedAt = rs.getTimestamp("last_modified_at").toLocalDateTime()
                        .atZone(ZoneId.systemDefault());
                Authorship authorship = new Authorship(creator, createdAt, modifier, modifiedAt);

                Integer forcedRelevance = rs.getObject("forced_relevance", Integer.class);
                Integer duplicateOf = rs.getObject("duplicate_of", Integer.class);
                Timestamp closingDate = rs.getTimestamp("closed_at");

                return new Report(
                        id,
                        rs.getString("title"),
                        Report.Type.valueOf(rs.getString("type")),
                        Report.Severity.valueOf(rs.getString("severity")),
                        rs.getString("version"),
                        authorship,
                        closingDate != null ? closingDate.toLocalDateTime().atZone(ZoneId.systemDefault()) : null,
                        duplicateOf,
                        forcedRelevance,
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
        List<Report> selectedReports = new ArrayList<>(Math.max(0, selection.getTotalSize()));
        String filter = ";";
        if (!showClosedReports) {
            filter = "AND closed_at IS NULL;";
        }
        if (!showOpenReports) {
            filter = "AND closed_at IS NOT NULL;";
        }
        if (!showClosedReports && !showOpenReports) {
            return selectedReports;
        }
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM report WHERE topic = ? " + filter)) {
            ResultSet rs = new StatementParametrizer(stmt).integer(topic.getId()).toStatement().executeQuery();

            while (rs.next()) {
                log.info("found a Report!");
                selectedReports.add(getReportFromResultSet(rs));
            }
        } catch (SQLException | NotFoundException e) {
            log.error("Error while searching for reports in topic with id " + topic.getId(), e);
            throw new StoreException("Error while searching reports in topic with id " + topic.getId(), e);
        }
        return selectedReports;
    }

    private Report getReportFromResultSet(final ResultSet rs) throws SQLException, NotFoundException {
        Report report = new Report();
        report.setId(rs.getInt("id"));
        report.setTitle(rs.getString("title"));
        report.setType(Report.Type.valueOf(rs.getString("type")));
        report.setSeverity(Report.Severity.valueOf(rs.getString("severity")));
        report.setVersion(rs.getString("version"));
        report.setForcedRelevance(rs.getInt("forced_relevance"));
        report.setTopic(rs.getInt("topic"));
        report.setDuplicateOf(rs.getInt("duplicate_of"));
        ZonedDateTime created = null;
        ZonedDateTime modified = null;
        ZonedDateTime closed = null;
        User creator = null;
        Integer creatorID = rs.getObject("created_by", Integer.class);
        if (creatorID != null) {
            creator = userGateway.getUserByID(creatorID);
        }
        User modifier = null;
        Integer modifierID = rs.getObject("last_modified_by", Integer.class);
        if (modifierID != null) {
            modifier = userGateway.getUserByID(modifierID);
        }
        if (rs.getTimestamp("created_at") != null) {
            created = (rs.getTimestamp("created_at").toInstant().atZone(ZoneId.systemDefault()));
        }
        if (rs.getTimestamp("last_modified_at") != null) {
            modified = (rs.getTimestamp("last_modified_at").toInstant().atZone(ZoneId.systemDefault()));
        }
        if (rs.getTimestamp("closed_at") != null) {
            closed = (rs.getTimestamp("closed_at").toInstant().atZone(ZoneId.systemDefault()));
        }
        report.setClosingDate(closed);
        Authorship authorship = new Authorship(creator, created, modifier, modified);
        report.setAuthorship(authorship);
        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(final Report report) {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO report (title, type, severity, created_by, last_modified_by, topic)"
                        + "VALUES (?, ?::report_type, ?::report_severity, ?, ?, ?);",
                PreparedStatement.RETURN_GENERATED_KEYS
        )) {
            User creator = report.getAuthorship().getCreator();
            User modifier = report.getAuthorship().getModifier();
            PreparedStatement statement = new StatementParametrizer(stmt)
                    .string(report.getTitle())
                    .string(report.getType().name())
                    .string(report.getSeverity().name())
                    .object(creator == null ? null : creator.getId(), Types.INTEGER)
                    .object(modifier == null ? null : modifier.getId(), Types.INTEGER)
                    .integer(report.getTopic())
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
                    .object(report.getTopic(), Types.INTEGER)
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
                    .string("'" + Timestamp.from(report.getClosingDate().toInstant()) + "'")
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
    public void markDuplicate(final Report duplicate, final int originalID) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unmarkDuplicate(final Report report) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void overwriteRelevance(final Report report, final Optional<Integer> relevance) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upvote(final Report report, final User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void downvote(final Report report, final User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeVote(final Report report, final User user) {
        // TODO Auto-generated method stub

    }

}
