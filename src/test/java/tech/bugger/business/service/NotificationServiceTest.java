package tech.bugger.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.PriorityExecutor;
import tech.bugger.business.util.PriorityTask;
import tech.bugger.business.util.Registry;
import tech.bugger.control.util.JFConfig;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.NotificationGateway;
import tech.bugger.persistence.gateway.UserGateway;
import tech.bugger.persistence.util.Mailer;
import tech.bugger.persistence.util.PropertiesReader;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private PropertiesReader configReader;

    @Mock
    private NotificationGateway notificationGateway;

    @Mock
    private UserGateway userGateway;

    @Mock
    private Registry registry;

    private Notification notification;

    private User user;

    private Selection selection;

    @BeforeEach
    public void setUp() {
        // Instantly run tasks.
        lenient().doAnswer(invocation -> {
            invocation.getArgument(0, PriorityTask.class).run();
            return null;
        }).when(priorityExecutor).enqueue(any());

        service = new NotificationService(transactionManager, registry);

        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(notificationGateway).when(tx).newNotificationGateway();
        lenient().doReturn(userGateway).when(tx).newUserGateway();

        notification = new Notification();
        notification.setId(42);
        user = new User();
        user.setId(666);
        selection = new Selection(42, 1, Selection.PageSize.NORMAL, "a", false);
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
    public void testCountNotificationsSuccess() throws Exception {
        doReturn(42).when(notificationGateway).countNotifications(user);
        assertEquals(42, service.countNotifications(user));
        verify(notificationGateway).countNotifications(user);
    }

    @Test
    public void testSelectNotificationsWhenUserIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.selectNotifications(null, null));
    }

    @Test
    public void testSelectNotificationsWhenUserIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.selectNotifications(new User(), null));
    }

    @Test
    public void testSelectNotificationsWhenSelectionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.selectNotifications(user, null));
    }

    @Test
    public void testSelectNotificationsWhenDatabaseError() throws Exception {
        doReturn(Collections.EMPTY_LIST).when(notificationGateway).selectNotifications(user, selection);
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(service.selectNotifications(user, selection));
    }

    @Test
    public void testSelectNotificationsSuccess() {
        List<Notification> expected = new ArrayList<>();
        expected.add(notification);
        doReturn(expected).when(notificationGateway).selectNotifications(user, selection);
        assertEquals(expected, service.selectNotifications(user, selection));
        verify(notificationGateway).selectNotifications(user, selection);
    }

    @Test
    public void testCreateNotificationWhenNotificationIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.createNotification(null));
    }

    @Test
    public void testCreateNotificationWhenReportIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.createNotification(new Notification()));
    }

    @Test
    public void testCreateNotificationWhenTopicIDIsNull() {
        notification.setReportID(420);
        assertThrows(IllegalArgumentException.class, () -> service.createNotification(notification));
    }

    @Test
    public void testCreateNotificationWhenDatabaseError() throws Exception {
        notification.setReportID(420);
        notification.setTopicID(69);
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> service.createNotification(notification));
    }

    @Test
    public void testCreateNotificationSuccess() {
        notification.setReportID(420);
        notification.setTopicID(69);
        try (MockedStatic<JFConfig> jfConfigMock = mockStatic(JFConfig.class)) {
            try (MockedStatic<FacesContext> fctxMock = mockStatic(FacesContext.class)) {
                jfConfigMock.when(() -> JFConfig.getApplicationPath(any())).thenReturn("Hi");
                FacesContext fctx = mock(FacesContext.class);
                fctxMock.when(() -> FacesContext.getCurrentInstance()).thenReturn(fctx);
                assertDoesNotThrow(() -> service.createNotification(notification));
            }
        }


    }

}