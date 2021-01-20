package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.StatisticsService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Registry;
import tech.bugger.global.transfer.ReportCriteria;
import tech.bugger.global.transfer.TopReport;
import tech.bugger.global.transfer.TopUser;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Backing bean for the statistics page.
 */
@ViewScoped
@Named
public class StatisticsBacker implements Serializable {

    @Serial
    private static final long serialVersionUID = 4674890962518519299L;

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(StatisticsBacker.class);

    /**
     * The maximum number of entries to be displayed in a leaderboard.
     */
    private static final int LEADERBOARDS_LIMIT = 10;

    /**
     * The statistics service providing logic.
     */
    private final StatisticsService statisticsService;

    /**
     * The topic service providing help.
     */
    private final TopicService topicService;

    /**
     * The current user session.
     */
    private final UserSession userSession;

    /**
     * Reference to the current {@link ExternalContext}.
     */
    private final ExternalContext ectx;

    /**
     * Feedback Event for user feedback.
     */
    private final Event<Feedback> feedbackEvent;

    /**
     * Registry for application dependencies.
     */
    private final Registry registry;

    /**
     * The current report filters.
     */
    private ReportCriteria reportCriteria;

    /**
     * The number of open reports matching the {@link ReportCriteria}.
     */
    private int openReportCount;

    /**
     * The average time a report matching the {@link ReportCriteria} is open.
     */
    private Duration averageTimeOpen;

    /**
     * The average number of posts per report of those matching the {@link ReportCriteria}.
     */
    private BigDecimal averagePostsPerReport;

    /**
     * The top ten reports.
     */
    private List<TopReport> topReports;

    /**
     * The top ten users.
     */
    private List<TopUser> topUsers;

    /**
     * The available topic titles.
     */
    private List<String> topicTitles;

    /**
     * Constructs a new statistics page backing bean with the necessary dependencies.
     *
     * @param statisticsService The statistics service to use.
     * @param topicService      The topic service to use.
     * @param userSession       The currently active user session.
     * @param feedbackEvent     The feedback Event for user feedback.
     * @param registry          The application-wide registry for resources.
     * @param ectx              The current {@link ExternalContext} of the application.
     */
    @Inject
    public StatisticsBacker(final StatisticsService statisticsService,
                            final TopicService topicService,
                            final UserSession userSession,
                            final Event<Feedback> feedbackEvent,
                            final Registry registry,
                            final ExternalContext ectx) {

        this.statisticsService = statisticsService;
        this.topicService = topicService;
        this.userSession = userSession;
        this.feedbackEvent = feedbackEvent;
        this.registry = registry;
        this.ectx = ectx;

        this.reportCriteria = new ReportCriteria("", null, null);
    }

    /**
     * Initializes the statistics page with the data to display.
     *
     * If a topic ID is given as request parameter, the criteria are restricted to that topic.
     */
    @PostConstruct
    public void init() {
        reportCriteria.setTopic(parseTopicParameter());
        topicTitles = topicService.discoverTopics();
        loadStatistics();
    }

    private void loadStatistics() {
        openReportCount = statisticsService.countOpenReports(reportCriteria);
        averageTimeOpen = statisticsService.averageTimeOpen(reportCriteria);
        averagePostsPerReport = statisticsService.averagePostsPerReport(reportCriteria);

        topReports = statisticsService.determineTopReports(LEADERBOARDS_LIMIT);
        topUsers = statisticsService.determineTopUsers(LEADERBOARDS_LIMIT);
    }

    private String parseTopicParameter() {
        String topicId = ectx.getRequestParameterMap().get("t");
        if (topicId != null) {
            try {
                Topic topic = topicService.getTopicByID(Integer.parseInt(topicId));
                if (topic != null) {
                    return topic.getTitle();
                } else {
                    log.warning("Request parameter t=" + topicId + " is not the ID of an existing topic.");
                }
            } catch (NumberFormatException e) {
                log.warning("Request parameter t=" + topicId + " is not an integer.", e);
            }
        }
        return "";
    }

    /**
     * Applies the current filters to the insight figures.
     *
     * @return {@code null} in order to reload the page.
     */
    public String applyFilters() {
        ResourceBundle messagesBundle = registry.getBundle("messages", userSession.getLocale());
        feedbackEvent.fire(new Feedback(messagesBundle.getString("filters_applied"), Feedback.Type.INFO));
        loadStatistics();
        return null;
    }

    /**
     * Returns the number of open reports matching the current filter criteria.
     *
     * @return The total number of reports.
     */
    public int getOpenReportCount() {
        return openReportCount;
    }

    /**
     * Returns the average time a report matching the current filter criteria remains open.
     *
     * @return The average time a report remains open.
     */
    public Duration getAverageTimeOpen() {
        return averageTimeOpen;
    }

    /**
     * Returns the average number of posts per report of those matching the current filter criteria.
     *
     * @return The average number of posts.
     */
    public BigDecimal getAveragePostsPerReport() {
        return averagePostsPerReport;
    }

    /**
     * Returns the ten reports that have gained the most relevance in the last 24 hours system-wide.
     *
     * @return The top ten reports.
     */
    public List<TopReport> getTopReports() {
        return topReports;
    }

    /**
     * Returns the ten users with the most relevance summed up over their created reports, either all-time or for a
     * specific time frame regarding the creation date of the reports.
     *
     * @return The top ten users.
     */
    public List<TopUser> getTopUsers() {
        return topUsers;
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
     * Returns the current report filters.
     *
     * @return The configured report criteria.
     */
    public ReportCriteria getReportCriteria() {
        return reportCriteria;
    }

    /**
     * Sets the current report filters.
     *
     * @param reportCriteria The new report criteria.
     */
    public void setReportCriteria(final ReportCriteria reportCriteria) {
        this.reportCriteria = reportCriteria;
    }

}
