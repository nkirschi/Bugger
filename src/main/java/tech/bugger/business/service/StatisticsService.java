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
 * Service providing methods related to statistics. A {@code Feedback} event is fired, if unexpected circumstances
 * occur.
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
     * @return The average time a report matching the {@code criteria} remains open.
     */
    public Duration averageTimeOpen(final ReportCriteria criteria) {
        Duration d = null;
        try (Transaction tx = transactionManager.begin()) {
            d = tx.newStatisticsGateway().getAverageTimeToClose(criteria);
            tx.commit();
            return d;
        } catch (TransactionException e) {
            log.error("Error when determining average activity duration of reports.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return d;
    }

    /**
     * Returns the average number of posts per report, filtered by the specified {@link ReportCriteria}.
     *
     * @param criteria The criteria reports must fulfill to be taken into consideration.
     * @return The average number of posts per report of those matching the {@code criteria}.
     */
    public Double averagePostsPerReport(final ReportCriteria criteria) {
        Double avgPostsPerReport = null;
        try (Transaction tx = transactionManager.begin()) {
            avgPostsPerReport = tx.newStatisticsGateway().getAveragePostsPerReport(criteria);
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when determining average posts per report.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return avgPostsPerReport;
    }

    /**
     * Returns the ten users with the most relevance summed up over their created reports, either all-time or for a
     * specific time frame regarding the creation date of the reports.
     *
     * @return The top ten users.
     */
    public List<TopUser> topTenUsers() {
        List<TopUser> topTenUsers = Collections.emptyList();
        try (Transaction tx = transactionManager.begin()) {
            topTenUsers = tx.newStatisticsGateway().getTopTenUsers();
            tx.commit();
        } catch (TransactionException e) {
            log.error("Error when fetching top ten users.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return topTenUsers;
    }

    /**
     * Returns the ten reports that have gained the most relevance in the last 24 hours system-wide.
     *
     * @return The top ten reports.
     */
    public List<TopReport> topTenReports() {
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
