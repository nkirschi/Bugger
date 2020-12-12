package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * A search gateway allows to retrieve statistics about application data stored in a persistent storage.
 */
public interface StatisticsGateway {

    /**
     * Retrieves the list of those ten users with the greatest total relevance of the reports created. The reports in
     * question can be filtered by specifying the period in which they were open.
     *
     * @param latestOpening   The date and time before which the considered reports have been opened.
     * @param earliestClosing The date and time after which the considered reports that are closed have been closed.
     * @return The top then users.
     */
    public List<User> getTopTenUsers(ZonedDateTime latestOpening, ZonedDateTime earliestClosing);

    /**
     * Retrieves the list of those ten reports that gained the most relevance in the last 24 hours.
     *
     * @return The list of the top ten reports of the last 24 hours.
     */
    public List<Report> getTopTenReports();

    /**
     * Retrieves the average time a report stays open, optionally filtering for a specific topic. The reports in
     * question can be filtered by specifying the period in which they were open.
     *
     * @param topic           The topic the considered reports have to belong to. Can be {@code null} to consider all
     *                        reports.
     * @param latestOpening   The date and time before which the considered reports have been opened.
     * @param earliestClosing The date and time after which the considered reports that are closed have been closed.
     * @return The average time the reports in question have been open.
     * @throws NotFoundException The topic could not be found.
     */
    public Duration getAverageTimeToClose(Topic topic, ZonedDateTime latestOpening, ZonedDateTime earliestClosing) throws NotFoundException;

    /**
     * Retrieves the number of reports that were open in a given period, optionally filtering for a specific topic.
     *
     * @param topic           The topic the considered reports have to belong to. Can be {@code null} to consider all
     *                        reports.
     * @param latestOpening   The date and time before which the considered reports have been opened.
     * @param earliestClosing The date and time after which the considered reports that are closed have been closed.
     * @return The number of open reports in question.
     * @throws NotFoundException The topic could not be found.
     */
    public int getNumberOfOpenReports(Topic topic, ZonedDateTime latestOpening, ZonedDateTime earliestClosing) throws NotFoundException;

    /**
     * The average number of posts for reports that were open in a given period, optionally filtering for a specific
     * topic.
     *
     * @param topic           The topic the considered reports have to belong to. Can be {@code null} to consider all
     *                        reports.
     * @param latestOpening   The date and time before which the considered reports have been opened.
     * @param earliestClosing The date and time after which the considered reports that are closed have been closed.
     * @return The average number of posts of the reports in question.
     * @throws NotFoundException The topic could not be found.
     */
    public double getAveragePostsPerReport(Topic topic, ZonedDateTime latestOpening, ZonedDateTime earliestClosing) throws NotFoundException;

}
