package tech.bugger.control.backing;

import com.sun.faces.context.RequestParameterMap;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ProfileService;
import tech.bugger.business.service.TopicService;
import tech.bugger.business.util.Paginator;
import tech.bugger.control.exception.Error404Exception;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;

import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@ExtendWith(LogExtension.class)
public class ProfileBackerTest {

    @InjectMocks
    private ProfileBacker profileBacker;

    @Mock
    private ProfileService profileService;

    @Mock
    private TopicService topicService;

    @Mock
    private UserSession session;

    @Mock
    private FacesContext fctx;

    @Mock
    private ExternalContext context;

    @Mock
    private RequestParameterMap map;

    @Mock
    private NavigationHandler navHandler;

    @Mock
    private Application application;

    private User user;
    private User otherUser;
    private Topic topic;
    private Report report;
    private static final int THE_ANSWER = 42;
    private static final String PARAMETER = "u";

    @BeforeEach
    public void setup() {
        user = new User(12345, "Helgi", "v3ry_s3cur3", "salt", "algorithm", "helga@web.de", "Helga", "Brötchen", null,
                new byte[]{1}, "Hallo, ich bin die Helgi | Perfect | He/They/Her | vergeben | Abo =|= endorsement",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, OffsetDateTime.now(), null, false);
        otherUser = new User(67890, "Helgo", "v3ry_s3cur3", "salt", "algorithm", "helgo@web.de", "Helgo", "Brötchen", null,
                new byte[]{1}, "Hallo, ich bin der Helgo | Perfect | He/They/Her | vergeben | Abo =|= endorsement",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, OffsetDateTime.now(), null, false);
        topic = new Topic(1, "Some title", "Some description");
        report = new Report(100, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "", mock(Authorship.class),
                mock(OffsetDateTime.class), null, null, false, 1);
        MockitoAnnotations.openMocks(this);
        profileBacker = new ProfileBacker(topicService, profileService, session, fctx);
        when(fctx.getExternalContext()).thenReturn(context);
        when(context.getRequestParameterMap()).thenReturn(map);
        when(fctx.getApplication()).thenReturn(application);
        when(application.getNavigationHandler()).thenReturn(navHandler);
    }

    /*@Test
    public void testInit() {
        when(map.containsKey(PARAMETER)).thenReturn(true);
        when(map.get(PARAMETER)).thenReturn(user.getUsername());
        profileBacker.setUsername(user.getUsername());
        when(profileService.getUserByUsername(user.getUsername())).thenReturn(user);
        profileBacker.init();
        assertAll(
                () -> assertEquals(user.getUsername(), profileBacker.getUsername()),
                () -> assertEquals(user, profileBacker.getUser()),
                () -> assertEquals(profileBacker.getProfileDialog(), ProfileBacker.ProfileDialog.NONE),
                () -> assertEquals(Selection.PageSize.SMALL,
                        profileBacker.getModeratedTopics().getSelection().getPageSize()),
                () -> assertEquals("title", profileBacker.getModeratedTopics().getSelection().getSortedBy())
        );
    }*/

    @Test
    public void testInitEqualUser() {
        when(map.containsKey(PARAMETER)).thenReturn(true);
        when(map.get(PARAMETER)).thenReturn(user.getUsername());
        profileBacker.setUsername(user.getUsername());
        when(profileService.getUserByUsername(user.getUsername())).thenReturn(user);
        when(session.getUser()).thenReturn(user);
        profileBacker.init();
        assertAll(
                () -> assertEquals(user, profileBacker.getUser()),
                () -> assertEquals(session.getUser(), profileBacker.getUser())
        );
    }

    @Test
    public void testInitNotEqualUser() {
        when(map.containsKey(PARAMETER)).thenReturn(true);
        when(map.get(PARAMETER)).thenReturn(user.getUsername());
        profileBacker.setUsername(user.getUsername());
        User sessionUser = new User(user);
        sessionUser.setId(23456);
        when(profileService.getUserByUsername(user.getUsername())).thenReturn(user);
        when(session.getUser()).thenReturn(sessionUser);
        profileBacker.init();
        assertAll(
                () -> assertEquals(user, profileBacker.getUser()),
                () -> assertNotEquals(session.getUser(), profileBacker.getUser())
        );
    }

    @Test
    public void testInitKeyNotPresent() {
        assertThrows(Error404Exception.class, () -> profileBacker.init());
    }

    @Test
    public void testInitUserSessionNotNull() {
        when(session.getUser()).thenReturn(user);
        when(profileService.getUserByUsername(user.getUsername())).thenReturn(user);
        profileBacker.init();
        assertAll(
                () -> assertEquals(user, profileBacker.getUser()),
                () -> assertEquals(session.getUser(), profileBacker.getUser())
        );
    }

    @Test
    public void testInitUserBioNull() {
        when(map.containsKey(PARAMETER)).thenReturn(true);
        when(map.get(PARAMETER)).thenReturn(user.getUsername());
        when(profileService.getUserByUsername(user.getUsername())).thenReturn(user);
        user.setBiography(null);
        profileBacker.init();
        assertNull(profileBacker.getSanitizedBiography());
    }

    @Test
    public void testOpenPromoteDemoteAdminDialog() {
        profileBacker.openPromoteDemoteAdminDialog();
        assertEquals(ProfileBacker.ProfileDialog.ADMIN, profileBacker.getProfileDialog());
    }

    @Test
    public void testOpenDeleteAllTopicSubscriptionsDialog() {
        profileBacker.openDeleteAllTopicSubscriptionsDialog();
        assertEquals(ProfileBacker.ProfileDialog.TOPIC, profileBacker.getProfileDialog());
    }

    @Test
    public void testOpenDeleteAllReportSubscriptionsDialog() {
        profileBacker.openDeleteAllReportSubscriptionsDialog();
        assertEquals(ProfileBacker.ProfileDialog.REPORT, profileBacker.getProfileDialog());
    }

    @Test
    public void testOpenDeleteAllUserSubscriptionsDialog() {
        profileBacker.openDeleteAllUserSubscriptionsDialog();
        assertEquals(ProfileBacker.ProfileDialog.USER, profileBacker.getProfileDialog());
    }

    @Test
    public void testCloseDialog() {
        profileBacker.setProfileDialog(ProfileBacker.ProfileDialog.ADMIN);
        profileBacker.closeDialog();
        assertEquals(ProfileBacker.ProfileDialog.NONE, profileBacker.getProfileDialog());
    }

    /*@Test
    public void testGetVotingWeight() {
        profileBacker.setUser(user);
        when(profileService.getVotingWeightForUser(user)).thenReturn(THE_ANSWER);
        int votingWeight = profileBacker.getVotingWeight();
        assertEquals(THE_ANSWER, votingWeight);
        verify(profileService, times(1)).getVotingWeightForUser(user);
    }

    @Test
    public void testGetNumberOfPosts() {
        profileBacker.setUser(user);
        when(profileService.getNumberOfPostsForUser(user)).thenReturn(THE_ANSWER);
        int posts = profileBacker.getNumberOfPosts();
        assertEquals(THE_ANSWER, posts);
        verify(profileService, times(1)).getNumberOfPostsForUser(user);
    }

    @Test
    public void testIsPrivilegedEqualUser() {
        profileBacker.setUser(user);
        when(session.getUser()).thenReturn(profileBacker.getUser());
        assertTrue(profileBacker.isPrivileged());
        verify(session, times(3)).getUser();
    }

    @Test
    public void testIsPrivilegedAdmin() {
        user.setAdministrator(true);
        when(session.getUser()).thenReturn(user);
        assertTrue(profileBacker.isPrivileged());
        verify(session, times(2)).getUser();
    }

    @Test
    public void testIsPrivilegedFalse() {
        profileBacker.setUser(user);
        User owner = new User(user);
        owner.setId(45678);
        when(session.getUser()).thenReturn(owner);
        assertFalse(profileBacker.isPrivileged());
        verify(session, times(3)).getUser();
    }

    @Test
    public void testIsPrivilegedNoSessionUser() {
        profileBacker.setUser(user);
        assertFalse(profileBacker.isPrivileged());
        verify(session, times(1)).getUser();
    }*/

    @Test
    public void testToggleAdmin() {
        user.setAdministrator(true);
        when(session.getUser()).thenReturn(user);
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        profileBacker.setUser(user);
        profileBacker.toggleAdmin();
        verify(profileService, times(1)).toggleAdmin(user);
        verify(session, times(5)).getUser();
    }

    @Test
    public void testToggleAdminUserNotAdmin() {
        when(session.getUser()).thenReturn(user);
        profileBacker.setUser(user);
        profileBacker.toggleAdmin();
        verify(profileService, times(0)).toggleAdmin(user);
        verify(session, times(2)).getUser();
    }

    @Test
    public void testToggleAdminDifferentSessionUser() {
        User sessionUser = new User(user);
        sessionUser.setId(23456);
        sessionUser.setAdministrator(true);
        when(session.getUser()).thenReturn(sessionUser);
        when(profileService.matchingPassword(any(), any())).thenReturn(true);
        profileBacker.setUser(user);
        profileBacker.toggleAdmin();
        verify(profileService, times(1)).toggleAdmin(user);
        verify(session, times(4)).getUser();
    }

    @Test
    public void testToggleAdminNoSessionUser() {
        profileBacker.setUser(user);
        profileBacker.toggleAdmin();
        verify(profileService, times(0)).toggleAdmin(user);
        verify(session, times(1)).getUser();
    }

    @Test
    public void testToggleAdminWrongPassword() {
        user.setAdministrator(true);
        when(session.getUser()).thenReturn(user);
        when(profileService.matchingPassword(any(), any())).thenReturn(false);
        profileBacker.setUser(user);
        profileBacker.toggleAdmin();
        verify(profileService, times(1)).matchingPassword(any(), any());
        verify(session, times(3)).getUser();
    }

    @Test
    public void testDeleteTopicSubscription() throws NoSuchFieldException, IllegalAccessException {
        Field topics = profileBacker.getClass().getDeclaredField("topicSubscriptions");
        topics.setAccessible(true);
        Paginator<Topic> mockTopics = mock(Paginator.class);
        topics.set(profileBacker, mockTopics);
        profileBacker.setUser(user);
        profileBacker.deleteTopicSubscription(topic);
        assertEquals(mockTopics, profileBacker.getTopicSubscriptions());
        verify(profileService).deleteTopicSubscription(user, topic);
        verify(mockTopics).updateReset();
    }

    @Test
    public void testDeleteReportSubscription() throws NoSuchFieldException, IllegalAccessException {
        Field reports = profileBacker.getClass().getDeclaredField("reportSubscriptions");
        reports.setAccessible(true);
        Paginator<Topic> mockReports = mock(Paginator.class);
        reports.set(profileBacker, mockReports);
        profileBacker.setUser(user);
        profileBacker.deleteReportSubscription(report);
        assertEquals(mockReports, profileBacker.getReportSubscriptions());
        verify(profileService).deleteReportSubscription(user, report);
        verify(mockReports).updateReset();
    }

    @Test
    public void testDeleteUserSubscription() throws NoSuchFieldException, IllegalAccessException {
        Field users = profileBacker.getClass().getDeclaredField("userSubscriptions");
        users.setAccessible(true);
        Paginator<Topic> mockUsers = mock(Paginator.class);
        users.set(profileBacker, mockUsers);
        profileBacker.setUser(user);
        profileBacker.deleteUserSubscription(user);
        assertEquals(mockUsers, profileBacker.getUserSubscriptions());
        verify(profileService).deleteUserSubscription(user, user);
        verify(mockUsers).updateReset();
    }

    @Test
    public void testDeleteAllTopicSubscription() throws NoSuchFieldException, IllegalAccessException {
        Field topics = profileBacker.getClass().getDeclaredField("topicSubscriptions");
        topics.setAccessible(true);
        Paginator<Topic> mockTopics = mock(Paginator.class);
        topics.set(profileBacker, mockTopics);
        profileBacker.setUser(user);
        profileBacker.deleteAllTopicSubscriptions();
        assertEquals(ProfileBacker.ProfileDialog.NONE, profileBacker.getProfileDialog());
        verify(profileService).deleteAllTopicSubscriptions(user);
        verify(mockTopics).updateReset();
    }

    @Test
    public void testDeleteAllReportSubscription() throws NoSuchFieldException, IllegalAccessException {
        Field reports = profileBacker.getClass().getDeclaredField("reportSubscriptions");
        reports.setAccessible(true);
        Paginator<Topic> mockReports = mock(Paginator.class);
        reports.set(profileBacker, mockReports);
        profileBacker.setUser(user);
        profileBacker.deleteAllReportSubscriptions();
        assertEquals(ProfileBacker.ProfileDialog.NONE, profileBacker.getProfileDialog());
        verify(profileService).deleteAllReportSubscriptions(user);
        verify(mockReports).updateReset();
    }

    @Test
    public void testDeleteAllUserSubscription() throws NoSuchFieldException, IllegalAccessException {
        Field users = profileBacker.getClass().getDeclaredField("userSubscriptions");
        users.setAccessible(true);
        Paginator<Topic> mockUsers = mock(Paginator.class);
        users.set(profileBacker, mockUsers);
        profileBacker.setUser(user);
        profileBacker.deleteAllUserSubscriptions();
        assertEquals(ProfileBacker.ProfileDialog.NONE, profileBacker.getProfileDialog());
        verify(profileService).deleteAllUserSubscriptions(user);
        verify(mockUsers).updateReset();
    }

    @Test
    public void testToggleUserSubscription() {
        when(session.getUser()).thenReturn(user);
        profileBacker.setUser(otherUser);
        profileBacker.toggleUserSubscription();
        verify(profileService).subscribeToUser(user, otherUser);
    }

    /*@Test
    public void testToggleUserSubscriptionUnsub() {
        when(session.getUser()).thenReturn(user);
        when(profileService.isSubscribed(user, otherUser)).thenReturn(true);
        profileBacker.setUser(otherUser);
        profileBacker.toggleUserSubscription();
        verify(profileService).deleteUserSubscription(user, otherUser);
    }*/

    @Test
    public void testToggleUserSubscriptionUserNull() {
        profileBacker.setUser(otherUser);
        profileBacker.toggleUserSubscription();
        verify(profileService, never()).deleteUserSubscription(user, otherUser);
        verify(profileService, never()).subscribeToUser(user, otherUser);
    }

    @Test
    public void testToggleUserSubscriptionUserProfileOwner() {
        when(session.getUser()).thenReturn(user);
        profileBacker.setUser(user);
        profileBacker.toggleUserSubscription();
        verify(profileService, never()).deleteUserSubscription(user, otherUser);
        verify(profileService, never()).subscribeToUser(user, otherUser);
    }

    @Test
    public void testSetPasswordForCoverage() {
        profileBacker.setPassword(user.getPasswordHash());
        assertEquals(user.getPasswordHash(), profileBacker.getPassword());
    }

}
