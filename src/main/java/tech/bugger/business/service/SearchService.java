package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Service providing methods related to searching for specific topics, reports and users. A {@code Feedback} event is
 * fired, if unexpected circumstances occur.
 */
@Dependent
public class SearchService {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(SearchService.class);

    /**
     * Feedback event for user feedback.
     */
    private final Event<Feedback> feedback;

    /**
     * The resource bundle for feedback messages.
     */
    private final ResourceBundle messages;

    /**
     * Transaction manager used for creating transactions.
     */
    private final TransactionManager transactionManager;

    /**
     * The maximum amount of suggestions for user input in a search field.
     */
    private static final int MAX_SUGGESTIONS = 5;

    /**
     * Constructs a new search service with the given dependencies.
     *
     * @param transactionManager The transaction manager to use for creating transactions.
     * @param feedback           The feedback event to be used for user feedback.
     * @param messages           The resource bundle to look up feedback messages.
     */
    @Inject
    public SearchService(final Event<Feedback> feedback, final @RegistryKey("messages") ResourceBundle messages,
                         final TransactionManager transactionManager) {
        this.feedback = feedback;
        this.messages = messages;
        this.transactionManager = transactionManager;
    }

    /**
     * Returns at most the first five results when searching the data source for users.
     *
     * @param query The search query for usernames.
     * @return A list containing the first few results.
     */
    public List<User> getUserSuggestions(final String query) {
        return null;
    }

    /**
     * Returns at most the first five results when searching the data source for users which could be banned from a
     * certain topic.
     *
     * @param query The search query for usernames.
     * @param topic The topic in question.
     * @return A list containing the first few results.
     */
    public List<String> getUserBanSuggestions(final String query, final Topic topic) {
        List<String> users = null;
        try (Transaction tx = transactionManager.begin()) {
            users = tx.newSearchGateway().getUserBanSuggestions(query, MAX_SUGGESTIONS, topic);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while loading the user search suggestions.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return users;
    }

    /**
     * Returns at most the first five results when searching the data source for users which could be made moderators of
     * a certain topic.
     *
     * @param query The search query for usernames.
     * @param topic The topic in question.
     * @return A list containing the first few results.
     */
    public List<String> getUserModSuggestions(final String query, final Topic topic) {
        List<String> users = null;
        try (Transaction tx = transactionManager.begin()) {
            users = tx.newSearchGateway().getUserModSuggestions(query, MAX_SUGGESTIONS, topic);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while loading the user search suggestions.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return users;
    }

    /**
     * Returns at most the first five results when searching the data source for topics.
     *
     * @param query The search query for topic titles.
     * @return A list containing the first few results.
     */
    public List<Topic> getTopicSuggestions(final String query) {
        return null;
    }

    /**
     * Returns at most the first five results when searching the data source for reports.
     *
     * @param query The search query for report titles.
     * @return A list containing the first few results.
     */
    public List<Report> getReportSuggestions(final String query) {
        return null;
    }

    /**
     * Searches the data source for specific users.
     *
     * @param query         The search query for usernames.
     * @param selection     Information on which parts of the result to retrieve.
     * @param showAdmins    Whether or not to include administrators.
     * @param showNonAdmins Whether or not to include non-administrators.
     * @return A list of users containing the selected search results.
     */
    public List<User> getUserResults(final String query, final Selection selection, final boolean showAdmins,
                                     final boolean showNonAdmins) {
        return null;
    }

    /**
     * Searches the data source for specific topics.
     *
     * @param query     The search query for topic titles.
     * @param selection Information on which part of the result to retrieve.
     * @return A list of topics containing the selected search results.
     */
    public List<Topic> getTopicResults(final String query, final Selection selection) {
        return null;
    }

    /**
     * Searches the data source for specific topics.
     *
     * @param query                   The search query for report titles.
     * @param selection               Information on which part of the result to retrieve.
     * @param latestCreationDateTime  Only reports created before this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were created.
     * @param earliestClosingDateTime Only reports closed after this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were closed. Reports still open are never
     *                                excluded via this filter.
     * @param showOpenReports         Whether or not to include open reports.
     * @param showClosedReports       Whether or not to include closed reports.
     * @param showDuplicates          Whether or not to include duplicates.
     * @param topic                   Only reports belonging to this topic are taken into account. Passing {@code null}
     *                                includes reports regardless of which topic they belong to.
     * @param reportTypeFilter        Which types of reports to include and which to exclude.
     * @param severityFilter          Which reports of certain severities to include or exclude.
     * @return A list of reports containing the selected search results.
     */
    public List<Report> getReportResults(final String query, final Selection selection,
                                         final ZonedDateTime latestCreationDateTime,
                                         final ZonedDateTime earliestClosingDateTime,
                                         final boolean showOpenReports, final boolean showClosedReports,
                                         final boolean showDuplicates, final Topic topic,
                                         final HashMap<Report.Type, Boolean> reportTypeFilter,
                                         final HashMap<Report.Severity, Boolean> severityFilter) {
        return null;
    }

    /**
     * Searches the data source for specific topics. The results are reports which can also contain the search query
     * somewhere in the full texts of their posts.
     *
     * @param query                   The search query for report titles and full texts of posts.
     * @param selection               Information on which part of the result to retrieve.
     * @param latestCreationDateTime  Only reports created before this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were created.
     * @param earliestClosingDateTime Only reports closed after this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were closed. Reports still open are never
     *                                excluded via this filter.
     * @param showOpenReports         Whether or not to include open reports.
     * @param showClosedReports       Whether or not to include closed reports.
     * @param showDuplicates          Whether or not to include duplicates.
     * @param topic                   Only reports belonging to this topic are taken into account. Passing {@code null}
     *                                includes reports regardless of which topic they belong to.
     * @param reportTypeFilter        Which types of reports to include and which to exclude.
     * @param severityFilter          Which reports of certain severities to include or exclude.
     * @return A list of reports containing the selected search results.
     */
    public List<Report> getFulltextResults(final String query, final Selection selection,
                                           final ZonedDateTime latestCreationDateTime,
                                           final ZonedDateTime earliestClosingDateTime, final boolean showOpenReports,
                                           final boolean showClosedReports, final boolean showDuplicates,
                                           final Topic topic, final HashMap<Report.Type, Boolean> reportTypeFilter,
                                           final HashMap<Report.Severity, Boolean> severityFilter) {
        return null;
    }

    /**
     * Returns the number of user results for a certain search request.
     *
     * @param query         The search query for usernames.
     * @param showAdmins    Whether or not to include administrators.
     * @param showNonAdmins Whether or not to include non-administrators.
     * @return The number of results as an {@code int}.
     */
    public int getNumberOfUserResults(final String query, final boolean showAdmins, final boolean showNonAdmins) {
        return 0;
    }

    /**
     * Returns the number of topic results for a certain search request.
     *
     * @param query The search query for topic titles.
     * @return The number of results as an {@code int}.
     */
    public int getNumberOfTopicResults(final String query) {
        return 0;
    }

    /**
     * Returns the number of report results for a certain search request.
     *
     * @param query                   The search query for report titles.
     * @param latestCreationDateTime  Only reports created before this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were created.
     * @param earliestClosingDateTime Only reports closed after this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were closed. Reports still open are never
     *                                excluded via this filter.
     * @param showOpenReports         Whether or not to include open reports.
     * @param showClosedReports       Whether or not to include closed reports.
     * @param showDuplicates          Whether or not to include duplicates.
     * @param topic                   Only reports belonging to this topic are taken into account. Passing {@code null}
     *                                includes reports regardless of which topic they belong to.
     * @param reportTypeFilter        Which types of reports to include and which to exclude.
     * @param severityFilter          Which reports of certain severities to include or exclude.
     * @return The number of results as an {@code int}.
     */
    public int getNumberOfReportResults(final String query, final ZonedDateTime latestCreationDateTime,
                                        final ZonedDateTime earliestClosingDateTime, final boolean showOpenReports,
                                        final boolean showClosedReports, final boolean showDuplicates,
                                        final Topic topic, final HashMap<Report.Type, Boolean> reportTypeFilter,
                                        final HashMap<Report.Severity, Boolean> severityFilter) {
        return 0;
    }

    /**
     * Returns the number of report results for a certain full text search request.
     *
     * @param query                   The search query for report titles.
     * @param latestCreationDateTime  Only reports created before this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were created.
     * @param earliestClosingDateTime Only reports closed after this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were closed. Reports still open are never
     *                                excluded via this filter.
     * @param showOpenReports         Whether or not to include open reports.
     * @param showClosedReports       Whether or not to include closed reports.
     * @param showDuplicates          Whether or not to include duplicates.
     * @param topic                   Only reports belonging to this topic are taken into account. Passing {@code null}
     *                                includes reports regardless of which topic they belong to.
     * @param reportTypeFilter        Which types of reports to include and which to exclude.
     * @param severityFilter          Which reports of certain severities to include or exclude.
     * @return The number of results as an {@code int}.
     */
    public int getNumberOfFulltextResults(final String query, final ZonedDateTime latestCreationDateTime,
                                          final ZonedDateTime earliestClosingDateTime, final boolean showOpenReports,
                                          final boolean showClosedReports, final boolean showDuplicates,
                                          final Topic topic, final HashMap<Report.Type, Boolean> reportTypeFilter,
                                          final HashMap<Report.Severity, Boolean> severityFilter) {
        return 0;
    }

}
