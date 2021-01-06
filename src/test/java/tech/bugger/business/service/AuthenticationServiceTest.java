package tech.bugger.business.service;

import java.lang.reflect.Field;
import javax.enterprise.event.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.ResourceBundleMocker;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Hasher;
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

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
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
                ResourceBundleMocker.mock(""), priorityExecutor, mailer, configReader);

        lenient().doReturn(tx).when(transactionManager).begin();
        lenient().doReturn(tokenGateway).when(tx).newTokenGateway();
        lenient().doReturn(userGateway).when(tx).newUserGateway();
        lenient().doReturn("SHA3-512").when(configReader).getString("HASH_ALGO");
        lenient().doReturn(16).when(configReader).getInt("SALT_LENGTH");

        testUser = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User", new Lazy<>(new byte[]{1, 2, 3, 4}), new byte[]{1}, "# I am a test user.",
                Language.GERMAN, User.ProfileVisibility.MINIMAL, null, 3, false);
        testToken = new Token("0123456789abcdef", Token.Type.REGISTER, null, testUser);
    }

    @AfterEach
    public void tearDown() throws Exception {
        priorityExecutor.shutdown(5000);
    }

    @Test
    public void testGenerateTokenFirstTry() throws Exception {
        try (MockedStatic<Hasher> hasherMock = mockStatic(Hasher.class)) {
            hasherMock.when(() -> Hasher.generateRandomBytes(anyInt())).thenReturn("0123456789abcdef");
            doThrow(NotFoundException.class).when(tokenGateway).findToken(any());
            String token = service.generateToken();
            assertAll(() -> assertNotNull(token),
                    () -> hasherMock.verify(() -> Hasher.generateRandomBytes(anyInt())));
        }
    }

    @Test
    public void testGenerateTokenSecondTry() throws Exception {
        try (MockedStatic<Hasher> hasherMock = mockStatic(Hasher.class)) {
            hasherMock.when(() -> Hasher.generateRandomBytes(anyInt())).thenReturn("0123456789abcdef");
            doReturn(testToken).doThrow(NotFoundException.class).when(tokenGateway).findToken(any());
            String token = service.generateToken();
            assertAll(() -> assertNotNull(token),
                    () -> hasherMock.verify(times(2), () -> Hasher.generateRandomBytes(anyInt())));
        }
    }

    @Test
    public void testRegister() throws Exception {
        doReturn(testToken).when(tokenGateway).createToken(any());
        doReturn(true).when(mailer).send(any());
        service.register(testUser, "http://test.de");
        verify(tokenGateway).createToken(any());
        verify(mailer).send(any());
    }

    @Test
    public void testRegisterMailNotOnFirstTry() throws Exception {
        doReturn(testToken).when(tokenGateway).createToken(any());
        doReturn(false).doReturn(true).when(mailer).send(any());
        service.register(testUser, "http://test.de");
        verify(tokenGateway).createToken(any());
        verify(mailer, times(2)).send(any());
    }

    @Test
    public void testRegisterMailOnTooManyTries() throws Exception {
        doReturn(testToken).when(tokenGateway).createToken(any());
        doReturn(false).when(mailer).send(any());
        service.register(testUser, "http://test.de");
        verify(tokenGateway).createToken(any());
        verify(mailer, times(4)).send(any());
    }

    @Test
    public void testRegisterWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(tokenGateway).createToken(any());
        service.register(testUser, "http://test.de");
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testRegisterWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        service.register(testUser, "http://test.de");
        verify(feedbackEvent, atLeastOnce()).fire(any());
    }

    @Test
    public void testIsValidYes() throws Exception {
        doReturn(testToken).when(tokenGateway).findToken(any());
        assertTrue(service.isValid("0123456789abcdef"));
        verify(tokenGateway).findToken(any());
    }

    @Test
    public void testIsValidNo() throws Exception {
        doThrow(NotFoundException.class).when(tokenGateway).findToken(any());
        assertFalse(service.isValid("0123456789abcdef"));
        verify(tokenGateway).findToken(any());
    }

    @Test
    public void testIsValidWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        service.isValid("0123456789abcdef");
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSetPassword() throws Exception {
        doNothing().when(userGateway).updateUser(any());
        doReturn(testToken).when(tokenGateway).findToken(any());

        boolean res = service.setPassword(testUser, "test1234", "0123456789abcdef");
        assertAll(() -> assertTrue(res),
                () -> assertNotEquals("", testUser.getPasswordHash()),
                () -> assertNotNull(testUser.getPasswordHash()),
                () -> assertNotEquals("", testUser.getPasswordSalt()),
                () -> assertNotNull(testUser.getPasswordSalt()),
                () -> assertNotEquals("", testUser.getHashingAlgorithm()),
                () -> assertNotNull(testUser.getHashingAlgorithm()));
    }

    @Test
    public void testSetPasswordNotValid() throws Exception {
        doThrow(NotFoundException.class).when(tokenGateway).findToken(any());
        boolean res = service.setPassword(testUser, "test1234", "0123456789abcdef");
        assertFalse(res);
        verify(feedbackEvent, atLeastOnce()).fire(any());
    }

    @Test
    public void testSetPasswordWhenCommitInIsValidFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        boolean res = service.setPassword(testUser, "test1234", "0123456789abcdef");
        assertFalse(res);
        // As it is only a query, we can still recover and use the real values.
        verify(feedbackEvent, times(2)).fire(any());
    }

    @Test
    public void testSetPasswordWhenCommitFails() throws Exception {
        AuthenticationService service = mock(AuthenticationService.class);

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

        boolean res = service.setPassword(testUser, "test1234", "0123456789abcdef");
        assertFalse(res);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testSetPasswordWhenUserNotFound() throws Exception {
        doReturn(testToken).when(tokenGateway).findToken(any());
        doThrow(NotFoundException.class).when(userGateway).updateUser(any());
        boolean res = service.setPassword(testUser, "test1234", "0123456789abcdef");
        assertFalse(res);
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testFindToken() throws Exception {
        doReturn(testToken).when(tokenGateway).findToken("0123456789abcdef");

        Token token = service.findToken("0123456789abcdef");
        assertEquals(testToken, token);
        verify(tokenGateway).findToken("0123456789abcdef");
    }

    @Test
    public void testFindTokenWhenNotFound() throws Exception {
        doThrow(NotFoundException.class).when(tokenGateway).findToken("0123456789abcdef");
        assertNull(service.findToken("0123456789abcdef"));
    }

    @Test
    public void testFindTokenWhenCommitFails() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertNull(service.findToken("0123456789abcdef"));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testUpdateEmail() throws NotFoundException {
        doReturn(testToken).when(tokenGateway).createToken(any());
        doReturn(true).when(mailer).send(any());
        assertTrue(service.updateEmail(testUser, "http://test.de"));
        verify(tokenGateway).createToken(any());
        verify(mailer).send(any());
        verify(feedbackEvent, times(1)).fire(any());
    }

    @Test
    public void testUpdateEmailTokenNull() throws NotFoundException {
        assertFalse(service.updateEmail(testUser, "http://test.de"));
        verify(tokenGateway).createToken(any());
    }

}