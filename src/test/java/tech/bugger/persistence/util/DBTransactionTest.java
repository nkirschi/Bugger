package tech.bugger.persistence.util;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.LogExtension;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.AttachmentDBGateway;
import tech.bugger.persistence.gateway.MetadataDBGateway;
import tech.bugger.persistence.gateway.NotificationDBGateway;
import tech.bugger.persistence.gateway.PostDBGateway;
import tech.bugger.persistence.gateway.ReportDBGateway;
import tech.bugger.persistence.gateway.SearchDBGateway;
import tech.bugger.persistence.gateway.SettingsDBGateway;
import tech.bugger.persistence.gateway.StatisticsDBGateway;
import tech.bugger.persistence.gateway.SubscriptionDBGateway;
import tech.bugger.persistence.gateway.TokenDBGateway;
import tech.bugger.persistence.gateway.TopicDBGateway;
import tech.bugger.persistence.gateway.UserDBGateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
public class DBTransactionTest {
    private DBTransaction tx;
    private Connection connectionMock;
    private ConnectionPool connectionPoolMock;

    @BeforeEach
    public void setup() throws Exception {
        connectionMock = mock(Connection.class);
        doNothing().when(connectionMock).commit();
        doNothing().when(connectionMock).rollback();
        connectionPoolMock = mock(ConnectionPool.class);
        when(connectionPoolMock.getConnection()).thenReturn(connectionMock);
        tx = new DBTransaction(connectionPoolMock);
    }

    @Test
    public void testConstructorAutoCommitFails() throws Exception {
        doThrow(SQLException.class).when(connectionMock).setAutoCommit(anyBoolean());
        assertThrows(InternalError.class, () -> new DBTransaction(connectionPoolMock));
    }

    @Test
    public void testCommit() throws TransactionException {
        tx.commit();
        assertTrue(tx.isCompleted());
    }

    @Test
    public void testCommitFails() throws Exception {
        doThrow(SQLException.class).when(connectionMock).commit();
        assertThrows(TransactionException.class, () -> tx.commit());
    }

    @Test
    public void testAbort() {
        tx.abort();
        assertTrue(tx.isCompleted());
    }

    @Test
    public void testAbortFails() throws Exception {
        doThrow(SQLException.class).when(connectionMock).rollback();
        assertThrows(InternalError.class, () -> tx.abort());
    }

    @Test
    public void testCloseWhenCompleted() throws Exception {
        tx.commit();
        tx.close();
        verify(connectionPoolMock).releaseConnection(any());
    }

    @Test
    public void testCloseWhenNotCompleted() throws Exception {
        tx.close();
        verify(connectionMock).rollback();
        verify(connectionPoolMock).releaseConnection(any());
    }

    @Test
    public void testCommitAfterClose() {
        tx.close();
        assertThrows(IllegalStateException.class, () -> tx.commit());
    }

    @Test
    public void testNewAttachmentGateway() {
        assertTrue(tx.newAttachmentGateway() instanceof AttachmentDBGateway);
    }

    @Test
    public void testNewMetadataGateway() {
        assertTrue(tx.newMetadataGateway() instanceof MetadataDBGateway);
    }

    @Test
    public void testNewNotificationGateway() {
        assertTrue(tx.newNotificationGateway() instanceof NotificationDBGateway);
    }

    @Test
    public void testNewPostGateway() {
        assertTrue(tx.newPostGateway() instanceof PostDBGateway);
    }

    @Test
    public void testNewReportGateway() {
        assertTrue(tx.newReportGateway() instanceof ReportDBGateway);
    }

    @Test
    public void testNewSearchGateway() {
        assertTrue(tx.newSearchGateway() instanceof SearchDBGateway);
    }

    @Test
    public void testNewSettingsGateway() {
        assertTrue(tx.newSettingsGateway() instanceof SettingsDBGateway);
    }

    @Test
    public void testNewStatisticsGateway() {
        assertTrue(tx.newStatisticsGateway() instanceof StatisticsDBGateway);
    }

    @Test
    public void testNewSubscriptionGateway() {
        assertTrue(tx.newSubscriptionGateway() instanceof SubscriptionDBGateway);
    }

    @Test
    public void testNewTokenGateway() {
        assertTrue(tx.newTokenGateway() instanceof TokenDBGateway);
    }

    @Test
    public void testNewTopicGateway() {
        assertTrue(tx.newTopicGateway() instanceof TopicDBGateway);
    }

    @Test
    public void testNewUserGateway() {
        assertTrue(tx.newUserGateway() instanceof UserDBGateway);
    }
}