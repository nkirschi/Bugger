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