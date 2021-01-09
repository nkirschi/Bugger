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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

}