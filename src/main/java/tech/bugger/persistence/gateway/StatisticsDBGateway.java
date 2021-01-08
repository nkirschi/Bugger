package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.ReportCriteria;
import tech.bugger.global.transfer.TopReport;
import tech.bugger.global.transfer.TopUser;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.ConnectionPool;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Statistics gateway that searches application data stored in a database.
 */
public class StatisticsDBGateway implements StatisticsGateway {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(StatisticsDBGateway.class);

    /**
     * Database connection used by this gateway.
     */
    private Connection conn;

    /**
     * Constructs a new statistics gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public StatisticsDBGateway(final Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfOpenReports(final ReportCriteria criteria) {
        // @formatter:off
        String query =
                "SELECT COUNT(*) "
              + "FROM   report AS r "
              + "JOIN   topic AS t "
              + "ON     r.topic = t.id "
              + "WHERE  r.closed_at IS NULL "
              + (!criteria.getTopic().equals("")
              ? "AND    t.title = ? " : "")
              + (criteria.getLatestOpeningDate() != null
              ? "AND    r.created_at <= ? " : "")
              + (criteria.getEarliestClosingDate() != null
              ? "AND   (r.closed_at >= ? OR r.closed_at IS NULL);" : "");
        // @formatter:on
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            StatementParametrizer parametrizer = new StatementParametrizer(stmt);
            if (!criteria.getTopic().equals("")) {
                parametrizer.string(criteria.getTopic());
            }
            if (criteria.getLatestOpeningDate() != null) {
                parametrizer.object(criteria.getLatestOpeningDate());
            }
            if (criteria.getEarliestClosingDate() != null) {
                parametrizer.object(criteria.getEarliestClosingDate());
            }
            ResultSet rs = parametrizer.toStatement().executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Unable to count open reports.", e);
            throw new StoreException("Unable to count open reports.", e);
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Duration getAverageTimeToClose(final ReportCriteria criteria) {
        // @formatter:off
        String query =
                "SELECT EXTRACT (epoch FROM AVG(r.closed_at - r.created_at)) " // duration in seconds
              + "FROM   report AS r "
              + "JOIN   topic AS t "
              + "ON     r.topic = t.id "
              + "WHERE  NULL IS NULL "
              + (!criteria.getTopic().equals("")
              ? "AND    t.title = ? " : "")
              + (criteria.getLatestOpeningDate() != null
              ? "AND    r.created_at <= ? " : "")
              + (criteria.getEarliestClosingDate() != null
              ? "AND   (r.closed_at >= ? OR r.closed_at IS NULL);" : "");
        // @formatter:on
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            StatementParametrizer parametrizer = new StatementParametrizer(stmt);
            if (!criteria.getTopic().equals("")) {
                parametrizer.string(criteria.getTopic());
            }
            if (criteria.getLatestOpeningDate() != null) {
                parametrizer.object(criteria.getLatestOpeningDate());
            }
            if (criteria.getEarliestClosingDate() != null) {
                parametrizer.object(criteria.getEarliestClosingDate());
            }
            ResultSet rs = parametrizer.toStatement().executeQuery();
            if (rs.next()) {
                return Duration.ofSeconds(rs.getLong(1));
            }
        } catch (SQLException e) {
            log.error("Unable to determine closing time with criteria " + criteria + ".", e);
            throw new StoreException("Unable to determine closing time with criteria " + criteria + ".", e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getAveragePostsPerReport(final ReportCriteria criteria) {
        // @formatter:off
        String query =
                "SELECT AVG(c) " // duration in seconds
              + "FROM ( "
              + "    SELECT   COUNT(*) AS c "
              + "    FROM     report AS r "
              + "    JOIN     post AS p "
              + "    ON       p.report = r.id "
              + "    JOIN     topic AS t "
              + "    ON       r.topic = t.id "
              + "    WHERE    NULL IS NULL "
              + (!criteria.getTopic().equals("")
              ? "    AND    t.title = ? " : "")
              + (criteria.getLatestOpeningDate() != null
              ? "    AND    r.created_at <= ? " : "")
              + (criteria.getEarliestClosingDate() != null
              ? "    AND   (r.closed_at >= ? OR r.closed_at IS NULL)" : "")
              + "    GROUP BY r.id "
              + ") AS x;";
        // @formatter:on
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            StatementParametrizer parametrizer = new StatementParametrizer(stmt);
            if (!criteria.getTopic().equals("")) {
                parametrizer.string(criteria.getTopic());
            }
            if (criteria.getLatestOpeningDate() != null) {
                parametrizer.object(criteria.getLatestOpeningDate());
            }
            if (criteria.getEarliestClosingDate() != null) {
                parametrizer.object(criteria.getEarliestClosingDate());
            }
            ResultSet rs = parametrizer.toStatement().executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            log.error("Unable to determine average posts per report with criteria " + criteria + ".", e);
            throw new StoreException("Unable to determine average posts per report with criteria " + criteria + ".", e);
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TopReport> getTopTenReports() {
        List<TopReport> topTenReports = new ArrayList<>();
        String query =
                "SELECT * "
                        + "FROM   top_reports AS t "
                        + "JOIN   report AS r "
                        + "ON     t.report = r.id "
                        + "JOIN   \"user\" AS u "
                        + "ON     r.created_by = u.id "
                        + "LIMIT  10;";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                topTenReports.add(new TopReport(
                        rs.getInt("report"),
                        rs.getString("title"),
                        rs.getString("username"),
                        rs.getInt("relevance_gain"))
                );
            }
        } catch (SQLException e) {
            log.error("Unable to determine top ten reports.", e);
            throw new StoreException("Unable to determine top ten reports.", e);
        }
        return topTenReports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TopUser> getTopTenUsers() {
        List<TopUser> topTenUsers = new ArrayList<>();
        String query =
                "SELECT * "
                        + "FROM   top_users AS t "
                        + "JOIN   \"user\" AS u "
                        + "ON     t.user = u.id "
                        + "LIMIT  10;";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                topTenUsers.add(new TopUser(rs.getString("username"), rs.getInt("earned_relevance")));
            }
        } catch (SQLException e) {
            log.error("Unable to determine top ten users.", e);
            throw new StoreException("Unable to determine top ten users.", e);
        }
        return topTenUsers;
    }

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        prop.put("user", "sep20g02");
        prop.put("password", "Kahquoo5noje");
        prop.put("ssl", "true");
        prop.put("sslfactory", "org.postgresql.ssl.DefaultJavaSSLFactory");
        ConnectionPool pool = new ConnectionPool("org.postgresql.Driver", "jdbc:postgresql://bueno.fim.uni-passau"
                + ".de:5432/sep20g02t", prop, 1, 1, 5000);
        Connection conn = pool.getConnection();

        System.out.println(new StatisticsDBGateway(conn).getTopTenReports());
        System.out.println(new StatisticsDBGateway(conn).getTopTenUsers());
        ReportCriteria criteria = new ReportCriteria("testtopic", OffsetDateTime.now(), OffsetDateTime.MAX);
        System.out.println(new StatisticsDBGateway(conn).getAverageTimeToClose(criteria).toSeconds());
        System.out.println(new StatisticsDBGateway(conn).getNumberOfOpenReports(criteria));
        System.out.println(new StatisticsDBGateway(conn).getAveragePostsPerReport(criteria));
        pool.releaseConnection(conn);
    }

}
