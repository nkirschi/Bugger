package tech.bugger.control.backing;

import tech.bugger.business.service.StatisticsService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
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
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Backing bean for the statistics page.
 */
@ViewScoped
@Named
public class StatisticsBacker implements Serializable {

    private static final Log log = Log.forClass(StatisticsBacker.class);
    private static final long serialVersionUID = 4674890962518519299L;

    private int topicID;
    private Topic topic;
    private ZonedDateTime latestCreationDateAverageTimeToClose;
    private ZonedDateTime earliestClosingDateAverageTimeToClose;
    private ZonedDateTime earliestCreationDateTopTenUsers;
    private ZonedDateTime latestDateTopTenUsers;

    @Inject
    private transient StatisticsService statisticsService;

    @Inject
    private FacesContext fctx;

    @Inject
    private transient TopicService topicService;

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
    }

    /**
     * Creates a FacesMessage to display if an event is fired in one of the injected services.
     *
     * @param feedback The feedback with details on what to display.
     */
    public void displayFeedback(@Observes @Any Feedback feedback) {
    }

    /**
     * Returns the total number of reports, either system-wide or for a specific topic.
     *
     * @return The total number of reports.
     */
    public int totalReportCount() {
        return 0;
    }

    /**
     * Returns the average number of posts per report, either system-wide or for the reports of a specific topic.
     *
     * @return The average number of posts.
     */
    public double averagePostsPerReport() {
        return 0.0;
    }

    /**
     * Returns the average time a report remains open, either system-wide or for the reports of a specific topic.
     *
     * @return The average time a report remains open.
     */
    public Duration averageTimeOpen() {
        return null;
    }

    /**
     * Returns the ten users with the most relevance summed up over their created reports, either all-time or for a
     * specific time frame regarding the creation date of the reports.
     *
     * @return The top ten users.
     */
    public List<User> topUsers() {
        return null;
    }

    /**
     * Returns the ten reports that have gained the most relevance in the last 24 hours system-wide.
     *
     * @return The top ten reports.
     */
    public List<Report> topReports() {
        return null;
    }

    /**
     * Gets all topics and adds a {@code null} element for selecting filters.
     *
     * @return A list containing all topics. One entry is {@code null}.
     */
    public List<Topic> getTopics() {
        return null;
    }

    /**
     * Applies the specified filters and refreshes the displayed data.
     */
    public void applyFilters() {
        // get statistics again
    }

    /**
     * Returns the sum of the relevance scores of all reports one particular user has created.
     *
     * @param user The user in question.
     * @return The total relevance as an {@code int}.
     */
    public int getTotalRelevanceForUser(User user) {
        return 0;
    }

    /**
     * @return The topic.
     */
    public Topic getTopic() {
        return topic;
    }

    /**
     * @param topic The topic to set.
     */
    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    /**
     * @return The latestCreationDateAverageTimeToClose.
     */
    public ZonedDateTime getLatestCreationDateAverageTimeToClose() {
        return latestCreationDateAverageTimeToClose;
    }

    /**
     * @param latestCreationDateAverageTimeToClose The latestCreationDateAverageTimeToClose to set.
     */
    public void setLatestCreationDateAverageTimeToClose(ZonedDateTime latestCreationDateAverageTimeToClose) {
        this.latestCreationDateAverageTimeToClose = latestCreationDateAverageTimeToClose;
    }

    /**
     * @return The earliestClosingDateAverageTimeToClose.
     */
    public ZonedDateTime getEarliestClosingDateAverageTimeToClose() {
        return earliestClosingDateAverageTimeToClose;
    }

    /**
     * @param earliestClosingDateAverageTimeToClose The earliestClosingDateAverageTimeToClose to set.
     */
    public void setEarliestClosingDateAverageTimeToClose(ZonedDateTime earliestClosingDateAverageTimeToClose) {
        this.earliestClosingDateAverageTimeToClose = earliestClosingDateAverageTimeToClose;
    }

    /**
     * @return The earliestCreationDateTopTenUsers.
     */
    public ZonedDateTime getEarliestCreationDateTopTenUsers() {
        return earliestCreationDateTopTenUsers;
    }

    /**
     * @param earliestCreationDateTopTenUsers The earliestCreationDateTopTenUsers to set.
     */
    public void setEarliestCreationDateTopTenUsers(ZonedDateTime earliestCreationDateTopTenUsers) {
        this.earliestCreationDateTopTenUsers = earliestCreationDateTopTenUsers;
    }

    /**
     * @return The latestDateTopTenUsers.
     */
    public ZonedDateTime getLatestDateTopTenUsers() {
        return latestDateTopTenUsers;
    }

    /**
     * @param latestDateTopTenUsers The latestDateTopTenUsers to set.
     */
    public void setLatestDateTopTenUsers(ZonedDateTime latestDateTopTenUsers) {
        this.latestDateTopTenUsers = latestDateTopTenUsers;
    }
}