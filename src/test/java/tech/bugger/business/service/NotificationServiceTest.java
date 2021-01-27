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
}