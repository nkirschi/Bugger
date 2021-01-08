package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
public class ReportDBGatewayTest {

    private ReportDBGateway gateway;

    private Connection connection;

    @Mock
    private UserDBGateway userGateway;

    private Report report;

    @BeforeEach
    public void setUp() throws Exception {
        DBExtension.insertMinimalTestData();
        connection = DBExtension.getConnection();
        gateway = new ReportDBGateway(connection, userGateway);

        Topic topic = new Topic(1, "topictitle", "topicdescription");
        Authorship authorship = new Authorship(new User(), ZonedDateTime.now(), new User(), ZonedDateTime.now());
        authorship.getCreator().setId(1);
        authorship.getModifier().setId(1);
        report = new Report(10000, "testtitle", Report.Type.BUG, Report.Severity.MINOR, "testversion", authorship, null, null, null, 1);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
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
        gateway.create(report);
        assertEquals(report, find(report.getId()));
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
