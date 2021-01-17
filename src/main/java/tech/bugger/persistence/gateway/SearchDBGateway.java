package tech.bugger.persistence.gateway;

import com.ocpsoft.pretty.faces.util.StringUtils;
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
     * User gateway used for finding users.
     */
    private final UserGateway userGateway;

    /**
     * Constructs a new search gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public SearchDBGateway(final Connection conn, final UserGateway userGateway) {
        this.conn = conn;
        this.userGateway = userGateway;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getUserResults(final String query, final Selection selection, final boolean showAdmins,
                                     final boolean showNonAdmins) {
        if (selection == null || query == null) {
            log.error("The selection or query cannot be null!");
            throw new IllegalArgumentException("The selection or query cannot be null!");
        } else if (StringUtils.isBlank(selection.getSortedBy())) {
            log.error("Error when trying to get users sorted by nothing.");
            throw new IllegalArgumentException("The selection needs to have a column to sort by.");
        }
        List<User> userResults = new ArrayList<>(Math.max(0, selection.getTotalSize()));
        if (!showAdmins && !showNonAdmins) {
            return userResults;
        }
        String adminFilter = "";
        if (showAdmins) {
            adminFilter = "AND is_admin = true";
        }
        if (showNonAdmins) {
            if (showAdmins) {
                adminFilter = "";
            } else {
                adminFilter = "AND is_admin = false";
            }
        }
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"user\" WHERE username LIKE ? "
                + adminFilter
                + "ORDER BY " + selection.getSortedBy() + (selection.isAscending() ? " ASC " : " DESC ")
                + "LIMIT ? OFFSET ?;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string(query + "%")
                    .integer(selection.getPageSize().getSize())
                    .integer(selection.getCurrentPage() * selection.getPageSize().getSize())
                    .toStatement().executeQuery();

            while (rs.next()) {
                userResults.add(UserDBGateway.getUserFromResultSet(rs));
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
                + "= ?) LIMIT ?;")) {
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
     * {@inheritDoc}
     */
    @Override
    public List<String> getUserSuggestions(final String query, final int limit) {
        validateSuggestionParams(query, limit);
        List<String> userResults = new ArrayList<>(limit);
        String newQuery = query
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("[", "![");

        try (PreparedStatement stmt = conn.prepareStatement("SELECT u.username FROM \"user\" AS u WHERE u.username "
                + "LIKE ? ;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string("%" + newQuery + "%")
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
    public List<String> getTopicSuggestions(final String query, final int limit) {
        validateSuggestionParams(query, limit);
        List<String> topicResults = new ArrayList<>(limit);
        String newQuery = query
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("[", "![");

        try (PreparedStatement stmt = conn.prepareStatement("SELECT t.title FROM \"topic\" AS t WHERE t.title "
                + "LIKE ? ;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string("%" + newQuery + "%")
                    .toStatement().executeQuery();

            while (rs.next()) {
                topicResults.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            log.error("Error while loading the topic search suggestions for the query " + query, e);
            throw new StoreException("Error while loading the topic search suggestions for the query " + query, e);
        }

        return topicResults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getReportSuggestions(final String query, final int limit) {
        validateSuggestionParams(query, limit);
        List<String> reportResults = new ArrayList<>(limit);
        String newQuery = query
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("[", "![");

        try (PreparedStatement stmt = conn.prepareStatement("SELECT t.title FROM \"report\" AS t WHERE t.title "
                + "LIKE ? ;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string("%" + newQuery + "%")
                    .toStatement().executeQuery();

            while (rs.next()) {
                reportResults.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            log.error("Error while loading the report search suggestions for the query " + query, e);
            throw new StoreException("Error while report the topic search suggestions for the query " + query, e);
        }

        return reportResults;
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
        if (query == null || query.isBlank() || topic.getId() == null || limit < 0) {
            log.error("The topic cannot be null and the query cannot be null or blank!");
            throw new IllegalArgumentException("The topic cannot be null and the query cannot be null or blank!");
        }
    }

    /**
     * Checks if the given parameters violate restrictions.
     *
     * @param query The query string to search for.
     * @param limit The maximum amount of results to return.
     * @throws IllegalArgumentException if the given parameters are invalid.
     */
    private void validateSuggestionParams(final String query, final int limit) {
        if (query == null || query.isBlank() || limit < 0) {
            log.error("The query cannot be null or blank!");
            throw new IllegalArgumentException("The query cannot be null or blank!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Topic> getTopicResults(final String query, final Selection selection) {
        if (selection == null || query == null) {
            log.error("The selection or query cannot be null!");
            throw new IllegalArgumentException("The selection or query cannot be null!");
        } else if (StringUtils.isBlank(selection.getSortedBy())) {
            log.error("Error when trying to get topics sorted by nothing.");
            throw new IllegalArgumentException("The selection needs to have a column to sort by.");
        }

        List<Topic> topicResults = new ArrayList<>(Math.max(0, selection.getTotalSize()));
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"topic\" WHERE title LIKE ?"
                + "ORDER BY " + selection.getSortedBy() + (selection.isAscending() ? " ASC " : " DESC ")
                + "LIMIT ? OFFSET ?;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string(query + "%")
                    .integer(selection.getPageSize().getSize())
                    .integer(selection.getCurrentPage() * selection.getPageSize().getSize())
                    .toStatement().executeQuery();
            while (rs.next()) {
                topicResults.add(TopicDBGateway.getTopicFromResultSet(rs));
            }
        } catch (SQLException e) {
            log.error("Error while loading the topic search suggestions for the query " + query, e);
            throw new StoreException("Error while loading the topic search suggestions for the query " + query, e);
        }

        return topicResults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getReportResults(final String query, final Selection selection,
                                         final ZonedDateTime latestOpeningDateTime,
                                         final ZonedDateTime earliestClosingDateTime, final boolean showOpenReports,
                                         final boolean showClosedReports, final boolean showDuplicates,
                                         final String topic, final HashMap<Report.Type, Boolean> reportTypeFilter,
                                         final HashMap<Report.Severity, Boolean> severityFilter) throws NotFoundException {
        if (selection == null || query == null || severityFilter == null || reportTypeFilter == null) {
            log.error("The selection or query cannot be null!");
            throw new IllegalArgumentException("The selection or query cannot be null!");
        } else if (StringUtils.isBlank(selection.getSortedBy())) {
            log.error("Error when trying to get reports sorted by nothing.");
            throw new IllegalArgumentException("The selection needs to have a column to sort by.");
        }

        List<Report> reportResults = new ArrayList<>(Math.max(0, selection.getTotalSize()));

        if (!reportTypeFilter.get(Report.Type.BUG) && !reportTypeFilter.get(Report.Type.HINT)
                && !reportTypeFilter.get(Report.Type.FEATURE)) {
            return reportResults;
        }
        if (!severityFilter.get(Report.Severity.RELEVANT) && !severityFilter.get(Report.Severity.MINOR)
                && !severityFilter.get(Report.Severity.SEVERE)) {
            return reportResults;
        }
        if (!showClosedReports && !showOpenReports) {
            return reportResults;
        }

        String filterClosedAt = "";
        if (!showClosedReports) {
            filterClosedAt = "AND closed_at IS NULL ";
        } else if (!showOpenReports) {
            filterClosedAt = "AND closed_at IS NOT NULL ";
        }
        String filterDuplicate = "";
        if (!showDuplicates) {
            filterDuplicate = "AND duplicate_of IS NULL ";
        }
        StringBuilder filterTypeBuilder = new StringBuilder();
        filterTypeBuilder.append("AND (");
        boolean filterTypeAdded = false;
        if (reportTypeFilter.get(Report.Type.BUG)) {
            filterTypeBuilder.append("type = 'BUG' ");
            filterTypeAdded = true;
        }
        if (reportTypeFilter.get(Report.Type.FEATURE)) {
            if (filterTypeAdded) {
                filterTypeBuilder.append("OR ");
            }
            filterTypeBuilder.append("type = 'FEATURE' ");
            filterTypeAdded = true;
        }
        if (reportTypeFilter.get(Report.Type.HINT)) {
            if (filterTypeAdded) {
                filterTypeBuilder.append("OR ");
            }
            filterTypeBuilder.append("type = 'HINT' ");
        }
        filterTypeBuilder.append(") ");
        String filterType = filterTypeBuilder.toString();
        StringBuilder filterSeverityBuilder = new StringBuilder();
        filterSeverityBuilder.append("AND (");
        boolean filterSeverityAdded = false;
        if (severityFilter.get(Report.Severity.MINOR)) {
            filterSeverityBuilder.append("severity = 'MINOR' ");
            filterSeverityAdded = true;
        }
        if (severityFilter.get(Report.Severity.RELEVANT)) {
            if (filterSeverityAdded) {
                filterSeverityBuilder.append("OR ");
            }
            filterSeverityBuilder.append("severity = 'RELEVANT' ");
            filterSeverityAdded = true;
        }
        if (severityFilter.get(Report.Severity.SEVERE)) {
            if (filterSeverityAdded) {
                filterSeverityBuilder.append("OR ");
            }
            filterSeverityBuilder.append("severity = 'SEVERE' ");
        }
        filterSeverityBuilder.append(") ");
        String filterSeverity = filterSeverityBuilder.toString();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"report\" AS r JOIN topic AS t "
                + "ON r.topic = t.id WHERE r.title LIKE ?"
                + "AND r.created_at <= COALESCE(?, r.created_at) "
                + "AND (r.closed_at >= COALESCE(?, r.closed_at) OR r.closed_at IS NULL) "
                + filterClosedAt + filterDuplicate + filterType + filterSeverity
                + "AND t.title = COALESCE(?, t.title) "
                + "ORDER BY " + selection.getSortedBy() + (selection.isAscending() ? " ASC " : " DESC ")
                + "LIMIT ? OFFSET ?;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string(query + "%")
                    .object(latestOpeningDateTime)
                    .object(earliestClosingDateTime)
                    .string(topic)
                    .integer(selection.getPageSize().getSize())
                    .integer(selection.getCurrentPage() * selection.getPageSize().getSize())
                    .toStatement().executeQuery();
            while (rs.next()) {
                reportResults.add(ReportDBGateway.getReportFromResultSet(rs, userGateway));
            }
        } catch (SQLException e) {
            log.error("Error while loading the topic search suggestions for the query " + query, e);
            throw new StoreException("Error while loading the topic search suggestions for the query " + query, e);
        }

        return reportResults;
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
        if (query == null) {
            log.error("The selection or query cannot be null!");
            throw new IllegalArgumentException("The selection or query cannot be null!");
        }

        int users = 0;

        if (!showAdmins && !showNonAdmins) {
            return 0;
        }
        String adminFilter = "";
        if (showAdmins) {
            adminFilter = "AND is_admin = true";
        }
        if (showNonAdmins) {
            if (showAdmins) {
                adminFilter = "";
            } else {
                adminFilter = "AND is_admin = false";
            }
        }
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS num_users FROM \"user\" WHERE username LIKE ? "
                + adminFilter)) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string(query + "%")
                    .toStatement().executeQuery();

            while (rs.next()) {
                users = rs.getInt("num_users");
            }
        } catch (SQLException e) {
            log.error("Error while loading the user search suggestions for the query " + query, e);
            throw new StoreException("Error while loading the user search suggestions for the query " + query, e);
        }

        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfTopicResults(final String query) {
        if (query == null) {
            log.error("The selection or query cannot be null!");
            throw new IllegalArgumentException("The selection or query cannot be null!");
        }

        int topics = 0;

        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS num_topics FROM \"topic\" WHERE title LIKE ?;")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string(query + "%")
                    .toStatement().executeQuery();
            while (rs.next()) {
                topics = rs.getInt("num_topics");
            }
        } catch (SQLException e) {
            log.error("Error while loading the topic search suggestions for the query " + query, e);
            throw new StoreException("Error while loading the topic search suggestions for the query " + query, e);
        }

        return topics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfReportResults(final String query, final ZonedDateTime latestOpeningDateTime,
                                        final ZonedDateTime earliestClosingDateTime, final boolean showOpenReports,
                                        final boolean showClosedReports, final boolean showDuplicates,
                                        final String topic, final HashMap<Report.Type, Boolean> reportTypeFilter,
                                        final HashMap<Report.Severity, Boolean> severityFilter) {
        if (query == null || severityFilter == null || reportTypeFilter == null) {
            log.error("The query cannot be null!");
            throw new IllegalArgumentException("The query cannot be null!");
        }

        if (!reportTypeFilter.get(Report.Type.BUG) && !reportTypeFilter.get(Report.Type.HINT)
                && !reportTypeFilter.get(Report.Type.FEATURE)) {
            return 0;
        }
        if (!severityFilter.get(Report.Severity.RELEVANT) && !severityFilter.get(Report.Severity.MINOR)
                && !severityFilter.get(Report.Severity.SEVERE)) {
            return 0;
        }
        if (!showClosedReports && !showOpenReports) {
            return 0;
        }

        int reports = 0;

        String filterClosedAt = "";
        if (!showClosedReports) {
            filterClosedAt = "AND closed_at IS NULL ";
        } else if (!showOpenReports) {
            filterClosedAt = "AND closed_at IS NOT NULL ";
        }
        String filterDuplicate = "";
        if (!showDuplicates) {
            filterDuplicate = "AND duplicate_of IS NULL ";
        }
        StringBuilder filterTypeBuilder = new StringBuilder();
        filterTypeBuilder.append("AND (");
        boolean filterTypeAdded = false;
        if (reportTypeFilter.get(Report.Type.BUG)) {
            filterTypeBuilder.append("r.type = 'BUG' ");
            filterTypeAdded = true;
        }
        if (reportTypeFilter.get(Report.Type.FEATURE)) {
            if (filterTypeAdded) {
                filterTypeBuilder.append("OR ");
            }
            filterTypeBuilder.append("r.type = 'FEATURE' ");
            filterTypeAdded = true;
        }
        if (reportTypeFilter.get(Report.Type.HINT)) {
            if (filterTypeAdded) {
                filterTypeBuilder.append("OR ");
            }
            filterTypeBuilder.append("r.type = 'HINT' ");
        }
        filterTypeBuilder.append(") ");
        String filterType = filterTypeBuilder.toString();
        StringBuilder filterSeverityBuilder = new StringBuilder();
        filterSeverityBuilder.append("AND (");
        boolean filterSeverityAdded = false;
        if (severityFilter.get(Report.Severity.MINOR)) {
            filterSeverityBuilder.append("r.severity = 'MINOR' ");
            filterSeverityAdded = true;
        }
        if (severityFilter.get(Report.Severity.RELEVANT)) {
            if (filterSeverityAdded) {
                filterSeverityBuilder.append("OR ");
            }
            filterSeverityBuilder.append("r.severity = 'RELEVANT' ");
            filterSeverityAdded = true;
        }
        if (severityFilter.get(Report.Severity.SEVERE)) {
            if (filterSeverityAdded) {
                filterSeverityBuilder.append("OR ");
            }
            filterSeverityBuilder.append("r.severity = 'SEVERE' ");
        }
        filterSeverityBuilder.append(") ");
        String filterSeverity = filterSeverityBuilder.toString();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT Count(*) AS num_reports FROM \"report\" AS r JOIN topic AS t "
                + "ON r.topic = t.id WHERE r.title LIKE ? "
                + "AND r.created_at <= COALESCE(?, r.created_at) "
                + "AND (r.closed_at >= COALESCE(?, r.closed_at) OR r.closed_at IS NULL)"
                + filterClosedAt + filterDuplicate + filterType + filterSeverity
                + "AND t.title = COALESCE(?, t.title);")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .string("%" + query)
                    .object(latestOpeningDateTime)
                    .object(earliestClosingDateTime)
                    .string(topic)
                    .toStatement().executeQuery();
            while (rs.next()) {
                reports = rs.getInt("num_reports");
            }
        } catch (SQLException e) {
            log.error("Error while loading the topic search suggestions for the query " + query, e);
            throw new StoreException("Error while loading the topic search suggestions for the query " + query, e);
        }

        return reports;
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
