package tech.bugger.business.service;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.exception.DataAccessException;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.PriorityExecutor;
import tech.bugger.business.util.PriorityTask;
import tech.bugger.business.util.Registry;
import tech.bugger.control.util.JFConfig;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
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
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    private NotificationService service;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private Mailer mailer;

    @Mock
    private PriorityExecutor priorityExecutor;

    @Mock
    private PropertiesReader configReader;

    @Mock
    private NotificationGateway notificationGateway;

    @Mock
    private Event<Feedback> feedbackEvent;

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

        lenient().doReturn(priorityExecutor).when(registry).getPriorityExecutor("mails");
        lenient().doReturn(mailer).when(registry).getMailer("main");
        lenient().doReturn(configReader).when(registry).getPropertiesReader("config");

        service = new NotificationService(transactionManager, feedbackEvent, configReader, priorityExecutor, mailer,
                ResourceBundleMocker.mock(""), registry);

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
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteNotificationWhenDatabaseError() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> service.deleteNotification(notification));
        verify(feedbackEvent).fire(any());

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
        verify(feedbackEvent).fire(any());
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
        verify(feedbackEvent).fire(any());
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
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSelectNotificationsSuccess() throws Exception {
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
        try (MockedStatic<JFConfig> jfConfigMock = mockStatic(JFConfig.class);
             MockedStatic<FacesContext> fctxMock = mockStatic(FacesContext.class)) {
            jfConfigMock.when(() -> JFConfig.getApplicationPath(any())).thenReturn("Hi");
            FacesContext fctx = mock(FacesContext.class);
            fctxMock.when(FacesContext::getCurrentInstance).thenReturn(fctx);
            assertDoesNotThrow(() -> service.createNotification(notification));
        }
    }

    @Test
    public void testCreateNotification() throws Exception {
        notification.setReportID(420);
        notification.setTopicID(69);
        notification.setActuatorID(user.getId());
        Notification notification1 = new Notification(notification);
        notification1.setPostID(66);
        User userSub = new User(user);
        userSub.setId(667);
        userSub.setEmailAddress("mail667");
        userSub.setPreferredLanguage(Locale.ENGLISH);
        User reportSub = new User(user);
        reportSub.setId(668);
        reportSub.setEmailAddress("");
        reportSub.setPreferredLanguage(Locale.ENGLISH);
        User topicSub = new User(user);
        topicSub.setId(669);
        topicSub.setEmailAddress("mail669");
        topicSub.setPreferredLanguage(Locale.GERMAN);
        ArrayList<User> userSubs = new ArrayList<>();
        userSubs.add(userSub);
        ArrayList<User> reportSubs = new ArrayList<>();
        reportSubs.add(reportSub);
        ArrayList<User> topicSubs = new ArrayList<>();
        topicSubs.add(topicSub);
        doReturn(userSubs).when(userGateway).getSubscribersOf(eq(user));
        Report report = new Report();
        report.setId(notification.getReportID());
        doReturn(reportSubs).when(userGateway).getSubscribersOf(eq(report));
        Topic topic = new Topic();
        topic.setId(notification.getTopicID());
        doReturn(topicSubs).when(userGateway).getSubscribersOf(eq(topic));
        doReturn(ResourceBundleMocker.mock("")).when(registry).getBundle(eq("interactions"), any());
        try (MockedStatic<JFConfig> jfConfigMock = mockStatic(JFConfig.class);
             MockedStatic<FacesContext> fctxMock = mockStatic(FacesContext.class)) {
            jfConfigMock.when(() -> JFConfig.getApplicationPath(any())).thenReturn("Hi");
            FacesContext fctx = mock(FacesContext.class);
            fctxMock.when(FacesContext::getCurrentInstance).thenReturn(fctx);
            doReturn(3).when(configReader).getInt("MAX_EMAIL_TRIES");
            assertDoesNotThrow(() -> service.createNotification(notification));
            doReturn(true).when(mailer).send(any());
            assertDoesNotThrow(() -> service.createNotification(notification1));
            doThrow(NotFoundException.class).doNothing().when(notificationGateway).update(any());
            assertDoesNotThrow(() -> service.createNotification(notification1));
            doNothing().doThrow(TransactionException.class).when(tx).commit();
            assertDoesNotThrow(() -> service.createNotification(notification1));
        }
    }

    @Test
    public void testDeleteAllNotificationsWhenUserIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteAllNotifications(null));
    }

    @Test
    public void testDeleteAllNotificationsWhenUserIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteAllNotifications(new User()));
    }

    @Test
    public void testDeleteAllNotificationsWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertDoesNotThrow(() -> service.deleteAllNotifications(user));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteAllNotificationsSuccess() {
        assertDoesNotThrow(() -> service.deleteAllNotifications(user));
        verify(feedbackEvent).fire(any());
    }

}