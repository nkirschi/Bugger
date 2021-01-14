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
     * @return The list of the top ten reports of the last 24 hours.
     */
    List<TopReport> getTopTenReports();

    /**
     * Retrieves the list of those ten users with the greatest total relevance of the reports created.
     *
     * @return The top then users.
     */
    List<TopUser> getTopTenUsers();

}
