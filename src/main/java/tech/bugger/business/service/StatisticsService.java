package tech.bugger.business.service;

import tech.bugger.business.exception.DataAccessException;
import tech.bugger.global.transfer.ReportCriteria;
import tech.bugger.global.transfer.TopReport;
import tech.bugger.global.transfer.TopUser;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * Service for content statistics.
 */
@ApplicationScoped
public class StatisticsService {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(StatisticsService.class);

    /**
     * Transaction manager used for creating transactions.
     */
    private final TransactionManager transactionManager;

    /**
     * Constructs a new statistics service with the given dependencies.
     *
     * @param transactionManager The transaction manager to use for creating transactions.
     */
    @Inject
    public StatisticsService(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Returns the number of open reports, filtered by the specified {@link ReportCriteria}.
     *
     * @param criteria criteria The criteria reports must fulfill to be taken into consideration.
     * @return The total number of open reports matching the {@code criteria}.
     * @throws DataAccessException if this amount could not be determined.
     */
    public int countOpenReports(final ReportCriteria criteria) throws DataAccessException {
        try (Transaction tx = transactionManager.begin()) {
            int openReports;
            openReports = tx.newStatisticsGateway().getNumberOfOpenReports(criteria);
            tx.commit();
            return openReports;
        } catch (TransactionException e) {
            log.error("Error when counting open reports.", e);
            throw new DataAccessException("Error when counting open reports.", e);
        }
    }

    /**
     * Returns the average time a report remains open, filtered by the specified {@link ReportCriteria}.
     *
     * @param criteria The criteria reports must fulfill to be taken into consideration.
     * @return The average time a report matching the {@code criteria} remains open as {@link Duration} or {@code null}
     *         iff this value is not well-defined.
     * @throws DataAccessException if this time span could not be determined.
     */
    public Duration averageTimeOpen(final ReportCriteria criteria) throws DataAccessException {
        try (Transaction tx = transactionManager.begin()) {
            Duration averageTimeOpen = tx.newStatisticsGateway().getAverageTimeToClose(criteria);
            tx.commit();
            return averageTimeOpen;
        } catch (TransactionException e) {
            log.error("Error when determining average activity duration of reports.", e);
            throw new DataAccessException("Error when determining average activity duration of reports.", e);
        }
    }

    /**
     * Returns the average number of posts per report, filtered by the specified {@link ReportCriteria}.
     *
     * @param criteria The criteria reports must fulfill to be taken into consideration.
     * @return The average number of posts per report of those matching the {@code criteria} or {@code null} iff this
     *         value is not well-defined.
     * @throws DataAccessException if this average could not be determined.
     */
    public BigDecimal averagePostsPerReport(final ReportCriteria criteria) throws DataAccessException {
        try (Transaction tx = transactionManager.begin()) {
            BigDecimal avgPostsPerReport = tx.newStatisticsGateway().getAveragePostsPerReport(criteria);
            tx.commit();
            return avgPostsPerReport;
        } catch (TransactionException e) {
            log.error("Error when determining average posts per report.", e);
            throw new DataAccessException("Error when determining average posts per report.", e);
        }
    }

    /**
     * Returns the users with the most relevance summed up over their created reports.
     *
     * @param limit The maximum number of top reports to return.
     * @return The {@code limit} top users sorted by total received relevance descending.
     * @throws DataAccessException if this top list could not be determined.
     */
    public List<TopUser> determineTopUsers(final int limit) throws DataAccessException {
        try (Transaction tx = transactionManager.begin()) {
            List<TopUser> topTenUsers = tx.newStatisticsGateway().getTopUsers(limit);
            tx.commit();
            return topTenUsers;
        } catch (TransactionException e) {
            log.error("Error when fetching top ten users.", e);
            throw new DataAccessException("Error when fetching top ten users.", e);
        }
    }

    /**
     * Returns the reports that have gained the most relevance in the last 24 hours system-wide.
     *
     * @param limit The maximum number of top users to return.
     * @return The {@code limit} top reports sorted by relevance gain descending.
     * @throws DataAccessException if this top list could not be determined.
     */
    public List<TopReport> determineTopReports(final int limit) throws DataAccessException {
        try (Transaction tx = transactionManager.begin()) {
            List<TopReport> topTenReports = tx.newStatisticsGateway().getTopReports(limit);
            tx.commit();
            return topTenReports;
        } catch (TransactionException e) {
            log.error("Error when fetching top ten reports.", e);
            throw new DataAccessException("Error when fetching top ten reports.", e);
        }
    }

}
