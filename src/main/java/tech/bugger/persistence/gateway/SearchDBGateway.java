package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * Search gateway that retrieves search results from data stored in a database.
 */
public class SearchDBGateway implements SearchGateway {

    private static final Log log = Log.forClass(SearchDBGateway.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getUserResults(String query, Selection selection, boolean showAdmins, boolean showNonAdmins) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Topic> getTopicResults(String query, Selection selection) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getReportResults(String query, Selection selection, ZonedDateTime latestOpeningDateTime,
                                         ZonedDateTime earliestClosingDateTime, boolean showOpenReports,
                                         boolean showClosedReports, boolean showDuplicates, Topic topic,
                                         HashMap<Report.Type, Boolean> reportTypeFilter,
                                         HashMap<Report.Severity, Boolean> severityFilter) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Report> getFulltextResults(String query, Selection selection, ZonedDateTime latestOpeningDateTime,
                                           ZonedDateTime earliestClosingDateTime, boolean showOpenReports,
                                           boolean showClosedReports, boolean showDuplicates, Topic topic,
                                           HashMap<Report.Type, Boolean> reportTypeFilter,
                                           HashMap<Report.Severity, Boolean> severityFilter) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfUserResults(String query, boolean showAdmins, boolean showNonAdmins) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfTopicResults(String query) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfReportResults(String query, ZonedDateTime latestOpeningDateTime,
                                        ZonedDateTime earliestClosingDateTime, boolean showOpenReports,
                                        boolean showClosedReports,
                                        boolean showDuplicates, Topic topic,
                                        HashMap<Report.Type, Boolean> reportTypeFilter,
                                        HashMap<Report.Severity, Boolean> severityFilter) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfFulltextResults(String query, ZonedDateTime latestOpeningDateTime,
                                          ZonedDateTime earliestClosingDateTime, boolean showOpenReports,
                                          boolean showClosedReports,
                                          boolean showDuplicates, Topic topic, HashMap<Report.Type, Boolean> reportTypeFilter,
                                          HashMap<Report.Severity, Boolean> severityFilter) {
        // TODO Auto-generated method stub
        return 0;
    }

}
