package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private Connection conn;

    /**
     * Constructs a new report gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public ReportDBGateway(final Connection conn) {
        this.conn = conn;
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
        // TODO Auto-generated method stub
        return null;
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
                    .integer(creator == null ? null : creator.getId())
                    .integer(modifier == null ? null : modifier.getId())
                    .integer(report.getTopic().get().getId())
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
        // TODO Auto-generated method stub

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
