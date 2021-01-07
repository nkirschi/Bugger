package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Report gateway that gives access to reports stored in a database.
 */
public class ReportDBGateway implements ReportGateway {

    private static final Log log = Log.forClass(ReportDBGateway.class);

    private Connection conn;

    /**
     * Constructs a new report gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public ReportDBGateway(Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfPosts(Report report) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Report getReportByID(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getSelectedReports(Topic topic, Selection selection, boolean showOpenReports,
                                           boolean showClosedReports) {
        log.info("searching for Reports");
        List<Report> selectedReports = new ArrayList<>(Math.max(0, selection.getTotalSize()));
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"reports\" WHERE topic = ?")) {
            ResultSet rs = new StatementParametrizer(stmt).integer(topic.getId()).toStatement().executeQuery();

            while (rs.next()) {
                log.info("found a Report!");
                selectedReports.add(getReportFromResultSet(rs));
            }
        } catch (SQLException e) {
            log.error("Error while searching for reports in topic with id " + topic.getId(), e);
            throw new StoreException("Error while searching reports in topic with id " + topic.getId(), e);
        }
        return selectedReports;
    }

    private Report getReportFromResultSet(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setId(rs.getInt("id"));
        report.setTitle(rs.getString("title"));
        report.setType(Report.Type.valueOf(rs.getString("type")));
        report.setSeverity(Report.Severity.valueOf(rs.getString("severity")));
        report.setVersion(rs.getString("version"));
        report.setForcedRelevance(rs.getInt("forcedrelevance"));
        report.setTopic(rs.getInt("topic"));
        report.setDuplicateOf(rs.getInt("duplicateof"));
        if (rs.getTimestamp("closingDate") != null) {
            report.setClosingDate(rs.getTimestamp("closingDate").toInstant().atZone(ZoneId.systemDefault()));
        }
        return report;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createReport(Report report) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateReport(Report report) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteReport(Report report) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeReport(Report report) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void openReport(Report report) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveReport(Report report, Topic destination) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markDuplicate(Report duplicate, int originalID) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unmarkDuplicate(Report report) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void overwriteRelevance(Report report, Optional<Integer> relevance) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upvote(Report report, User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void downvote(Report report, User user) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeVote(Report report, User user) {
        // TODO Auto-generated method stub

    }

}
