package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.util.Log;
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
<<<<<<< HEAD
     * Database connection used by this gateway.
     */
    private final Connection conn;

    /**
     * A user gateway for further querys.
     */
    private final UserGateway userGateway;

    /**
     * Constructs a new report gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     * @param userGateway The user gateway for further querys.
     */
    public ReportDBGateway(final Connection conn, final UserGateway userGateway) {
        this.conn = conn;
        this.userGateway = userGateway;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfPosts(final Report report) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Report find(final int id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getSelectedReports(final Topic topic, final Selection selection, final boolean showOpenReports,
                                           final boolean showClosedReports) {
        log.info("searching for Reports");
        List<Report> selectedReports = new ArrayList<>(Math.max(0, selection.getTotalSize()));
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"report\" WHERE topic = ?")) {
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
        Integer creatorID = rs.getInt("created_by");
        if (!rs.wasNull()) {
            creator = userGateway.getUserByID(creatorID);
        }
        User modifier = null;
        Integer modifierID = rs.getInt("last_modified_by");
        if (!rs.wasNull()) {
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
    public void update(final Report report) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Report report) {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeReport(final Report report) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openReport(final Report report) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveReport(final Report report, final Topic destination) {
        // TODO Auto-generated method stub

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
