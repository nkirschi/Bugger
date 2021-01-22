package tech.bugger.control.backing;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.NotificationService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Notification;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
class HomeBackerTest {

    private HomeBacker homeBacker;

    @Mock
    private UserSession session;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TopicService topicService;

    @Mock
    private FacesContext fctx;

    @Mock
    private ExternalContext ext;

    private final Topic testTopic1 = new Topic(1, "Hi", "senberg");
    private final Topic testTopic2 = new Topic(2, "Hi", "performance");
    private final Topic testTopic3 = new Topic(3, "Hi", "de and seek");
    private final Notification notification = new Notification();
    private static MockedStatic<FacesContext> facesMock;

    @BeforeEach
    public void setUp() {
        this.homeBacker = new HomeBacker(session, notificationService, topicService);
        facesMock = mockStatic(FacesContext.class);
        facesMock.when(FacesContext::getCurrentInstance).thenReturn(fctx);
        lenient().doReturn(ext).when(fctx).getExternalContext();
    }

    @AfterEach
    public void tearDown() {
        facesMock.close();
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
    public void testDeleteNotification() throws NoSuchFieldException, IllegalAccessException {
        Field inbox = homeBacker.getClass().getDeclaredField("inbox");
        inbox.setAccessible(true);
        inbox.set(homeBacker, mock(Paginator.class));
        homeBacker.deleteNotification(notification);
        verify(notificationService).deleteNotification(notification);
    }

    @Test
    public void testOpenNotification() throws IOException {
        notification.setPostID(100);
        assertNull(homeBacker.openNotification(notification));
        verify(ext).redirect(any());
    }

    @Test
    public void testOpenNotificationPostIdNull() throws IOException {
        notification.setTopicID(100);
        assertNull(homeBacker.openNotification(notification));
        verify(ext).redirect(any());
    }

    @Test
    public void testOpenNotificationIOException() throws IOException {
        doThrow(IOException.class).when(ext).redirect(any());
        notification.setPostID(100);
        assertEquals("pretty:error", homeBacker.openNotification(notification));
    }

    @Test
    public void testIsSubscribed() {
        assertFalse(homeBacker.isSubscribed(testTopic1));
    }

    @Test
    public void testLastChange() {
        OffsetDateTime mockDate = OffsetDateTime.now();
        doReturn(mockDate).when(topicService).lastChange(any());
        assertEquals(mockDate, homeBacker.lastChange(testTopic1));
    }

}