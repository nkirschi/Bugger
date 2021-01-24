package tech.bugger.persistence.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.DuplicateException;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.SelfReferenceException;
import tech.bugger.persistence.exception.StoreException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(DBExtension.class)
@ExtendWith(LogExtension.class)
class SubscriptionDBGatewayTest {

    private SubscriptionDBGateway gateway;

    private Connection connection;
    private User user = new User();
    private User testUser = new User();
    private Topic topic = new Topic();
    private Report report = new Report();
    private static final String DUPLICATE_KEY = "23505";
    private static final String SUBSCRIBER = "subscriber";
    private static final int WRONG_ID = 0;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        gateway = new SubscriptionDBGateway(connection);
        DBExtension.insertMinimalTestData();
        user.setId(1);
        testUser.setId(2);
        topic.setId(1);
        report.setId(100);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testSubscribeTopicTwice() {
        assertAll(
                () -> assertDoesNotThrow(() -> gateway.subscribe(topic, user)),
                () -> assertThrows(DuplicateException.class, () -> gateway.subscribe(topic, user))
        );
    }

    @Test
    public void testSubscribeTopicDuplicateKey() throws SQLException {
        SQLException exception = mock(SQLException.class);
        Connection connectionSpy = spy(connection);
        doReturn(DUPLICATE_KEY).when(exception).getSQLState();
        doThrow(exception).when(connectionSpy).prepareStatement(any());
        assertThrows(DuplicateException.class,
                () -> new SubscriptionDBGateway(connectionSpy).subscribe(topic, user)
        );
    }

    @Test
    public void testSubscribeTopicSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SubscriptionDBGateway(connectionSpy).subscribe(topic, user)
        );
    }

    @Test
    public void testSubscribeTopicTopicNull() {
        topic = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.subscribe(topic, user)
        );
    }

    @Test
    public void testSubscribeTopicTopicIdNull() {
        topic.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.subscribe(topic, user)
        );
    }

    @Test
    public void testSubscribeTopicUserNull() {
        user = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.subscribe(topic, user)
        );
    }

    @Test
    public void testSubscribeTopicUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.subscribe(topic, user)
        );
    }

    @Test
    public void testSubscribeTopicNoRowsAffected() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(0).when(stmt).executeUpdate();
        assertThrows(NotFoundException.class,
                () -> new SubscriptionDBGateway(connectionSpy).subscribe(topic, user)
        );
    }

    @Test
    public void testSubscribeReport() throws NotFoundException, DuplicateException {
        gateway.subscribe(report, user);
        assertThrows(DuplicateException.class, () -> gateway.subscribe(report, user));
    }

    @Test
    public void testSubscribeReportDuplicateKey() throws SQLException {
        SQLException exception = mock(SQLException.class);
        Connection connectionSpy = spy(connection);
        doReturn(DUPLICATE_KEY).when(exception).getSQLState();
        doThrow(exception).when(connectionSpy).prepareStatement(any());
        assertThrows(DuplicateException.class,
                () -> new SubscriptionDBGateway(connectionSpy).subscribe(report, user)
        );
    }

    @Test
    public void testSubscribeReportSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SubscriptionDBGateway(connectionSpy).subscribe(report, user)
        );
    }

    @Test
    public void testSubscribeReportReportNull() {
        report = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.subscribe(report, user)
        );
    }

    @Test
    public void testSubscribeReportReportIdNull() {
        report.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.subscribe(report, user)
        );
    }

    @Test
    public void testSubscribeReportUserNull() {
        user = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.subscribe(report, user)
        );
    }

    @Test
    public void testSubscribeReportUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.subscribe(report, user)
        );
    }

    @Test
    public void testSubscribeReportNoRowsAffected() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(0).when(stmt).executeUpdate();
        assertThrows(NotFoundException.class,
                () -> new SubscriptionDBGateway(connectionSpy).subscribe(report, user)
        );
    }

    @Test
    public void testSubscribeUser() throws NotFoundException, DuplicateException, SelfReferenceException {
        gateway.subscribe(testUser, user);
        assertThrows(DuplicateException.class, () -> gateway.subscribe(testUser, user));
    }

    @Test
    public void testSubscribeUserDuplicateKey() throws SQLException {
        SQLException exception = mock(SQLException.class);
        Connection connectionSpy = spy(connection);
        doReturn(DUPLICATE_KEY).when(exception).getSQLState();
        doThrow(exception).when(connectionSpy).prepareStatement(any());
        assertThrows(DuplicateException.class,
                () -> new SubscriptionDBGateway(connectionSpy).subscribe(testUser, user)
        );
    }

    @Test
    public void testSubscribeUserSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SubscriptionDBGateway(connectionSpy).subscribe(testUser, user)
        );
    }

    @Test
    public void testSubscribeUserUserNull() {
        testUser = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.subscribe(testUser, user)
        );
    }

    @Test
    public void testSubscribeUserUserIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.subscribe(testUser, user)
        );
    }

    @Test
    public void testSubscribeUserSubscriberNull() {
        user = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.subscribe(testUser, user)
        );
    }

    @Test
    public void testSubscribeUserSubscriberIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.subscribe(testUser, user)
        );
    }

    @Test
    public void testSubscribeUserSubscribeSelf() {
        assertThrows(SelfReferenceException.class,
                () -> gateway.subscribe(user, user)
        );
    }

    @Test
    public void testSubscribeUserNoRowsAffected() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(0).when(stmt).executeUpdate();
        assertThrows(NotFoundException.class,
                () -> new SubscriptionDBGateway(connectionSpy).subscribe(testUser, user)
        );
    }

    @Test
    public void testUnsubscribeTopic() throws NotFoundException, DuplicateException {
        gateway.subscribe(topic, user);
        assertAll(
                () -> assertDoesNotThrow(() -> gateway.unsubscribe(topic, user)),
                () -> assertThrows(NotFoundException.class, () -> gateway.unsubscribe(topic, user))
        );
    }

    @Test
    public void testUnsubscribeTopicSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SubscriptionDBGateway(connectionSpy).unsubscribe(topic, user)
        );
    }

    @Test
    public void testUnsubscribeTopicWrongUserId() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        doReturn(true).when(rs).next();
        doReturn(WRONG_ID).when(rs).getInt(SUBSCRIBER);
        assertThrows(InternalError.class,
                () -> new SubscriptionDBGateway(connectionSpy).unsubscribe(topic, user)
        );
    }

    @Test
    public void testUnsubscribeTopicWrongTopicId() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        doReturn(true).when(rs).next();
        doReturn(user.getId()).when(rs).getInt(SUBSCRIBER);
        doReturn(WRONG_ID).when(rs).getInt("topic");
        assertThrows(InternalError.class,
                () -> new SubscriptionDBGateway(connectionSpy).unsubscribe(topic, user)
        );
    }

    @Test
    public void testUnsubscribeTopicTopicNull() {
        topic = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribe(topic, user)
        );
    }

    @Test
    public void testUnsubscribeTopicTopicIdNull() {
        topic.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribe(topic, user)
        );
    }

    @Test
    public void testUnsubscribeTopicSubscriberNull() {
        user = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribe(topic, user)
        );
    }

    @Test
    public void testUnsubscribeTopicSubscriberIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribe(topic, user)
        );
    }

    @Test
    public void testUnsubscribeReport() throws NotFoundException, DuplicateException {
        gateway.subscribe(report, user);
        assertAll(
                () -> assertDoesNotThrow(() -> gateway.unsubscribe(report, user)),
                () -> assertThrows(NotFoundException.class, () -> gateway.unsubscribe(report, user))
        );
    }

    @Test
    public void testUnsubscribeReportSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SubscriptionDBGateway(connectionSpy).unsubscribe(report, user)
        );
    }

    @Test
    public void testUnsubscribeReportWrongUserId() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        doReturn(true).when(rs).next();
        doReturn(WRONG_ID).when(rs).getInt(SUBSCRIBER);
        assertThrows(InternalError.class,
                () -> new SubscriptionDBGateway(connectionSpy).unsubscribe(report, user)
        );
    }

    @Test
    public void testUnsubscribeReportWrongReportId() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        doReturn(true).when(rs).next();
        doReturn(user.getId()).when(rs).getInt(SUBSCRIBER);
        doReturn(WRONG_ID).when(rs).getInt("report");
        assertThrows(InternalError.class,
                () -> new SubscriptionDBGateway(connectionSpy).unsubscribe(report, user)
        );
    }

    @Test
    public void testUnsubscribeReportReportNull() {
        report = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribe(report, user)
        );
    }

    @Test
    public void testUnsubscribeReportReportIdNull() {
        report.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribe(report, user)
        );
    }

    @Test
    public void testUnsubscribeReportSubscriberNull() {
        user = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribe(report, user)
        );
    }

    @Test
    public void testUnsubscribeReportSubscriberIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribe(report, user)
        );
    }

    @Test
    public void testUnsubscribeUser() throws NotFoundException, DuplicateException, SelfReferenceException {
        gateway.subscribe(testUser, user);
        assertAll(
                () -> assertDoesNotThrow(() -> gateway.unsubscribe(testUser, user)),
                () -> assertThrows(NotFoundException.class, () -> gateway.unsubscribe(testUser, user))
        );
    }

    @Test
    public void testUnsubscribeUserSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SubscriptionDBGateway(connectionSpy).unsubscribe(testUser, user)
        );
    }

    @Test
    public void testUnsubscribeUserWrongSubscriberId() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        doReturn(true).when(rs).next();
        doReturn(WRONG_ID).when(rs).getInt(SUBSCRIBER);
        assertThrows(InternalError.class,
                () -> new SubscriptionDBGateway(connectionSpy).unsubscribe(testUser, user)
        );
    }

    @Test
    public void testUnsubscribeUserWrongSubscribedToId() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        doReturn(true).when(rs).next();
        doReturn(user.getId()).when(rs).getInt(SUBSCRIBER);
        doReturn(WRONG_ID).when(rs).getInt("subscribee");
        assertThrows(InternalError.class,
                () -> new SubscriptionDBGateway(connectionSpy).unsubscribe(testUser, user)
        );
    }

    @Test
    public void testUnsubscribeUserSubscribedToNull() {
        testUser = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribe(testUser, user)
        );
    }

    @Test
    public void testUnsubscribeUserSubscribedToIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribe(testUser, user)
        );
    }

    @Test
    public void testUnsubscribeUserSubscriberNull() {
        user = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribe(testUser, user)
        );
    }

    @Test
    public void testUnsubscribeUserSubscriberIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribe(testUser, user)
        );
    }

    @Test
    public void testUnsubscribeAllTopics() throws NotFoundException, DuplicateException {
        gateway.subscribe(topic, user);
        gateway.unsubscribeAllTopics(user);
        assertThrows(NotFoundException.class,
                () -> gateway.unsubscribe(topic, user)
        );
    }

    @Test
    public void testUnsubscribeAllTopicsSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SubscriptionDBGateway(connectionSpy).unsubscribeAllTopics(user)
        );
    }

    @Test
    public void testUnsubscribeAllTopicsUserNull() {
        user = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribeAllTopics(user)
        );
    }

    @Test
    public void testUnsubscribeAllTopicsUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribeAllTopics(user)
        );
    }

    @Test
    public void testUnsubscribeAllReports() throws NotFoundException, DuplicateException {
        Report someReport = new Report();
        someReport.setId(101);
        gateway.subscribe(report, user);
        gateway.subscribe(someReport, user);
        gateway.unsubscribeAllReports(user);
        assertAll(
                () -> assertThrows(NotFoundException.class, () -> gateway.unsubscribe(report, user)),
                () -> assertThrows(NotFoundException.class, () -> gateway.unsubscribe(someReport, user))
        );
    }

    @Test
    public void testUnsubscribeAllReportsSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SubscriptionDBGateway(connectionSpy).unsubscribeAllReports(user)
        );
    }

    @Test
    public void testUnsubscribeAllReportsUserNull() {
        user = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribeAllReports(user)
        );
    }

    @Test
    public void testUnsubscribeAllReportsUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribeAllReports(user)
        );
    }

    @Test
    public void testUnsubscribeAllUsers() throws NotFoundException, DuplicateException, SelfReferenceException {
        User testUser2 = new User();
        testUser2.setId(3);
        gateway.subscribe(testUser, user);
        gateway.subscribe(testUser2, user);
        gateway.unsubscribeAllUsers(user);
        assertAll(
                () -> assertThrows(NotFoundException.class, () -> gateway.unsubscribe(testUser, user)),
                () -> assertThrows(NotFoundException.class, () -> gateway.unsubscribe(testUser2, user))
        );
    }

    @Test
    public void testUnsubscribeAllUsersSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SubscriptionDBGateway(connectionSpy).unsubscribeAllUsers(user)
        );
    }

    @Test
    public void testUnsubscribeAllUsersUserNull() {
        user = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribeAllUsers(user)
        );
    }

    @Test
    public void testUnsubscribeAllUsersUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.unsubscribeAllUsers(user)
        );
    }

    @Test
    public void testIsSubscribedTopic() throws NotFoundException, DuplicateException {
        gateway.subscribe(topic, user);
        assertTrue(gateway.isSubscribed(user, topic));
    }

    @Test
    public void testIsSubscribedTopicFalse() {
        assertFalse(gateway.isSubscribed(user, topic));
    }

    @Test
    public void testIsSubscribedTopicSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SubscriptionDBGateway(connectionSpy).isSubscribed(user, topic)
        );
    }

    @Test
    public void testIsSubscribedTopicTopicNull() {
        topic = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.isSubscribed(user, topic)
        );
    }

    @Test
    public void testIsSubscribedTopicTopicIdNull() {
        topic.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.isSubscribed(user, topic)
        );
    }

    @Test
    public void testIsSubscribedTopicUserNull() {
        user = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.isSubscribed(user, topic)
        );
    }

    @Test
    public void testIsSubscribedTopicUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.isSubscribed(user, topic)
        );
    }

    @Test
    public void testIsSubscribedReport() throws NotFoundException, DuplicateException {
        gateway.subscribe(report, user);
        assertTrue(gateway.isSubscribed(user, report));
    }

    @Test
    public void testIsSubscribedReportFalse() {
        assertFalse(gateway.isSubscribed(user, report));
    }

    @Test
    public void testIsSubscribedReportSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SubscriptionDBGateway(connectionSpy).isSubscribed(user, report)
        );
    }

    @Test
    public void testIsSubscribedReportReportNull() {
        report = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.isSubscribed(user, report)
        );
    }

    @Test
    public void testIsSubscribedReportReportIdNull() {
        report.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.isSubscribed(user, report)
        );
    }

    @Test
    public void testIsSubscribedReportUserNull() {
        user = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.isSubscribed(user, report)
        );
    }

    @Test
    public void testIsSubscribedReportUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.isSubscribed(user, report)
        );
    }

    @Test
    public void testIsSubscribedUser() throws NotFoundException, DuplicateException, SelfReferenceException {
        gateway.subscribe(testUser, user);
        assertTrue(gateway.isSubscribed(user, testUser));
    }

    @Test
    public void testIsSubscribedUserFalse() {
        assertFalse(gateway.isSubscribed(user, testUser));
    }

    @Test
    public void testIsSubscribedUserSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SubscriptionDBGateway(connectionSpy).isSubscribed(user, testUser)
        );
    }

    @Test
    public void testIsSubscribedUserSubscriberNull() {
        user = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.isSubscribed(user, testUser)
        );
    }

    @Test
    public void testIsSubscribedUserSubscriberIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.isSubscribed(user, testUser)
        );
    }

    @Test
    public void testIsSubscribedUserSubscribedToNull() {
        testUser = null;
        assertThrows(IllegalArgumentException.class,
                () -> gateway.isSubscribed(user, testUser)
        );
    }

    @Test
    public void testIsSubscribedReportUserSubscribedToIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> gateway.isSubscribed(user, testUser)
        );
    }

}