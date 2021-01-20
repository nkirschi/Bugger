package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.ReportCriteria;
import tech.bugger.global.transfer.TopReport;
import tech.bugger.global.transfer.TopUser;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * A search gateway allows to retrieve statistics about application data stored in a persistent storage.
 */
public interface StatisticsGateway {

    /**
     * Retrieves the number of open reports, filtering by the specified {@link ReportCriteria}.
     *
     * @param criteria The criteria reports must fulfill to be taken into consideration.
     * @return The number of open reports in question.
     */
    int getNumberOfOpenReports(ReportCriteria criteria);

    /**
     * Retrieves the average time a report stays open, filtering by the specified {@link ReportCriteria}.
     *
     * @param criteria The criteria reports must fulfill to be taken into consideration.
     * @return The average time the reports in question have been open.
     */
    Duration getAverageTimeToClose(ReportCriteria criteria);

    /**
     * Retrieves the average number of posts per reports, filtering by the specified {@link ReportCriteria}.
     *
     * @param criteria The criteria reports must fulfill to be taken into consideration.
     * @return The average number of posts of the reports in question.
     */
    BigDecimal getAveragePostsPerReport(ReportCriteria criteria);

    /**
     * Retrieves the list of those ten reports that gained the most relevance in the last 24 hours.
     *
     * @param limit The maximum number of top reports to return.
     * @return The {@code limit} top reports sorted by gained relevance descending.
     */
    List<TopReport> getTopReports(int limit);

    /**
     * Retrieves the list of those ten users with the greatest total relevance of the reports created.
     *
     * @param limit The maximum number of top users to return.
     * @return The {@code limit} top users sorted by total received relevance descending.
     */
    List<TopUser> getTopUsers(int limit);

}
