package tech.bugger.control.backing;

import tech.bugger.business.service.ProfileService;
import tech.bugger.business.service.SearchService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
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

    private static final Log log = Log.forClass(SearchBacker.class);
    private static final long serialVersionUID = -1264737473650782156L;

    private String searchQuery;
    private Tab tab;

    private Paginator<Topic> topicResults;
    private Paginator<Report> reportResults;
    private Paginator<User> userResults;
    private ZonedDateTime latestCreationDateTime;
    private ZonedDateTime earliestClosingDateTime;
    private boolean openReportShown;
    private boolean closedReportShown;
    private boolean duplicatesShown;
    private boolean searchInFullText;
    private Topic searchTopic;
    private HashMap<Report.Type, Boolean> reportTypeFilter; // selectManyCheckbox
    private HashMap<Report.Severity, Boolean> severityFilter;

    private boolean adminShown;
    private boolean nonAdminShown;

    @Inject
    private transient SearchService searchService;

    @Inject
    private transient TopicService topicService;

    @Inject
    private transient ProfileService profileService;

    @Inject
    private FacesContext fctx;

    /**
     * Initializes the search page. The default tab is {@code Tab.REPORT}.
     */
    @PostConstruct
    public void init() {
    }

    /**
     * Creates a FacesMessage to display if an event is fired in one of the injected services.
     *
     * @param feedback The feedback with details on what to display.
     */
    public void displayFeedback(@Observes @Any Feedback feedback) {
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
    public int getRelevance(Report report) {
        return 0;
    }

    /**
     * Returns the current voting weight of a specific user.
     *
     * @param user The user whose voting weight is to be returned.
     * @return The voting weight as an {@code int}.
     */
    public int getVotingWeightForUser(User user) {
        return 0;
    }

    /**
     * Returns the time stamp of the last action in one particular topic. Creating, editing and moving a report as well
     * as creating and editing posts count as actions. Moving a report is an action in the destination topic only.
     *
     * @param topic The topic in question.
     * @return The time stamp of the last action as a {@code ZonedDateTime}.
     */
    public ZonedDateTime lastChange(Topic topic) {
        return null;
    }

    /**
     * Returns the time stamp of the last action in one particular report. Creating, editing and moving a report as well
     * as creating and editing posts count as actions.
     *
     * @param report The report in question.
     * @return The time stamp of the last action as a {@code ZonedDateTime}.
     */
    public ZonedDateTime lastChange(Report report) {
        return null;
    }

    /**
     * Returns the number of subscribers of one particular topic.
     *
     * @param topic The topic in question.
     * @return The number of subscribers as an {@code int}.
     */
    public int getNumberOfSubscribers(Topic topic) {
        return 0;
    }

    /**
     * Returns the total number of posts of reports in one particular topic.
     *
     * @param topic The topic in question.
     * @return The number of posts as an {@code int}.
     */
    public int getNumberOfPosts(Topic topic) {
        return 0;
    }

    /**
     * @return The searchQuery.
     */
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * @param searchQuery The searchQuery to set.
     */
    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
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
    public void setTab(Tab tab) {
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
    public void setLatestOpeningDate(ZonedDateTime latestOpeningDate) {
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
    public void setEarliestClosingDate(ZonedDateTime earliestClosingDate) {
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
    public void setOpenReportShown(boolean showOpenReports) {
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
    public void setClosedReportShown(boolean showClosedReports) {
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
    public void setDuplicatesShown(boolean showDuplicates) {
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
    public void setSearchInFullText(boolean searchInFullText) {
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
    public void setSearchTopic(Topic searchTopic) {
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
    public void setReportTypeFilter(HashMap<Report.Type, Boolean> reportTypeFilter) {
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
    public void setSeverityFilter(HashMap<Report.Severity, Boolean> severityFilter) {
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
    public void setAdminShown(boolean showAdmins) {
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
    public void setNonAdminShown(boolean showNonAdmins) {
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
