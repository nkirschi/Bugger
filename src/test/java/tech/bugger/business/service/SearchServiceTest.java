package tech.bugger.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.SearchGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.event.Event;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

    private SearchService service;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private ApplicationSettings applicationSettings;

    @Mock
    private Transaction tx;

    @Mock
    private SearchGateway searchGateway;

    @Mock
    private Event<Feedback> feedbackEvent;

    @Mock
    private ResourceBundle messages;

    private User user;
    private Topic topic;
    private final String query = "test";

    @BeforeEach
    public void setUp() {
        service = new SearchService(feedbackEvent, messages, transactionManager, applicationSettings);
        when(transactionManager.begin()).thenReturn(tx);
        when(tx.newSearchGateway()).thenReturn(searchGateway);
        user = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        topic = new Topic(1, "title", "description");
    }

    @Test
    public void testGetUserBanSuggestions() {
        List<String> users = new ArrayList<>();
        users.add(user.getUsername());
        when(searchGateway.getUserBanSuggestions(any(), anyInt(), any())).thenReturn(users);
        assertEquals(users, service.getUserBanSuggestions(query, topic));
    }

    @Test
    public void testGetUserBanSuggestionsNoUsers() {
        when(searchGateway.getUserBanSuggestions(any(), anyInt(), any())).thenReturn(new ArrayList<>());
        assertTrue(service.getUserBanSuggestions(query, topic).isEmpty());
    }

    @Test
    public void testGetUserBanSuggestionsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(service.getUserBanSuggestions(query, topic).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetUserUnbanSuggestions() {
        List<String> users = new ArrayList<>();
        users.add(user.getUsername());
        when(searchGateway.getUserUnbanSuggestions(any(), anyInt(), any())).thenReturn(users);
        assertEquals(users, service.getUserUnbanSuggestions(query, topic));
    }

    @Test
    public void testGetUserUnbanSuggestionsNoUsers() {
        when(searchGateway.getUserUnbanSuggestions(any(), anyInt(), any())).thenReturn(new ArrayList<>());
        assertTrue(service.getUserUnbanSuggestions(query, topic).isEmpty());
    }

    @Test
    public void testGetUserUnbanSuggestionsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(service.getUserUnbanSuggestions(query, topic).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetUserModSuggestions() {
        List<String> users = new ArrayList<>();
        users.add(user.getUsername());
        when(searchGateway.getUserModSuggestions(any(), anyInt(), any())).thenReturn(users);
        assertEquals(users, service.getUserModSuggestions(query, topic));
    }

    @Test
    public void testGetUserModSuggestionsNoUsers() {
        when(searchGateway.getUserBanSuggestions(any(), anyInt(), any())).thenReturn(new ArrayList<>());
        assertTrue(service.getUserBanSuggestions(query, topic).isEmpty());
    }

    @Test
    public void testGetUserModSuggestionsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(service.getUserModSuggestions(query, topic).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetUserUnmodSuggestions() {
        List<String> users = new ArrayList<>();
        users.add(user.getUsername());
        when(searchGateway.getUserUnmodSuggestions(any(), anyInt(), any())).thenReturn(users);
        assertEquals(users, service.getUserUnmodSuggestions(query, topic));
    }

    @Test
    public void testGetUserUnmodSuggestionsNoUsers() {
        when(searchGateway.getUserUnmodSuggestions(any(), anyInt(), any())).thenReturn(new ArrayList<>());
        assertTrue(service.getUserUnmodSuggestions(query, topic).isEmpty());
    }

    @Test
    public void testGetUserUnmodSuggestionsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(service.getUserUnmodSuggestions(query, topic).isEmpty());
        verify(feedbackEvent).fire(any());
    }

}
