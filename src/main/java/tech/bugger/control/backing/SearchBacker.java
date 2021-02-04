package tech.bugger.control.backing;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import tech.bugger.business.service.SearchService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Paginator;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;

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
     * The available topic titles.
     */
    private List<String> topicTitles;

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
    private OffsetDateTime latestCreationDateTime;

    /**
     * The earliest closing date to search for.
     */
    private OffsetDateTime earliestClosingDateTime;

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
     * Whether to search for reports of th BUG type.
     */
    private boolean showBug;

    /**
     * Whether to search for reports of th REPORT type.
     */
    private boolean showFeature;

    /**
     * Whether to search for reports of th HINT type.
     */
    private boolean showHint;

    /**
     * Whether to search for reports with the MINOR severity.
     */
    private boolean showMinor;

    /**
     * Whether to search for reports with the RELEVANT severity.
     */
    private boolean showRelevant;

    /**
     * Whether to search for reports with the SEVERE severity.
     */
    private boolean showSevere;

    /**
     * The title of a filtered topic.
     */
    private String topic;

    /**
     * Whether to show administrators in the user search results.
     */
    private boolean adminShown;

    /**
     * Whether to show non-administrators in the user search results.
     */
    private boolean nonAdminShown;

    /**
     * A search service.
     */
    private final SearchService searchService;

    /**
     * A topic service.
     */
    private final TopicService topicService;

    /**
     * The current external context.
     */
    private final ExternalContext ectx;

    /**
     * Constructs a new search page backing bean with the necessary dependencies.
     *
     * @param searchService The search service to use.
     * @param topicService  The topic service to use.
     * @param ectx          The current {@link ExternalContext} of the application.
     */
    @Inject
    public SearchBacker(final SearchService searchService,
                        final TopicService topicService,
                        final ExternalContext ectx) {
        this.searchService = searchService;
        this.topicService = topicService;
        this.ectx = ectx;
    }

    /**
     * Initializes the search page. The default tab is {@link Tab#REPORT}.
     */
    @PostConstruct
    void init() {
        tab = Tab.REPORT;
        query = "";
        if (ectx.getRequestParameterMap().containsKey("q")) {
            query = ectx.getRequestParameterMap().get("q");
        }
        if (ectx.getRequestParameterMap().containsKey("t")) {
            try {
                tab = Tab.valueOf(ectx.getRequestParameterMap().get("t"));
            } catch (IllegalArgumentException e) {
                throw new Error404Exception();
            }
        }
        openReportShown = true;
        closedReportShown = true;
        duplicatesShown = true;
        nonAdminShown = true;
        adminShown = true;
        showBug = true;
        showFeature = true;
        showHint = true;
        showMinor = true;
        showSevere = true;
        showRelevant = true;
        topic = null;
        if (tab == Tab.USER) {
            userResults = new Paginator<>("username", Selection.PageSize.NORMAL) {
                @Override
                protected Iterable<User> fetch() {
                    return searchService.getUserResults(query, getSelection(), adminShown, nonAdminShown);
                }

                @Override
                protected int totalSize() {
                    return searchService.getNumberOfUserResults(query, adminShown, nonAdminShown);
                }
            };
        }

        if (tab == Tab.REPORT) {
            reportResults = new Paginator<>("id", Selection.PageSize.NORMAL) {
                @Override
                protected Iterable<Report> fetch() {
                    Map<Report.Type, Boolean> typeHashMap = getTypeHashMap();
                    Map<Report.Severity, Boolean> severityHashMap = getSeverityHashMap();
                    if (topic != null && topic.isBlank()) {
                        topic = null;
                    }
                    return searchService.getReportResults(query, getSelection(), latestCreationDateTime,
                            earliestClosingDateTime, openReportShown, closedReportShown, duplicatesShown, topic,
                            typeHashMap, severityHashMap);
                }

                @Override
                protected int totalSize() {
                    Map<Report.Type, Boolean> typeHashMap = getTypeHashMap();
                    Map<Report.Severity, Boolean> severityHashMap = getSeverityHashMap();
                    if (topic != null && topic.isBlank()) {
                        topic = null;
                    }
                    return searchService.getNumberOfReportResults(query, latestCreationDateTime,
                            earliestClosingDateTime, openReportShown, closedReportShown, duplicatesShown, topic,
                            typeHashMap, severityHashMap);
                }

                private Map<Report.Type, Boolean> getTypeHashMap() {
                    Map<Report.Type, Boolean> typeHashMap = new HashMap<>();
                    typeHashMap.put(Report.Type.BUG, showBug);
                    typeHashMap.put(Report.Type.FEATURE, showFeature);
                    typeHashMap.put(Report.Type.HINT, showHint);
                    return typeHashMap;
                }

                private Map<Report.Severity, Boolean> getSeverityHashMap() {
                    Map<Report.Severity, Boolean> severityHashMap = new HashMap<>();
                    severityHashMap.put(Report.Severity.MINOR, showMinor);
                    severityHashMap.put(Report.Severity.RELEVANT, showRelevant);
                    severityHashMap.put(Report.Severity.SEVERE, showSevere);
                    return severityHashMap;
                }
            };
        }

        if (tab == Tab.TOPIC) {
            topicResults = new Paginator<>("id", Selection.PageSize.NORMAL) {
                @Override
                protected Iterable<Topic> fetch() {
                    return searchService.getTopicResults(query, getSelection());
                }

                @Override
                protected int totalSize() {
                    return searchService.getNumberOfTopicResults(query);
                }
            };
            topicTitles = topicService.discoverTopics().stream().map(Topic::getTitle).collect(Collectors.toList());
        }
    }

    /**
     * Executes the search with the specified query and filters.
     *
     * @return The site to redirect to or {@code null} to reload the page.
     */
    public String search() {
        if (tab == Tab.REPORT) {
            reportResults.updateReset();
        } else if (tab == Tab.USER) {
            userResults.updateReset();
        } else if (tab == Tab.TOPIC) {
            topicResults.updateReset();
        }
        return null;
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
        if (tab == Tab.REPORT) {
            topicTitles = topicService.discoverTopics().stream().map(Topic::getTitle).collect(Collectors.toList());
        }
    }

    /**
     * @return The latestOpeningDate.
     */
    public OffsetDateTime getLatestOpeningDateTime() {
        return latestCreationDateTime;
    }

    /**
     * @param latestOpeningDate The latestOpeningDate to set.
     */
    public void setLatestOpeningDateTime(final OffsetDateTime latestOpeningDate) {
        this.latestCreationDateTime = latestOpeningDate;
    }

    /**
     * @return The earliestClosingDate.
     */
    public OffsetDateTime getEarliestClosingDateTime() {
        return earliestClosingDateTime;
    }

    /**
     * @param earliestClosingDate The earliestClosingDate to set.
     */
    public void setEarliestClosingDateTime(final OffsetDateTime earliestClosingDate) {
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
     * @return {@code true} if Bug-type reports are shown, {@code false} otherwise.
     */
    public boolean isShowBug() {
        return showBug;
    }

    /**
     * @param showBug {@code true} if Bug-type reports are shown, {@code false} otherwise.
     */
    public void setShowBug(final boolean showBug) {
        this.showBug = showBug;
    }

    /**
     * @return {@code true} if Hint-type reports are shown, {@code false} otherwise.
     */
    public boolean isShowHint() {
        return showHint;
    }

    /**
     * @param showHint {@code true} if Hint-type reports are shown, {@code false} otherwise.
     */
    public void setShowHint(final boolean showHint) {
        this.showHint = showHint;
    }

    /**
     * @return {@code true} if Feature-type reports are shown, {@code false} otherwise.
     */
    public boolean isShowFeature() {
        return showFeature;
    }

    /**
     * @param showFeature {@code true} if Feature-type reports are shown, {@code false} otherwise.
     */
    public void setShowFeature(final boolean showFeature) {
        this.showFeature = showFeature;
    }

    /**
     * @return {@code true} if Minor-Severity reports are shown, {@code false} otherwise.
     */
    public boolean isShowMinor() {
        return showMinor;
    }

    /**
     * @param showMinor {@code true} if Minor-Severity reports are shown, {@code false} otherwise.
     */
    public void setShowMinor(final boolean showMinor) {
        this.showMinor = showMinor;
    }

    /**
     * @return {@code true} if Relevant-Severity reports are shown, {@code false} otherwise.
     */
    public boolean isShowRelevant() {
        return showRelevant;
    }

    /**
     * @param showRelevant {@code true} if Relevant-Severity reports are shown, {@code false} otherwise.
     */
    public void setShowRelevant(final boolean showRelevant) {
        this.showRelevant = showRelevant;
    }

    /**
     * @return {@code true} if Severe-Severity reports are shown, {@code false} otherwise.
     */

    public boolean isShowSevere() {
        return showSevere;
    }

    /**
     * @param showSevere {@code true} if Severe-Severity reports are shown, {@code false} otherwise.
     */
    public void setShowSevere(final boolean showSevere) {
        this.showSevere = showSevere;
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

    /**
     * Retrieves the titles of all topics in the system.
     *
     * @return A list of all topic titles.
     */
    public List<String> getTopicTitles() {
        return topicTitles;
    }

    /**
     * @return The topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @param topic The topic to set.
     */
    public void setTopic(final String topic) {
        this.topic = topic;
    }

}
