package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Service providing methods related to searching for specific topics, reports and users. A {@code Feedback} event is
 * fired, if unexpected circumstances occur.
 */
@ApplicationScoped
public class SearchService {

    private static final Log log = Log.forClass(SearchService.class);

    /**
     * Transaction manager used for creating transactions.
     */
    private final TransactionManager transactionManager;

    /**
     * Feedback Event for user feedback.
     */
    private final Event<Feedback> feedbackEvent;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new topic service with the given dependencies.
     *
     * @param transactionManager The transaction manager to use for creating transactions.
     * @param feedbackEvent      The feedback event to use for user feedback.
     * @param messagesBundle     The resource bundle for feedback messages.
     */
    @Inject
    public SearchService(final TransactionManager transactionManager, final Event<Feedback> feedbackEvent,
                         final @RegistryKey("messages") ResourceBundle messagesBundle) {
        this.transactionManager = transactionManager;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
    }


    /**
     * Returns at most the first five results when searching the data source for users.
     *
     * @param query The search query for usernames.
     * @return A list containing the first few results.
     */
    public List<User> getUserSuggestions(String query) {
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
    public List<User> getUserBanSuggestions(String query, Topic topic) {
        return null;
    }

    /**
     * Returns at most the first five results when searching the data source for users which could be made moderators of
     * a certain topic.
     *
     * @param query The search query for usernames.
     * @param topic The topic in question.
     * @return A list containing the first few results.
     */
    public List<User> getUserModSuggestions(String query, Topic topic) {
        return null;
    }

    /**
     * Returns at most the first five results when searching the data source for topics.
     *
     * @param query The search query for topic titles.
     * @return A list containing the first few results.
     */
    public List<Topic> getTopicSuggestions(String query) {
        return null;
    }

    /**
     * Returns at most the first five results when searching the data source for reports.
     *
     * @param query The search query for report titles.
     * @return A list containing the first few results.
     */
    public List<Report> getReportSuggestions(String query) {
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
    public List<User> getUserResults(String query, Selection selection, boolean showAdmins, boolean showNonAdmins) {
        return null;
    }

    /**
     * Searches the data source for specific topics.
     *
     * @param query     The search query for topic titles.
     * @param selection Information on which part of the result to retrieve.
     * @return A list of topics containing the selected search results.
     */
    public List<Topic> getTopicResults(String query, Selection selection) {
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
    public List<Report> getReportResults(String query, Selection selection, ZonedDateTime latestCreationDateTime,
                                         ZonedDateTime earliestClosingDateTime,
                                         boolean showOpenReports, boolean showClosedReports, boolean showDuplicates,
                                         Topic topic,
                                         HashMap<Report.Type, Boolean> reportTypeFilter, HashMap<Report.Severity,
            Boolean> severityFilter) {
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
    public List<Report> getFulltextResults(String query, Selection selection, ZonedDateTime latestCreationDateTime,
                                           ZonedDateTime earliestClosingDateTime, boolean showOpenReports,
                                           boolean showClosedReports, boolean showDuplicates, Topic topic,
                                           HashMap<Report.Type, Boolean> reportTypeFilter, HashMap<Report.Severity,
            Boolean> severityFilter) {
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
    public int getNumberOfUserResults(String query, boolean showAdmins, boolean showNonAdmins) {
        return 0;
    }

    /**
     * Returns the number of topic results for a certain search request.
     *
     * @param query The search query for topic titles.
     * @return The number of results as an {@code int}.
     */
    public int getNumberOfTopicResults(String query) {
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
    public int getNumberOfReportResults(String query, ZonedDateTime latestCreationDateTime,
                                        ZonedDateTime earliestClosingDateTime, boolean showOpenReports,
                                        boolean showClosedReports, boolean showDuplicates, Topic topic,
                                        HashMap<Report.Type, Boolean> reportTypeFilter, HashMap<Report.Severity,
            Boolean> severityFilter) {
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
    public int getNumberOfFulltextResults(String query, ZonedDateTime latestCreationDateTime,
                                          ZonedDateTime earliestClosingDateTime, boolean showOpenReports,
                                          boolean showClosedReports, boolean showDuplicates, Topic topic,
                                          HashMap<Report.Type, Boolean> reportTypeFilter, HashMap<Report.Severity,
            Boolean> severityFilter) {
        return 0;
    }
}
