package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.ReportCriteria;
import tech.bugger.global.transfer.TopReport;
import tech.bugger.global.transfer.TopUser;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Service for content statistics. A {@code Feedback} event is fired if unexpected circumstances occur.
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
     * Feedback Event for user feedback.
     */
    private final Event<Feedback> feedbackEvent;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new statistics service with the given dependencies.
     *
     * @param transactionManager The transaction manager to use for creating transactions.
     * @param feedbackEvent      The feedback event to use for user feedback.
     * @param messagesBundle     The resource bundle for feedback messages.
     */
    @Inject
    public StatisticsService(final TransactionManager transactionManager,
                             final Event<Feedback> feedbackEvent,
                             final @RegistryKey("messages") ResourceBundle messagesBundle) {
        this.transactionManager = transactionManager;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
    }

    /**
     * Returns the number of open reports, filtered by the specified {@link ReportCriteria}.
     *
     * @param criteria criteria The criteria reports must fulfill to be taken into consideration.
     * @return The total number of open reports matching the {@code criteria}.
     */
    public int countOpenReports(final ReportCriteria criteria) {
        int openReports = 0;
        try (Transaction tx = transactionManager.begin()) {
            openReports = tx.newStatisticsGateway().getNumberOfOpenReports(criteria);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when counting open reports.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return openReports;
    }

    /**
     * Returns the average time a report remains open, filtered by the specified {@link ReportCriteria}.
     *
     * @param criteria The criteria reports must fulfill to be taken into consideration.
     * @return The average time a report matching the {@code criteria} remains open as {@link Duration} or {@code null}
     *         iff this time span could not be determined.
     */
    public Duration averageTimeOpen(final ReportCriteria criteria) {
        try (Transaction tx = transactionManager.begin()) {
            Duration averageTimeOpen = tx.newStatisticsGateway().getAverageTimeToClose(criteria);
            tx.commit();
            return averageTimeOpen;
        } catch (TransactionException e) {
            log.error("Error when determining average activity duration of reports.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return null;
    }

    /**
     * Returns the average number of posts per report, filtered by the specified {@link ReportCriteria}.
     *
     * @param criteria The criteria reports must fulfill to be taken into consideration.
     * @return The average number of posts per report of those matching the {@code criteria} or {@code null} if this
     *         average could not be determined.
     */
    public Double averagePostsPerReport(final ReportCriteria criteria) {
        try (Transaction tx = transactionManager.begin()) {
            Double avgPostsPerReport = tx.newStatisticsGateway().getAveragePostsPerReport(criteria);
            tx.commit();
            return avgPostsPerReport;
        } catch (TransactionException e) {
            log.error("Error when determining average posts per report.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return null;
    }

    /**
     * Returns the ten users with the most relevance summed up over their created reports.
     *
     * @return The top ten users.
     */
    public List<TopUser> determineTopTenUsers() {
        try (Transaction tx = transactionManager.begin()) {
            List<TopUser> topTenUsers = tx.newStatisticsGateway().getTopTenUsers();
            tx.commit();
            return topTenUsers;
        } catch (TransactionException e) {
            log.error("Error when fetching top ten users.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return Collections.emptyList();
    }

    /**
     * Returns the ten reports that have gained the most relevance in the last 24 hours system-wide.
     *
     * @return The top ten reports.
     */
    public List<TopReport> determineTopTenReports() {
        List<TopReport> topTenReports = Collections.emptyList();
        try (Transaction tx = transactionManager.begin()) {
            topTenReports = tx.newStatisticsGateway().getTopTenReports();
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when fetching top ten reports.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return topTenReports;
    }

}
