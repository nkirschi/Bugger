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
    private final String votingWeightDef = "0,10,25,50,100,200,400,600,800,1000";

    @BeforeEach
    public void setup()
    {
        user = new User();
        user.setId(12345);
        user.setUsername("Helgi");
        user.setPasswordHash("v3ry_s3cur3");
        user.setEmailAddress("helga@web.de");
        user.setFirstName("Helga");
        user.setLastName("Brötchen");
        user.setBiography("Hallo, ich bin die Helgi | Perfect | He/They/Her | vergeben | Abo =|= endorsement)");
        user.setProfileVisibility(User.ProfileVisibility.MINIMAL);
        user.setRegistrationDate(ZonedDateTime.now());
        user.setAdministrator(false);
        MockitoAnnotations.openMocks(this);
        profileService = new ProfileService(feedback, transactionManager, applicationSettings, messages);
        when(transactionManager.begin()).thenReturn(transaction);
        when(transaction.newUserGateway()).thenReturn(gateway);
    }

    @Test
    public void testGetUser() {
        try {
            when(gateway.getUserByID(user.getId())).thenReturn(user);
            User getUser = profileService.getUser(user.getId());
            assertEquals(user.getId(), getUser.getId());
            verify(gateway, times(1)).getUserByID(user.getId());
        } catch (NotFoundException e) {
            fail("The gateway has falsely thrown a NotFoundException!");
        }
    }

    @Test
    public void testGetUserNotFound() throws NotFoundException {
        when(gateway.getUserByID(user.getId())).thenThrow(NotFoundException.class);
        assertNull(profileService.getUser(user.getId()));
        verify(gateway, times(1)).getUserByID(user.getId());
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testGetUserTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(transaction).commit();
        assertNull(profileService.getUser(user.getId()));
        verify(transaction, times(1)).commit();
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testUpdateUser() {
        try {
            profileService.updateUser(user);
            verify(gateway, times(1)).updateUser(user);
        } catch (NotFoundException e) {
            fail("The gateway has falsely thrown a NotFoundException!");
        }
    }

    @Test
    public void testUpdateUserNotFound() throws NotFoundException {
        doThrow(NotFoundException.class).when(gateway).updateUser(user);
        profileService.updateUser(user);
        verify(gateway, times(1)).updateUser(user);
        verify(feedback, times(1)).fire(any());
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
        try {
            when(gateway.getNumberOfPosts(user)).thenReturn(theAnswer);
            when(applicationSettings.getConfiguration()).thenReturn(config);
            when(config.getVotingWeightDefinition()).thenReturn(votingWeightDef);
            assertEquals(3, profileService.getVotingWeightForUser(user));
            verify(gateway, times(1)).getNumberOfPosts(user);
        } catch (NotFoundException e) {
            fail("The gateway has falsely thrown a NotFoundException!");
        }
    }

    @Test
    public void testGetVotingWeightNumberFormatException() {
        try {
            when(gateway.getNumberOfPosts(user)).thenReturn(theAnswer);
            when(applicationSettings.getConfiguration()).thenReturn(config);
            when(config.getVotingWeightDefinition()).thenReturn("a, b");
            profileService.getVotingWeightForUser(user);
            verify(gateway, times(1)).getNumberOfPosts(user);
            verify(feedback, times(1)).fire(any());
        } catch (NotFoundException e) {
            fail("The gateway has falsely thrown a NotFoundException!");
        }
    }

    @Test
    public void testGetVotingWeightNotFound() throws NotFoundException {
        when(gateway.getNumberOfPosts(user)).thenThrow(NotFoundException.class);
        assertEquals(0, profileService.getVotingWeightForUser(user));
        verify(gateway, times(1)).getNumberOfPosts(user);
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testGetVotingWeightTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(transaction).commit();
        assertEquals(0, profileService.getVotingWeightForUser(user));
        verify(transaction, times(1)).commit();
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testGetNumberOfPosts() {
        try {
            when(gateway.getNumberOfPosts(user)).thenReturn(theAnswer);
            assertEquals(42, profileService.getNumberOfPostsForUser(user));
            verify(gateway, times(1)).getNumberOfPosts(user);
        } catch (NotFoundException e) {
            fail("The gateway has falsely thrown a NotFoundException!");
        }
    }

    @Test
    public void testGetNumberOfPostsNotFound() throws NotFoundException {
        when(gateway.getNumberOfPosts(user)).thenThrow(NotFoundException.class);
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
    public void testToggleAdminPromote() {
        try {
            profileService.toggleAdmin(user);
            assertTrue(user.isAdministrator());
            verify(gateway, times(1)).updateUser(user);
        } catch (NotFoundException e) {
            fail("The gateway has falsely thrown a NotFoundException!");
        }
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
    public void testToggleAdminDemoteAdmin() {
        try {
            user.setAdministrator(true);
            when(gateway.getNumberOfAdmins()).thenReturn(theAnswer);
            profileService.toggleAdmin(user);
            assertFalse(user.isAdministrator());
            verify(gateway, times(1)).getNumberOfAdmins();
            verify(gateway, times(1)).updateUser(user);
        } catch (NotFoundException e) {
            fail("The gateway has falsely thrown a NotFoundException!");
        }
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
    public void testToggleAdminDemoteAdminTransactionException() throws TransactionException {
        try {
            user.setAdministrator(true);
            when(gateway.getNumberOfAdmins()).thenReturn(theAnswer);
            doNothing().doThrow(TransactionException.class).when(transaction).commit();
            profileService.toggleAdmin(user);
            assertTrue(user.isAdministrator());
            verify(gateway, times(1)).getNumberOfAdmins();
            verify(gateway, times(1)).updateUser(user);
            verify(transaction, times(2)).commit();
            verify(feedback, times(1)).fire(any());
        } catch (NotFoundException e) {
            fail("The gateway has falsely thrown a NotFoundException!");
        }
    }

    @Test
    public void testToggleAdminLastAdmin() {
        try {
            user.setAdministrator(true);
            when(gateway.getNumberOfAdmins()).thenReturn(1);
            profileService.toggleAdmin(user);
            assertTrue(user.isAdministrator());
            verify(gateway, times(1)).getNumberOfAdmins();
            verify(feedback, times(1)).fire(any());
        } catch (NotFoundException e) {
            fail("The gateway has falsely thrown a NotFoundException!");
        }
    }

    @Test
    public void testToggleAdminNotFound() throws NotFoundException {
        user.setAdministrator(true);
        doThrow(NotFoundException.class).when(gateway).getNumberOfAdmins();
        profileService.toggleAdmin(user);
        assertTrue(user.isAdministrator());
        verify(gateway, times(1)).getNumberOfAdmins();
        verify(feedback, times(1)).fire(any());
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
