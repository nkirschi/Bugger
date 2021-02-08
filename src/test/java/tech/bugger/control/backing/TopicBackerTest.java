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
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;

import javax.faces.context.ExternalContext;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    private static final String USERNAME = "testuser";
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
    public void testSearchBanUser() {
        List<String> usernames = new ArrayList<>();
        usernames.add(user.getUsername());
        doReturn(usernames).when(searchService).getUserBanSuggestions(USERNAME, topic);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        topicBacker.searchBanUsers();
        assertTrue(topicBacker.getUserBanSuggestions().contains(USERNAME));
    }

    @Test
    public void testSearchBanUserNull() {
        topicBacker.searchBanUsers();
        assertNull(topicBacker.getUserBanSuggestions());
    }

    @Test
    public void testSearchBanUserBlank() {
        topicBacker.setUserBan("");
        topicBacker.searchBanUsers();
        assertNull(topicBacker.getUserBanSuggestions());
    }

    @Test
    public void testSearchUnbanUser() {
        List<String> usernames = new ArrayList<>();
        usernames.add(user.getUsername());
        doReturn(usernames).when(searchService).getUserUnbanSuggestions(USERNAME, topic);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        topicBacker.searchUnbanUsers();
        assertTrue(topicBacker.getUserBanSuggestions().contains(USERNAME));
    }

    @Test
    public void testSearchUnbanUserNull() {
        topicBacker.searchUnbanUsers();
        assertNull(topicBacker.getUserBanSuggestions());
    }

    @Test
    public void testSearchUnbanUserBlank() {
        topicBacker.setUserBan("");
        topicBacker.searchUnbanUsers();
        assertNull(topicBacker.getUserBanSuggestions());
    }

    @Test
    public void testSearchModUser() {
        List<String> usernames = new ArrayList<>();
        usernames.add(user.getUsername());
        doReturn(usernames).when(searchService).getUserModSuggestions(USERNAME, topic);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        topicBacker.searchModUsers();
        assertTrue(topicBacker.getUserModSuggestions().contains(USERNAME));
    }

    @Test
    public void testSearchModUserNull() {
        topicBacker.searchModUsers();
        assertNull(topicBacker.getUserModSuggestions());
    }

    @Test
    public void testSearchModUserBlank() {
        topicBacker.setUserMod("");
        topicBacker.searchModUsers();
        assertNull(topicBacker.getUserModSuggestions());
    }

    @Test
    public void testSearchUnmodUser() {
        List<String> usernames = new ArrayList<>();
        usernames.add(user.getUsername());
        doReturn(usernames).when(searchService).getUserUnmodSuggestions(USERNAME, topic);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        topicBacker.searchUnmodUsers();
        assertTrue(topicBacker.getUserModSuggestions().contains(USERNAME));
    }

    @Test
    public void testSearchUnmodUserNull() {
        topicBacker.searchUnmodUsers();
        assertNull(topicBacker.getUserModSuggestions());
    }

    @Test
    public void testSearchUnmodUserBlank() {
        topicBacker.setUserMod("");
        topicBacker.searchUnmodUsers();
        assertNull(topicBacker.getUserModSuggestions());
    }

    @Test
    public void testApplyFilters() throws NoSuchFieldException, IllegalAccessException {
        Paginator<Report> reports = mock(Paginator.class);
        Field field = topicBacker.getClass().getDeclaredField("reports");
        field.setAccessible(true);
        field.set(topicBacker, reports);
        topicBacker.applyFilters();
        verify(reports).updateReset();
    }

    @Test
    public void testApplyFiltersReportsNull() {
        topicBacker.applyFilters();
        assertNull(topicBacker.getReports());
    }

    @Test
    public void testOpenDeleteDialog() {
        assertNull(topicBacker.openDeleteDialog());
        assertEquals(TopicBacker.TopicDialog.DELETE, topicBacker.getTopicDialog());
    }

    @Test
    public void testOpenModDialog() {
        assertNull(topicBacker.openModDialog());
        assertEquals(TopicBacker.TopicDialog.MOD, topicBacker.getTopicDialog());
    }

    @Test
    public void testOpenUnmodDialog() {
        assertNull(topicBacker.openUnmodDialog());
        assertEquals(TopicBacker.TopicDialog.UNMOD, topicBacker.getTopicDialog());
    }

    @Test
    public void testOpenBanDialog() {
        assertNull(topicBacker.openBanDialog());
        assertEquals(TopicBacker.TopicDialog.BAN, topicBacker.getTopicDialog());
    }

    @Test
    public void testOpenUnbanDialog() {
        assertNull(topicBacker.openUnbanDialog());
        assertEquals(TopicBacker.TopicDialog.UNBAN, topicBacker.getTopicDialog());
    }

    @Test
    public void testCloseDialog() {
        topicBacker.setTopicDialog(TopicBacker.TopicDialog.BAN);
        assertNull(topicBacker.closeDialog());
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
    public void testIsModeratorAdmin() throws NoSuchFieldException, IllegalAccessException {
        Field field = topicBacker.getClass().getDeclaredField("administrator");
        field.setAccessible(true);
        field.set(topicBacker, true);
        assertTrue(topicBacker.isModerator());
    }

    @Test
    public void testIsModeratorMod() throws NoSuchFieldException, IllegalAccessException {
        Field field = topicBacker.getClass().getDeclaredField("moderator");
        field.setAccessible(true);
        field.set(topicBacker, true);
        assertTrue(topicBacker.isModerator());
    }

    @Test
    public void testIsModeratorNoModerator() {
        assertFalse(topicBacker.isModerator());
    }

    @Test
    public void testIsBannedUserNull() {
        assertFalse(topicBacker.isBanned());
    }

    @Test
    public void testDelete() {
        topicBacker.setTopic(topic);
        assertEquals("pretty:home", topicBacker.delete());
        verify(topicService).deleteTopic(topic);
    }

    @Test
    public void testBanUserAdmin() throws NoSuchFieldException, IllegalAccessException {
        user.setAdministrator(true);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        when(topicService.ban(USERNAME, topic)).thenReturn(true);
        Paginator<User> bannedUsers = mock(Paginator.class);
        Field banned = topicBacker.getClass().getDeclaredField("bannedUsers");
        banned.setAccessible(true);
        banned.set(topicBacker, bannedUsers);
        assertAll(
                () -> assertEquals("", topicBacker.banUser()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService).ban(USERNAME, topic);
        verify(bannedUsers).update();
    }

    @Test
    public void testBanUserMod() throws NoSuchFieldException, IllegalAccessException {
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        doReturn(user).when(session).getUser();
        doReturn(true).when(topicService).isModerator(user, topic);
        doReturn(true).when(topicService).ban(USERNAME, topic);
        Field banned = topicBacker.getClass().getDeclaredField("bannedUsers");
        banned.setAccessible(true);
        banned.set(topicBacker, mock(Paginator.class));
        assertEquals("", topicBacker.banUser());
        verify(topicService).ban(USERNAME, topic);
    }

    @Test
    public void testBanUserNoMod() {
        doReturn(user).when(session).getUser();
        assertAll(
                () -> assertNull(topicBacker.banUser()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService, never()).ban(USERNAME, topic);
    }

    @Test
    public void testBanUserNotBanned() {
        user.setAdministrator(true);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        assertNull(topicBacker.banUser());
    }

    @Test
    public void testUnbanUserAdmin() throws NoSuchFieldException, IllegalAccessException {
        user.setAdministrator(true);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        doReturn(user).when(session).getUser();
        doReturn(true).when(topicService).unban(USERNAME, topic);
        Paginator<User> bannedUsers = mock(Paginator.class);
        Field banned = topicBacker.getClass().getDeclaredField("bannedUsers");
        banned.setAccessible(true);
        banned.set(topicBacker, bannedUsers);
        assertAll(
                () -> assertEquals("", topicBacker.unbanUser()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService).unban(USERNAME, topic);
        verify(bannedUsers).update();
    }

    @Test
    public void testUnbanUserMod() throws NoSuchFieldException, IllegalAccessException {
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        doReturn(user).when(session).getUser();
        doReturn(true).when(topicService).isModerator(user, topic);
        doReturn(true).when(topicService).unban(USERNAME, topic);
        Field banned = topicBacker.getClass().getDeclaredField("bannedUsers");
        banned.setAccessible(true);
        banned.set(topicBacker, mock(Paginator.class));
        assertEquals("", topicBacker.unbanUser());
        verify(topicService).unban(USERNAME, topic);
    }

    @Test
    public void testUnbanUserNoMod() {
        doReturn(user).when(session).getUser();
        assertAll(
                () -> assertNull(topicBacker.unbanUser()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService, never()).unban(USERNAME, topic);
    }

    @Test
    public void testUnbanUserNotBanned() {
        user.setAdministrator(true);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        assertNull(topicBacker.unbanUser());
    }

    @Test
    public void testMakeModeratorAdmin() throws NoSuchFieldException, IllegalAccessException {
        user.setAdministrator(true);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        when(topicService.makeModerator(USERNAME, topic)).thenReturn(true);
        Paginator<User> mods = mock(Paginator.class);
        Field moderators = topicBacker.getClass().getDeclaredField("moderators");
        Paginator<User> bannedUsers = mock(Paginator.class);
        Field banned = topicBacker.getClass().getDeclaredField("bannedUsers");
        moderators.setAccessible(true);
        moderators.set(topicBacker, mods);
        banned.setAccessible(true);
        banned.set(topicBacker, bannedUsers);
        assertAll(
                () -> assertEquals("", topicBacker.makeModerator()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService).makeModerator(USERNAME, topic);
        verify(mods).update();
        verify(bannedUsers).update();
    }

    @Test
    public void testMakeModeratorMod() throws NoSuchFieldException, IllegalAccessException {
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        doReturn(user).when(session).getUser();
        doReturn(true).when(topicService).isModerator(user, topic);
        doReturn(true).when(topicService).makeModerator(USERNAME, topic);
        Field banned = topicBacker.getClass().getDeclaredField("bannedUsers");
        banned.setAccessible(true);
        banned.set(topicBacker, mock(Paginator.class));
        Field moderators = topicBacker.getClass().getDeclaredField("moderators");
        moderators.setAccessible(true);
        moderators.set(topicBacker, mock(Paginator.class));
        assertEquals("", topicBacker.makeModerator());
        verify(topicService).makeModerator(USERNAME, topic);
    }

    @Test
    public void testMakeModeratorNoMod() {
        doReturn(user).when(session).getUser();
        assertAll(
                () -> assertNull(topicBacker.makeModerator()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService, never()).makeModerator(USERNAME, topic);
    }

    @Test
    public void testMakeModeratorNotPromoted() {
        user.setAdministrator(true);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        assertNull(topicBacker.makeModerator());
    }

    @Test
    public void testRemoveModeratorAdmin() throws NoSuchFieldException, IllegalAccessException {
        user.setAdministrator(true);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        doReturn(user).when(session).getUser();
        doReturn(true).when(topicService).removeModerator(USERNAME, topic);
        Paginator<User> mods = mock(Paginator.class);
        Field moderators = topicBacker.getClass().getDeclaredField("moderators");
        moderators.setAccessible(true);
        moderators.set(topicBacker, mods);
        assertAll(
                () -> assertEquals("", topicBacker.removeModerator()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService).removeModerator(USERNAME, topic);
        verify(mods).update();
    }

    @Test
    public void testRemoveModeratorMod() throws NoSuchFieldException, IllegalAccessException {
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        doReturn(user).when(session).getUser();
        doReturn(true).when(topicService).isModerator(user, topic);
        doReturn(true).when(topicService).removeModerator(USERNAME, topic);
        Field banned = topicBacker.getClass().getDeclaredField("bannedUsers");
        banned.setAccessible(true);
        banned.set(topicBacker, mock(Paginator.class));
        Field moderators = topicBacker.getClass().getDeclaredField("moderators");
        moderators.setAccessible(true);
        moderators.set(topicBacker, mock(Paginator.class));
        assertEquals("", topicBacker.removeModerator());
        verify(topicService).removeModerator(USERNAME, topic);
    }

    @Test
    public void testRemoveModeratorNoMod() {
        doReturn(user).when(session).getUser();
        assertAll(
                () -> assertNull(topicBacker.removeModerator()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService, never()).removeModerator(USERNAME, topic);
    }

    @Test
    public void testRemoveModeratorNotPromoted() {
        user.setAdministrator(true);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        assertNull(topicBacker.removeModerator());
    }

    @Test
    public void toggleTopicSubscriptionSubscribe() {
        doReturn(user).when(session).getUser();
        topicBacker.setTopic(topic);
        assertNull(topicBacker.toggleTopicSubscription());
        verify(topicService).subscribeToTopic(user, topic);
    }

    @Test
    public void toggleTopicSubscriptionUnsubscribe() throws NoSuchFieldException, IllegalAccessException {
        doReturn(user).when(session).getUser();
        Field field = topicBacker.getClass().getDeclaredField("subscribed");
        field.setAccessible(true);
        field.set(topicBacker, true);
        topicBacker.setTopic(topic);
        assertNull(topicBacker.toggleTopicSubscription());
        verify(topicService).unsubscribeFromTopic(user, topic);
    }

    @Test
    public void toggleTopicSubscriptionUserNull() {
        assertNull(topicBacker.toggleTopicSubscription());
        verify(topicService, never()).unsubscribeFromTopic(user, topic);
        verify(topicService, never()).subscribeToTopic(user, topic);
    }

    @Test
    public void testGetHelpSuffixAnonymousUser() {
        doReturn(null).when(session).getUser();
        assertEquals("", topicBacker.getHelpSuffix());
    }

    @Test
    public void testGetHelpSuffixNormalUser() {
        doReturn(user).when(session).getUser();
        assertEquals("", topicBacker.getHelpSuffix());
    }

    @Test
    public void testGetHelpSuffixMod() throws Exception {
        doReturn(user).when(session).getUser();

        Field f = TopicBacker.class.getDeclaredField("moderator");
        f.setAccessible(true);
        f.set(topicBacker, true);

        assertEquals("_mod", topicBacker.getHelpSuffix());
    }

    @Test
    public void testGetHelpSuffixAdmin() {
        user.setAdministrator(true);
        doReturn(user).when(session).getUser();
        assertEquals("_admin", topicBacker.getHelpSuffix());
    }

    @Test
    public void testSettersForCoverage() {
        List<String> suggestions = new ArrayList<>();
        suggestions.add(user.getUsername());
        topicBacker.setOpenReportShown(true);
        topicBacker.setClosedReportShown(true);
        topicBacker.setTopicID(topic.getId());
        topicBacker.setUserModSuggestions(suggestions);
        topicBacker.setUserBanSuggestions(suggestions);
        assertAll(
                () -> assertTrue(topicBacker.isOpenReportShown()),
                () -> assertTrue(topicBacker.isClosedReportShown()),
                () -> assertEquals(topic.getId(), topicBacker.getTopicID()),
                () -> assertEquals(suggestions, topicBacker.getUserModSuggestions()),
                () -> assertEquals(suggestions, topicBacker.getUserBanSuggestions())
        );
    }

}
