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

@ExtendWith(MockitoExtension.class)
@ExtendWith(LogExtension.class)
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
        User copy = new User(testUser);
        doAnswer((invocation) -> {
            invocation.getArgument(0, User.class).setId(1);
            return null;
        }).when(userGateway).createUser(any());
        service.createUser(copy);
        assertEquals(1, copy.getId());
        verify(userGateway).createUser(any());
    }

    @Test
    public void testCreateUserWhenCommitFails() throws Exception {
        User copy = new User(testUser);
        doThrow(TransactionException.class).when(tx).commit();
        service.createUser(copy);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpdateUser() throws Exception {
        User copy = new User(testUser);
        service.updateUser(copy);
        assertEquals(1, copy.getId());
        verify(userGateway).updateUser(any());
    }

    @Test
    public void testUpdateWhenNotFound() throws Exception {
        User copy = new User(testUser);
        doThrow(NotFoundException.class).when(userGateway).updateUser(any());
        service.updateUser(copy);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpdateWhenCommitFails() throws Exception {
        User copy = new User(testUser);
        doThrow(TransactionException.class).when(tx).commit();
        service.updateUser(copy);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsEmailAssignedYes() {
        doReturn(true).when(userGateway).isEmailAssigned(any());
        assertTrue(service.isEmailAssigned("test@test.de"));
        verify(userGateway).isEmailAssigned(any());
    }

    @Test
    public void testIsEmailAssignedNo() {
        doReturn(false).when(userGateway).isEmailAssigned(any());
        assertFalse(service.isEmailAssigned("test@test.de"));
        verify(userGateway).isEmailAssigned(any());
    }

    @Test
    public void testIsEmailAssignedWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        service.isEmailAssigned("test@test.de");
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsUsernameAssignedYes() {
        doReturn(true).when(userGateway).isUsernameAssigned(any());
        assertTrue(service.isUsernameAssigned("test"));
        verify(userGateway).isUsernameAssigned(any());
    }

    @Test
    public void testIsUsernameAssignedNo() {
        doReturn(false).when(userGateway).isUsernameAssigned(any());
        assertFalse(service.isUsernameAssigned("test"));
        verify(userGateway).isUsernameAssigned(any());
    }

    @Test
    public void testIsUsernameAssignedWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        service.isUsernameAssigned("test");
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetUser() throws Exception {
        User copy = new User(testUser);
        service.getUser(copy.getId());
        assertEquals(1, copy.getId());
        verify(userGateway).getUserByID(1);
    }

    @Test
    public void testGetUserWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(userGateway).getUserByID(1);
        service.getUser(1);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetUserWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        service.getUser(1);
        verify(feedbackEvent).fire(any());
    }

}