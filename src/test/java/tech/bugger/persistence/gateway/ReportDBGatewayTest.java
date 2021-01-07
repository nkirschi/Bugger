package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.util.PSQLException;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Report;
import tech.bugger.persistence.exception.StoreException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
        assertThrows(StoreException.class, () -> gateway.countPosts(report));
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
    void deleteReport() {
    }

    @Test
    void closeReport() {
    }

    @Test
    void openReport() {
    }
}