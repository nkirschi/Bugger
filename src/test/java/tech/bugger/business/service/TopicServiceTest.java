package tech.bugger.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.DuplicateException;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.ReportGateway;
import tech.bugger.persistence.gateway.SubscriptionGateway;
import tech.bugger.persistence.gateway.TopicGateway;
import tech.bugger.persistence.gateway.UserGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ExtendWith(LogExtension.class)
public class TopicServiceTest {

    private TopicService topicService;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private TopicGateway topicGateway;

    @Mock
    private UserGateway userGateway;

    @Mock
    private ReportGateway reportGateway;

    @Mock
    private SubscriptionGateway subscriptionGateway;

    @Mock
    private Event<Feedback> feedbackEvent;

    private List<Topic> testSelectedTopics;

    private int testNumberOfTopics;

    private Selection testSelection;

    private final Topic testTopic1 = new Topic(1, "Hi", "senberg");
    private final Topic testTopic2 = new Topic(2, "Hi", "performance");
    private final Topic testTopic3 = new Topic(3, "Hi", "de and seek");
    private final Report testReport1 = new Report(200, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "", null,
            mock(OffsetDateTime.class), null, null, false, 1, null);
    private final Report testReport2 = new Report(201, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "", null,
            mock(OffsetDateTime.class), null, null, false, 1, null);

    private User user;

    @BeforeEach
    public void setUp() {
        topicService = new TopicService(transactionManager, feedbackEvent, ResourceBundleMocker.mock(""));
        testSelectedTopics = new ArrayList<>();
        testSelectedTopics.add(testTopic1);
        testSelectedTopics.add(testTopic2);
        testSelectedTopics.add(testTopic3);
        testNumberOfTopics = testSelectedTopics.size();
        testSelection = new Selection(3, 1, Selection.PageSize.NORMAL, "", true);
        user = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(topicGateway).when(tx).newTopicGateway();
        lenient().doReturn(userGateway).when(tx).newUserGateway();
        lenient().doReturn(reportGateway).when(tx).newReportGateway();
        lenient().doReturn(subscriptionGateway).when(tx).newSubscriptionGateway();
    }

    @Test
    public void testSelectTopicsWhenFound() {
        doReturn(testSelectedTopics).when(topicGateway).selectTopics(any());
        assertEquals(testSelectedTopics, topicService.selectTopics(testSelection));
    }

    @Test
    public void testSelectTopicsWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(topicService.selectTopics(testSelection));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSelectTopicsWhenSelectionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> topicService.selectTopics(null));
    }

    @Test
    public void testCountTopics() {
        doReturn(testNumberOfTopics).when(topicGateway).countTopics();
        assertEquals(testNumberOfTopics, topicService.countTopics());
    }

    @Test
    public void testCountTopicsWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, topicService.countTopics());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testLastChange() throws Exception {
        OffsetDateTime lastChange = OffsetDateTime.now();
        doReturn(lastChange).when(topicGateway).determineLastActivity(any());
        assertEquals(lastChange, topicService.lastChange(testTopic1));
    }

    @Test
    public void testLastChangeWhenNoChange() throws Exception {
        doReturn(null).when(topicGateway).determineLastActivity(any());
        assertNull(topicService.lastChange(testTopic1));
    }

    @Test
    public void testLastChangeWhenTopicIsNull() {
        assertThrows(IllegalArgumentException.class, () -> topicService.lastChange(null));
    }

    @Test
    public void testLastChangeWhenTopicIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> topicService.lastChange(new Topic()));
    }

    @Test
    public void testLastChangeWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(topicGateway).determineLastActivity(any());
        assertNull(topicService.lastChange(testTopic1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testLastChangeWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(topicService.lastChange(testTopic1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMakeModerator() throws NotFoundException, DuplicateException {
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        topicService.makeModerator(user.getUsername(), testTopic1);
        verify(topicGateway).promoteModerator(testTopic1, user);
        verify(subscriptionGateway).subscribe(testTopic1, user);
    }

    @Test
    public void testMakeModeratorAlreadySubscribed() throws NotFoundException {
        doReturn(true).when(subscriptionGateway).isSubscribed(user, testTopic1);
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        topicService.makeModerator(user.getUsername(), testTopic1);
        verify(topicGateway).promoteModerator(testTopic1, user);
    }

    @Test
    public void testMakeModeratorBanned() throws NotFoundException {
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        when(userGateway.isBanned(user, testTopic1)).thenReturn(true);
        topicService.makeModerator(user.getUsername(), testTopic1);
        verify(topicGateway).promoteModerator(testTopic1, user);
        verify(topicGateway).unbanUser(testTopic1, user);
    }

    @Test
    public void testMakeModeratorIsModerator() throws NotFoundException {
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        when(userGateway.isModerator(user, testTopic1)).thenReturn(true);
        topicService.makeModerator(user.getUsername(), testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMakeModeratorIsAdmin() throws NotFoundException {
        user.setAdministrator(true);
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        topicService.makeModerator(user.getUsername(), testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMakeModeratorUserNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(userGateway).getUserByUsername(user.getUsername());
        topicService.makeModerator(user.getUsername(), testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMakeModeratorPromoteNotFound() throws NotFoundException {
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        doThrow(NotFoundException.class).when(topicGateway).promoteModerator(testTopic1, user);
        assertFalse(topicService.makeModerator(user.getUsername(), testTopic1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMakeModeratorTransactionException() throws TransactionException, NotFoundException {
        doThrow(TransactionException.class).when(tx).commit();
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        topicService.makeModerator(user.getUsername(), testTopic1);
        verify(feedbackEvent, times(2)).fire(any());
    }

    @Test
    public void testMakeModeratorTransactionExceptionUserAdmin() throws TransactionException, NotFoundException {
        doThrow(TransactionException.class).when(tx).commit();
        user.setAdministrator(true);
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        topicService.makeModerator(user.getUsername(), testTopic1);
        verify(feedbackEvent, times(2)).fire(any());
    }

    @Test
    public void testRemoveModerator() throws NotFoundException {
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        topicService.removeModerator(user.getUsername(), testTopic1);
        verify(topicGateway).demoteModerator(testTopic1, user);
    }

    @Test
    public void testRemoveModeratorUserNotModerator() throws NotFoundException {
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        doThrow(NotFoundException.class).when(topicGateway).demoteModerator(any(), any());
        topicService.removeModerator(user.getUsername(), testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testRemoveModeratorUserAdmin() throws NotFoundException {
        user.setAdministrator(true);
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        topicService.removeModerator(user.getUsername(), testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testRemoveModeratorNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(userGateway).getUserByUsername(any());
        topicService.removeModerator(user.getUsername(), testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testRemoveModeratorTransactionException() throws TransactionException, NotFoundException {
        doThrow(TransactionException.class).when(tx).commit();
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        topicService.removeModerator(user.getUsername(), testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetSelectedModerators() throws NotFoundException {
        List<User> users = new ArrayList<>();
        users.add(user);
        Selection selection = new Selection(1, 0, Selection.PageSize.SMALL, "id", true);
        when(userGateway.getSelectedModerators(testTopic1, selection)).thenReturn(users);
        assertEquals(users, topicService.getSelectedModerators(testTopic1, selection));
        verify(userGateway).getSelectedModerators(testTopic1, selection);
    }

    @Test
    public void testGetSelectedModeratorsNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(userGateway).getSelectedModerators(any(), any());
        assertNull(topicService.getSelectedModerators(testTopic1, null));
    }

    @Test
    public void testGetSelectedModeratorsTransactionException() throws TransactionException, NotFoundException {
        doThrow(TransactionException.class).when(tx).commit();
        when(userGateway.getSelectedModerators(any(), any())).thenReturn(null);
        assertNull(topicService.getSelectedModerators(testTopic1, null));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetNumberOfModerators() throws NotFoundException {
        when(topicGateway.countModerators(testTopic1)).thenReturn(3);
        assertEquals(3, topicService.getNumberOfModerators(testTopic1));
        verify(topicGateway).countModerators(testTopic1);
    }

    @Test
    public void testGetNumberOfModeratorsNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(topicGateway).countModerators(testTopic1);
        assertEquals(0, topicService.getNumberOfModerators(testTopic1));
    }

    @Test
    public void testGetNumberOfModeratorsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, topicService.getNumberOfModerators(testTopic1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsModerator() {
        when(userGateway.isModerator(user, testTopic1)).thenReturn(true);
        assertTrue(topicService.isModerator(user, testTopic1));
    }

    @Test
    public void testIsModeratorFalse() {
        assertFalse(topicService.isModerator(user, testTopic1));
    }

    @Test
    public void testIsModeratorUserNull() {
        assertFalse(topicService.isModerator(null, testTopic1));
    }

    @Test
    public void testIsModeratorTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(topicService.isModerator(user, testTopic1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetModeratedTopics() {
        List<Topic> topics = new ArrayList<>();
        topics.add(testTopic1);
        topics.add(testTopic2);
        topics.add(testTopic3);
        when(topicGateway.getModeratedTopics(user, testSelection)).thenReturn(topics);
        assertEquals(topics, topicService.getModeratedTopics(user, testSelection));
        verify(topicGateway).getModeratedTopics(user, testSelection);
    }

    @Test
    public void testGetModeratedTopicsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(topicService.getModeratedTopics(user, testSelection).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testBan() throws NotFoundException {
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        assertTrue(topicService.ban(user.getUsername(), testTopic1));
        verify(topicGateway).banUser(testTopic1, user);
    }

    @Test
    public void testBanUserAdmin() throws NotFoundException {
        user.setAdministrator(true);
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        assertFalse(topicService.ban(user.getUsername(), testTopic1));
        verify(feedbackEvent).fire(any());
        verify(topicGateway, times(0)).banUser(testTopic1, user);
    }

    @Test
    public void testBanUserModerator() throws NotFoundException {
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        when(userGateway.isModerator(user, testTopic1)).thenReturn(true);
        assertFalse(topicService.ban(user.getUsername(), testTopic1));
        verify(feedbackEvent).fire(any());
        verify(topicGateway, times(0)).banUser(testTopic1, user);
    }

    @Test
    public void testBanUserBanned() throws NotFoundException {
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        when(userGateway.isBanned(user, testTopic1)).thenReturn(true);
        assertFalse(topicService.ban(user.getUsername(), testTopic1));
        verify(feedbackEvent).fire(any());
        verify(topicGateway, times(0)).banUser(testTopic1, user);
    }

    @Test
    public void testBanUserNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(userGateway).getUserByUsername(user.getUsername());
        assertFalse(topicService.ban(user.getUsername(), testTopic1));
        verify(feedbackEvent).fire(any());
        verify(topicGateway, times(0)).banUser(testTopic1, user);
    }

    @Test
    public void testBanUserTopicNotFound() throws NotFoundException {
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        doThrow(NotFoundException.class).when(topicGateway).banUser(testTopic1, user);
        assertFalse(topicService.ban(user.getUsername(), testTopic1));
        verify(feedbackEvent).fire(any());
        verify(topicGateway).banUser(testTopic1, user);
    }

    @Test
    public void testBanUserTransactionException() throws TransactionException, NotFoundException {
        user.setAdministrator(true);
        doThrow(TransactionException.class).when(tx).commit();
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        assertFalse(topicService.ban(user.getUsername(), testTopic1));
        verify(feedbackEvent, times(2)).fire(any());
        verify(topicGateway, times(0)).banUser(testTopic1, user);
    }

    @Test
    public void testUnbanUser() throws NotFoundException {
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        assertTrue(topicService.unban(user.getUsername(), testTopic1));
        verify(topicGateway).unbanUser(testTopic1, user);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUnbanUserNull() throws NotFoundException {
        assertFalse(topicService.unban(user.getUsername(), testTopic1));
        verify(topicGateway, times(0)).unbanUser(testTopic1, user);
    }

    @Test
    public void testUnbanUserNotFound() throws NotFoundException {
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        doThrow(NotFoundException.class).when(topicGateway).unbanUser(testTopic1, user);
        assertFalse(topicService.unban(user.getUsername(), testTopic1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUnbanUserTransactionException() throws NotFoundException, TransactionException {
        when(userGateway.getUserByUsername(user.getUsername())).thenReturn(user);
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(topicService.unban(user.getUsername(), testTopic1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUnbanUserUserNullTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(topicService.unban(user.getUsername(), testTopic1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetSelectedBannedUsers() throws NotFoundException {
        List<User> users = new ArrayList<>();
        users.add(user);
        Selection selection = new Selection(1, 0, Selection.PageSize.SMALL, "id", true);
        when(userGateway.getSelectedBannedUsers(testTopic1, selection)).thenReturn(users);
        assertEquals(users, topicService.getSelectedBannedUsers(testTopic1, selection));
        verify(userGateway).getSelectedBannedUsers(testTopic1, selection);
    }

    @Test
    public void testGetSelectedBannedUsersNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(userGateway).getSelectedBannedUsers(any(), any());
        assertNull(topicService.getSelectedBannedUsers(testTopic1, null));
    }

    @Test
    public void testGetSelectedBannedUsersTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(topicService.getSelectedBannedUsers(testTopic1, null).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetNumberOfBannedUsers() throws NotFoundException {
        when(topicGateway.countBannedUsers(testTopic1)).thenReturn(5);
        assertEquals(5, topicService.getNumberOfBannedUsers(testTopic1));
        verify(topicGateway).countBannedUsers(testTopic1);
    }

    @Test
    public void testGetNumberOfBannedUsersNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(topicGateway).countBannedUsers(testTopic1);
        assertEquals(0, topicService.getNumberOfBannedUsers(testTopic1));
    }

    @Test
    public void testGetNumberOfBannedUsersTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, topicService.getNumberOfBannedUsers(testTopic1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsBanned() {
        when(userGateway.isBanned(user, testTopic1)).thenReturn(true);
        assertTrue(topicService.isBanned(user, testTopic1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsBannedTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(topicService.isBanned(user, testTopic1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsBannedUserNull() {
        assertFalse(topicService.isBanned(null, testTopic1));
    }

    @Test
    public void testSubscribeToTopic() throws NotFoundException, DuplicateException {
        topicService.subscribeToTopic(user, testTopic1);
        verify(subscriptionGateway).subscribe(testTopic1, user);
    }

    @Test
    public void testSubscribeToTopicDuplicate() throws NotFoundException, DuplicateException {
        doThrow(DuplicateException.class).when(subscriptionGateway).subscribe(testTopic1, user);
        topicService.subscribeToTopic(user, testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSubscribeToTopicNotFound() throws NotFoundException, DuplicateException {
        doThrow(NotFoundException.class).when(subscriptionGateway).subscribe(testTopic1, user);
        topicService.subscribeToTopic(user, testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSubscribeToTopicTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        topicService.subscribeToTopic(user, testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSubscribeToTopicUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicService.subscribeToTopic(null, testTopic1)
        );
    }

    @Test
    public void testSubscribeToTopicUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> topicService.subscribeToTopic(user, testTopic1)
        );
    }

    @Test
    public void testSubscribeToTopicTopicNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicService.subscribeToTopic(user, null)
        );
    }

    @Test
    public void testSubscribeToTopicTopicIdNull() {
        testTopic1.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> topicService.subscribeToTopic(user, testTopic1)
        );
    }

    @Test
    public void testUnsubscribeFromTopic() throws NotFoundException {
        topicService.unsubscribeFromTopic(user, testTopic1);
        verify(subscriptionGateway).unsubscribe(testTopic1, user);
    }

    @Test
    public void testUnsubscribeFromTopicNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(subscriptionGateway).unsubscribe(testTopic1, user);
        topicService.unsubscribeFromTopic(user, testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUnsubscribeFromTopicTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        topicService.unsubscribeFromTopic(user, testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUnsubscribeFromTopicUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicService.unsubscribeFromTopic(null, testTopic1)
        );
    }

    @Test
    public void testUnsubscribeFromTopicUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> topicService.unsubscribeFromTopic(user, testTopic1)
        );
    }

    @Test
    public void testUnsubscribeFromTopicTopicNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicService.unsubscribeFromTopic(user, null)
        );
    }

    @Test
    public void testUnsubscribeFromTopicTopicIdNull() {
        testTopic1.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> topicService.unsubscribeFromTopic(user, testTopic1)
        );
    }

    @Test
    public void testGetTopicByID() throws NotFoundException {
        doReturn(testTopic1).when(topicGateway).findTopic(testTopic1.getId());
        assertEquals(testTopic1, topicService.getTopicByID(testTopic1.getId()));
    }

    @Test
    public void testGetTopicByIDNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(topicGateway).findTopic(testTopic1.getId());
        assertNull(topicService.getTopicByID(testTopic1.getId()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetTopicByIDTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(topicService.getTopicByID(testTopic1.getId()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testCreateTopic() {
        assertTrue(topicService.createTopic(testTopic1, user));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testCreateTopicNotFound() throws NotFoundException, DuplicateException {
        doThrow(NotFoundException.class).when(topicGateway).createTopic(testTopic1);
        assertFalse(topicService.createTopic(testTopic1, user));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testCreateTopicTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(topicService.createTopic(testTopic1, user));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testCreateTopicDuplicate() throws NotFoundException, DuplicateException {
        doThrow(DuplicateException.class).when(topicGateway).createTopic(testTopic1);
        assertFalse(topicService.createTopic(testTopic1, user));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpdateTopic() throws NotFoundException {
        assertTrue(topicService.updateTopic(testTopic1));
        verify(topicGateway).updateTopic(testTopic1);
    }

    @Test
    public void testUpdateTopicNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(topicGateway).updateTopic(testTopic1);
        assertFalse(topicService.updateTopic(testTopic1));
    }

    @Test
    public void testUpdateTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(topicService.updateTopic(testTopic1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteTopic() throws NotFoundException {
        topicService.deleteTopic(testTopic1);
        verify(topicGateway).deleteTopic(testTopic1);
        verify(feedbackEvent, never()).fire(any());
    }

    @Test
    public void testDeleteTopicNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(topicGateway).deleteTopic(testTopic1);
        topicService.deleteTopic(testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        topicService.deleteTopic(testTopic1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetSelectedReports() throws NotFoundException {
        List<Report> reports = new ArrayList<>();
        reports.add(testReport1);
        reports.add(testReport2);
        doReturn(reports).when(reportGateway).getSelectedReports(testTopic1, testSelection, true, true);
        List<Report> findReports = topicService.getSelectedReports(testTopic1, testSelection, true, true);
        assertAll(
                () -> assertEquals(2, reports.size()),
                () -> assertTrue(findReports.contains(testReport1)),
                () -> assertTrue(findReports.contains(testReport2))
        );
    }

    @Test
    public void testGetSelectedReportsNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(reportGateway).getSelectedReports(testTopic1, testSelection, true, true);
        assertNull(topicService.getSelectedReports(testTopic1, testSelection, true, true));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetSelectedReportsTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(topicService.getSelectedReports(testTopic1, testSelection, true, true).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testCanCreateReportIn() {
        assertTrue(topicService.canCreateReportIn(user, testTopic1));
    }

    @Test
    public void testCanCreateReportInAdmin() {
        user.setAdministrator(true);
        assertTrue(topicService.canCreateReportIn(user, testTopic1));
    }

    @Test
    public void testCanCreateReportInBanned() {
        doReturn(true).when(userGateway).isBanned(user, testTopic1);
        assertFalse(topicService.canCreateReportIn(user, testTopic1));
    }

    @Test
    public void testCanCreateReportInTopicNull() {
        assertFalse(topicService.canCreateReportIn(user, null));
    }

    @Test
    public void testCanCreateReportInUserNull() {
        assertFalse(topicService.canCreateReportIn(null, testTopic1));
    }

    @Test
    public void testGetNumberOfReports() throws NotFoundException {
        doReturn(2).when(topicGateway).countReports(testTopic1, true, true);
        assertEquals(2, topicService.getNumberOfReports(testTopic1, true, true));
        verify(topicGateway).countReports(testTopic1, true, true);
    }

    @Test
    public void testGetNumberOfReportsNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(topicGateway).countReports(testTopic1, true, true);
        assertEquals(0, topicService.getNumberOfReports(testTopic1, true, true));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetNumberOfReportsTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, topicService.getNumberOfReports(testTopic1, true, true));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsSubscribed() throws NotFoundException {
        doReturn(true).when(subscriptionGateway).isSubscribed(user, testTopic1);
        assertTrue(topicService.isSubscribed(user, testTopic1));
    }

    @Test
    public void testIsSubscribedFalse() {
        assertFalse(topicService.isSubscribed(user, testTopic1));
    }

    @Test
    public void testIsSubscribedNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(subscriptionGateway).isSubscribed(user, testTopic1);
        assertFalse(topicService.isSubscribed(user, testTopic1));
    }

    @Test
    public void testIsSubscribedTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(topicService.isSubscribed(user, testTopic1));
    }

    @Test
    public void testIsSubscribedUserNull() {
        assertFalse(topicService.isSubscribed(null, testTopic1));
    }

    @Test
    public void testIsSubscribedUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class, () -> topicService.isSubscribed(user, testTopic1));
    }

    @Test
    public void testIsSubscribedTopicNull() {
        assertThrows(IllegalArgumentException.class, () -> topicService.isSubscribed(user, null));
    }

    @Test
    public void testIsSubscribedTopicIdNull() {
        testTopic1.setId(null);
        assertThrows(IllegalArgumentException.class, () -> topicService.isSubscribed(user, testTopic1));
    }

    @Test
    public void testDiscoverTopics() {
        doReturn(List.of(testTopic1, testTopic2)).when(topicGateway).discoverTopics();
        List<String> titles = topicService.discoverTopics().stream().map(Topic::getTitle).collect(Collectors.toList());
        assertAll(
                () -> assertEquals(2, titles.size()),
                () -> assertTrue(titles.contains(testTopic1.getTitle())),
                () -> assertTrue(titles.contains(testTopic2.getTitle()))
        );
    }

    @Test
    public void testDiscoverTopicsTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(topicService.discoverTopics().isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSelectSubscribedTopics() {
        doReturn(List.of(testTopic1, testTopic2)).when(topicGateway).selectSubscribedTopics(user, testSelection);
        List<Topic> selectedTopics = topicService.selectSubscribedTopics(user, testSelection);
        assertAll(
                () -> assertEquals(2, selectedTopics.size()),
                () -> assertTrue(selectedTopics.contains(testTopic1)),
                () -> assertTrue(selectedTopics.contains(testTopic2))
        );
    }

    @Test
    public void testSelectSubscribedTopicsTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(topicService.selectSubscribedTopics(user, testSelection));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSelectSubscribedTopicsSelectionNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicService.selectSubscribedTopics(user, null)
        );
    }

    @Test
    public void testSelectSubscribedTopicsUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicService.selectSubscribedTopics(null, testSelection)
        );
    }

    @Test
    public void testSelectSubscribedTopicsUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> topicService.selectSubscribedTopics(user, testSelection)
        );
    }

    @Test
    public void testCountSubscribedTopics() {
        doReturn(5).when(topicGateway).countSubscribedTopics(user);
        assertEquals(5, topicService.countSubscribedTopics(user));
    }

    @Test
    public void testCountSubscribedTopicsTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, topicService.countSubscribedTopics(user));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testCountSubscribedTopicsUserNull() {
        assertEquals(0, topicService.countSubscribedTopics(null));
    }

    @Test
    public void testCountSubscribedTopicsUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> topicService.countSubscribedTopics(user)
        );
    }

    @Test
    public void testGetNumberOfSubscribers() throws NotFoundException {
        doReturn(42).when(topicGateway).countSubscribers(testTopic1);
        assertEquals(42, topicService.getNumberOfSubscribers(testTopic1));
    }

    @Test
    public void testGetNumberOfSubscribersTopicNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicService.getNumberOfSubscribers(null)
        );
    }

    @Test
    public void testGetNumberOfSubscribersTopicIdNull() {
        testTopic1.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> topicService.getNumberOfSubscribers(testTopic1)
        );
    }

    @Test
    public void testGetNumberOfSubscribersNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(topicGateway).countSubscribers(testTopic1);
        assertEquals(0, topicService.getNumberOfSubscribers(testTopic1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetNumberOfSubscribersTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, topicService.getNumberOfSubscribers(testTopic1));
        verify(feedbackEvent).fire(any());
    }

}