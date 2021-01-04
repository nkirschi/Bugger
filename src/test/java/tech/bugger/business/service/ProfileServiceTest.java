package tech.bugger.business.service;

import javax.enterprise.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.UserGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    private ProfileService service;

    private User testUser;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private UserGateway userGateway;

    @Mock
    private Event<Feedback> feedbackEvent;

    @BeforeEach
    public void setUp() {
        service = new ProfileService(transactionManager, feedbackEvent, ResourceBundleMocker.mock(""));
        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(userGateway).when(tx).newUserGateway();
        testUser = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User", new Lazy<>(new byte[]{1, 2, 3, 4}), new byte[]{1}, "# I am a test user.",
                Language.GERMAN, User.ProfileVisibility.MINIMAL, null, 3, false);
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
    public void testUpdateUser() throws Exception {
        assertAll(() -> assertTrue(service.updateUser(testUser)),
                () -> assertEquals(1, testUser.getId()));
        verify(userGateway).updateUser(any());
    }

    @Test
    public void testUpdateWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(userGateway).updateUser(any());
        assertFalse(service.updateUser(testUser));
        verify(feedbackEvent).fire(any());
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
        assertNull(service.getUser(1));
    }

    @Test
    public void testGetUserWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(service.getUser(1));
        verify(feedbackEvent).fire(any());
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

}
package tech.bugger.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.UserDBGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.any;

@ExtendWith(LogExtension.class)
public class ProfileServiceTest {

    @Mock
    Event<Feedback> feedback;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    ResourceBundle messages;

    @Mock
    private Transaction transaction;

    @Mock
    private UserDBGateway gateway;

    @Mock
    private Configuration config;


    private ProfileService profileService;

    private User user;
    private final int theAnswer = 42;
    private final int manyPosts = 1500;
    private final String votingWeightDef = "1000,0,200,50,100,25,400,600,800,10";

    @BeforeEach
    public void setup()
    {
        user = new User(12345, "Helgi", "v3ry_s3cur3", "salt", "algorithm", "helga@web.de", "Helga", "Brötchen", null,
                null, "Hallo, ich bin die Helgi | Perfect | He/They/Her | vergeben | Abo =|= endorsement",
                Language.GERMAN, User.ProfileVisibility.MINIMAL, ZonedDateTime.now(), null, false);
        MockitoAnnotations.openMocks(this);
        profileService = new ProfileService(feedback, transactionManager, applicationSettings, messages);
        when(transactionManager.begin()).thenReturn(transaction);
        when(transaction.newUserGateway()).thenReturn(gateway);
    }

    @Test
    public void testGetUser() throws NotFoundException {
        when(gateway.getUserByID(user.getId())).thenReturn(user);
        User getUser = profileService.getUser(user.getId());
        assertEquals(user.getId(), getUser.getId());
        verify(gateway, times(1)).getUserByID(user.getId());
    }

    @Test
    public void testGetUserNotFound() throws NotFoundException {
        when(gateway.getUserByID(user.getId())).thenThrow(NotFoundException.class);
        assertThrows(tech.bugger.business.exception.NotFoundException.class,
                () -> profileService.getUser(user.getId())
        );
        verify(gateway, times(1)).getUserByID(user.getId());
    }

    @Test
    public void testGetUserTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(transaction).commit();
        assertNull(profileService.getUser(user.getId()));
        verify(transaction, times(1)).commit();
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testUpdateUser() throws NotFoundException {
        profileService.updateUser(user);
        verify(gateway, times(1)).updateUser(user);
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testUpdateUserNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(gateway).updateUser(user);
        assertThrows(tech.bugger.business.exception.NotFoundException.class,
                () -> profileService.updateUser(user)
        );
        verify(gateway, times(1)).updateUser(user);
    }

    @Test
    public void testUpdateUserTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(transaction).commit();
        profileService.updateUser(user);
        verify(transaction, times(1)).commit();
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testGetVotingWeight() {
        when(gateway.getNumberOfPosts(user)).thenReturn(theAnswer);
        when(applicationSettings.getConfiguration()).thenReturn(config);
        when(config.getVotingWeightDefinition()).thenReturn(votingWeightDef);
        assertEquals(3, profileService.getVotingWeightForUser(user));
        verify(gateway, times(1)).getNumberOfPosts(user);
    }

    @Test
    public void testGetVotingWeightMaxWeight() {
        when(gateway.getNumberOfPosts(user)).thenReturn(manyPosts);
        when(applicationSettings.getConfiguration()).thenReturn(config);
        when(config.getVotingWeightDefinition()).thenReturn(votingWeightDef);
        assertEquals(10, profileService.getVotingWeightForUser(user));
        verify(gateway, times(1)).getNumberOfPosts(user);
    }

    @Test
    public void testGetVotingWeightOverwritten() {
        user.setForcedVotingWeight(100);
        assertEquals(100, profileService.getVotingWeightForUser(user));
    }

    @Test
    public void testGetVotingWeightEmpty() {
        when(gateway.getNumberOfPosts(user)).thenReturn(theAnswer);
        when(applicationSettings.getConfiguration()).thenReturn(config);
        when(config.getVotingWeightDefinition()).thenReturn(",");
        profileService.getVotingWeightForUser(user);
        verify(gateway, times(1)).getNumberOfPosts(user);
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testGetVotingWeightNumberFormatException() {
        when(gateway.getNumberOfPosts(user)).thenReturn(theAnswer);
        when(applicationSettings.getConfiguration()).thenReturn(config);
        when(config.getVotingWeightDefinition()).thenReturn("a, b");
        profileService.getVotingWeightForUser(user);
        verify(gateway, times(1)).getNumberOfPosts(user);
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testGetVotingWeightNoPosts() {
        when(gateway.getNumberOfPosts(user)).thenReturn(0);
        when(applicationSettings.getConfiguration()).thenReturn(config);
        when(config.getVotingWeightDefinition()).thenReturn(votingWeightDef);
        assertEquals(0, profileService.getVotingWeightForUser(user));
        verify(gateway, times(1)).getNumberOfPosts(user);
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testGetVotingWeightContainsNoZero() {
        when(gateway.getNumberOfPosts(user)).thenReturn(theAnswer);
        when(applicationSettings.getConfiguration()).thenReturn(config);
        when(config.getVotingWeightDefinition()).thenReturn("100,200");
        profileService.getVotingWeightForUser(user);
        verify(gateway, times(1)).getNumberOfPosts(user);
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testGetNumberOfPosts() {
        when(gateway.getNumberOfPosts(user)).thenReturn(theAnswer);
        assertEquals(42, profileService.getNumberOfPostsForUser(user));
        verify(gateway, times(1)).getNumberOfPosts(user);
    }

    @Test
    public void testGetNumberOfPostsNoPosts() {
        when(gateway.getNumberOfPosts(user)).thenReturn(0);
        assertEquals(0, profileService.getNumberOfPostsForUser(user));
        verify(gateway, times(1)).getNumberOfPosts(user);
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testGetNumberOfPostsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(transaction).commit();
        assertEquals(0, profileService.getNumberOfPostsForUser(user));
        verify(transaction, times(1)).commit();
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminPromote() throws NotFoundException {
        profileService.toggleAdmin(user);
        assertTrue(user.isAdministrator());
        verify(gateway, times(1)).updateUser(user);
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminPromoteNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(gateway).updateUser(user);
        profileService.toggleAdmin(user);
        assertFalse(user.isAdministrator());
        verify(gateway, times(1)).updateUser(user);
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminPromoteTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(transaction).commit();
        profileService.toggleAdmin(user);
        assertFalse(user.isAdministrator());
        verify(transaction, times(1)).commit();
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminDemoteAdmin() throws NotFoundException {
        user.setAdministrator(true);
        when(gateway.getNumberOfAdmins()).thenReturn(theAnswer);
        profileService.toggleAdmin(user);
        assertFalse(user.isAdministrator());
        verify(gateway, times(1)).getNumberOfAdmins();
        verify(gateway, times(1)).updateUser(user);
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminDemoteAdminNotFound() throws NotFoundException {
        user.setAdministrator(true);
        when(gateway.getNumberOfAdmins()).thenReturn(theAnswer);
        doThrow(NotFoundException.class).when(gateway).updateUser(user);
        profileService.toggleAdmin(user);
        assertTrue(user.isAdministrator());
        verify(gateway, times(1)).getNumberOfAdmins();
        verify(gateway, times(1)).updateUser(user);
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminDemoteAdminTransactionException() throws TransactionException, NotFoundException {
        user.setAdministrator(true);
        when(gateway.getNumberOfAdmins()).thenReturn(theAnswer);
        doNothing().doThrow(TransactionException.class).when(transaction).commit();
        profileService.toggleAdmin(user);
        assertTrue(user.isAdministrator());
        verify(gateway, times(1)).getNumberOfAdmins();
        verify(gateway, times(1)).updateUser(user);
        verify(transaction, times(2)).commit();
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminLastAdmin() {
        user.setAdministrator(true);
        when(gateway.getNumberOfAdmins()).thenReturn(1);
        profileService.toggleAdmin(user);
        assertTrue(user.isAdministrator());
        verify(gateway, times(1)).getNumberOfAdmins();
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testToggleAdminNoAdmins() {
        user.setAdministrator(true);
        when(gateway.getNumberOfAdmins()).thenReturn(0);
        assertThrows(InternalError.class,
                () -> profileService.toggleAdmin(user)
        );
        assertTrue(user.isAdministrator());
        verify(gateway, times(1)).getNumberOfAdmins();
    }

    @Test
    public void testToggleAdminTransactionException() throws TransactionException {
        user.setAdministrator(true);
        doThrow(TransactionException.class).when(transaction).commit();
        profileService.toggleAdmin(user);
        assertTrue(user.isAdministrator());
        verify(transaction, times(1)).commit();
        verify(feedback, times(1)).fire(any());
    }
}
