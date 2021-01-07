package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import java.sql.*;
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
        if (report == null) {
            log.error("Cannot count posts of report null.");
            throw new IllegalArgumentException("Report cannot be null.");
        } else if (report.getId() == null) {
            log.error("Cannot count posts of report with ID null.");
            throw new IllegalArgumentException("Report ID must not be null.");
        }

        int count = 0;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM post WHERE report = "
                + report.getId())) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
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
    public void deleteReport(final Report report) throws NotFoundException {
        if (report == null) {
            log.error("Cannot delete report null.");
            throw new IllegalArgumentException("Report cannot be null.");
        } else if (report.getId() == null) {
            log.error("Cannot delete report with ID null");
            throw new IllegalArgumentException("Report ID cannot be null.");
        }

        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM report * WHERE id = " + report.getId()
                + " RETURNING *")) {
            ResultSet rs = stmt.executeQuery();
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

        String sql = "UPDATE report SET closed_at = '" + Timestamp.from(report.getClosingDate().toInstant())
                + "' WHERE id = " + report.getId() + ";";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int affectedRows = stmt.executeUpdate();
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
