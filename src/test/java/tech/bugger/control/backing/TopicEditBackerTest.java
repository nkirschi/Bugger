package tech.bugger.control.backing;

import com.sun.faces.context.RequestParameterMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.TopicService;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;

import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class TopicEditBackerTest {

    private TopicEditBacker topicEditBacker;

    @Mock
    private TopicService topicService;

    @Mock
    private FacesContext fctx;

    @Mock
    private UserSession session;

    @Mock
    private ExternalContext ext;

    @Mock
    private RequestParameterMap map;

    @Mock
    private NavigationHandler navHandler;

    @Mock
    private Application application;

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
        topicEditBacker = new TopicEditBacker(topicService, fctx, session);
        create = topicEditBacker.getClass().getDeclaredField("create");
        create.setAccessible(true);
        lenient().doReturn(ext).when(fctx).getExternalContext();
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
        doReturn(application).when(fctx).getApplication();
        doReturn(navHandler).when(application).getNavigationHandler();
        doReturn(true).when(map).containsKey(KEY);
        doReturn(KEY).when(map).get(KEY);
        topicEditBacker.init();
        verify(navHandler).handleNavigation(any(), any(), any());
    }

    @Test
    public void testInitUserNotAdmin() {
        user.setAdministrator(false);
        topicEditBacker.setTopicID(topic.getId());
        doReturn(application).when(fctx).getApplication();
        doReturn(navHandler).when(application).getNavigationHandler();
        doReturn(user).when(session).getUser();
        topicEditBacker.init();
        verify(navHandler).handleNavigation(any(), any(), any());
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
    public void testSaveChangesFalse() throws IOException {
        doReturn(application).when(fctx).getApplication();
        doReturn(navHandler).when(application).getNavigationHandler();
        topicEditBacker.setTopic(topic);
        topicEditBacker.saveChanges();
        verify(navHandler).handleNavigation(any(), any(), any());
        verify(topicService).updateTopic(topic);
    }

    @Test
    public void testSaveChangesCreate() throws IOException, IllegalAccessException {
        create.setBoolean(topicEditBacker, true);
        doReturn(true).when(topicService).createTopic(topic);
        topicEditBacker.setTopic(topic);
        topicEditBacker.saveChanges();
        verify(ext).redirect(any());
        verify(topicService).createTopic(topic);
    }

    @Test
    public void testSaveChangesCreateFalse() throws IOException, IllegalAccessException {
        doReturn(application).when(fctx).getApplication();
        doReturn(navHandler).when(application).getNavigationHandler();
        create.setBoolean(topicEditBacker, true);
        topicEditBacker.setTopic(topic);
        topicEditBacker.saveChanges();
        verify(navHandler).handleNavigation(any(), any(), any());
        verify(topicService).createTopic(topic);
    }

}
