package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import java.sql.Connection;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Statistics gateway that searches application data stored in a database.
 */
public class StatisticsDBGateway implements StatisticsGateway {

    private static final Log log = Log.forClass(StatisticsDBGateway.class);

    private Connection conn;

    /**
     * Constructs a new statistics gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public StatisticsDBGateway(Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getTopTenUsers(ZonedDateTime latestOpening, ZonedDateTime earliestClosing) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getTopTenReports() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Duration getAverageTimeToClose(Topic topic, ZonedDateTime latestOpening, ZonedDateTime earliestClosing) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfOpenReports(Topic topic, ZonedDateTime latestOpening, ZonedDateTime earliestClosing) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAveragePostsPerReport(Topic topic, ZonedDateTime latestOpening, ZonedDateTime earliestClosing) {
        // TODO Auto-generated method stub
        return 0;
    }

}
