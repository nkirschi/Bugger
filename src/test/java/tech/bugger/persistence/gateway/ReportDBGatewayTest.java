package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(DBExtension.class)
@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReportDBGatewayTest {

    @Mock
    private UserDBGateway userGateway;

    private ReportDBGateway gateway;

    private Connection connection;

    private Report report;

    private Topic topic;

    @BeforeEach
    public void setUp() throws Exception {
        DBExtension.insertMinimalTestData();
        connection = DBExtension.getConnection();
        gateway = new ReportDBGateway(connection, userGateway);

        topic = new Topic(1, "topictitle", "topicdescription");
        Authorship authorship = new Authorship(new User(), ZonedDateTime.now(), new User(), ZonedDateTime.now());
        authorship.getCreator().setId(1);
        authorship.getModifier().setId(1);
        report = new Report(0, "App crashes", Report.Type.HINT, Report.Severity.SEVERE, "1.4.1",
                new Authorship(null, ZonedDateTime.now(), null, ZonedDateTime.now()), null,
                null, null, 1);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    public void insertReport() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO topic (title, description) VALUES ('topic1', 'description1');");
            stmt.execute("INSERT INTO report (title, type, severity, topic) VALUES ('HI', 'BUG', 'MINOR', 1);");
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
        report.setTopic(topic.getId());
        gateway.update(report);

        Report reportFromDatabase = gateway.find(100);
        assertAll(() -> assertEquals(report.getId(), reportFromDatabase.getId()),
                () -> assertEquals(report.getTitle(), reportFromDatabase.getTitle()),
                () -> assertEquals(report.getType(), reportFromDatabase.getType()),
                () -> assertEquals(report.getSeverity(), reportFromDatabase.getSeverity()),
                () -> assertEquals(report.getVersion(), reportFromDatabase.getVersion()));
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
                    null, null, null, null, null, null, null, 0
            );
        } else {
            return null;
        }
    }

    @Test
    public void testCreate() throws Exception {
        report.setTopic(topic.getId());
        gateway.create(report);
        Report created = find(report.getId());
        assertEquals(report.getId(), created.getId());
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
        assertThrows(IllegalArgumentException.class, () -> gateway.countPosts(null));
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
        insertReport();
        report.setId(100);
        assertEquals(0, gateway.countPosts(report));
    }

    @Test
    public void testCountPostsWhenThereAreSome() throws Exception {
        DBExtension.emptyDatabase();
        insertReport();
        insertPosts(100, 34);
        report.setId(100);
        assertEquals(34, gateway.countPosts(report));
    }

    @Test
    public void testDeleteReportWhenReportIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.delete(null));
    }

    @Test
    public void testDeleteReportWhenReportIDIsNull() {
        report.setId(null);
        assertThrows(IllegalArgumentException.class, () -> gateway.delete(null));
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
        insertReport();
        report.setId(100);
        gateway.delete(report);
        assertTrue(isGone(100));
    }

    @Test
    public void testDeleteReportTwice() throws Exception {
        insertReport();
        report.setId(100);
        gateway.delete(report);
        assertThrows(NotFoundException.class, () -> gateway.delete(report));
    }

    @Test
    public void testCloseReportWhenReportIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.closeReport(null));
    }

    @Test
    public void testCloseReportWhenReportIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.closeReport(new Report()));
    }

    @Test
    public void testCloseReportWhenReportClosingDateIsNull() {
        report.setId(100);
        assertThrows(IllegalArgumentException.class, () -> gateway.closeReport(report));
    }

    @Test
    public void testCloseReportWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        report.setId(100);
        report.setClosingDate(ZonedDateTime.now());
        assertThrows(StoreException.class, () -> new ReportDBGateway(connectionSpy, userGateway).closeReport(report));
    }

    @Test
    public void testCloseReportWhenReportDoesNotExist() {
        report.setId(21);
        report.setClosingDate(ZonedDateTime.now());
        // assertNull(ZonedDateTime.now());
        assertThrows(NotFoundException.class, () -> gateway.closeReport(report));
    }

    @Test
    public void testCloseReport() throws Exception {
        insertReport();
        report.setId(100);
        report.setClosingDate(ZonedDateTime.now());
        assertDoesNotThrow(() -> gateway.closeReport(report));
    }

    @Test
    public void testCloseReportVerifyClosingDate() throws Exception {
        insertReport();
        report.setId(100);
        report.setClosingDate(ZonedDateTime.now());
        gateway.closeReport(report);
        ZonedDateTime fromDatabase = null;
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM report WHERE id = 100");
            if (rs.next()) {
                fromDatabase = rs.getTimestamp("closed_at").toInstant().atZone(ZoneId.systemDefault());
            }
        }
        assertEquals(report.getClosingDate().truncatedTo(ChronoUnit.SECONDS), fromDatabase.truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    public void testOpenReportWhenReportIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.openReport(null));
    }

    @Test
    public void testOpenReportWhenReportIDIsNull() {
        Report report = new Report();
        report.setId(null);
        assertThrows(IllegalArgumentException.class, ()  -> gateway.openReport(report));
    }

    @Test
    public void testOpenReportWhenReportDoesNotExist() {
        report.setId(8);
        assertThrows(NotFoundException.class, () -> gateway.openReport(report));
    }

    @Test
    public void testOpenReportWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        report.setId(100);
        assertThrows(StoreException.class, () -> new ReportDBGateway(connectionSpy, userGateway).openReport(report));
    }

    @Test
    public void testOpenReportWhenReportIsOpen() throws Exception {
        insertReport();
        report.setId(100);
        assertDoesNotThrow(() -> gateway.openReport(report));
    }

    @Test
    public void testOpenReportWhenReportIsClosed() throws Exception {
        insertReport();
        report.setId(100);
        report.setClosingDate(ZonedDateTime.now());
        gateway.closeReport(report);
        assertDoesNotThrow(() -> gateway.openReport(report));
    }

}
