package tech.bugger.persistence.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(DBExtension.class)
@ExtendWith(LogExtension.class)
public class NotificationDBGatewayTest {

    private NotificationGateway notificationGateway;

    private Connection connection;

    private Notification notification1;
    private Notification notification2;
    private User admin;
    private Selection selection;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        notificationGateway = new NotificationDBGateway(connection);
        UserGateway userGateway = new UserDBGateway(connection);
        DBExtension.insertMinimalTestData();
        notification1 = new Notification(null, 2, 1, Notification.Type.NEW_POST, null, false, false, 1, 100, 100,
                null, null, null);
        notification2 = new Notification(null, 2, 1, Notification.Type.NEW_POST, null, false, false, 1, 100, 100,
                null, null, null);
        admin = userGateway.getUserByID(1);
        selection = new Selection(2, 0, Selection.PageSize.SMALL, "id", true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testCountNotifications() {
        notificationGateway.create(notification1);
        notificationGateway.create(notification2);
        assertEquals(2, notificationGateway.countNotifications(admin));
    }

    @Test
    public void testCountNotificationsNone() {
        assertEquals(0, notificationGateway.countNotifications(admin));
    }

    @Test
    public void testCountNotificationsNoResult() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        assertEquals(0, new NotificationDBGateway(connectionSpy).countNotifications(admin));
    }

    @Test
    public void testCountNotificationsSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new NotificationDBGateway(connectionSpy).countNotifications(admin)
        );
    }

    @Test
    public void testCountNotificationsUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationGateway.countNotifications(null)
        );
    }

    @Test
    public void testCountNotificationsUserIdNull() {
        admin.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> notificationGateway.countNotifications(admin)
        );
    }

    @Test
    public void testCreate() {
        assertDoesNotThrow(() -> notificationGateway.create(notification1));
    }

    @Test
    public void testCreateNoResult() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any(), anyInt());
        doReturn(rs).when(stmt).getGeneratedKeys();
        assertThrows(StoreException.class,
                () -> new NotificationDBGateway(connectionSpy).create(notification1)
        );
    }

    @Test
    public void testCreateSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any(), anyInt());
        assertThrows(StoreException.class,
                () -> new NotificationDBGateway(connectionSpy).create(notification1)
        );
    }

    @Test
    public void testCreateNotificationNull() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationGateway.create(null)
        );
    }

    @Test
    public void testFindNotification() throws NotFoundException {
        notificationGateway.create(notification1);
        Notification notification = notificationGateway.find(notification1.getId());
        assertAll(
                () -> assertEquals(notification1.getId(), notification.getId()),
                () -> assertEquals(notification1.getActuatorID(), notification.getActuatorID()),
                () -> assertEquals(notification1.getRecipientID(), notification.getRecipientID()),
                () -> assertEquals(notification1.getType(), notification.getType()),
                () -> assertEquals(notification1.getTopicID(), notification.getTopicID()),
                () -> assertEquals(notification1.getReportID(), notification.getReportID()),
                () -> assertEquals(notification1.getPostID(), notification.getPostID()),
                () -> assertFalse(notification.isRead()),
                () -> assertFalse(notification.isSent())
        );
    }

    @Test
    public void testFindNotificationNotFound() {
        assertThrows(NotFoundException.class,
                () -> notificationGateway.find(1)
        );
    }

    @Test
    public void testFindNotificationSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new NotificationDBGateway(connectionSpy).find(1)
        );
    }

    @Test
    public void testSelectNotifications() {
        notificationGateway.create(notification1);
        notificationGateway.create(notification2);
        List<Notification> notifications = notificationGateway.selectNotifications(admin, selection);
        assertAll(
                () -> assertEquals(2, notifications.size()),
                () -> assertTrue(notifications.contains(notification1)),
                () -> assertTrue(notifications.contains(notification2))
        );
    }

    @Test
    public void testSelectNotificationsSelectionDesc() {
        selection.setAscending(false);
        notificationGateway.create(notification1);
        notificationGateway.create(notification2);
        List<Notification> notifications = notificationGateway.selectNotifications(admin, selection);
        assertAll(
                () -> assertEquals(2, notifications.size()),
                () -> assertEquals(notification2, notifications.get(0)),
                () -> assertEquals(notification1, notifications.get(1))
        );
    }

    @Test
    public void testSelectNotificationsNone() {
        assertTrue(notificationGateway.selectNotifications(admin, selection).isEmpty());
    }

    @Test
    public void testSelectNotificationsSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new NotificationDBGateway(connectionSpy).selectNotifications(admin, selection)
        );
    }

    @Test
    public void testSelectNotificationsUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationGateway.selectNotifications(null, selection)
        );
    }

    @Test
    public void testSelectNotificationsUserIdNull() {
        admin.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> notificationGateway.selectNotifications(admin, selection)
        );
    }

    @Test
    public void testSelectNotificationsSelectionNull() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationGateway.selectNotifications(admin, null)
        );
    }

    @Test
    public void testSelectNotificationsSelectionSortedByBlank() {
        selection.setSortedBy("");
        assertThrows(IllegalArgumentException.class,
                () -> notificationGateway.selectNotifications(admin, selection)
        );
    }

    @Test
    public void testUpdate() throws NotFoundException {
        notificationGateway.create(notification1);
        notification1.setSent(true);
        notification1.setRead(true);
        notificationGateway.update(notification1);
        Notification notification = notificationGateway.find(notification1.getId());
        assertAll(
                () -> assertTrue(notification.isSent()),
                () -> assertTrue(notification.isRead())
        );
    }

    @Test
    public void testUpdateNotFound() {
        notification1.setId(1);
        assertThrows(NotFoundException.class,
                () -> notificationGateway.update(notification1)
        );
    }

    @Test
    public void testUpdateNoID() {
        assertThrows(IllegalArgumentException.class, () -> notificationGateway.update(new Notification()));
    }

    @Test
    public void testUpdateSQLException() throws SQLException {
        notification1.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new NotificationDBGateway(connectionSpy).update(notification1)
        );
    }

    @Test
    public void testUpdateNotificationNull() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationGateway.update(null)
        );
    }

    @Test
    public void testDelete() throws NotFoundException {
        notificationGateway.create(notification1);
        notificationGateway.delete(notification1);
        assertThrows(NotFoundException.class,
                () -> notificationGateway.delete(notification1)
        );
    }

    @Test
    public void testDeleteNotFound() {
        notification1.setId(1);
        assertThrows(NotFoundException.class,
                () -> notificationGateway.delete(notification1)
        );
    }

    @Test
    public void testDeleteInternalError() throws SQLException {
        notification1.setId(1);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        doReturn(true).when(rs).next();
        doReturn(100).when(rs).getInt("id");
        assertThrows(InternalError.class,
                () -> new NotificationDBGateway(connectionSpy).delete(notification1)
        );
    }

    @Test
    public void testDeleteSQLException() throws SQLException {
        notification1.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new NotificationDBGateway(connectionSpy).delete(notification1)
        );
    }

    @Test
    public void testDeleteNotificationNull() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationGateway.delete(null)
        );
    }

    @Test
    public void testDeleteNotificationIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationGateway.delete(notification1)
        );
    }

    @Test
    public void testCreateNotificationBulk() {
        List<Notification> notifications = new ArrayList<>(20);
        for (int i = 0; i < 20; i++) {
            notifications.add(new Notification(null, 2, 1, Notification.Type.NEW_POST, null, false, false, 1, 100, 100, null, null, null));
        }
        assertDoesNotThrow(() -> notificationGateway.createNotificationBulk(notifications));
        assertDoesNotThrow(() -> notificationGateway.find(1));
        assertDoesNotThrow(() -> notificationGateway.find(20));
    }

    @Test
    public void testCreateNotificationBulkSQLException() throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        notifications.add(notification1);
        notifications.add(notification2);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any(), eq(Statement.RETURN_GENERATED_KEYS));
        assertThrows(StoreException.class,
                () -> new NotificationDBGateway(connectionSpy).createNotificationBulk(notifications)
        );
    }

    @Test
    public void testCreateNotificationBulkNull() {
        assertThrows(IllegalArgumentException.class,
                () -> notificationGateway.createNotificationBulk(null)
        );
    }

    @Test
    public void testGetUnsentNotifications() {
        notificationGateway.create(notification1);
        notificationGateway.create(notification2);
        List<Notification> notifications = notificationGateway.getUnsentNotifications();
        assertAll(
                () -> assertEquals(2, notifications.size()),
                () -> assertTrue(notifications.contains(notification2)),
                () -> assertTrue(notifications.contains(notification1))
        );
    }

    @Test
    public void testGetUnsentNotificationsNone() {
        notification1.setSent(true);
        notification2.setSent(true);
        notificationGateway.create(notification1);
        notificationGateway.create(notification2);
        assertTrue(notificationGateway.getUnsentNotifications().isEmpty());
    }

    @Test
    public void testGetUnsentNotificationsSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new NotificationDBGateway(connectionSpy).getUnsentNotifications()
        );
    }

}