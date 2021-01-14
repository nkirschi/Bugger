package tech.bugger.control.backing;

import tech.bugger.business.service.ProfileService;
import tech.bugger.business.service.SearchService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;

/**
 * Backing bean for the search page.
 */
@ViewScoped
@Named
public class SearchBacker implements Serializable {

    /**
     * Enumeration of the tabs (and thus the types of results) displayed on the search page.
     */
    public enum Tab {
        /**
         * Tab for topic results.
         */
        TOPIC,

        /**
         * Tab for report results.
         */
        REPORT,

        /**
         * Tab for user results.
         */
        USER
    }

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(SearchBacker.class);

    @Serial
    private static final long serialVersionUID = -1264737473650782156L;

    /**
     * The current search query.
     */
    private String query;

    /**
     * The current tab.
     */
    private Tab tab;

    /**
     * The paginator for topic results.
     */
    private Paginator<Topic> topicResults;

    /**
     * The paginator for report results.
     */
    private Paginator<Report> reportResults;

    /**
     * The paginator for user results.
     */
    private Paginator<User> userResults;

    /**
     * The latest creation date to search for.
     */
    private ZonedDateTime latestCreationDateTime;

    /**
     * The earliest closing date to search for.
     */
    private ZonedDateTime earliestClosingDateTime;

    /**
     * Whether to show open reports in the report search results.
     */
    private boolean openReportShown;

    /**
     * Whether to show closed reports in the report search results.
     */
    private boolean closedReportShown;

    /**
     * Whether to show reports marked as duplicates in the report search results.
     */
    private boolean duplicatesShown;

    /**
     * Whether to search for the query string in posts.
     */
    private boolean searchInFullText;

    /**
     * The topic to search for.
     */
    private Topic searchTopic;

    /**
     * A hash map containing information for which report type to filter.
     */
    private HashMap<Report.Type, Boolean> reportTypeFilter; // selectManyCheckbox

    /**
     * A hash map containing information for which report severity to filter.
     */
    private HashMap<Report.Severity, Boolean> severityFilter;

    /**
     * Whether to show administrators in the user search results.
     */
    private boolean adminShown;

    /**
     * Whether to show non-administrators in the user search results.
     */
    private boolean nonAdminShown;

    /**
     * A transient search service.
     */
    @Inject
    private transient SearchService searchService;

    /**
     * A transient topic service.
     */
    @Inject
    private transient TopicService topicService;

    /**
     * A transient profile service.
     */
    @Inject
    private transient ProfileService profileService;

    /**
     * The current faces context.
     */
    @Inject
    private FacesContext fctx;

    /**
     * Initializes the search page. The default tab is {@code Tab.REPORT}.
     */
    @PostConstruct
    public void init() {
    }

    /**
     * Changes the tab (and with it, the type of search results).
     */
    public void changeTab() {
    }

    /**
     * Executes the search with the specified query and filters.
     */
    public void search() {
    }

    /**
     * Returns the current relevance of a specific report.
     *
     * @param report The report whose relevance is to be returned.
     * @return The relevance as an {@code int}.
     */
    public int getRelevance(final Report report) {
        return 0;
    }

    /**
     * Returns the current voting weight of a specific user.
     *
     * @param user The user whose voting weight is to be returned.
     * @return The voting weight as an {@code int}.
     */
    public int getVotingWeightForUser(final User user) {
        return 0;
    }

    /**
     * Returns the time stamp of the last action in one particular topic. Creating, editing and moving a report as well
     * as creating and editing posts count as actions. Moving a report is an action in the destination topic only.
     *
     * @param topic The topic in question.
     * @return The time stamp of the last action as a {@code ZonedDateTime}.
     */
    public ZonedDateTime lastChange(final Topic topic) {
        return null;
    }

    /**
     * Returns the time stamp of the last action in one particular report. Creating, editing and moving a report as well
     * as creating and editing posts count as actions.
     *
     * @param report The report in question.
     * @return The time stamp of the last action as a {@code ZonedDateTime}.
     */
    public ZonedDateTime lastChange(final Report report) {
        return null;
    }

    /**
     * Returns the number of subscribers of one particular topic.
     *
     * @param topic The topic in question.
     * @return The number of subscribers as an {@code int}.
     */
    public int getNumberOfSubscribers(final Topic topic) {
        return 0;
    }

    /**
     * Returns the total number of posts of reports in one particular topic.
     *
     * @param topic The topic in question.
     * @return The number of posts as an {@code int}.
     */
    public int getNumberOfPosts(final Topic topic) {
        return 0;
    }

    /**
     * @return The searchQuery.
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query The searchQuery to set.
     */
    public void setQuery(final String query) {
        this.query = query;
    }

    /**
     * @return The tab.
     */
    public Tab getTab() {
        return tab;
    }

    /**
     * @param tab The tab to set.
     */
    public void setTab(final Tab tab) {
        this.tab = tab;
    }

    /**
     * @return The latestOpeningDate.
     */
    public ZonedDateTime getLatestCreationDateTime() {
        return latestCreationDateTime;
    }

    /**
     * @param latestOpeningDate The latestOpeningDate to set.
     */
    public void setLatestOpeningDate(final ZonedDateTime latestOpeningDate) {
        this.latestCreationDateTime = latestOpeningDate;
    }

    /**
     * @return The earliestClosingDate.
     */
    public ZonedDateTime getEarliestClosingDateTime() {
        return earliestClosingDateTime;
    }

    /**
     * @param earliestClosingDate The earliestClosingDate to set.
     */
    public void setEarliestClosingDate(final ZonedDateTime earliestClosingDate) {
        this.earliestClosingDateTime = earliestClosingDate;
    }

    /**
     * @return {@code true} if open reports are shown, {@code false} otherwise.
     */
    public boolean isOpenReportShown() {
        return openReportShown;
    }

    /**
     * @param showOpenReports The showOpenReports to set.
     */
    public void setOpenReportShown(final boolean showOpenReports) {
        this.openReportShown = showOpenReports;
    }

    /**
     * @return {@code true} if closed reports are shown, {@code false} otherwise.
     */
    public boolean isClosedReportShown() {
        return closedReportShown;
    }

    /**
     * @param showClosedReports The showClosedReports to set.
     */
    public void setClosedReportShown(final boolean showClosedReports) {
        this.closedReportShown = showClosedReports;
    }

    /**
     * @return {@code true} if duplicate reports are shown, {@code false} otherwise.
     */
    public boolean isDuplicatesShown() {
        return duplicatesShown;
    }

    /**
     * @param showDuplicates The showDuplicates to set.
     */
    public void setDuplicatesShown(final boolean showDuplicates) {
        this.duplicatesShown = showDuplicates;
    }

    /**
     * @return {@code true} if the search is conducted in the full text of posts shown, {@code false} otherwise.
     */
    public boolean isSearchInFullText() {
        return searchInFullText;
    }

    /**
     * @param searchInFullText The searchInFullText to set.
     */
    public void setSearchInFullText(final boolean searchInFullText) {
        this.searchInFullText = searchInFullText;
    }

    /**
     * @return The searchTopic.
     */
    public Topic getSearchTopic() {
        return searchTopic;
    }

    /**
     * @param searchTopic The searchTopic to set.
     */
    public void setSearchTopic(final Topic searchTopic) {
        this.searchTopic = searchTopic;
    }

    /**
     * @return The reportTypeFilter.
     */
    public HashMap<Report.Type, Boolean> getReportTypeFilter() {
        return reportTypeFilter;
    }

    /**
     * @param reportTypeFilter The reportTypeFilter to set.
     */
    public void setReportTypeFilter(final HashMap<Report.Type, Boolean> reportTypeFilter) {
        this.reportTypeFilter = reportTypeFilter;
    }

    /**
     * @return The severityFilter.
     */
    public HashMap<Report.Severity, Boolean> getSeverityFilter() {
        return severityFilter;
    }

    /**
     * @param severityFilter The severityFilter to set.
     */
    public void setSeverityFilter(final HashMap<Report.Severity, Boolean> severityFilter) {
        this.severityFilter = severityFilter;
    }

    /**
     * @return {@code true} if administrators are shown, {@code false} otherwise.
     */
    public boolean isAdminShown() {
        return adminShown;
    }

    /**
     * @param showAdmins The showAdmins to set.
     */
    public void setAdminShown(final boolean showAdmins) {
        this.adminShown = showAdmins;
    }

    /**
     * @return {@code true} if non-administrators are shown, {@code false} otherwise.
     */
    public boolean isNonAdminShown() {
        return nonAdminShown;
    }

    /**
     * @param showNonAdmins The showNonAdmins to set.
     */
    public void setNonAdminShown(final boolean showNonAdmins) {
        this.nonAdminShown = showNonAdmins;
    }

    /**
     * @return The topicResults.
     */
    public Paginator<Topic> getTopicResults() {
        return topicResults;
    }

    /**
     * @return The reportResults.
     */
    public Paginator<Report> getReportResults() {
        return reportResults;
    }

    /**
     * @return The userResults.
     */
    public Paginator<User> getUserResults() {
        return userResults;
    }

}
