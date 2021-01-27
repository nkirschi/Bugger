package tech.bugger.business.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.PriorityExecutor;
import tech.bugger.business.util.PriorityTask;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.NotificationGateway;
import tech.bugger.persistence.gateway.TokenDBGateway;
import tech.bugger.persistence.gateway.UserDBGateway;
import tech.bugger.persistence.util.Mailer;
import tech.bugger.persistence.util.PropertiesReader;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private NotificationService service;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private Event<Feedback> feedbackEvent;

    @Mock
    private Mailer mailer;

    @Mock
    private PriorityExecutor priorityExecutor;

    @Mock
    private NotificationGateway notificationGateway;

    private Notification notification;

    private User user;

    @BeforeEach
    public void setUp() {
        // Instantly run tasks.
        lenient().doAnswer(invocation -> {
            invocation.getArgument(0, PriorityTask.class).run();
            return null;
        }).when(priorityExecutor).enqueue(any());

        service = new NotificationService(transactionManager, feedbackEvent, ResourceBundleMocker.mock(""),
                ResourceBundleMocker.mock(""), priorityExecutor, mailer);

        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(notificationGateway).when(tx).newNotificationGateway();

        notification = new Notification();
        notification.setId(42);
        user = new User();
        user.setId(666);
    }

    @Test
    public void testDeleteNotificationWhenNotificationIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteNotification(null));
    }

    @Test
    public void testDeleteNotificationWhenNotificationIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteNotification(new Notification()));
    }

    @Test
    public void testDeleteNotificationWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(notificationGateway).delete(any());
        assertDoesNotThrow(() -> service.deleteNotification(notification));
    }

    @Test
    public void testDeleteNotificationWhenDatabaseError() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> service.deleteNotification(notification));
    }

    @Test
    public void testDeleteNotificationSuccess() throws Exception {
        assertDoesNotThrow(() -> service.deleteNotification(notification));
        verify(notificationGateway).delete(notification);
    }

    @Test
    public void testMarkAsReadWhenNotificationIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.markAsRead(null));
    }

    @Test
    public void testMarkAsReadWhenNotificationIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.markAsRead(new Notification()));
    }

    @Test
    public void testMarkAsReadWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(notificationGateway).update(any());
        assertDoesNotThrow(() -> service.markAsRead(notification));
    }

    @Test
    public void testMarkAsReadWhenDatabaseError() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> service.markAsRead(notification));
    }

    @Test
    public void testMarkAsReadSuccess() throws Exception {
        assertDoesNotThrow(() -> service.markAsRead(notification));
        verify(notificationGateway).update(notification);
        assertTrue(notification.isRead());
    }

    @Test
    public void testCountNotificationsWhenUserIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.countNotifications(null));
    }

    @Test
    public void testCountNotificationsWhenUserIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.countNotifications(new User()));
    }

    @Test
    public void testCountNotificationsWhenDatabaseError() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        doReturn(42).when(notificationGateway).countNotifications(user);
        assertEquals(0, service.countNotifications(user));
    }

    @Test
    public void testCountNotificationsSuccess() {
        doReturn(42).when(notificationGateway).countNotifications(user);
        assertEquals(42, service.countNotifications(user));
    }
}