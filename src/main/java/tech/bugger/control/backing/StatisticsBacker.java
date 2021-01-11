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
     * Constant for the number of seconds in a minute. In case someone forgets :)
     */
    private static final double SECONDS_IN_A_MINUTE = 60.0;

    /**
     * Symbol to use when no meaningful value can be displayed.
     */
    private static final String NO_VALUE_INDICATOR = "-";

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
     * Initializes the statistics page. The total number of reports, average number of posts per report and average time
     * a report remains open are shown system-wide by default. If the user navigated to the statistics page from the
     * page of a particular topic, those statistics are restricted to reports in that particular topic instead.
     * Additionally, those statistics are not restricted to a certain time frame by default.
     *
     * Furthermore, a list with the ten reports that have gained the most relevance in the last 24 hours is shown.
     * Another list contains the ten users with the greatest sums of the relevance scores of their reports. By default,
     * that second list is not restricted to a certain time frame for the creation date of the reports.
     */
    @PostConstruct
    public void init() {
        String topicId = ectx.getRequestParameterMap().get("t");
        if (topicId != null) {
            try {
                Topic topic = topicService.getTopicByID(Integer.parseInt(topicId));
                if (topic != null) {
                    reportCriteria.setTopic(topic.getTitle());
                } else {
                    log.warning("Request parameter t=" + topicId + " is not the ID of an existing topic.");
                }
            } catch (NumberFormatException e) {
                log.warning("Request parameter t=" + topicId + " is not an integer.", e);
            }
        }
    }

    /**
     * Applies the current filters to the insight figures.
     *
     * @return {@code null} in order to reload the page.
     */
    public String applyFilters() {
        ResourceBundle messagesBundle = registry.getBundle("messages", userSession);
        feedbackEvent.fire(new Feedback(messagesBundle.getString("filters_applied"), Feedback.Type.INFO));
        return null;
    }

    /**
     * Returns the number of open reports matching the current filter criteria.
     *
     * @return The total number of reports.
     */
    public int getOpenReportCount() {
        log.debug("getOpenReportCount");
        return statisticsService.countOpenReports(reportCriteria);
    }

    /**
     * Returns the average time a report matching the current filter criteria remains open.
     *
     * @return The average time a report remains open.
     */
    public String getAverageTimeOpen() {
        log.debug("getAverageTimeOpen");
        Duration duration = statisticsService.averageTimeOpen(reportCriteria);
        if (duration != null) {
            return String.format(userSession.getLocale(), "%.2f", duration.toMinutes() / SECONDS_IN_A_MINUTE);
        } else {
            return NO_VALUE_INDICATOR;
        }
    }

    /**
     * Returns the average number of posts per report of those matching the current filter criteria.
     *
     * @return The average number of posts.
     */
    public String getAveragePostsPerReport() {
        log.debug("getAveragePostsPerReport");
        Double avgPosts = statisticsService.averagePostsPerReport(reportCriteria);
        if (avgPosts != null) {
            return String.format(userSession.getLocale(), "%.2f", avgPosts);
        } else {
            return NO_VALUE_INDICATOR;
        }
    }

    /**
     * Returns the ten users with the most relevance summed up over their created reports, either all-time or for a
     * specific time frame regarding the creation date of the reports.
     *
     * @return The top ten users.
     */
    public List<TopUser> getTopUsers() {
        log.debug("getTopUsers");
        return statisticsService.topTenUsers();
    }

    /**
     * Returns the ten reports that have gained the most relevance in the last 24 hours system-wide.
     *
     * @return The top ten reports.
     */
    public List<TopReport> getTopReports() {
        log.debug("getTopReports");
        return statisticsService.topTenReports();
    }

    /**
     * Retrieves the titles of all topics in the system.
     *
     * @return A list of all topic titles.
     */
    public List<String> getTopicTitles() {
        log.debug("getTopicTitles");
        List<String> topicTitles = topicService.discoverTopics();
        topicTitles.add(0, ""); // empty string for no restriction to topic (JSF doesn't like null)
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
