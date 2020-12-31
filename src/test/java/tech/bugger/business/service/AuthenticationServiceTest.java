package tech.bugger.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.bugger.LogExtension;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Hasher;
import tech.bugger.business.util.PriorityExecutor;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.UserDBGateway;
import tech.bugger.persistence.util.Mailer;
import tech.bugger.persistence.util.PropertiesReader;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;
import java.time.ZonedDateTime;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;

@ExtendWith(LogExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private TransactionManager manager;

    @Mock
    private Transaction transaction;

    @Mock
    private UserDBGateway gateway;

    @Mock
    private Event<Feedback> feedback;

    @Mock
    private Mailer mailer;

    @Mock
    private PriorityExecutor priorityExecutor;

    @Mock
    private PropertiesReader configReader;

    @Mock
    ResourceBundle messages;

    private AuthenticationService authenticationService;

    private User user;
    private final String password = "v3rys3cur3";
    private String wrongPassword = "password";
    private String salt = "2f73616c7421";
    private String hashingAlgo = "SHA3-512";

    @BeforeEach
    public void setUp() {
        user = new User(12345, "Helgi", password, salt, hashingAlgo, "helga@web.de", "Helga", "Brötchen",
                new Lazy<>(new byte[0]), null, "Hallo, ich bin die Helgi | Perfect | He/They/Her | vergeben | Abo =|= "
                + "endorsement", Language.GERMAN, User.ProfileVisibility.MINIMAL, ZonedDateTime.now(), null, false);
        user.setPasswordHash(Hasher.hash(password, user.getPasswordSalt(), user.getHashingAlgorithm()));
        MockitoAnnotations.openMocks(this);
        authenticationService = new AuthenticationService(manager, feedback, messages, priorityExecutor, mailer,
                configReader);
        when(manager.begin()).thenReturn(transaction);
        when(transaction.newUserGateway()).thenReturn(gateway);
    }

    @Test
    public void testAuthenticate() throws NotFoundException {
        when(gateway.getUserByUsername(user.getUsername())).thenReturn(user);
        assertEquals(user, authenticationService.authenticate(user.getUsername(), password));
        verify(gateway, times(1)).getUserByUsername(user.getUsername());
    }

    @Test
    public void testAuthenticateWrongPassword() throws NotFoundException {
        when(gateway.getUserByUsername(user.getUsername())).thenReturn(user);
        assertNull(authenticationService.authenticate(user.getUsername(), wrongPassword));
        verify(gateway, times(1)).getUserByUsername(user.getUsername());
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testAuthenticateNotFound() throws NotFoundException {
        when(gateway.getUserByUsername(user.getUsername())).thenThrow(NotFoundException.class);
        assertNull(authenticationService.authenticate(user.getUsername(), password));
        verify(gateway, times(1)).getUserByUsername(user.getUsername());
        verify(feedback, times(1)).fire(any());
    }

    @Test
    public void testAuthenticateTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(transaction).commit();
        assertNull(authenticationService.authenticate(user.getUsername(), password));
        verify(transaction, times(1)).commit();
        verify(feedback, times(1)).fire(any());
    }
}
