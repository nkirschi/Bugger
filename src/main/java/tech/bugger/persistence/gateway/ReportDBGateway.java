package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import java.sql.Connection;
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
    public int countPosts(final Report report) {
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
        // TODO Auto-generated method stub
        return null;
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
    public void deleteReport(final Report report) {
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
