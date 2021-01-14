package tech.bugger.control.backing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ReportService;
import tech.bugger.business.service.SearchService;
import tech.bugger.business.service.TopicService;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;

import javax.faces.context.FacesContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@ExtendWith(LogExtension.class)
public class TopicBackerTest {

    @InjectMocks
    private TopicBacker topicBacker;

    @Mock
    private UserSession session;

    @Mock
    private TopicService topicService;

    @Mock
    private ReportService reportService;

    @Mock
    private SearchService searchService;

    @Mock
    private FacesContext fctx;

    private User user;
    private Topic topic;
    private static final String USERNAME = "Helgi";

    @BeforeEach
    public void setUp() throws Exception {
        user = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User", new Lazy<>(new byte[]{1, 2, 3, 4}), new byte[]{1}, "# I am a test user.",
                Language.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        topic = new Topic(1, "Some title", "Some description");
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testOpenModDialog() {
        topicBacker.openModDialog();
        assertEquals(TopicBacker.DialogType.MOD, topicBacker.getDisplayDialog());
    }

    @Test
    public void testOpenUnmodDialog() {
        topicBacker.openUnmodDialog();
        assertEquals(TopicBacker.DialogType.UNMOD, topicBacker.getDisplayDialog());
    }

    @Test
    public void testOpenBanDialog() {
        topicBacker.openBanDialog();
        assertEquals(TopicBacker.DialogType.BAN, topicBacker.getDisplayDialog());
    }

    @Test
    public void testOpenUnbanDialog() {
        topicBacker.openUnbanDialog();
        assertEquals(TopicBacker.DialogType.UNBAN, topicBacker.getDisplayDialog());
    }

    @Test
    public void testOpenDeleteDialog() {
        topicBacker.openDeleteDialog();
        assertEquals(TopicBacker.DialogType.DELETE, topicBacker.getDisplayDialog());
    }

    @Test
    public void testCloseDialog() {
        topicBacker.setDisplayDialog(TopicBacker.DialogType.BAN);
        topicBacker.closeDialog();
        assertEquals(TopicBacker.DialogType.NONE, topicBacker.getDisplayDialog());
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
    public void testMakeModerator() {
        user.setAdministrator(true);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        when(topicService.makeModerator(USERNAME, topic)).thenReturn(true);
        assertAll(
                () -> assertEquals("", topicBacker.makeModerator()),
                () -> assertEquals(TopicBacker.DialogType.NONE, topicBacker.getDisplayDialog())
        );
        verify(topicService).makeModerator(USERNAME, topic);
    }

    @Test
    public void testMakeModeratorNotPrivileged() {
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        assertAll(
                () -> assertNull(topicBacker.makeModerator()),
                () -> assertEquals(TopicBacker.DialogType.NONE, topicBacker.getDisplayDialog())
        );
        verify(topicService, times(0)).makeModerator(USERNAME, topic);
    }

    @Test
    public void testMakeModeratorUnsuccessful() {
        user.setAdministrator(true);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        topicBacker.setDisplayDialog(TopicBacker.DialogType.MOD);
        when(session.getUser()).thenReturn(user);
        assertAll(
                () -> assertNull(topicBacker.makeModerator()),
                () -> assertEquals(TopicBacker.DialogType.MOD, topicBacker.getDisplayDialog())
        );
        verify(topicService).makeModerator(USERNAME, topic);
    }

    @Test
    public void testRemoveModerator() {
        user.setAdministrator(true);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        when(topicService.removeModerator(USERNAME, topic)).thenReturn(true);
        assertAll(
                () -> assertEquals("", topicBacker.removeModerator()),
                () -> assertEquals(TopicBacker.DialogType.NONE, topicBacker.getDisplayDialog())
        );
        verify(topicService).removeModerator(USERNAME, topic);
    }

    @Test
    public void testRemoveModeratorNotPrivileged() {
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        assertAll(
                () -> assertNull(topicBacker.removeModerator()),
                () -> assertEquals(TopicBacker.DialogType.NONE, topicBacker.getDisplayDialog())
        );
        verify(topicService, times(0)).removeModerator(USERNAME, topic);
    }

    @Test
    public void testRemoveModeratorUnsuccessful() {
        user.setAdministrator(true);
        topicBacker.setUserMod(USERNAME);
        topicBacker.setTopic(topic);
        topicBacker.setDisplayDialog(TopicBacker.DialogType.UNMOD);
        when(session.getUser()).thenReturn(user);
        assertAll(
                () -> assertNull(topicBacker.removeModerator()),
                () -> assertEquals(TopicBacker.DialogType.UNMOD, topicBacker.getDisplayDialog())
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
    public void testBanUser() {
        user.setAdministrator(true);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        when(topicService.ban(USERNAME, topic)).thenReturn(true);
        assertAll(
                () -> assertEquals("", topicBacker.banUser()),
                () -> assertEquals(TopicBacker.DialogType.NONE, topicBacker.getDisplayDialog())
        );
        verify(topicService).ban(USERNAME, topic);
    }

    @Test
    public void testBanUserNotPrivileged() {
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        assertAll(
                () -> assertNull(topicBacker.banUser()),
                () -> assertEquals(TopicBacker.DialogType.NONE, topicBacker.getDisplayDialog())
        );
        verify(topicService, times(0)).ban(USERNAME, topic);
    }

    @Test
    public void testBanUserUnsuccessful() {
        user.setAdministrator(true);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        topicBacker.setDisplayDialog(TopicBacker.DialogType.BAN);
        when(session.getUser()).thenReturn(user);
        assertAll(
                () -> assertNull(topicBacker.banUser()),
                () -> assertEquals(TopicBacker.DialogType.BAN, topicBacker.getDisplayDialog())
        );
        verify(topicService).ban(USERNAME, topic);
    }

    @Test
    public void testUnbanUser() {
        user.setAdministrator(true);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        when(session.getUser()).thenReturn(user);
        when(topicService.unban(USERNAME, topic)).thenReturn(true);
        assertAll(
                () -> assertEquals("", topicBacker.unbanUser()),
                () -> assertEquals(TopicBacker.DialogType.NONE, topicBacker.getDisplayDialog())
        );
        verify(topicService).unban(USERNAME, topic);
    }

    @Test
    public void testUnbanUserNotPrivileged() {
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        assertAll(
                () -> assertNull(topicBacker.unbanUser()),
                () -> assertEquals(TopicBacker.DialogType.NONE, topicBacker.getDisplayDialog())
        );
        verify(topicService, times(0)).unban(USERNAME, topic);
    }

    @Test
    public void testUnbanUserUnsuccessful() {
        user.setAdministrator(true);
        topicBacker.setUserBan(USERNAME);
        topicBacker.setTopic(topic);
        topicBacker.setDisplayDialog(TopicBacker.DialogType.UNBAN);
        when(session.getUser()).thenReturn(user);
        assertAll(
                () -> assertNull(topicBacker.unbanUser()),
                () -> assertEquals(TopicBacker.DialogType.UNBAN, topicBacker.getDisplayDialog())
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
