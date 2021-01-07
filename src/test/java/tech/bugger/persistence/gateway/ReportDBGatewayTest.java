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
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

@ExtendWith(DBExtension.class)
@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class ReportDBGatewayTest {

    @Mock
    private UserDBGateway userGateway;

    private ReportDBGateway gateway;

    private Connection connection;

    private Report report;

    @BeforeEach
    public void setUp() throws Exception {
        DBExtension.insertMinimalTestData();
        connection = DBExtension.getConnection();
        gateway = new ReportDBGateway(connection, userGateway);

        report = new Report(0, "App crashes", Report.Type.HINT, Report.Severity.SEVERE, "1.4.1",
                new Authorship(null, ZonedDateTime.now(), null, ZonedDateTime.now()), null,
                null, null, 0);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
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
        doReturn(1).when(report).getTopic();
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

}