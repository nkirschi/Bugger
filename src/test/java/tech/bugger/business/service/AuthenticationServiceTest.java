package tech.bugger.business.service;

import java.lang.reflect.Field;
import javax.enterprise.event.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.PriorityExecutor;
import tech.bugger.business.util.PriorityTask;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.TokenDBGateway;
import tech.bugger.persistence.gateway.UserDBGateway;
import tech.bugger.persistence.util.Mailer;
import tech.bugger.persistence.util.PropertiesReader;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(LogExtension.class)
public class AuthenticationServiceTest {

    private AuthenticationService service;

    private User testUser;

    private Token testToken;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Transaction tx;

    @Mock
    private TokenDBGateway tokenGateway;

    @Mock
    private UserDBGateway userGateway;

    @Mock
    private Event<Feedback> feedbackEvent;

    @Mock
    private Mailer mailer;

    @Mock
    private PriorityExecutor priorityExecutor;

    @Mock
    private PropertiesReader configReader;

    @BeforeEach
    public void setUp() {
        // Instantly run tasks.
        lenient().doAnswer(invocation -> {
            invocation.getArgument(0, PriorityTask.class).run();
            return null;
        }).when(priorityExecutor).enqueue(any());

        service = new AuthenticationService(transactionManager, feedbackEvent, ResourceBundleMocker.mock(""),
                priorityExecutor, mailer, configReader);

        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(tokenGateway).when(tx).newTokenGateway();
        lenient().doReturn(userGateway).when(tx).newUserGateway();
        lenient().doReturn("SHA3-512").when(configReader).getString("HASH_ALGO");

        testUser = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User", new Lazy<>(new byte[]{1, 2, 3, 4}), new byte[]{1}, "# I am a test user.",
                Language.GERMAN, User.ProfileVisibility.MINIMAL, null, 3, false);
        testToken = new Token("0123456789abcdef", Token.Type.REGISTER, null, testUser);
    }

    @AfterEach
    public void tearDown() throws Exception {
        priorityExecutor.shutdown(5000);
    }

    @Test
    public void testRegister() throws Exception {
        User copy = new User(testUser);
        doReturn(testToken).when(tokenGateway).generateToken(any(), any());
        doReturn(true).when(mailer).send(any());
        service.register(copy);
        verify(tokenGateway).generateToken(any(), any());
        verify(mailer).send(any());
    }

    @Test
    public void testRegisterMailNotOnFirstTry() throws Exception {
        User copy = new User(testUser);
        doReturn(testToken).when(tokenGateway).generateToken(any(), any());
        doReturn(false).doReturn(true).when(mailer).send(any());
        service.register(copy);
        verify(tokenGateway).generateToken(any(), any());
        verify(mailer, times(2)).send(any());
    }

    @Test
    public void testRegisterWhenNotFound() throws Exception {
        User copy = new User(testUser);
        doThrow(NotFoundException.class).when(tokenGateway).generateToken(any(), any());
        service.register(copy);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testRegisterWhenCommitFails() throws Exception {
        User copy = new User(testUser);
        doThrow(TransactionException.class).when(tx).commit();
        service.register(copy);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testIsValidYes() {
        doReturn(true).when(tokenGateway).isValid(any());
        assertTrue(service.isValid("0123456789abcdef"));
        verify(tokenGateway).isValid(any());
    }

    @Test
    public void testIsValidNo() {
        doReturn(false).when(tokenGateway).isValid(any());
        assertFalse(service.isValid("0123456789abcdef"));
        verify(tokenGateway).isValid(any());
    }

    @Test
    public void testIsValidWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        service.isValid("0123456789abcdef");
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSetPassword() throws Exception {
        User copy = new User(testUser);
        doNothing().when(userGateway).updateUser(any());
        doReturn(true).when(tokenGateway).isValid(any());

        boolean res = service.setPassword(copy, "test1234", "0123456789abcdef");
        assertAll(() -> assertTrue(res),
                () -> assertNotEquals("", copy.getPasswordHash()),
                () -> assertNotNull(copy.getPasswordHash()),
                () -> assertNotEquals("", copy.getPasswordSalt()),
                () -> assertNotNull(copy.getPasswordSalt()),
                () -> assertNotEquals("", copy.getHashingAlgorithm()),
                () -> assertNotNull(copy.getHashingAlgorithm()));
    }

    @Test
    public void testSetPasswordNotValid() {
        User copy = new User(testUser);

        doReturn(false).when(tokenGateway).isValid(any());
        boolean res = service.setPassword(copy, "test1234", "0123456789abcdef");
        assertFalse(res);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSetPasswordWhenCommitInIsValidFails() throws Exception {
        User copy = new User(testUser);

        doThrow(TransactionException.class).when(tx).commit();
        boolean res = service.setPassword(copy, "test1234", "0123456789abcdef");
        assertFalse(res);
        // As it is only a query, we can still recover and use the real values.
        verify(feedbackEvent, times(2)).fire(any());
    }

    @Test
    public void testSetPasswordWhenCommitFails() throws Exception {
        AuthenticationService service = mock(AuthenticationService.class);
        User copy = new User(testUser);

        doCallRealMethod().when(service).setPassword(any(), any(), any());
        doReturn(true).when(service).isValid(any());
        doThrow(TransactionException.class).when(tx).commit();

        Field f = AuthenticationService.class.getDeclaredField("transactionManager");
        f.setAccessible(true);
        f.set(service, transactionManager);

        f = AuthenticationService.class.getDeclaredField("configReader");
        f.setAccessible(true);
        f.set(service, configReader);

        f = AuthenticationService.class.getDeclaredField("feedbackEvent");
        f.setAccessible(true);
        f.set(service, feedbackEvent);

        f = AuthenticationService.class.getDeclaredField("messagesBundle");
        f.setAccessible(true);
        f.set(service, ResourceBundleMocker.mock(""));

        boolean res = service.setPassword(copy, "test1234", "0123456789abcdef");
        assertFalse(res);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSetPasswordWhenUserNotFound() throws Exception {
        User copy = new User(testUser);

        doReturn(true).when(tokenGateway).isValid(any());
        doThrow(NotFoundException.class).when(userGateway).updateUser(any());
        boolean res = service.setPassword(copy, "test1234", "0123456789abcdef");
        assertFalse(res);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetUserIdForToken() throws Exception {
        doReturn(1).when(tokenGateway).getUserIdForToken("0123456789abcdef");

        int id = service.getUserIdForToken("0123456789abcdef");
        assertEquals(1, id);
        verify(tokenGateway).getUserIdForToken("0123456789abcdef");
    }

    @Test
    public void testGetUserIdForTokenWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(tokenGateway).getUserIdForToken("0123456789abcdef");
        service.getUserIdForToken("0123456789abcdef");
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetUserIdForTokenWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        service.getUserIdForToken("0123456789abcdef");
        verify(feedbackEvent).fire(any());
    }

}