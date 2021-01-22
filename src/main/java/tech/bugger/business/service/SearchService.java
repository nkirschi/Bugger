package tech.bugger.business.service;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.User;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;

/**
 * Service providing methods related to searching for specific topics, reports and users. A {@code Feedback} event is
 * fired, if unexpected circumstances occur.
 */
@ApplicationScoped
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
     * The current application Settings.
     */
    private final ApplicationSettings applicationSettings;

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
     * @param applicationSettings The application settings for the service.
     */
    @Inject
    public SearchService(final Event<Feedback> feedback, final @RegistryKey("messages") ResourceBundle messages,
                         final TransactionManager transactionManager, ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
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
    public List<String> getUserSuggestions(final String query) {
        List<String> users = new ArrayList<>();
        String searchInput = query.trim().toLowerCase();
        try (Transaction tx = transactionManager.begin()) {
            users = tx.newSearchGateway().getUserSuggestions(searchInput, MAX_SUGGESTIONS);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while loading the user search suggestions.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return users;
    }

    /**
     * Returns at most the first five results when searching the data source for reports.
     *
     * @param query The search query for report titles.
     * @return A list containing the first few results.
     */
    public List<String> getReportSuggestions(final String query) {
        List<String> reports = new ArrayList<>();
        String searchInput = query.trim().toLowerCase();
        try (Transaction tx = transactionManager.begin()) {
            reports = tx.newSearchGateway().getReportSuggestions(searchInput, MAX_SUGGESTIONS);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while loading the report search suggestions.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return reports;
    }

    /**
     * Returns at most the first five results when searching the data source for topics.
     *
     * @param query The search query for topic titles.
     * @return A list containing the first few results.
     */
    public List<String> getTopicSuggestions(final String query) {
        List<String> topics = new ArrayList<>();
        String searchInput = query.trim().toLowerCase();
        try (Transaction tx = transactionManager.begin()) {
            topics = tx.newSearchGateway().getTopicSuggestions(searchInput, MAX_SUGGESTIONS);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while loading the user search suggestions.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return topics;
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
        List<String> users = new ArrayList<>();

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
     * Returns at most the first five results when searching the data source for users which could be unbanned from a
     * certain topic.
     *
     * @param query The search query for usernames.
     * @param topic The topic in question.
     * @return A list containing the first few results.
     */
    public List<String> getUserUnbanSuggestions(final String query, final Topic topic) {
        List<String> users = null;

        try (Transaction tx = transactionManager.begin()) {
            users = tx.newSearchGateway().getUserUnbanSuggestions(query, MAX_SUGGESTIONS, topic);
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
        List<String> users = new ArrayList<>();

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
     * Returns at most the first five results when searching the data source for users which could be demoted as
     * moderators of a certain topic.
     *
     * @param query The search query for usernames.
     * @param topic The topic in question.
     * @return A list containing the first few results.
     */
    public List<String> getUserUnmodSuggestions(final String query, final Topic topic) {
        List<String> users = null;
        try (Transaction tx = transactionManager.begin()) {
            users = tx.newSearchGateway().getUserUnmodSuggestions(query, MAX_SUGGESTIONS, topic);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while loading the user search suggestions.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return users;
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
        List<User> users = new ArrayList<>();
        String searchInput = query.trim().toLowerCase();
        try (Transaction tx = transactionManager.begin()) {
            users = tx.newSearchGateway().getUserResults(searchInput, selection, showAdmins, showNonAdmins);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while loading the user search results.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        for (User u : users) {
            u.setVotingWeight(getVotingWeightFromPosts(u.getNumPosts()));
        }
        return users;
    }

    /**
     * Returns the voting weight calculated from a given number of posts.
     *
     * @param posts The number of posts.
     * @return The voting weight as an {@code int}.
     */
    public int getVotingWeightFromPosts(final int posts) {
        int votingWeight = 0;

        if (posts == 0) {
            votingWeight = 1;
        } else {
            String[] votingDef = applicationSettings.getConfiguration().getVotingWeightDefinition().split(",");
            if (votingDef.length == 0) {
                log.error("The voting weight definition is empty");
                feedback.fire(new Feedback(messages.getString("voting_weight_failure"), Feedback.Type.ERROR));
                return votingWeight;
            }

            try {
                int[] votingWeightDef = Arrays.stream(votingDef).mapToInt(Integer::parseInt).sorted().toArray();
                votingWeight = calculateVotingWeight(posts, votingWeightDef);
            } catch (NumberFormatException e) {
                log.error("The voting weight definition could not be parsed to a number");
                feedback.fire(new Feedback(messages.getString("voting_weight_failure"), Feedback.Type.ERROR));
            }
        }
        return votingWeight;
    }

    /**
     * Calculates the voting weight from the given number of posts and the voting weight definition.
     *
     * @param numPosts        The number of posts.
     * @param votingWeightDef The voting weight definition.
     * @return The calculated voting weight.
     */
    private int calculateVotingWeight(final int numPosts, final int[] votingWeightDef) {
        if (votingWeightDef[0] != 0) {
            log.error("The voting weight definition needs to contain a 0.");
            feedback.fire(new Feedback(messages.getString("voting_weight_failure"), Feedback.Type.ERROR));
            return 0;
        }
        for (int i = 1; i < votingWeightDef.length; i++) {
            if (numPosts < votingWeightDef[i]) {
                return i;
            }
        }
        return votingWeightDef.length;
    }

    /**
     * Searches the data source for specific topics.
     *
     * @param query     The search query for topic titles.
     * @param selection Information on which part of the result to retrieve.
     * @return A list of topics containing the selected search results.
     */
    public List<Topic> getTopicResults(final String query, final Selection selection) {
        List<Topic> topics = new ArrayList<>();
        String searchInput = query.trim().toLowerCase();
        try (Transaction tx = transactionManager.begin()) {
            topics = tx.newSearchGateway().getTopicResults(searchInput, selection);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while loading the topic search results.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return topics;
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
                                         final OffsetDateTime latestCreationDateTime,
                                         final OffsetDateTime earliestClosingDateTime,
                                         final boolean showOpenReports, final boolean showClosedReports,
                                         final boolean showDuplicates, final String topic,
                                         final HashMap<Report.Type, Boolean> reportTypeFilter,
                                         final HashMap<Report.Severity, Boolean> severityFilter) {
        List<Report> reports = new ArrayList<>();
        String searchInput = query.trim().toLowerCase();
        try (Transaction tx = transactionManager.begin()) {
            reports = tx.newSearchGateway().getReportResults(searchInput, selection, latestCreationDateTime,
                    earliestClosingDateTime, showOpenReports, showClosedReports, showDuplicates, topic,
                    reportTypeFilter, severityFilter);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Filter Topic " + topic + " not found while searching for reports", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while loading the report search results.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return reports;
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
                                           final OffsetDateTime latestCreationDateTime,
                                           final OffsetDateTime earliestClosingDateTime, final boolean showOpenReports,
                                           final boolean showClosedReports, final boolean showDuplicates,
                                           final String topic, final HashMap<Report.Type, Boolean> reportTypeFilter,
                                           final HashMap<Report.Severity, Boolean> severityFilter) {
            List<Report> reports = new ArrayList<>();
        String searchInput = query.trim().toLowerCase();
            try (Transaction tx = transactionManager.begin()) {
                reports = tx.newSearchGateway().getFulltextResults(searchInput, selection, latestCreationDateTime,
                        earliestClosingDateTime, showOpenReports, showClosedReports, showDuplicates, topic,
                        reportTypeFilter, severityFilter);
                tx.commit();
            } catch (NotFoundException e) {
                log.error("Filter Topic with title " + topic + " not found while searching for reports", e);
                feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
            } catch (TransactionException e) {
                log.error("Error while loading the report search results.", e);
                feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
            }
            return reports;
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
        int results = 0;
        String searchInput = query.trim().toLowerCase();
        try (Transaction tx = transactionManager.begin()) {
            results = tx.newSearchGateway().getNumberOfUserResults(searchInput, showAdmins, showNonAdmins);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while loading the user search results.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return results;
    }

    /**
     * Returns the number of topic results for a certain search request.
     *
     * @param query The search query for topic titles.
     * @return The number of results as an {@code int}.
     */
    public int getNumberOfTopicResults(final String query) {
        int results = 0;
        String searchInput = query.trim().toLowerCase();
        try (Transaction tx = transactionManager.begin()) {
            results = tx.newSearchGateway().getNumberOfTopicResults(searchInput);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error while loading the topic search results.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return results;
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
    public int getNumberOfReportResults(final String query, final OffsetDateTime latestCreationDateTime,
                                        final OffsetDateTime earliestClosingDateTime, final boolean showOpenReports,
                                        final boolean showClosedReports, final boolean showDuplicates,
                                        final String topic, final HashMap<Report.Type, Boolean> reportTypeFilter,
                                        final HashMap<Report.Severity, Boolean> severityFilter) {
        String searchInput = query.trim().toLowerCase();
        int results = 0;
        try (Transaction tx = transactionManager.begin()) {
            results = tx.newSearchGateway().getNumberOfReportResults(searchInput, latestCreationDateTime,
                    earliestClosingDateTime, showOpenReports, showClosedReports, showDuplicates, topic,
                    reportTypeFilter, severityFilter);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Filter Topic " + topic + " not found while searching for reports", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while loading the report search results.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return results;
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
    public int getNumberOfFulltextResults(final String query, final OffsetDateTime latestCreationDateTime,
                                          final OffsetDateTime earliestClosingDateTime, final boolean showOpenReports,
                                          final boolean showClosedReports, final boolean showDuplicates,
                                          final String topic, final HashMap<Report.Type, Boolean> reportTypeFilter,
                                          final HashMap<Report.Severity, Boolean> severityFilter) {
        int results = 0;
        try (Transaction tx = transactionManager.begin()) {
            results = tx.newSearchGateway().getNumberOfFulltextResults(query, latestCreationDateTime,
                    earliestClosingDateTime, showOpenReports, showClosedReports, showDuplicates, topic,
                    reportTypeFilter, severityFilter);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Filter Topic with title " + topic + " not found while searching for reports", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while loading the report search results.", e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return results;
    }

}
