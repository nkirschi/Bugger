package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Service providing methods related to statistics. A {@code Feedback} event is fired, if unexpected circumstances
 * occur.
 */
@Dependent
public class StatisticsService {

    private static final Log log = Log.forClass(StatisticsService.class);

    @Inject
    @Any
    Event<Feedback> feedback;

    /**
     * Returns the total number of reports, either system-wide or for a specific topic.
     *
     * @param topic                   Only reports that belong to this topic are taken into account. Passing
     *                                {@code null} includes reports regardless of their topic.
     * @param latestCreationDateTime  Only reports created before this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were created.
     * @param earliestClosingDateTime Only reports closed after this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were closed. Reports still open are never
     *                                excluded via this filter.
     * @return The total number of reports.
     */
    public int totalReportCount(Topic topic, ZonedDateTime latestCreationDateTime,
                                ZonedDateTime earliestClosingDateTime) {

        return 0;
    }

    /**
     * Returns the average number of posts per report, either system-wide or for the reports of a specific topic.
     *
     * @param topic                   Only reports belonging to this topic are taken into account. Passing {@code null}
     *                                includes reports regardless of which topic they belong to.
     * @param latestCreationDateTime  Only reports created before this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were created.
     * @param earliestClosingDateTime Only reports closed after this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were closed. Reports still open are never
     *                                excluded via this filter.
     * @return The average number of posts.
     */
    public int averagePostsPerReport(Topic topic, ZonedDateTime latestCreationDateTime,
                                     ZonedDateTime earliestClosingDateTime) {
        return 0;
    }

    /**
     * Returns the average time a report remains open, either system-wide or for the reports of a specific topic.
     *
     * @param topic                   Only reports belonging to this topic are taken into account. Passing {@code null}
     *                                includes reports regardless of which topic they belong to.
     * @param latestCreationDateTime  Only reports created before this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were created.
     * @param earliestClosingDateTime Only reports closed after this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were closed. Reports still open are never
     *                                excluded via this filter.
     * @return The average time a report remains open.
     */
    public int averageTimeOpen(Topic topic, ZonedDateTime latestCreationDateTime,
                               ZonedDateTime earliestClosingDateTime) {
        return 0;
    }

    /**
     * Returns the ten users with the most relevance summed up over their created reports, either all-time or for a
     * specific time frame regarding the creation date of the reports.
     *
     * @param latestCreationDateTime  Only reports created before this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were created.
     * @param earliestClosingDateTime Only reports closed after this date are taken into account. Passing {@code null}
     *                                includes reports regardless of when they were closed. Reports still open are never
     *                                excluded via this filter.
     * @return The top ten users.
     */
    public List<User> topTenUsers(ZonedDateTime latestCreationDateTime, ZonedDateTime earliestClosingDateTime) {
        return null;
    }

    /**
     * Returns the ten reports that have gained the most relevance in the last 24 hours system-wide.
     *
     * @return The top ten reports.
     */
    public List<Report> topTenReports() {
        return null;
    }
}
