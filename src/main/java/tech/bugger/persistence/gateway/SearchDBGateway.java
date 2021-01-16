package tech.bugger.persistence.gateway;

import com.ocpsoft.pretty.faces.util.StringUtils;
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Search gateway that retrieves search results from data stored in a database.
 */
public class SearchDBGateway implements SearchGateway {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(SearchDBGateway.class);

    /**
     * The database connection used by this gateway.
     */
    private final Connection conn;

    /**
     * Constructs a new search gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public SearchDBGateway(final Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getUserResults(final String query, final Selection selection, final boolean showAdmins,
                                     final boolean showNonAdmins) {
        if (selection == null || query == null) {
            log.error("The selection or topic ID cannot be null!");
            throw new IllegalArgumentException("The selection or topic ID cannot be null!");
        } else if (StringUtils.isBlank(selection.getSortedBy())) {
            log.error("Error when trying to get moderators sorted by nothing.");
            throw new IllegalArgumentException("The selection needs to have a column to sort by.");
        }

        List<User> userResults = new ArrayList<>(Math.max(0, selection.getTotalSize()));
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"user\" WHERE username LIKE ? "
                + "AND is_admin = ? ORDER BY username LIMIT ? OFFSET ?;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string("'%" + query + "%'")
                    .bool(showAdmins)
                    .integer(selection.getPageSize().getSize())
                    .integer(selection.getCurrentPage() * selection.getPageSize().getSize())
                    .toStatement().executeQuery();

            while (rs.next()) {
                UserDBGateway.getUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            log.error("Error while loading the user search suggestions for the query " + query, e);
            throw new StoreException("Error while loading the user search suggestions for the query " + query, e);
        }

        return userResults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getUserBanSuggestions(final String query, final int limit, final Topic topic) {
        validateSuggestionParams(query, limit, topic);
        List<String> userResults = new ArrayList<>(limit);
        String newQuery = query
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("[", "![");

        try (PreparedStatement stmt = conn.prepareStatement("SELECT u.username FROM \"user\" AS u WHERE u.username "
                + "LIKE ? AND u.is_admin = false AND u.id NOT IN (SELECT t.outcast FROM topic_ban AS t WHERE t.topic "
                + "= ?) AND u.id NOT IN (SELECT m.moderator FROM topic_moderation AS m WHERE m.topic = ?) LIMIT ?;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string("%" + newQuery + "%")
                    .integer(topic.getId())
                    .integer(topic.getId())
                    .integer(limit)
                    .toStatement().executeQuery();

            while (rs.next()) {
                userResults.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            log.error("Error while loading the user search suggestions for the query " + query, e);
            throw new StoreException("Error while loading the user search suggestions for the query " + query, e);
        }

        return userResults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getUserUnbanSuggestions(final String query, final int limit, final Topic topic) {
        validateSuggestionParams(query, limit, topic);
        List<String> userResults = new ArrayList<>(limit);
        String newQuery = query
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("[", "![");

        try (PreparedStatement stmt = conn.prepareStatement("SELECT u.username FROM \"user\" AS u INNER JOIN "
                + "topic_ban as t ON t.outcast = u.id WHERE u.username LIKE ? AND t.topic = ? LIMIT ?;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string("%" + newQuery + "%")
                    .integer(topic.getId())
                    .integer(limit)
                    .toStatement().executeQuery();

            while (rs.next()) {
                userResults.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            log.error("Error while loading the user search suggestions for the query " + query, e);
            throw new StoreException("Error while loading the user search suggestions for the query " + query, e);
        }

        return userResults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getUserModSuggestions(final String query, final int limit, final Topic topic) {
        validateSuggestionParams(query, limit, topic);
        List<String> modResults = new ArrayList<>(limit);
        String newQuery = query
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("[", "![");

        try (PreparedStatement stmt = conn.prepareStatement("SELECT u.username FROM \"user\" AS u WHERE u.username "
                + "LIKE ? AND u.is_admin = false AND u.id NOT IN (SELECT t.moderator FROM topic_moderation AS t WHERE "
                + "t.topic = ?) LIMIT ?;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string("%" + newQuery + "%")
                    .integer(topic.getId())
                    .integer(limit)
                    .toStatement().executeQuery();

            while (rs.next()) {
                modResults.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            log.error("Error while loading the user search suggestions for the query " + query, e);
            throw new StoreException("Error while loading the user search suggestions for the query " + query, e);
        }
        return modResults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getUserUnmodSuggestions(final String query, final int limit, final Topic topic) {
        validateSuggestionParams(query, limit, topic);
        List<String> unmodResults = new ArrayList<>(limit);
        String newQuery = query
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("[", "![");

        try (PreparedStatement stmt = conn.prepareStatement("SELECT u.username FROM \"user\" AS u INNER JOIN "
                + "topic_moderation as t ON t.moderator = u.id WHERE u.username LIKE ? AND t.topic = ? LIMIT ?;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string("%" + newQuery + "%")
                    .integer(topic.getId())
                    .integer(limit)
                    .toStatement().executeQuery();

            while (rs.next()) {
                unmodResults.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            log.error("Error while loading the user search suggestions for the query " + query, e);
            throw new StoreException("Error while loading the user search suggestions for the query " + query, e);
        }

        return unmodResults;
    }

    /**
     * Checks if the given parameters violate restrictions.
     *
     * @param query The query string to search for.
     * @param limit The maximum amount of results to return.
     * @param topic The topic to check.
     * @throws IllegalArgumentException if the given parameters are invalid.
     */
    private void validateSuggestionParams(final String query, final int limit, final Topic topic) {
        if (query == null) {
            log.error("The search query cannot be null!");
            throw new IllegalArgumentException("The search query cannot be null!");
        } else if (query.isBlank()) {
            log.error("The search query cannot be blank!");
            throw new IllegalArgumentException("The search query cannot be blank!");
        } else if (limit < 0) {
            log.error("The limit of search suggestions to return cannot be negative!");
            throw new IllegalArgumentException("The limit of search suggestions to return cannot be negative!");
        } else if (topic.getId() == null) {
            log.error("The topic cannot be null!");
            throw new IllegalArgumentException("The topic cannot be null!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Topic> getTopicResults(final String query, final Selection selection) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getReportResults(final String query, final Selection selection,
                                         final ZonedDateTime latestOpeningDateTime,
                                         final ZonedDateTime earliestClosingDateTime, final boolean showOpenReports,
                                         final boolean showClosedReports, final boolean showDuplicates,
                                         final Topic topic, final HashMap<Report.Type, Boolean> reportTypeFilter,
                                         final HashMap<Report.Severity, Boolean> severityFilter) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getFulltextResults(final String query, final Selection selection,
                                           final ZonedDateTime latestOpeningDateTime,
                                           final ZonedDateTime earliestClosingDateTime, final boolean showOpenReports,
                                           final boolean showClosedReports, final boolean showDuplicates,
                                           final Topic topic, final HashMap<Report.Type, Boolean> reportTypeFilter,
                                           final HashMap<Report.Severity, Boolean> severityFilter) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfUserResults(final String query, final boolean showAdmins, final boolean showNonAdmins) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfTopicResults(final String query) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfReportResults(final String query, final ZonedDateTime latestOpeningDateTime,
                                        final ZonedDateTime earliestClosingDateTime, final boolean showOpenReports,
                                        final boolean showClosedReports, final boolean showDuplicates,
                                        final Topic topic, final HashMap<Report.Type, Boolean> reportTypeFilter,
                                        final HashMap<Report.Severity, Boolean> severityFilter) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfFulltextResults(final String query, final ZonedDateTime latestOpeningDateTime,
                                          final ZonedDateTime earliestClosingDateTime, final boolean showOpenReports,
                                          final boolean showClosedReports, final boolean showDuplicates,
                                          final Topic topic, final HashMap<Report.Type, Boolean> reportTypeFilter,
                                          final HashMap<Report.Severity, Boolean> severityFilter) {
        // TODO Auto-generated method stub
        return 0;
    }

}
