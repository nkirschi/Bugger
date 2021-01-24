package tech.bugger.persistence.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.DuplicateException;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.SelfReferenceException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doReturn;

@ExtendWith(DBExtension.class)
@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReportDBGatewayTest {

    private ReportDBGateway gateway;

    private SubscriptionGateway subscriptionGateway;

    private Connection connection;

    private Report report;

    private Topic topic;

    private Selection selection;

    private final User creator = new User();
    private final User modifier = new User();

    @Mock
    private UserDBGateway userGateway;

    @BeforeEach
    public void setUp() throws Exception {
        DBExtension.insertMinimalTestData();
        connection = DBExtension.getConnection();
        gateway = new ReportDBGateway(connection, userGateway);
        subscriptionGateway = new SubscriptionDBGateway(connection);

        topic = new Topic(1, "topictitle", "topicdescription");
        Authorship authorship = new Authorship(new User(), OffsetDateTime.now(), new User(), OffsetDateTime.now());
        authorship.getCreator().setId(1);
        authorship.getModifier().setId(1);
        report = new Report(0, "App crashes", Report.Type.HINT, Report.Severity.SEVERE, "1.4.1",
                new Authorship(null, OffsetDateTime.now(), null, OffsetDateTime.now()), null,
                null, null, false, 1);
        selection = new Selection(3, 0, Selection.PageSize.LARGE, "ID", true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    public void insertReports() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO topic (title, description) VALUES ('topic1', 'description1');");
            stmt.execute("INSERT INTO report (title, type, severity, topic) VALUES ('HI', 'BUG', 'MINOR', 1);");
            stmt.execute("INSERT INTO report (title, type, severity, topic, duplicate_of) VALUES ('HI', 'BUG', 'MINOR', 1, 100);");
        }
    }

    public void insertPosts(int reportID, int numberOfPosts) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DO\n" +
                    "$$\n" +
                    "BEGIN\n" +
                    "FOR i IN 1.." + numberOfPosts + " LOOP\n" +
                    "    INSERT INTO post (content, report) VALUES\n" +
                    "        (CONCAT('testpost', CURRVAL('post_id_seq'))," + reportID + ");\n" +
                    "END LOOP;\n" +
                    "END;\n" +
                    "$$\n" +
                    ";\n");
        }
    }

    public boolean isGone(int reportID) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM report WHERE id = " + reportID);
            return (!rs.next());
        }
    }

    @Test
    public void testFind() throws Exception {
        Report report = gateway.find(100);

        // Check if report is equal to report from minimal test data.
        assertAll(() -> assertEquals(100, report.getId()),
                () -> assertEquals("testreport", report.getTitle()),
                () -> assertEquals(Report.Type.BUG, report.getType()),
                () -> assertEquals(Report.Severity.MINOR, report.getSeverity()),
                () -> assertEquals("testversion", report.getVersion()));
    }

    @Test
    public void testFindOverwrittenRelevance() throws NotFoundException {
        gateway.create(report);
        gateway.overwriteRelevance(report, 100);
        Report finReport = gateway.find(report.getId());
        assertEquals(100, finReport.getRelevance());
    }

    @Test
    public void testFindWhenNotExists() {
        assertThrows(NotFoundException.class, () -> gateway.find(42));
    }

    @Test
    public void testFindWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new ReportDBGateway(connectionSpy, userGateway).find(1));
    }

    @Test
    public void testUpdate() throws Exception {
        report.setId(100);
        report.setTopicID(topic.getId());
        gateway.update(report);

        Report reportFromDatabase = gateway.find(100);
        assertAll(() -> assertEquals(report.getId(), reportFromDatabase.getId()),
                () -> assertEquals(report.getTitle(), reportFromDatabase.getTitle()),
                () -> assertEquals(report.getType(), reportFromDatabase.getType()),
                () -> assertEquals(report.getSeverity(), reportFromDatabase.getSeverity()),
                () -> assertEquals(report.getVersion(), reportFromDatabase.getVersion()));
    }

    @Test
    public void testUpdateModifier() throws NotFoundException {
        modifier.setId(1);
        Authorship authorship = report.getAuthorship();
        authorship.setModifier(modifier);
        report.setId(100);
        report.setTopicID(topic.getId());
        doReturn(modifier).when(userGateway).getUserByID(1);
        gateway.update(report);
        assertEquals(modifier, gateway.find(100).getAuthorship().getModifier());
    }

    @Test
    public void testUpdateWhenNotExists() {
        report.setId(42);
        assertThrows(NotFoundException.class, () -> gateway.update(report));
    }

    @Test
    public void testUpdateWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new ReportDBGateway(connectionSpy, userGateway).update(report));
    }

    private Report find(int id) throws Exception {
        // necessary until ReportDBGateway#find is implemented
        PreparedStatement stmt = connection.prepareStatement("SELECT * FROM report WHERE id = ?;");
        ResultSet rs = new StatementParametrizer(stmt).integer(id).toStatement().executeQuery();

        if (rs.next()) {
            return new Report(
                    rs.getInt("id"), rs.getString("title"),
                    null, null, null, null, null, null, null, false, 0
            );
        } else {
            return null;
        }
    }

    @Test
    public void testCreate() throws Exception {
        report.setTopicID(topic.getId());
        gateway.create(report);
        Report created = find(report.getId());
        assertEquals(report.getId(), created.getId());
    }

    @Test
    public void testCreateCreatorAndModifier() throws Exception {
        Authorship authorship = report.getAuthorship();
        creator.setId(1);
        authorship.setCreator(creator);
        authorship.setModifier(creator);
        report.setAuthorship(authorship);
        report.setTopicID(topic.getId());
        gateway.create(report);
        doReturn(creator).when(userGateway).getUserByID(anyInt());
        Report created = gateway.find(report.getId());
        assertAll(
                () -> assertEquals(creator, created.getAuthorship().getCreator()),
                () -> assertEquals(creator, created.getAuthorship().getModifier())
        );
    }

    @Test
    public void testCreateNoKeysGenerated() throws Exception {
        Connection connectionSpy = spy(connection);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        ResultSet rsMock = mock(ResultSet.class);
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any(), anyInt());
        when(stmtMock.getGeneratedKeys()).thenReturn(rsMock);
        when(rsMock.next()).thenReturn(false);
        assertThrows(StoreException.class, () -> new ReportDBGateway(connectionSpy, userGateway).create(report));
    }

    @Test
    public void testCreateWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any(), anyInt());
        assertThrows(StoreException.class, () -> new ReportDBGateway(connectionSpy, userGateway).create(report));
    }

    @Test
    public void testCountPostsWhenReportIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.countPosts(null));
    }

    @Test
    public void testCountPostsWhenReportIDIsNull() {
        report.setId(null);
        assertThrows(IllegalArgumentException.class, () -> gateway.countPosts(report));
    }

    @Test
    public void testCountPostsWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        report.setId(100);
        assertThrows(StoreException.class, () -> new ReportDBGateway(connectionSpy, userGateway).countPosts(report));
    }

    @Test
    public void testCountPostsWhenReportDoesNotExist() {
        report.setId(93);
        assertEquals(0, gateway.countPosts(report));
    }

    @Test
    public void testCountPostsWhenThereAreNone() throws Exception {
        DBExtension.emptyDatabase();
        insertReports();
        report.setId(100);
        assertEquals(0, gateway.countPosts(report));
    }

    @Test
    public void testCountPostsWhenThereAreSome() throws Exception {
        DBExtension.emptyDatabase();
        insertReports();
        insertPosts(100, 34);
        report.setId(100);
        assertEquals(34, gateway.countPosts(report));
    }

    @Test
    public void testCountPostsNoResult() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        assertEquals(0, new ReportDBGateway(connectionSpy, userGateway).countPosts(report));
    }

    @Test
    public void testDeleteReportWhenReportIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.delete(null));
    }

    @Test
    public void testDeleteReportWhenReportIDIsNull() {
        report.setId(null);
        assertThrows(IllegalArgumentException.class, () -> gateway.delete(report));
    }

    @Test
    public void testDeleteReportWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        report.setId(100);
        assertThrows(StoreException.class, () -> new ReportDBGateway(connectionSpy, userGateway).delete(report));
    }

    @Test
    public void testDeleteReportWhenReportDoesNotExist() {
        report.setId(11);
        assertThrows(NotFoundException.class, () -> gateway.delete(report));
    }

    @Test
    public void testDeleteReport() throws Exception {
        insertReports();
        report.setId(100);
        gateway.delete(report);
        assertTrue(isGone(100));
    }

    @Test
    public void testDeleteReportTwice() throws Exception {
        insertReports();
        report.setId(100);
        gateway.delete(report);
        assertThrows(NotFoundException.class, () -> gateway.delete(report));
    }

    @Test
    public void testDeleteReportInternalError() throws SQLException {
        report.setId(100);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        doReturn(true).when(rs).next();
        doReturn(1).when(rs).getInt("id");
        assertThrows(InternalError.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).delete(report)
        );
    }

    @Test
    public void testCountDuplicatesWhenReportIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.countDuplicates(null));
    }

    @Test
    public void testCountDuplicatesWhenReportIDIsNull() {
        report.setId(null);
        assertThrows(IllegalArgumentException.class, () -> gateway.countDuplicates(report));
    }

    @Test
    public void testCountDuplicatesWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        report.setId(100);
        assertThrows(StoreException.class, () -> new ReportDBGateway(connectionSpy, userGateway).countDuplicates(report));
    }

    @Test
    public void testCountDuplicatesWhenReportDoesNotExist() throws Exception {
        report.setId(93);
        assertEquals(0, gateway.countDuplicates(report));
    }

    @Test
    public void testCountDuplicatesWhenThereAreNone() throws Exception {
        DBExtension.emptyDatabase();
        insertReports();
        report.setId(101);
        assertEquals(0, gateway.countDuplicates(report));
    }

    @Test
    public void testCountDuplicatesWhenCountNotFound() throws Exception {
        ResultSet resultSetMock = mock(ResultSet.class);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(false).when(resultSetMock).next();
        doReturn(resultSetMock).when(stmtMock).executeQuery();
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any());
        report.setId(93);
        assertThrows(NotFoundException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).countDuplicates(report));
        reset(connectionSpy, stmtMock);
    }

    @Test
    public void testSelectDuplicatesWhenThereAreNone() {
        report.setId(93);
        assertEquals(List.of(), gateway.selectDuplicates(report, selection));
    }

    @Test
    public void testSelectDuplicatesWhenThereAreSome() throws Exception {
        DBExtension.emptyDatabase();
        insertReports();
        Report original = new Report();
        original.setId(100);
        report.setId(101);
        selection.setAscending(false);
        assertEquals(List.of(report), gateway.selectDuplicates(original, selection));
    }

    @Test
    public void testSelectDuplicatesWhenCountNotFound() throws Exception {
        try (MockedStatic<ReportDBGateway> gatewayMock = mockStatic(ReportDBGateway.class)) {
            gatewayMock.when(() -> ReportDBGateway.getReportFromResultSet(any(), any()))
                    .thenThrow(NotFoundException.class);

            ResultSet resultSetMock = mock(ResultSet.class);
            PreparedStatement stmtMock = mock(PreparedStatement.class);
            Connection connectionSpy = spy(connection);
            doReturn(true).when(resultSetMock).next();
            doReturn(resultSetMock).when(stmtMock).executeQuery();
            doReturn(stmtMock).when(connectionSpy).prepareStatement(any());

            report.setId(93);

            assertThrows(StoreException.class,
                    () -> new ReportDBGateway(connectionSpy, userGateway).selectDuplicates(report, selection));
            reset(connectionSpy, stmtMock);
        }
    }

    @Test
    public void testUnmarkDuplicateNullReport() {
        assertThrows(IllegalArgumentException.class, () -> gateway.unmarkDuplicate(null));
    }

    @Test
    public void testUnmarkDuplicateNullReportID() {
        report.setId(null);
        assertThrows(IllegalArgumentException.class, () -> gateway.unmarkDuplicate(report));
    }

    @Test
    public void testUnmarkDuplicateNoRowsAffected() throws Exception {
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(0).when(stmtMock).executeUpdate();
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any());

        report.setId(100);
        assertThrows(NotFoundException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).unmarkDuplicate(report));
    }

    @Test
    public void testUnmarkDuplicateWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        report.setId(100);
        assertThrows(StoreException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).unmarkDuplicate(report));
    }

    @Test
    public void testUnmarkDuplicateSuccess() throws Exception {
        DBExtension.emptyDatabase();
        insertReports();
        report.setId(101);
        assertDoesNotThrow(() -> gateway.unmarkDuplicate(report));
        assertNull(report.getDuplicateOf());
    }

    @Test
    public void testMarkDuplicateNullReport() {
        assertThrows(IllegalArgumentException.class, () -> gateway.markDuplicate(null, 100));
    }

    @Test
    public void testMarkDuplicateNullReportID() {
        report.setId(null);
        assertThrows(IllegalArgumentException.class, () -> gateway.markDuplicate(report, 100));
    }

    @Test
    public void testMarkDuplicateSelfReference() {
        report.setId(100);
        assertThrows(SelfReferenceException.class, () -> gateway.markDuplicate(report, 100));
    }

    @Test
    public void testMarkDuplicateNoRowsAffected() throws Exception {
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(0).when(stmtMock).executeUpdate();
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any());

        report.setId(101);
        assertThrows(NotFoundException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).markDuplicate(report, 100));
    }

    @Test
    public void testMarkDuplicateWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        report.setId(101);
        assertThrows(StoreException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).markDuplicate(report, 100));
    }

    @Test
    public void testMarkDuplicateNotFound() throws Exception {
        DBExtension.emptyDatabase();
        insertReports();
        report.setId(101);
        assertThrows(NotFoundException.class, () -> gateway.markDuplicate(report, 93));
    }

    @Test
    public void testMarkDuplicateSuccess() throws Exception {
        DBExtension.emptyDatabase();
        insertReports();
        report.setId(101);
        assertDoesNotThrow(() -> gateway.markDuplicate(report, 100));
        assertEquals(100, report.getDuplicateOf());
    }

    @Test
    public void testGetSelectedReports() throws NotFoundException {
        Report firstReport = gateway.find(100);
        gateway.overwriteRelevance(firstReport, 100);
        List<Report> reports = gateway.getSelectedReports(topic, selection, true, true);
        assertAll(
                () -> assertEquals(3, reports.size()),
                () -> assertEquals(100, reports.get(0).getId()),
                () -> assertEquals(100, reports.get(0).getRelevance())
        );
    }

    @Test
    public void testGetSelectedReportsOpen() {
        selection.setAscending(false);
        List<Report> reports = gateway.getSelectedReports(topic, selection, true, false);
        assertAll(
                () -> assertEquals(1, reports.size()),
                () -> assertEquals("testreport", reports.get(0).getTitle())
        );
    }

    @Test
    public void testGetSelectedReportsClosed() {
        selection.setSortedBy("relevance");
        List<Report> reports = gateway.getSelectedReports(topic, selection, false, true);
        assertAll(
                () -> assertEquals(2, reports.size()),
                () -> assertNotEquals("testreport", reports.get(0).getTitle()),
                () -> assertNotEquals("testreport", reports.get(1).getTitle())
        );
    }

    @Test
    public void testGetSelectedReportsNoReports() {
        topic.setId(100);
        assertEquals(0, gateway.getSelectedReports(topic, selection, true, true).size());
    }

    @Test
    public void testGetSelectedReportsSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).getSelectedReports(topic, selection, true, true)
        );
    }

    @Test
    public void testGetSelectedReportsEmpty() {
        assertTrue(gateway.getSelectedReports(topic, selection, false, false).isEmpty());
    }

    @Test
    public void testCountDuplicates() throws NotFoundException, SelfReferenceException {
        report.setId(100);
        gateway.markDuplicate(report, 101);
        report.setId(102);
        gateway.markDuplicate(report, 101);
        report.setId(101);
        assertEquals(2, gateway.countDuplicates(report));
    }

    @Test
    public void testCountDuplicatesNone() throws NotFoundException {
        assertEquals(0, gateway.countDuplicates(report));
    }

    @Test
    public void testCountDuplicatesNotFound() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        assertThrows(NotFoundException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).countDuplicates(report)
        );
    }

    @Test
    public void testCountDuplicatesReportNull() {
        assertThrows(IllegalArgumentException.class,
                () -> gateway.countDuplicates(null)
        );
    }

    @Test
    public void testCountDuplicatesReportIdNull() {
        report.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.countDuplicates(report)
        );
    }

    @Test
    public void testOverwriteRelevanceNotFound() throws SQLException {
        report.setId(100);
        PreparedStatement stmt = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(0).when(stmt).executeUpdate();
        assertThrows(StoreException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).overwriteRelevance(report, 100)
        );
    }

    @Test
    public void testGetVote() {
        report.setId(100);
        creator.setId(1);
        assertEquals(2, gateway.getVote(creator, report));
    }

    @Test
    public void testGetVoteNone() {
        creator.setId(100);
        report.setId(100);
        assertNull(gateway.getVote(creator, report));
    }

    @Test
    public void testGetVoteSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).getVote(creator, report)
        );
    }

    @Test
    public void testAddVote() throws DuplicateException {
        creator.setId(2);
        report.setId(100);
        gateway.addVote(report, creator, 100);
        assertEquals(100, gateway.getVote(creator, report));
    }

    @Test
    public void testAddVoteAlreadyVoted() {
        creator.setId(1);
        report.setId(100);
        assertThrows(DuplicateException.class, () -> gateway.addVote(report, creator, 5));
    }

    @Test
    public void testAddVoteStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).addVote(report, creator, 100)
        );
    }

    @Test
    public void testRemoveVote() {
        creator.setId(1);
        report.setId(100);
        gateway.removeVote(report, creator);
        assertNull(gateway.getVote(creator, report));
    }

    @Test
    public void testRemoveVoteStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).removeVote(report, creator)
        );
    }

    @Test
    public void testRemoveVoteReportNull() {
        assertThrows(IllegalArgumentException.class,
                () -> gateway.removeVote(null, creator)
        );
    }

    @Test
    public void testRemoveVoteReportIdNull() {
        report.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.removeVote(report, creator)
        );
    }

    @Test
    public void testFindReportOfPost() throws NotFoundException {
        assertEquals(100, gateway.findReportOfPost(100));
    }

    @Test
    public void testFindReportOfPostNotFound() {
        assertThrows(NotFoundException.class,
                () -> gateway.findReportOfPost(10000)
        );
    }

    @Test
    public void testFindReportOfPostStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).findReportOfPost(100)
        );
    }

    @Test
    public void testSelectSubscribedReports() throws NotFoundException, DuplicateException {
        creator.setId(1);
        Report firstReport = gateway.find(100);
        Report secondReport = gateway.find(101);
        subscriptionGateway.subscribe(firstReport, creator);
        subscriptionGateway.subscribe(secondReport, creator);
        List<Report> reports = gateway.selectSubscribedReports(creator, selection);
        assertAll(
                () -> assertTrue(reports.contains(firstReport)),
                () -> assertTrue(reports.contains(secondReport))
        );
    }

    @Test
    public void testSelectSubscribedReportsSelectionDesc() throws NotFoundException, DuplicateException {
        selection.setAscending(false);
        creator.setId(1);
        Report firstReport = gateway.find(100);
        Report secondReport = gateway.find(101);
        subscriptionGateway.subscribe(firstReport, creator);
        subscriptionGateway.subscribe(secondReport, creator);
        List<Report> reports = gateway.selectSubscribedReports(creator, selection);
        assertAll(
                () -> assertTrue(reports.contains(firstReport)),
                () -> assertTrue(reports.contains(secondReport))
        );
    }

    @Test
    public void testSelectSubscribedReportsInternalError() throws NotFoundException, DuplicateException {
        creator.setId(1);
        Report firstReport = gateway.find(100);
        subscriptionGateway.subscribe(firstReport, creator);
        doThrow(NotFoundException.class).when(userGateway).getUserByID(anyInt());
        assertThrows(InternalError.class,
                () -> gateway.selectSubscribedReports(creator, selection)
        );
    }

    @Test
    public void testSelectSubscribedReportsStoreException() throws SQLException {
        creator.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).selectSubscribedReports(creator, selection)
        );
    }

    @Test
    public void testSelectSubscribedReportsSelectionNull() {
        creator.setId(1);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.selectSubscribedReports(creator, null)
        );
    }

    @Test
    public void testSelectSubscribedReportsSelectionSortedByBlank() {
        creator.setId(1);
        selection.setSortedBy("");
        assertThrows(IllegalArgumentException.class,
                () -> gateway.selectSubscribedReports(creator, selection)
        );
    }

    @Test
    public void testSelectSubscribedReportsUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> gateway.selectSubscribedReports(null, selection)
        );
    }

    @Test
    public void testSelectSubscribedReportsUserIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> gateway.selectSubscribedReports(creator, selection)
        );
    }

    @Test
    public void testCountSubscribedReports() throws NotFoundException, DuplicateException {
        creator.setId(1);
        Report firstReport = gateway.find(100);
        Report secondReport = gateway.find(101);
        subscriptionGateway.subscribe(firstReport, creator);
        subscriptionGateway.subscribe(secondReport, creator);
        assertEquals(2, gateway.countSubscribedReports(creator));
    }

    @Test
    public void testCountSubscribedReportsNone() {
        creator.setId(1);
        assertEquals(0, gateway.countSubscribedReports(creator));
    }

    @Test
    public void testCountSubscribedReportsInternalError() throws SQLException {
        creator.setId(1);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        assertThrows(InternalError.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).countSubscribedReports(creator)
        );
    }

    @Test
    public void testCountSubscribedReportsStoreException() throws SQLException {
        creator.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new ReportDBGateway(connectionSpy, userGateway).countSubscribedReports(creator)
        );
    }

    @Test
    public void testCountSubscribedReportsUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> gateway.countSubscribedReports(null)
        );
    }

    @Test
    public void testCountSubscribedReportsUserIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> gateway.countSubscribedReports(creator)
        );
    }

}
