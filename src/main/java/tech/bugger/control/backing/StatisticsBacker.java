package tech.bugger.control.backing;

import tech.bugger.business.service.StatisticsService;
import tech.bugger.business.service.TopicService;
import tech.bugger.global.transfer.ReportCriteria;
import tech.bugger.global.transfer.TopReport;
import tech.bugger.global.transfer.TopUser;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.List;

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
     * The statistics service providing logic.
     */
    private StatisticsService statisticsService;

    /**
     * The topic service providing help.
     */
    private TopicService topicService;

    /**
     * The current report filters.
     */
    private ReportCriteria reportCriteria;

    /**
     * Reference to the current {@link ExternalContext}.
     */
    private ExternalContext ectx;

    /**
     * Constructs a new statistics page backing bean with the necessary dependencies.
     *
     * @param statisticsService The statistics service to use.
     * @param topicService      The topic service to use.
     * @param ectx              The current {@link ExternalContext} of the application.
     */
    @Inject
    public StatisticsBacker(final StatisticsService statisticsService,
                            final TopicService topicService,
                            final ExternalContext ectx) {
        this.statisticsService = statisticsService;
        this.topicService = topicService;
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
     * Returns the number of open reports matching the current filter criteria.
     *
     * @return The total number of reports.
     */
    public int openReportCount() {
        return statisticsService.countOpenReports(reportCriteria);
    }

    /**
     * Returns the average time a report matching the current filter criteria remains open.
     *
     * @return The average time a report remains open.
     */
    public double averageTimeOpen() {
        Duration duration = statisticsService.averageTimeOpen(reportCriteria);
        return duration.toMinutes() / SECONDS_IN_A_MINUTE; // with decimal places
    }

    /**
     * Returns the average number of posts per report of those matching the current filter criteria.
     *
     * @return The average number of posts.
     */
    public double averagePostsPerReport() {
        return statisticsService.averagePostsPerReport(reportCriteria);
    }

    /**
     * Returns the ten users with the most relevance summed up over their created reports, either all-time or for a
     * specific time frame regarding the creation date of the reports.
     *
     * @return The top ten users.
     */
    public List<TopUser> topUsers() {
        return statisticsService.topTenUsers();
    }

    /**
     * Returns the ten reports that have gained the most relevance in the last 24 hours system-wide.
     *
     * @return The top ten reports.
     */
    public List<TopReport> topReports() {
        return statisticsService.topTenReports();
    }

    /**
     * Applies the specified filters and refreshes the displayed data.
     *
     * @return {@code null} in order to stay on the statistics page.
     */
    public String applyFilters() {
        return null;
    }

    /**
     * Retrieves the titles of all topics in the system.
     *
     * @return A list of all topic titles.
     */
    public List<String> getTopicTitles() {
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
