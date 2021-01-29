package tech.bugger.control.backing;

import com.sun.faces.context.RequestParameterMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.SearchService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Paginator;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;

import javax.faces.context.ExternalContext;
import java.lang.reflect.Field;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(LogExtension.class)
public class TopicBackerTest {

    private TopicBacker topicBacker;

    @Mock
    private UserSession session;

    @Mock
    private TopicService topicService;

    @Mock
    private SearchService searchService;

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    private Configuration configuration;

    @Mock
    private ExternalContext ectx;

    @Mock
    private RequestParameterMap map;

    private User user;
    private Topic topic;
    private static final String USERNAME = "Helgi";
    private static final String KEY = "id";
    private static final String ID = "1";

    @BeforeEach
    public void setUp() {
        user = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        topic = new Topic(1, "Some title", "Some description");
        topicBacker = new TopicBacker(topicService, searchService, ectx, session, applicationSettings);
        lenient().doReturn(map).when(ectx).getRequestParameterMap();
        lenient().doReturn(configuration).when(applicationSettings).getConfiguration();
    }

    @Test
    public void testInit() {
        doReturn(true).when(map).containsKey(KEY);
        doReturn(ID).when(map).get(KEY);
        doReturn(topic).when(topicService).getTopicByID(anyInt());
        topicBacker.init();
        assertAll(
                () -> assertEquals(topic, topicBacker.getTopic()),
                () -> assertEquals(topic.getId(), topicBacker.getTopicID()),
                () -> assertTrue(topicBacker.isOpenReportShown()),
                () -> assertFalse(topicBacker.isClosedReportShown()),
                () -> assertNotNull(topicBacker.getSanitizedDescription()),
                () -> assertNotNull(topicBacker.getModerators()),
                () -> assertNotNull(topicBacker.getReports()),
                () -> assertNotNull(topicBacker.getBannedUsers())
        );
    }

    @Test
    public void testInitTopicNull() {
        doReturn(true).when(map).containsKey(KEY);
        doReturn(ID).when(map).get(KEY);
        assertThrows(Error404Exception.class,
                () -> topicBacker.init()
        );
    }

    @Test
    public void testInitUserBanned() {
        doReturn(true).when(map).containsKey(KEY);
        doReturn(ID).when(map).get(KEY);
        doReturn(topic).when(topicService).getTopicByID(anyInt());
        doReturn(user).when(session).getUser();
        doReturn(true).when(topicService).isBanned(user, topic);
        assertThrows(Error404Exception.class,
                () -> topicBacker.init()
        );
    }

    @Test
    public void testInitNumberFormat() {
        doReturn(true).when(map).containsKey(KEY);
        doReturn(KEY).when(map).get(KEY);
        assertThrows(Error404Exception.class,
                () -> topicBacker.init()
        );
    }

    @Test
    public void testInitNoKey() {
        assertThrows(Error404Exception.class,
                () -> topicBacker.init()
        );
    }

    @Test
    public void testOpenDeleteDialog() {
        topicBacker.openDeleteDialog();
        assertEquals(TopicBacker.TopicDialog.DELETE, topicBacker.getTopicDialog());
    }

    @Test
    public void testOpenModDialog() {
        topicBacker.openModDialog();
        assertEquals(TopicBacker.TopicDialog.MOD, topicBacker.getTopicDialog());
    }

    @Test
    public void testOpenUnmodDialog() {
        topicBacker.openUnmodDialog();
        assertEquals(TopicBacker.TopicDialog.UNMOD, topicBacker.getTopicDialog());
    }

    @Test
    public void testOpenBanDialog() {
        topicBacker.openBanDialog();
        assertEquals(TopicBacker.TopicDialog.BAN, topicBacker.getTopicDialog());
    }

    @Test
    public void testOpenUnbanDialog() {
        topicBacker.openUnbanDialog();
        assertEquals(TopicBacker.TopicDialog.UNBAN, topicBacker.getTopicDialog());
    }

    @Test
    public void testCloseDialog() {
        topicBacker.setTopicDialog(TopicBacker.TopicDialog.BAN);
        topicBacker.closeDialog();
        assertNull(topicBacker.getTopicDialog());
    }

    @Test
    public void testUnbanSingleUser() {
        topicBacker.unbanSingleUser(USERNAME);
        assertAll(
                () -> assertEquals(USERNAME, topicBacker.getUserBan()),
                () -> assertEquals(TopicBacker.TopicDialog.UNBAN, topicBacker.getTopicDialog())
        );
    }

    @Test
    public void testUnmodSingleUser() {
        topicBacker.unmodSingleUser(USERNAME);
        assertAll(
                () -> assertEquals(USERNAME, topicBacker.getUserMod()),
                () -> assertEquals(TopicBacker.TopicDialog.UNMOD, topicBacker.getTopicDialog())
        );
    }

    @Test
    public void testIsModeratorNoModerator() {
        assertFalse(topicBacker.isModerator());
    }

    @Test
    public void testMakeModerator() throws NoSuchFieldException, IllegalAccessException {
        user.setAdministrator(true);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        when(topicService.makeModerator(USERNAME, topic)).thenReturn(true);
        Field moderators = topicBacker.getClass().getDeclaredField("moderators");
        Field banned = topicBacker.getClass().getDeclaredField("bannedUsers");
        moderators.setAccessible(true);
        moderators.set(topicBacker, mock(Paginator.class));
        banned.setAccessible(true);
        banned.set(topicBacker, mock(Paginator.class));
        assertAll(
                () -> assertEquals("", topicBacker.makeModerator()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService).makeModerator(USERNAME, topic);
    }

    @Test
    public void testIsBannedUserNull() {
        assertFalse(topicBacker.isBanned());
    }

    @Test
    public void testBanUser() throws NoSuchFieldException, IllegalAccessException {
        user.setAdministrator(true);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        when(topicService.ban(USERNAME, topic)).thenReturn(true);
        Field banned = topicBacker.getClass().getDeclaredField("bannedUsers");
        banned.setAccessible(true);
        banned.set(topicBacker, mock(Paginator.class));
        assertAll(
                () -> assertEquals("", topicBacker.banUser()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService).ban(USERNAME, topic);
    }

}
