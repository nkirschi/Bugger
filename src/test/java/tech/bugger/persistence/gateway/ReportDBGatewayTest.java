package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.util.PSQLException;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Report;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
class ReportDBGatewayTest {

    private ReportDBGateway gateway;
    private Connection connection;
    private Report report = new Report();

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        gateway = new ReportDBGateway(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
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
    public void testCountPostsWhenReportIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.countPosts(null));
    }

    @Test
    public void testCountPostsWhenReportIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.countPosts(new Report()));
    }

    @Test
    public void testCountPostsWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        report.setId(100);
        assertThrows(StoreException.class, () -> new ReportDBGateway(connectionSpy).countPosts(report));
    }

    @Test
    public void testCountPostsWhenReportDoesNotExist() {
        report.setId(93);
        assertEquals(0, gateway.countPosts(report));
    }

    @Test
    public void testCountPostsWhenThereAreNone() throws Exception {
        insertReport();
        report.setId(100);
        assertEquals(0, gateway.countPosts(report));
    }

    @Test
    public void testCountPostsWhenThereAreSome() throws Exception {
        insertReport();
        insertPosts(100, 34);
        report.setId(100);
        assertEquals(34, gateway.countPosts(report));
    }

    @Test
    public void testDeleteReportWhenReportIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.deleteReport(null));
    }

    @Test
    public void testDeleteReportWhenReportIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gateway.deleteReport(new Report()));
    }

    @Test
    public void testDeleteReportWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        report.setId(100);
        assertThrows(StoreException.class, () -> new ReportDBGateway(connectionSpy).deleteReport(report));
    }

    @Test
    public void testDeleteReportWhenReportDoesNotExist() {
        report.setId(11);
        assertThrows(NotFoundException.class, () -> gateway.deleteReport(report));
    }

    @Test
    public void testDeleteReport() throws Exception {
        insertReport();
        report.setId(100);
        gateway.deleteReport(report);
        assertTrue(isGone(100));
    }

    @Test
    public void testDeleteReportTwice() throws Exception {
        insertReport();
        report.setId(100);
        gateway.deleteReport(report);
        assertThrows(NotFoundException.class, () -> gateway.deleteReport(report));
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
        assertThrows(StoreException.class, () -> new ReportDBGateway(connectionSpy).closeReport(report));
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
        assertThrows(IllegalArgumentException.class, ()  -> gateway.openReport(new Report()));
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
        assertThrows(StoreException.class, () -> new ReportDBGateway(connectionSpy).openReport(report));
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