package tech.bugger.control.backing;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.SearchService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Paginator;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private FacesContext fctx;

    @Mock
    private ExternalContext ectx;

    private User user;
    private Topic topic;
    private static final String USERNAME = "Helgi";

    @BeforeEach
    public void setUp() {
        user = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        topic = new Topic(1, "Some title", "Some description");
        topicBacker = new TopicBacker(topicService, searchService, fctx, ectx, session);
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
    public void testIsModerator() {
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        when(topicService.isModerator(user, topic)).thenReturn(true);
        assertTrue(topicBacker.isModerator());
    }

    @Test
    public void testIsModeratorAdmin() {
        topicBacker.setTopic(topic);
        user.setAdministrator(true);
        when(session.getUser()).thenReturn(user);
        assertTrue(topicBacker.isModerator());
    }

    @Test
    public void testIsModeratorNoModerator() {
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        assertFalse(topicBacker.isModerator());
    }

    @Test
    public void testIsModeratorUserNull() {
        topicBacker.setTopic(topic);
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
        moderators.setAccessible(true);
        moderators.set(topicBacker, mock(Paginator.class));
        assertAll(
                () -> assertEquals("", topicBacker.makeModerator()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService).makeModerator(USERNAME, topic);
    }

    @Test
    public void testMakeModeratorNotPrivileged() {
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        assertAll(
                () -> assertNull(topicBacker.makeModerator()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService, times(0)).makeModerator(USERNAME, topic);
    }

    @Test
    public void testMakeModeratorUnsuccessful() {
        user.setAdministrator(true);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        topicBacker.setTopicDialog(TopicBacker.TopicDialog.MOD);
        when(session.getUser()).thenReturn(user);
        assertAll(
                () -> assertNull(topicBacker.makeModerator()),
                () -> assertEquals(TopicBacker.TopicDialog.MOD, topicBacker.getTopicDialog())
        );
        verify(topicService).makeModerator(USERNAME, topic);
    }

    @Test
    public void testRemoveModerator() throws NoSuchFieldException, IllegalAccessException {
        user.setAdministrator(true);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        when(topicService.removeModerator(USERNAME, topic)).thenReturn(true);
        Field moderators = topicBacker.getClass().getDeclaredField("moderators");
        moderators.setAccessible(true);
        moderators.set(topicBacker, mock(Paginator.class));
        assertAll(
                () -> assertEquals("", topicBacker.removeModerator()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService).removeModerator(USERNAME, topic);
    }

    @Test
    public void testRemoveModeratorNotPrivileged() {
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        assertAll(
                () -> assertNull(topicBacker.removeModerator()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService, times(0)).removeModerator(USERNAME, topic);
    }

    @Test
    public void testRemoveModeratorUnsuccessful() {
        user.setAdministrator(true);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        topicBacker.setTopicDialog(TopicBacker.TopicDialog.UNMOD);
        when(session.getUser()).thenReturn(user);
        assertAll(
                () -> assertNull(topicBacker.removeModerator()),
                () -> assertEquals(TopicBacker.TopicDialog.UNMOD, topicBacker.getTopicDialog())
        );
        verify(topicService).removeModerator(USERNAME, topic);
    }

    @Test
    public void testIsBanned() {
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        when(topicService.isBanned(user, topic)).thenReturn(true);
        assertTrue(topicBacker.isBanned());
    }

    @Test
    public void testIsBannedUserNull() {
        assertFalse(topicBacker.isBanned());
    }

    @Test
    public void testIsBannedNotBanned() {
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
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

    @Test
    public void testBanUserNotPrivileged() {
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        assertAll(
                () -> assertNull(topicBacker.banUser()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService, times(0)).ban(USERNAME, topic);
    }

    @Test
    public void testBanUserUnsuccessful() {
        user.setAdministrator(true);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        topicBacker.setTopicDialog(TopicBacker.TopicDialog.BAN);
        when(session.getUser()).thenReturn(user);
        assertAll(
                () -> assertNull(topicBacker.banUser()),
                () -> assertEquals(TopicBacker.TopicDialog.BAN, topicBacker.getTopicDialog())
        );
        verify(topicService).ban(USERNAME, topic);
    }

    @Test
    public void testUnbanUser() throws NoSuchFieldException, IllegalAccessException {
        user.setAdministrator(true);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        when(topicService.unban(USERNAME, topic)).thenReturn(true);
        Field banned = topicBacker.getClass().getDeclaredField("bannedUsers");
        banned.setAccessible(true);
        banned.set(topicBacker, mock(Paginator.class));
        assertAll(
                () -> assertEquals("", topicBacker.unbanUser()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService).unban(USERNAME, topic);
    }

    @Test
    public void testUnbanUserNotPrivileged() {
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        assertAll(
                () -> assertNull(topicBacker.unbanUser()),
                () -> assertNull(topicBacker.getTopicDialog())
        );
        verify(topicService, times(0)).unban(USERNAME, topic);
    }

    @Test
    public void testUnbanUserUnsuccessful() {
        user.setAdministrator(true);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        topicBacker.setTopicDialog(TopicBacker.TopicDialog.UNBAN);
        when(session.getUser()).thenReturn(user);
        assertAll(
                () -> assertNull(topicBacker.unbanUser()),
                () -> assertEquals(TopicBacker.TopicDialog.UNBAN, topicBacker.getTopicDialog())
        );
        verify(topicService).unban(USERNAME, topic);
    }

    @Test
    public void testSearchBanUsers() {
        List<String> users = new ArrayList<>();
        users.add(user.getUsername());
        when(searchService.getUserBanSuggestions(any(), any())).thenReturn(users);
        topicBacker.setUserBan(USERNAME);
        topicBacker.searchBanUsers();
        assertEquals(users, topicBacker.getUserBanSuggestions());
        verify(searchService).getUserBanSuggestions(any(), any());
    }

    @Test
    public void testSearchBanUsersStringNull() {
        topicBacker.searchBanUsers();
        verify(searchService, times(0)).getUserBanSuggestions(any(), any());
    }

    @Test
    public void testSearchBanUsersStringBlank() {
        topicBacker.setUserBan("");
        topicBacker.searchBanUsers();
        verify(searchService, times(0)).getUserBanSuggestions(any(), any());
    }

    @Test
    public void testSearchUnbanUsers() {
        List<String> users = new ArrayList<>();
        users.add(user.getUsername());
        when(searchService.getUserUnbanSuggestions(any(), any())).thenReturn(users);
        topicBacker.setUserBan(USERNAME);
        topicBacker.searchUnbanUsers();
        assertEquals(users, topicBacker.getUserBanSuggestions());
        verify(searchService).getUserUnbanSuggestions(any(), any());
    }

    @Test
    public void testSearchUnbanUsersStringNull() {
        topicBacker.searchUnbanUsers();
        verify(searchService, times(0)).getUserUnbanSuggestions(any(), any());
    }

    @Test
    public void testSearchUnbanUsersStringBlank() {
        topicBacker.setUserBan("");
        topicBacker.searchUnbanUsers();
        verify(searchService, times(0)).getUserUnbanSuggestions(any(), any());
    }

    @Test
    public void testSearchModUsers() {
        List<String> users = new ArrayList<>();
        users.add(user.getUsername());
        when(searchService.getUserModSuggestions(any(), any())).thenReturn(users);
        topicBacker.setUserMod(USERNAME);
        topicBacker.searchModUsers();
        assertEquals(users, topicBacker.getUserModSuggestions());
        verify(searchService).getUserModSuggestions(any(), any());
    }

    @Test
    public void testSearchModUsersStringNull() {
        topicBacker.searchModUsers();
        verify(searchService, times(0)).getUserModSuggestions(any(), any());
    }

    @Test
    public void testSearchModUsersStringBlank() {
        topicBacker.setUserMod("");
        topicBacker.searchModUsers();
        verify(searchService, times(0)).getUserModSuggestions(any(), any());
    }

    @Test
    public void testSearchUnmodUsers() {
        List<String> users = new ArrayList<>();
        users.add(user.getUsername());
        when(searchService.getUserUnmodSuggestions(any(), any())).thenReturn(users);
        topicBacker.setUserMod(USERNAME);
        topicBacker.searchUnmodUsers();
        assertEquals(users, topicBacker.getUserModSuggestions());
        verify(searchService).getUserUnmodSuggestions(any(), any());
    }

    @Test
    public void testSearchUnmodUsersStringNull() {
        topicBacker.searchUnmodUsers();
        verify(searchService, times(0)).getUserUnmodSuggestions(any(), any());
    }

    @Test
    public void testSearchUnmodUsersStringBlank() {
        topicBacker.setUserMod("");
        topicBacker.searchUnmodUsers();
        verify(searchService, times(0)).getUserUnmodSuggestions(any(), any());
    }

}
