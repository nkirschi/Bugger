package tech.bugger.persistence.gateway;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import tech.bugger.global.transfer.ReportCriteria;
import tech.bugger.global.transfer.TopReport;
import tech.bugger.global.transfer.TopUser;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

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
    private final Connection conn;

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
        if (criteria == null) {
            throw new IllegalArgumentException("Report criteria must not be null!");
        }

        // @formatter:off
        String query =
                "SELECT COUNT(*) "
              + "FROM   report AS r "
              + "JOIN   topic AS t "
              + "ON     r.topic = t.id "
              + "WHERE  r.closed_at IS NULL "
              + "AND    t.title = COALESCE(NULLIF(?, ''), t.title) "
              + "AND    r.created_at <= COALESCE(?, r.created_at) "
              + "AND    (r.closed_at >= COALESCE(?, r.closed_at) OR r.closed_at IS NULL);";
        // @formatter:on
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string(criteria.getTopic())
                    .object(criteria.getLatestOpeningDate())
                    .object(criteria.getEarliestClosingDate())
                    .toStatement().executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                log.error("Empty result for open reports with criteria " + criteria + ".");
                throw new InternalError("Empty result for open reports with criteria " + criteria + ".");
            }
        } catch (SQLException e) {
            log.error("Error while counting open reports with criteria " + criteria + ".", e);
            throw new StoreException("Error while counting open reports with criteria " + criteria + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Duration getAverageTimeToClose(final ReportCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Report criteria must not be null!");
        }

        // @formatter:off
        String query =
                "SELECT EXTRACT (epoch FROM AVG(r.closed_at - r.created_at)) " // duration in seconds
              + "FROM   report AS r "
              + "JOIN   topic AS t "
              + "ON     r.topic = t.id "
              + "WHERE  t.title = COALESCE(NULLIF(?, ''), t.title) "
              + "AND    r.created_at <= COALESCE(?, r.created_at) "
              + "AND    (r.closed_at >= COALESCE(?, r.closed_at) OR r.closed_at IS NULL);";
        // @formatter:on
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string(criteria.getTopic())
                    .object(criteria.getLatestOpeningDate())
                    .object(criteria.getEarliestClosingDate())
                    .toStatement().executeQuery();
            if (rs.next()) {
                long seconds = rs.getLong(1);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return Duration.ofSeconds(seconds);
                }
            } else {
                log.error("Empty result for average time to close with criteria " + criteria + ".");
                throw new InternalError("Empty result for average time to close with criteria " + criteria + ".");
            }
        } catch (SQLException e) {
            log.error("Unable to determine closing time with criteria " + criteria + ".", e);
            throw new StoreException("Unable to determine closing time with criteria " + criteria + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getAveragePostsPerReport(final ReportCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Report criteria must not be null!");
        }

        // @formatter:off
        String query =
                "SELECT AVG(c) "
              + "FROM ( "
              + "    SELECT          COUNT(p.id) AS c "
              + "    FROM            report AS r "
              + "    LEFT OUTER JOIN post AS p "
              + "    ON              p.report = r.id "
              + "    JOIN            topic AS t "
              + "    ON              r.topic = t.id "
              + "    WHERE           t.title = COALESCE(NULLIF(?, ''), t.title) "
              + "    AND             r.created_at <= COALESCE(?, r.created_at) "
              + "    AND             (r.closed_at >= COALESCE(?, r.closed_at) OR r.closed_at IS NULL)"
              + "    GROUP BY        r.id "
              + ") AS x;";
        // @formatter:on
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string(criteria.getTopic())
                    .object(criteria.getLatestOpeningDate())
                    .object(criteria.getEarliestClosingDate())
                    .toStatement().executeQuery();
            if (rs.next()) {
                return rs.getObject(1, BigDecimal.class);
            } else {
                log.error("Empty result for average posts per report with criteria " + criteria + ".");
                throw new InternalError("Empty result for average posts per report with criteria " + criteria + ".");
            }
        } catch (SQLException e) {
            log.error("Unable to determine average posts per report with criteria " + criteria + ".", e);
            throw new StoreException("Unable to determine average posts per report with criteria " + criteria + ".", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TopReport> getTopReports(final int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Top reports limit cannot be negative!");
        }

        List<TopReport> topReports = new ArrayList<>();
        // @formatter:off
        String query =
                "SELECT   * "
              + "FROM     top_reports AS t "
              + "JOIN     report AS r "
              + "ON       t.report = r.id "
              + "JOIN     \"user\" AS u "
              + "ON       r.created_by = u.id "
              + "ORDER BY t.relevance_gain DESC "
              + "LIMIT    ?;";
        // @formatter:on
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = new StatementParametrizer(stmt).integer(limit).toStatement().executeQuery();
            while (rs.next()) {
                topReports.add(new TopReport(
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
        return topReports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TopUser> getTopUsers(final int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Top users limit cannot be negative!");
        }

        List<TopUser> topUsers = new ArrayList<>();
        // @formatter:off
        String query =
                "SELECT   * "
              + "FROM     top_users AS t "
              + "JOIN     \"user\" AS u "
              + "ON       t.user = u.id "
              + "ORDER BY t.earned_relevance DESC "
              + "LIMIT    ?;";
        // @formatter:on
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = new StatementParametrizer(stmt).integer(limit).toStatement().executeQuery();
            while (rs.next()) {
                topUsers.add(new TopUser(rs.getString("username"), rs.getInt("earned_relevance")));
            }
        } catch (SQLException e) {
            log.error("Unable to determine top ten users.", e);
            throw new StoreException("Unable to determine top ten users.", e);
        }
        return topUsers;
    }

}
