package tech.bugger.control.backing;

import com.sun.faces.context.RequestParameterMap;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Locale;
import javax.faces.context.ExternalContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.TopicService;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class TopicEditBackerTest {

    private TopicEditBacker topicEditBacker;

    @Mock
    private TopicService topicService;

    @Mock
    private UserSession session;

    @Mock
    private ExternalContext ext;

    @Mock
    private RequestParameterMap map;

    private Field create;
    private User user;
    private Topic topic;
    private static final String KEY = "id";
    private static final String ID = "1";

    @BeforeEach
    public void setUp() throws NoSuchFieldException {
        user = new User(12345, "Helgi", "v3ry_s3cur3", "salt", "algorithm", "helga@web.de", "Helga", "Brötchen", null,
                new byte[]{1}, "Hallo, ich bin die Helgi | Perfect | He/They/Her | vergeben | Abo =|= endorsement",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, OffsetDateTime.now(), null, true);
        topic = new Topic(1, "Some title", "Some description");
        topicEditBacker = new TopicEditBacker(topicService, ext, session);
        create = topicEditBacker.getClass().getDeclaredField("create");
        create.setAccessible(true);
        lenient().doReturn(map).when(ext).getRequestParameterMap();
    }

    @Test
    public void testInit() {
        doReturn(user).when(session).getUser();
        doReturn(true).when(map).containsKey(KEY);
        doReturn(ID).when(map).get(KEY);
        doReturn(topic).when(topicService).getTopicByID(topic.getId());
        topicEditBacker.init();
        assertAll(
                () -> assertEquals(topic, topicEditBacker.getTopic()),
                () -> assertEquals(topic.getId(), topicEditBacker.getTopicID()),
                () -> assertFalse(create.getBoolean(topicEditBacker))
        );
    }

    @Test
    public void testInitCreate() {
        doReturn(user).when(session).getUser();
        topicEditBacker.init();
        assertAll(
                () -> assertNull(topicEditBacker.getTopic().getId()),
                () -> assertNull(topicEditBacker.getTopic().getTitle()),
                () -> assertNull(topicEditBacker.getTopic().getDescription()),
                () -> assertTrue(create.getBoolean(topicEditBacker))
        );
    }

    @Test
    public void testInitIdNull() {
        doReturn(user).when(session).getUser();
        doReturn(true).when(map).containsKey(KEY);
        topicEditBacker.init();
        assertAll(
                () -> assertNull(topicEditBacker.getTopic().getId()),
                () -> assertNull(topicEditBacker.getTopic().getTitle()),
                () -> assertNull(topicEditBacker.getTopic().getDescription()),
                () -> assertTrue(create.getBoolean(topicEditBacker))
        );
    }

    @Test
    public void testInitNumberFormat() {
        doReturn(user).when(session).getUser();
        doReturn(true).when(map).containsKey(KEY);
        doReturn(KEY).when(map).get(KEY);
        assertThrows(Error404Exception.class,
                () -> topicEditBacker.init()
        );
    }

    @Test
    public void testInitUserNotAdmin() {
        doReturn(user).when(session).getUser();
        user.setAdministrator(false);
        topicEditBacker.setTopicID(topic.getId());
        assertThrows(Error404Exception.class,
                () -> topicEditBacker.init()
        );
    }

    @Test
    public void testSaveChanges() throws IOException {
        doReturn(true).when(topicService).updateTopic(topic);
        topicEditBacker.setTopic(topic);
        topicEditBacker.saveChanges();
        verify(ext).redirect(any());
        verify(topicService).updateTopic(topic);
    }

    @Test
    public void testSaveChangesFalse() {
        topicEditBacker.setTopic(topic);
        assertThrows(Error404Exception.class,
                () -> topicEditBacker.saveChanges()
        );
    }

    @Test
    public void testSaveChangesCreate() throws IOException, IllegalAccessException {
        create.setBoolean(topicEditBacker, true);
        doReturn(true).when(topicService).createTopic(eq(topic), any());
        topicEditBacker.setTopic(topic);
        topicEditBacker.saveChanges();
        verify(ext).redirect(any());
        verify(topicService).createTopic(eq(topic), any());
    }

    @Test
    public void testSaveChangesCreateFalse() throws IllegalAccessException {
        create.setBoolean(topicEditBacker, true);
        topicEditBacker.setTopic(topic);
        assertThrows(Error404Exception.class,
                () -> topicEditBacker.saveChanges()
        );
    }

}
