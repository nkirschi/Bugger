package tech.bugger.business.service;

import java.util.Locale;
import javax.enterprise.event.Event;
import javax.servlet.http.Part;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Hasher;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.DuplicateException;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.SelfReferenceException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.ReportGateway;
import tech.bugger.persistence.gateway.SubscriptionGateway;
import tech.bugger.persistence.gateway.UserGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    private ProfileService service;

    private User testUser;
    private User admin;
    private Topic testTopic;
    private Report testReport;
    private Selection selection;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private UserGateway userGateway;

    @Mock
    private SubscriptionGateway subscriptionGateway;

    @Mock
    private ReportGateway reportGateway;

    @Mock
    private Event<Feedback> feedbackEvent;

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    private ResourceBundle messages;

    @Mock
    private Configuration config;

    private static final int ZERO = 0;
    private static final int THE_ANSWER = 42;
    private static final int MANY_POSTS = 1500;
    private static final String VOTING_WEIGHT_DEF = "1000,0,200,50,100,25,400,600,800,10";

    @BeforeEach
    public void setUp() {
        service = new ProfileService(feedbackEvent, transactionManager, applicationSettings, messages);
        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(userGateway).when(tx).newUserGateway();
        lenient().doReturn(subscriptionGateway).when(tx).newSubscriptionGateway();
        lenient().doReturn(reportGateway).when(tx).newReportGateway();
        testUser = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        admin = new User(3, "Helgo", "v3ry_s3cur3", "salt", "algorithm", "helgo@admin.de", "Helgo", "Brötchen",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "Ich bin der Administrator hier!",
                Locale.ENGLISH, User.ProfileVisibility.MINIMAL, OffsetDateTime.now(), null, true);
        testTopic = new Topic(1, "title", "description");
        testReport = new Report(100, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "",
                mock(Authorship.class), mock(OffsetDateTime.class), null, null, false, null);
        selection = new Selection(1, 0, Selection.PageSize.SMALL, "id", true);
    }

    @Test
    public void testCreateUser() {
        testUser.setId(null);

        doAnswer((invocation) -> {
            invocation.getArgument(0, User.class).setId(1);
            return null;
        }).when(userGateway).createUser(any());

        assertAll(() -> assertTrue(service.createUser(testUser)),
                () -> assertEquals(1, testUser.getId()),
                () -> verify(userGateway).createUser(any()));
    }

    @Test
    public void testCreateUserWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.createUser(testUser));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteUser() {
        assertTrue(service.deleteUser(testUser));
    }

    @Test
    public void testDeleteUserAdmin() {
        testUser.setAdministrator(true);
        when(userGateway.getNumberOfAdmins()).thenReturn(THE_ANSWER);
        assertTrue(service.deleteUser(testUser));
    }

    @Test
    public void testDeleteUserLasAdmin() {
        testUser.setAdministrator(true);
        when(userGateway.getNumberOfAdmins()).thenReturn(1);
        assertFalse(service.deleteUser(testUser));
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testDeleteUserNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(userGateway).deleteUser(testUser);
        assertTrue(service.deleteUser(testUser));
    }

    @Test
    public void testDeleteUserTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.deleteUser(testUser));
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testUpdateUser() throws Exception {
        assertAll(() -> assertTrue(service.updateUser(testUser)),
                () -> assertEquals(1, testUser.getId()));
        verify(userGateway).updateUser(any());
    }

    @Test
    public void testUpdateWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(userGateway).updateUser(any());
        assertThrows(tech.bugger.business.exception.NotFoundException.class,
                () -> service.updateUser(testUser)
        );
    }

    @Test
    public void testUpdateWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.updateUser(testUser));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetUser() throws Exception {
        doReturn(testUser).when(userGateway).getUserByID(testUser.getId());
        User user = service.getUser(testUser.getId());
        assertEquals(testUser.getId(), user.getId());
        verify(userGateway).getUserByID(testUser.getId());
    }

    @Test
    public void testGetUserWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(userGateway).getUserByID(1);
        assertThrows(tech.bugger.business.exception.NotFoundException.class,
                () -> service.getUser(1)
        );
    }

    @Test
    public void testGetUserWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(service.getUser(1));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testMatchingPassword() {
        String hashedPassword = Hasher.hash(testUser.getPasswordHash(), testUser.getPasswordSalt(),
                testUser.getHashingAlgorithm());
        String password = testUser.getPasswordHash();
        testUser.setPasswordHash(hashedPassword);
        assertTrue(service.matchingPassword(testUser, password));
    }

    @Test
    public void testMatchingPasswordFalse() {
        assertFalse(service.matchingPassword(testUser, testUser.getPasswordHash()));
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testGetUserByEmail() throws Exception {
        doReturn(testUser).when(userGateway).getUserByEmail(testUser.getEmailAddress());
        User user = service.getUserByEmail(testUser.getEmailAddress());
        assertEquals(testUser, user);
        verify(userGateway).getUserByEmail(testUser.getEmailAddress());
    }

    @Test
    public void testGetUserByEmailWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(userGateway).getUserByEmail("test@test.de");
        assertNull(service.getUserByEmail("test@test.de"));
    }

    @Test
    public void testGetUserByEmailWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(service.getUserByEmail("test@test.de"));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetUserByUsername() throws Exception {
        doReturn(testUser).when(userGateway).getUserByUsername(testUser.getUsername());
        User user = service.getUserByUsername(testUser.getUsername());
        assertEquals(testUser, user);
        verify(userGateway).getUserByUsername(testUser.getUsername());
    }

    @Test
    public void testGetUserByUsernameWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(userGateway).getUserByUsername("test");
        assertNull(service.getUserByUsername("test"));
    }

    @Test
    public void testGetUserByUsernameWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(service.getUserByUsername("test"));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetVotingWeight() throws NotFoundException {
        when(userGateway.getNumberOfPosts(testUser)).thenReturn(THE_ANSWER);
        when(applicationSettings.getConfiguration()).thenReturn(config);
        when(config.getVotingWeightDefinition()).thenReturn(VOTING_WEIGHT_DEF);
        assertEquals(3, service.getVotingWeightForUser(testUser));
        verify(userGateway, times(1)).getNumberOfPosts(testUser);
    }

    @Test
    public void testGetVotingWeightMaxWeight() throws NotFoundException {
        when(userGateway.getNumberOfPosts(testUser)).thenReturn(MANY_POSTS);
        when(applicationSettings.getConfiguration()).thenReturn(config);
        when(config.getVotingWeightDefinition()).thenReturn(VOTING_WEIGHT_DEF);
        assertEquals(10, service.getVotingWeightForUser(testUser));
        verify(userGateway, times(1)).getNumberOfPosts(testUser);
    }

    @Test
    public void testGetVotingWeightOverwritten() {
        testUser.setForcedVotingWeight(100);
        assertEquals(100, service.getVotingWeightForUser(testUser));
    }

    @Test
    public void testGetVotingWeightEmpty() throws NotFoundException {
        when(userGateway.getNumberOfPosts(testUser)).thenReturn(THE_ANSWER);
        when(applicationSettings.getConfiguration()).thenReturn(config);
        when(config.getVotingWeightDefinition()).thenReturn(",");
        service.getVotingWeightForUser(testUser);
        verify(userGateway, times(1)).getNumberOfPosts(testUser);
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testGetVotingWeightNumberFormatException() throws NotFoundException {
        when(userGateway.getNumberOfPosts(testUser)).thenReturn(THE_ANSWER);
        when(applicationSettings.getConfiguration()).thenReturn(config);
        when(config.getVotingWeightDefinition()).thenReturn("a, b");
        service.getVotingWeightForUser(testUser);
        verify(userGateway, times(1)).getNumberOfPosts(testUser);
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testGetVotingWeightNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(userGateway).getNumberOfPosts(testUser);
        assertEquals(1, service.getVotingWeightForUser(testUser));
        verify(userGateway, times(1)).getNumberOfPosts(testUser);
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testGetVotingWeightContainsNoZero() throws NotFoundException {
        when(userGateway.getNumberOfPosts(testUser)).thenReturn(THE_ANSWER);
        when(applicationSettings.getConfiguration()).thenReturn(config);
        when(config.getVotingWeightDefinition()).thenReturn("100,200");
        service.getVotingWeightForUser(testUser);
        verify(userGateway, times(1)).getNumberOfPosts(testUser);
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testGetNumberOfPosts() throws NotFoundException {
        when(userGateway.getNumberOfPosts(testUser)).thenReturn(THE_ANSWER);
        assertEquals(42, service.getNumberOfPostsForUser(testUser));
        verify(userGateway, times(1)).getNumberOfPosts(testUser);
    }

    @Test
    public void testGetNumberOfPostsNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(userGateway).getNumberOfPosts(testUser);
        assertEquals(ZERO, service.getNumberOfPostsForUser(testUser));
        verify(userGateway, times(1)).getNumberOfPosts(testUser);
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testGetNumberOfPostsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(ZERO, service.getNumberOfPostsForUser(testUser));
        verify(tx, times(1)).commit();
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminPromote() throws NotFoundException {
        service.toggleAdmin(testUser);
        assertTrue(testUser.isAdministrator());
        verify(userGateway, times(1)).updateUser(testUser);
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminPromoteNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(userGateway).updateUser(testUser);
        service.toggleAdmin(testUser);
        assertFalse(testUser.isAdministrator());
        verify(userGateway, times(1)).updateUser(testUser);
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminPromoteTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.toggleAdmin(testUser);
        assertFalse(testUser.isAdministrator());
        verify(tx, times(1)).commit();
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminDemoteAdmin() throws NotFoundException {
        testUser.setAdministrator(true);
        when(userGateway.getNumberOfAdmins()).thenReturn(THE_ANSWER);
        service.toggleAdmin(testUser);
        assertFalse(testUser.isAdministrator());
        verify(userGateway, times(1)).getNumberOfAdmins();
        verify(userGateway, times(1)).updateUser(testUser);
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminDemoteAdminNotFound() throws NotFoundException {
        testUser.setAdministrator(true);
        when(userGateway.getNumberOfAdmins()).thenReturn(THE_ANSWER);
        doThrow(NotFoundException.class).when(userGateway).updateUser(testUser);
        service.toggleAdmin(testUser);
        assertTrue(testUser.isAdministrator());
        verify(userGateway, times(1)).getNumberOfAdmins();
        verify(userGateway, times(1)).updateUser(testUser);
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminDemoteAdminTransactionException() throws TransactionException, NotFoundException {
        testUser.setAdministrator(true);
        when(userGateway.getNumberOfAdmins()).thenReturn(THE_ANSWER);
        doNothing().doThrow(TransactionException.class).when(tx).commit();
        service.toggleAdmin(testUser);
        assertTrue(testUser.isAdministrator());
        verify(userGateway, times(1)).getNumberOfAdmins();
        verify(userGateway, times(1)).updateUser(testUser);
        verify(tx, times(2)).commit();
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminLastAdmin() {
        testUser.setAdministrator(true);
        when(userGateway.getNumberOfAdmins()).thenReturn(1);
        service.toggleAdmin(testUser);
        assertTrue(testUser.isAdministrator());
        verify(userGateway, times(1)).getNumberOfAdmins();
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminNoAdmins() {
        testUser.setAdministrator(true);
        when(userGateway.getNumberOfAdmins()).thenReturn(ZERO);
        assertThrows(InternalError.class,
                () -> service.toggleAdmin(testUser)
        );
        assertTrue(testUser.isAdministrator());
        verify(userGateway, times(1)).getNumberOfAdmins();
    }

    @Test
    public void testToggleAdminTransactionException() throws TransactionException {
        testUser.setAdministrator(true);
        doThrow(TransactionException.class).when(tx).commit();
        service.toggleAdmin(testUser);
        assertTrue(testUser.isAdministrator());
        verify(tx, times(1)).commit();
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testUploadAvatar() throws IOException {
        Part part = mock(Part.class);
        when(part.getInputStream()).thenReturn(ClassLoader.getSystemResourceAsStream("images/bugger.png"));
        assertNotNull(service.uploadAvatar(part));
    }

    @Test
    public void testUploadAvatarIOException() throws IOException {
        Part part = mock(Part.class);
        when(part.getInputStream()).thenThrow(IOException.class);
        assertNull(service.uploadAvatar(part));
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testGenerateThumbnail() throws IOException {
        byte[] bytes = ClassLoader.getSystemResourceAsStream("images/bugger.png").readAllBytes();
        assertNotNull(service.generateThumbnail(bytes));
    }

    @Test
    public void testGenerateThumbnailCorruptImageException() {
        assertNull(service.generateThumbnail(new byte[0]));
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testGetNumberOfModeratedTopics() {
        when(userGateway.getNumberOfModeratedTopics(testUser)).thenReturn(THE_ANSWER);
        assertEquals(THE_ANSWER, service.getNumberOfModeratedTopics(testUser));
        verify(userGateway).getNumberOfModeratedTopics(testUser);
    }

    @Test
    public void testGetNumberOfModeratedTopicsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(ZERO, service.getNumberOfModeratedTopics(testUser));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteTopicSubscription() throws NotFoundException {
        service.deleteTopicSubscription(testUser, testTopic);
        verify(subscriptionGateway).unsubscribe(testTopic, testUser);
        verify(feedbackEvent, never()).fire(any());
    }

    @Test
    public void testDeleteTopicSubscriptionSubscriberNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteTopicSubscription(null, testTopic)
        );
    }

    @Test
    public void testDeleteTopicSubscriptionSubscriberIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteTopicSubscription(testUser, testTopic)
        );
    }

    @Test
    public void testDeleteTopicSubscriptionTopicNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteTopicSubscription(testUser, null)
        );
    }

    @Test
    public void testDeleteTopicSubscriptionTopicIdNull() {
        testTopic.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteTopicSubscription(testUser, testTopic)
        );
    }

    @Test
    public void testDeleteTopicSubscriptionNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(subscriptionGateway).unsubscribe(testTopic, testUser);
        service.deleteTopicSubscription(testUser, testTopic);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteTopicSubscriptionTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.deleteTopicSubscription(testUser, testTopic);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteReportSubscriptions() throws NotFoundException {
        service.deleteReportSubscription(testUser, testReport);
        verify(subscriptionGateway).unsubscribe(testReport, testUser);
        verify(feedbackEvent, never()).fire(any());
    }

    @Test
    public void testDeleteReportSubscriptionsReportNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteReportSubscription(testUser, null)
        );
    }

    @Test
    public void testDeleteReportSubscriptionsReportIdNull() {
        testReport.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteReportSubscription(testUser, testReport)
        );
    }

    @Test
    public void testDeleteReportSubscriptionNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(subscriptionGateway).unsubscribe(testReport, testUser);
        service.deleteReportSubscription(testUser, testReport);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteReportSubscriptionTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.deleteReportSubscription(testUser, testReport);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteUserSubscriptions() throws NotFoundException {
        service.deleteUserSubscription(testUser, admin);
        verify(subscriptionGateway).unsubscribe(admin, testUser);
        verify(feedbackEvent, never()).fire(any());
    }

    @Test
    public void testDeleteUserSubscriptionsUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteUserSubscription(testUser, null)
        );
    }

    @Test
    public void testDeleteUserSubscriptionsUserIdNull() {
        admin.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteUserSubscription(testUser, admin)
        );
    }

    @Test
    public void testDeleteUserSubscriptionNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(subscriptionGateway).unsubscribe(admin, testUser);
        service.deleteUserSubscription(testUser, admin);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteUserSubscriptionTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.deleteUserSubscription(testUser, admin);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteAllTopicSubscriptions() throws NotFoundException {
        service.deleteAllTopicSubscriptions(testUser);
        verify(subscriptionGateway).unsubscribeAllTopics(testUser);
        verify(feedbackEvent, never()).fire(any());
    }

    @Test
    public void testDeleteAllTopicSubscriptionsUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteAllTopicSubscriptions(null)
        );
    }

    @Test
    public void testDeleteAllTopicSubscriptionsUserIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteAllTopicSubscriptions(testUser)
        );
    }

    @Test
    public void testDeleteAllTopicSubscriptionsNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(subscriptionGateway).unsubscribeAllTopics(testUser);
        service.deleteAllTopicSubscriptions(testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteAllTopicSubscriptionsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.deleteAllTopicSubscriptions(testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteAllReportSubscriptions() throws NotFoundException {
        service.deleteAllReportSubscriptions(testUser);
        verify(subscriptionGateway).unsubscribeAllReports(testUser);
        verify(feedbackEvent, never()).fire(any());
    }

    @Test
    public void testDeleteAllReportSubscriptionsUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteAllReportSubscriptions(null)
        );
    }

    @Test
    public void testDeleteAllReportSubscriptionsUserIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteAllReportSubscriptions(testUser)
        );
    }

    @Test
    public void testDeleteAllReportSubscriptionsNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(subscriptionGateway).unsubscribeAllReports(testUser);
        service.deleteAllReportSubscriptions(testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteAllReportSubscriptionsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.deleteAllReportSubscriptions(testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteAllUserSubscriptions() throws NotFoundException {
        service.deleteAllUserSubscriptions(testUser);
        verify(subscriptionGateway).unsubscribeAllUsers(testUser);
        verify(feedbackEvent, never()).fire(any());
    }

    @Test
    public void testDeleteAllUserSubscriptionsUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteAllUserSubscriptions(null)
        );
    }

    @Test
    public void testDeleteAllUserSubscriptionsUserIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.deleteAllUserSubscriptions(testUser)
        );
    }

    @Test
    public void testDeleteAllUserSubscriptionsNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(subscriptionGateway).unsubscribeAllUsers(testUser);
        service.deleteAllUserSubscriptions(testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testDeleteAllUserSubscriptionsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.deleteAllUserSubscriptions(testUser);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSubscribeToUser() throws DuplicateException, NotFoundException, SelfReferenceException {
        service.subscribeToUser(testUser, admin);
        verify(subscriptionGateway).subscribe(admin, testUser);
        verify(feedbackEvent, never()).fire(any());
    }

    @Test
    public void testSubscribeToUserSubscriberNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.subscribeToUser(null, admin)
        );
    }

    @Test
    public void testSubscribeToUserSubscriberIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.subscribeToUser(testUser, admin)
        );
    }

    @Test
    public void testSubscribeToUserSubscribeToNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.subscribeToUser(testUser, null)
        );
    }

    @Test
    public void testSubscribeToUserSubscribeToIdNull() {
        admin.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.subscribeToUser(testUser, admin)
        );
    }

    @Test
    public void testSubscribeToUserDuplicate() throws DuplicateException, NotFoundException, SelfReferenceException {
        doThrow(DuplicateException.class).when(subscriptionGateway).subscribe(admin, testUser);
        service.subscribeToUser(testUser, admin);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSubscribeToUserNotFound() throws DuplicateException, NotFoundException, SelfReferenceException {
        doThrow(NotFoundException.class).when(subscriptionGateway).subscribe(admin, testUser);
        service.subscribeToUser(testUser, admin);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSubscribeToUserReference() throws DuplicateException, NotFoundException, SelfReferenceException {
        doThrow(SelfReferenceException.class).when(subscriptionGateway).subscribe(admin, testUser);
        service.subscribeToUser(testUser, admin);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSubscribeToUserTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        service.subscribeToUser(testUser, admin);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsSubscribed() throws NotFoundException {
        doReturn(true).when(subscriptionGateway).isSubscribed(testUser, admin);
        assertTrue(service.isSubscribed(testUser, admin));
    }

    @Test
    public void testIsSubscribedFalse() {
        assertFalse(service.isSubscribed(testUser, admin));
    }

    @Test
    public void testIsSubscribedSubscriberNull() {
        assertFalse(service.isSubscribed(null, admin));
    }

    @Test
    public void testIsSubscribedSubscriberIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.isSubscribed(testUser, admin)
        );
    }

    @Test
    public void testIsSubscribedSubscribedToNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.isSubscribed(testUser, null)
        );
    }

    @Test
    public void testIsSubscribedSubscribedToIdNull() {
        admin.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.isSubscribed(testUser, admin)
        );
    }

    @Test
    public void testIsSubscribedNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(subscriptionGateway).isSubscribed(testUser, admin);
        assertFalse(service.isSubscribed(testUser, admin));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsSubscribedTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertFalse(service.isSubscribed(testUser, admin));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSelectSubscribedUsers() {
        List<User> users = new ArrayList<>();
        users.add(admin);
        doReturn(users).when(userGateway).selectSubscribedUsers(testUser, selection);
        assertEquals(users, service.selectSubscribedUsers(testUser, selection));
    }

    @Test
    public void testSelectSubscribedUsersSelectionNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.selectSubscribedUsers(testUser, null)
        );
    }

    @Test
    public void testSelectSubscribedUsersUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.selectSubscribedUsers(null, selection)
        );
    }

    @Test
    public void testSelectSubscribedUsersUserIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.selectSubscribedUsers(testUser, selection)
        );
    }

    @Test
    public void testSelectSubscribedUsersTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(service.selectSubscribedUsers(testUser, selection));
    }

    @Test
    public void testCountSubscribedUsers() {
        doReturn(THE_ANSWER).when(userGateway).countSubscribedUsers(testUser);
        assertEquals(THE_ANSWER, service.countSubscribedUsers(testUser));
    }

    @Test
    public void testCountSubscribedUsersUserNull() {
        assertEquals(ZERO, service.countSubscribedUsers(null));
    }

    @Test
    public void testCountSubscribedUsersUserIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.countSubscribedUsers(testUser)
        );
    }

    @Test
    public void testCountSubscribedUsersTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(ZERO, service.countSubscribedUsers(testUser));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSelectSubscribedReports() {
        List<Report> reports = new ArrayList<>();
        reports.add(testReport);
        doReturn(reports).when(reportGateway).selectSubscribedReports(testUser, selection);
        assertEquals(reports, service.selectSubscribedReports(testUser, selection));
    }

    @Test
    public void testSelectSubscribedReportsSelectionNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.selectSubscribedReports(testUser, null)
        );
    }

    @Test
    public void testSelectSubscribedReportsUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> service.selectSubscribedReports(null, selection)
        );
    }

    @Test
    public void testSelectSubscribedReportsUserIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.selectSubscribedReports(testUser, selection)
        );
    }

    @Test
    public void testSelectSubscribedReportsTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(service.selectSubscribedReports(testUser, selection));
    }

    @Test
    public void testCountSubscribedReports() {
        doReturn(THE_ANSWER).when(reportGateway).countSubscribedReports(testUser);
        assertEquals(THE_ANSWER, service.countSubscribedReports(testUser));
    }

    @Test
    public void testCountSubscribedReportsUserNull() {
        assertEquals(ZERO, service.countSubscribedReports(null));
    }

    @Test
    public void testCountSubscribedReportsUserIdNull() {
        testUser.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.countSubscribedReports(testUser)
        );
    }

    @Test
    public void testCountSubscribedReportsTransaction() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(ZERO, service.countSubscribedReports(testUser));
        verify(feedbackEvent).fire(any());
    }

}
