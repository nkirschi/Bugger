package tech.bugger.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.exception.DataAccessException;
import tech.bugger.global.transfer.TopReport;
import tech.bugger.global.transfer.TopUser;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.StatisticsGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class StatisticsServiceTest {

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction transaction;

    @Mock
    private StatisticsGateway statisticsGateway;

    @InjectMocks
    private StatisticsService statisticsService;

    @BeforeEach
    public void setUp() {
        doReturn(statisticsGateway).when(transaction).newStatisticsGateway();
        doReturn(transaction).when(transactionManager).begin();
    }

    @Test
    public void testCountOpenReportsWhenSuccess() throws Exception {
        doReturn(42).when(statisticsGateway).getNumberOfOpenReports(any());
        assertEquals(42, statisticsService.countOpenReports(null));
        verify(statisticsGateway).getNumberOfOpenReports(any());
    }

    @Test
    public void testCountOpenReportsWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(transaction).commit();
        assertThrows(DataAccessException.class, () -> statisticsService.countOpenReports(null));
        verify(statisticsGateway).getNumberOfOpenReports(any());
    }

    @Test
    public void testAverageTimeOpenWhenSuccess() throws Exception {
        Duration duration = mock(Duration.class);
        doReturn(duration).when(statisticsGateway).getAverageTimeToClose(any());
        assertEquals(duration, statisticsService.averageTimeOpen(null));
        verify(statisticsGateway).getAverageTimeToClose(any());
    }

    @Test
    public void testAverageTimeOpenWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(transaction).commit();
        assertThrows(DataAccessException.class, () -> statisticsService.averageTimeOpen(null));
        verify(statisticsGateway).getAverageTimeToClose(any());
    }

    @Test
    public void testAveragePostsPerReportWhenSuccess() throws Exception {
        BigDecimal d = BigDecimal.valueOf(1.337);
        doReturn(d).when(statisticsGateway).getAveragePostsPerReport(any());
        assertEquals(d, statisticsService.averagePostsPerReport(null));
        verify(statisticsGateway).getAveragePostsPerReport(any());
    }

    @Test
    public void testAveragePostsPerReportWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(transaction).commit();
        assertThrows(DataAccessException.class, () -> statisticsService.averagePostsPerReport(null));
        verify(statisticsGateway).getAveragePostsPerReport(any());
    }

    @Test
    public void testDetermineTopTenReportsWhenSuccess() throws Exception {
        TopReport topReport = mock(TopReport.class);
        List<TopReport> topReports = Collections.singletonList(topReport);
        doReturn(topReports).when(statisticsGateway).getTopReports(anyInt());
        assertEquals(topReports, statisticsService.determineTopReports(42));
        verify(statisticsGateway).getTopReports(anyInt());
    }

    @Test
    public void testDetermineTopTenReportsWhenCommitsFails() throws Exception {
        doThrow(TransactionException.class).when(transaction).commit();
        assertThrows(DataAccessException.class, () -> statisticsService.determineTopReports(42));
        verify(statisticsGateway).getTopReports(anyInt());
    }

    @Test
    public void testDetermineTopTenUsersWhenSuccess() throws Exception {
        TopUser topUser = mock(TopUser.class);
        List<TopUser> topUsers = Collections.singletonList(topUser);
        doReturn(topUsers).when(statisticsGateway).getTopUsers(anyInt());
        assertEquals(topUsers, statisticsService.determineTopUsers(42));
        verify(statisticsGateway).getTopUsers(anyInt());
    }

    @Test
    public void testDetermineTopTenUsersWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(transaction).commit();
        assertThrows(DataAccessException.class, () -> statisticsService.determineTopUsers(42));
        verify(statisticsGateway).getTopUsers(anyInt());
    }

}