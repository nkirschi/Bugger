package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.ReportCriteria;
import tech.bugger.global.transfer.TopReport;
import tech.bugger.global.transfer.TopUser;
import tech.bugger.persistence.exception.StoreException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class StatisticsDBGatewayTest {

    @Nested
    @ExtendWith(DBExtension.class)
    public class StatisticsDBGatewaySuccessfulUsageTest {

        private Connection connection;

        private StatisticsDBGateway gateway;

        private ReportCriteria criteria;

        @BeforeEach
        public void setUp() throws Exception {
            DBExtension.insertMinimalTestData();
            connection = DBExtension.getConnection();
            gateway = new StatisticsDBGateway(connection);
            criteria = new ReportCriteria("", null, null);
        }

        @AfterEach
        public void tearDown() throws Exception {
            connection.close();
        }

        @Test
        public void testGetNumberOfOpenReports() {
            assertEquals(1, gateway.getNumberOfOpenReports(criteria));
        }

        @Test
        public void testGetAverageTimeToCloseWhenThereAreClosedReportsSatisfyingTheCriteria() {
            assertEquals(12, gateway.getAverageTimeToClose(criteria).toHours());
        }

        @Test
        public void testGetAverageTimeToCloseWhenThereAreNoClosedReportsSatisfyingTheCriteria() {
            assertNull(gateway.getAverageTimeToClose(new ReportCriteria("", OffsetDateTime.MIN, null)));
        }

        @Test
        public void testGetAveragePostsPerReport() {
            BigDecimal avg = gateway.getAveragePostsPerReport(criteria);
            BigDecimal exp = BigDecimal.valueOf((double) 2 / 3);
            assertEquals(exp.setScale(2, RoundingMode.FLOOR), avg.setScale(2, RoundingMode.FLOOR));
        }

        @Test
        public void testGetTopReports() {
            List<TopReport> expected = List.of(
                    new TopReport(101, "bestreport", "testuser", 5),
                    new TopReport(100, "testreport", "admin", 2),
                    new TopReport(102, "westreport", "admin", 0)
            );
            assertEquals(expected, gateway.getTopReports(42));
        }

        @Test
        public void testGetTopUsers() {
            List<TopUser> expected = List.of(
                    new TopUser("admin", 10),
                    new TopUser("testuser", 5),
                    new TopUser("pending", 0),
                    new TopUser("corpse", 0)
            );
            assertEquals(expected, gateway.getTopUsers(42));
        }

    }


    @Nested
    public class StatisticsDBGatewayDatabaseErrorTest {

        @InjectMocks
        private StatisticsDBGateway gateway;

        @Mock
        private Connection connectionMock;

        @Mock
        private ReportCriteria criteriaMock;

        @BeforeEach
        public void setUp()throws Exception {
            doThrow(SQLException.class).when(connectionMock).prepareStatement(any());
        }

        @Test
        public void testGetNumberOfOpenReportsWhenDatabaseError() {
            assertThrows(StoreException.class, () -> gateway.getNumberOfOpenReports(criteriaMock));
        }

        @Test
        public void testGetAverageTimeToCloseWhenDatabaseError() {
            assertThrows(StoreException.class, () -> gateway.getAverageTimeToClose(criteriaMock));
        }

        @Test
        public void testGetAveragePostsPerReportWhenDatabaseError() {
            assertThrows(StoreException.class, () -> gateway.getAveragePostsPerReport(criteriaMock));
        }

        @Test
        public void testGetTopTenReportsWhenDatabaseError() {
            assertThrows(StoreException.class, () -> gateway.getTopReports(42));
        }

        @Test
        public void testGetTopTenUsersWhenDatabaseError() {
            assertThrows(StoreException.class, () -> gateway.getTopUsers(42));
        }

    }

    @Nested
    public class StatisticsDBGatewayEmptyResultSetTest {

        @InjectMocks
        private StatisticsDBGateway gateway;

        @Mock
        private Connection connectionMock;

        @Mock
        private ReportCriteria criteriaMock;

        @BeforeEach
        public void setUp()throws Exception {
            PreparedStatement preparedStatementMock = mock(PreparedStatement.class);
            ResultSet resultSetMock = mock(ResultSet.class);
            doReturn(false).when(resultSetMock).next();
            doReturn(resultSetMock).when(preparedStatementMock).executeQuery();
            doReturn(preparedStatementMock).when(connectionMock).prepareStatement(any());
        }

        @Test
        public void testGetNumberOfOpenReportsWhenEmptyResultSet() {
            assertThrows(InternalError.class, () -> gateway.getNumberOfOpenReports(criteriaMock));
        }

        @Test
        public void testGetAverageTimeToCloseWhenEmptyResultSet() {
            assertThrows(InternalError.class, () -> gateway.getAverageTimeToClose(criteriaMock));
        }

        @Test
        public void testGetAveragePostsPerReportWhenEmptyResultSet() {
            assertThrows(InternalError.class, () -> gateway.getAveragePostsPerReport(criteriaMock));
        }

    }

    @Nested
    public class StatisticsDBGatewayIllegalArgumentsTest {

        @InjectMocks
        private StatisticsDBGateway gateway;

        @Mock
        private Connection connectionMock;

        @Mock
        private ReportCriteria criteriaMock;

        @Test
        public void testGetNumberOfOpenReportsWhenCriteriaNull() {
            assertThrows(IllegalArgumentException.class, () -> gateway.getNumberOfOpenReports(null));
        }

        @Test
        public void testGetAverageTimeToCloseWhenCriteriaNull() {
            assertThrows(IllegalArgumentException.class, () -> gateway.getAverageTimeToClose(null));
        }

        @Test
        public void testGetAveragePostsPerReportWhenCriteriaNull() {
            assertThrows(IllegalArgumentException.class, () -> gateway.getAveragePostsPerReport(null));
        }

        @Test
        public void testGetTopReportsWhenLimitNegative() {
            assertThrows(IllegalArgumentException.class, () -> gateway.getTopReports(-42));
        }

        @Test
        public void testGetTopUsersWhenLimitNegative() {
            assertThrows(IllegalArgumentException.class, () -> gateway.getTopUsers(-42));
        }

    }

}