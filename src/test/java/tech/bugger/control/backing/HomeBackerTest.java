package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.exception.DataAccessException;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.NotificationService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.MarkdownHandler;
import tech.bugger.business.util.Paginator;
import tech.bugger.business.util.Registry;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;

import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class HomeBackerTest {

    private HomeBacker homeBacker;

    @Mock
    private UserSession session;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TopicService topicService;

    @Mock
    private ExternalContext ectx;

    @Mock
    private Event<Feedback> feedbackEvent;

    @Mock
    private Registry registry;

    @Mock
    private Paginator<Notification> inboxMock;

    private final Topic testTopic1 = new Topic(1, "Hi", "senberg");
    private final Topic testTopic2 = new Topic(2, "Hi", "performance");
    private final Topic testTopic3 = new Topic(3, "Hi", "de and seek");
    private final Notification notification = new Notification();
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        lenient().doReturn(ResourceBundleMocker.mock("")).when(registry).getBundle(eq("messages"), any());
        this.homeBacker = new HomeBacker(session, notificationService, topicService, ectx, feedbackEvent, registry);
    }

    @Test
    public void testInit() {
        List<Topic> topicsMock = new ArrayList<>();
        topicsMock.add(testTopic1);
        topicsMock.add(testTopic2);
        topicsMock.add(testTopic3);
        doReturn(topicsMock).when(topicService).selectTopics(any());
        doReturn(topicsMock.size()).when(topicService).countTopics();
        homeBacker.init();
        assertAll(
                () -> assertNotNull(homeBacker.getTopics()),
                () -> assertEquals(topicsMock.size(), homeBacker.getTopics().getSelection().getTotalSize()),
                () -> assertEquals(topicsMock, homeBacker.getTopics().getWrappedData())
        );
    }

    @Test
    public void testInitUserNotNull() {
        doReturn(new User()).when(session).getUser();
        homeBacker.init();
        assertAll(
                () -> assertNotNull(homeBacker.getInbox()),
                () -> assertNotNull(homeBacker.getTopics())
        );
    }

    @Test
    public void testInboxWhenDataAccessExceptionDuringSelect() throws Exception {
        doThrow(DataAccessException.class).when(notificationService).selectNotifications(any(), any());
        doReturn(42).when(notificationService).countNotifications(any());
        doReturn(user).when(session).getUser();
        assertDoesNotThrow(() -> homeBacker.init());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testInboxWhenDataAccessExceptionDuringCount() throws Exception {
        doThrow(DataAccessException.class).when(notificationService).countNotifications(any());
        doReturn(user).when(session).getUser();
        assertDoesNotThrow(() -> homeBacker.init());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteNotification() throws Exception {
        setupInbox();
        doReturn(true).when(notificationService).deleteNotification(any());
        assertNull(homeBacker.deleteNotification(notification));
        verify(notificationService).deleteNotification(notification);
        verify(inboxMock).updateReset();
    }

    @Test
    public void testDeleteNotificationWhenDataAccessException() throws Exception {
        setupInbox();
        doThrow(DataAccessException.class).when(notificationService).deleteNotification(any());
        assertNull(homeBacker.deleteNotification(notification));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteNotificationWHenServiceFails() throws Exception {
        setupInbox();
        assertNull(homeBacker.deleteNotification(notification));
        verify(feedbackEvent).fire(any());
    }

    private void setupInbox() throws NoSuchFieldException, IllegalAccessException {
        Field inbox = homeBacker.getClass().getDeclaredField("inbox");
        inbox.setAccessible(true);
        inbox.set(homeBacker, inboxMock);
    }

    @Test
    public void testOpenNotification() throws Exception {
        notification.setPostID(100);
        doReturn(true).when(notificationService).markAsRead(any());
        assertNull(homeBacker.openNotification(notification));
        verify(ectx).redirect(any());
    }

    @Test
    public void testOpenNotificationPostIdNull() throws Exception {
        notification.setTopicID(100);
        doReturn(true).when(notificationService).markAsRead(any());
        assertNull(homeBacker.openNotification(notification));
        verify(ectx).redirect(any());
    }

    @Test
    public void testOpenNotificationIOException() throws Exception {
        doThrow(IOException.class).when(ectx).redirect(any());
        doReturn(true).when(notificationService).markAsRead(any());
        notification.setPostID(100);
        assertThrows(Error404Exception.class, () -> homeBacker.openNotification(notification));
    }

    @Test
    public void testOpenNotificationWhenMarkAsReadFails() {
        assertNull(homeBacker.openNotification(notification));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testOpenNotificationWhenDataAccessException() throws Exception {
        doThrow(DataAccessException.class).when(notificationService).markAsRead(any());
        assertNull(homeBacker.openNotification(notification));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsSubscribed() {
        assertFalse(homeBacker.isSubscribed(testTopic1));
    }

    @Test
    public void testIsSubscribedWhenUserIsSubscribed() {
        doReturn(user).when(session).getUser();
        doReturn(true).when(topicService).isSubscribed(any(), any());
        assertTrue(homeBacker.isSubscribed(testTopic1));
    }

    @Test
    public void testLastChange() {
        OffsetDateTime mockDate = OffsetDateTime.now();
        doReturn(mockDate).when(topicService).lastChange(any());
        assertEquals(mockDate, homeBacker.lastChange(testTopic1));
    }

    @Test
    public void testGetDescriptionWhenDescriptionIsNull() {
        assertEquals("", homeBacker.getDescription(new Topic()));
    }

    @Test
    public void testGetDescription() {
        try (MockedStatic<MarkdownHandler> markdownHandlerMockedStatic = mockStatic(MarkdownHandler.class)) {
            markdownHandlerMockedStatic.when(() -> MarkdownHandler.toHtml("senberg")).thenReturn("Walter White");
            assertEquals("Walter White", homeBacker.getDescription(testTopic1));
        }
    }

    @Test
    public void testGetHelpSuffix() {
        doReturn(user).when(session).getUser();
        assertEquals("_user", homeBacker.getHelpSuffix());
    }

    @Test
    public void testGetHelpSuffixAdmin() {
        user.setAdministrator(true);
        doReturn(user).when(session).getUser();
        assertEquals("_admin", homeBacker.getHelpSuffix());
    }

    @Test
    public void testGetHelpSuffixNoUser() {
        assertEquals("", homeBacker.getHelpSuffix());
    }

}