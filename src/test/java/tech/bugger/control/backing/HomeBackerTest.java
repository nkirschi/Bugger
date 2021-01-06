package tech.bugger.control.backing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.NotificationService;
import tech.bugger.business.service.TopicService;
import tech.bugger.global.transfer.Topic;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class HomeBackerTest {

    private HomeBacker homeBacker;

    @Mock
    private UserSession session;

    @Mock
    private NotificationService notificationService;

    @Mock
    private TopicService topicService;

    private Topic testTopic1 = new Topic(1, "Hi", "senberg");
    private Topic testTopic2 = new Topic(2, "Hi", "performance");
    private Topic testTopic3 = new Topic(3, "Hi", "de and seek");

    @BeforeEach
    public void setUp() {
        this.homeBacker = new HomeBacker(session, notificationService, topicService);
    }

    @AfterEach
    public void tearDown() {
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
    public void testLastChange() {
        ZonedDateTime mockDate = ZonedDateTime.now();
        doReturn(mockDate).when(topicService).lastChange(any());
        assertEquals(mockDate, homeBacker.lastChange(testTopic1));
    }
}