package tech.bugger.business.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.enterprise.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bugger.LogExtension;
import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.gateway.SearchGateway;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(MockitoExtension.class)
public class SearchServiceTest {

    private SearchService service;

    @Mock
    private TransactionManager transactionManager;

    @Mock
    private Configuration config;

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
    private Report report;
    private final String query = "test";

    @BeforeEach
    public void setUp() {
        service = new SearchService(feedbackEvent, messages, transactionManager, applicationSettings);
        lenient().doReturn(config).when(applicationSettings).getConfiguration();
        lenient().when(transactionManager.begin()).thenReturn(tx);
        lenient().when(tx.newSearchGateway()).thenReturn(searchGateway);
        user = new User(1, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        topic = new Topic(1, "title", "description");
        report = new Report(100, "Some Report", null, null, null, null, null, null, null, false, topic.getId(), topic.getTitle());
    }

    @Test
    public void testGetUserSuggestions() {
        List<String> users = new ArrayList<>();
        users.add(user.getUsername());
        when(searchGateway.getUserSuggestions(any(), anyInt())).thenReturn(users);
        assertEquals(users, service.getUserSuggestions(query));
    }

    @Test
    public void testGetUserSuggestionsNoUsers() {
        when(searchGateway.getUserSuggestions(any(), anyInt())).thenReturn(new ArrayList<>());
        assertTrue(service.getUserSuggestions(query).isEmpty());
    }

    @Test
    public void testGetUserSuggestionsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(service.getUserSuggestions(query).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetReportSuggestions() {
        List<String> reports = new ArrayList<>();
        reports.add(report.getTitle());
        when(searchGateway.getReportSuggestions(any(), anyInt())).thenReturn(reports);
        assertEquals(reports, service.getReportSuggestions(query));
    }

    @Test
    public void testGetReportSuggestionsNoReports() {
        when(searchGateway.getReportSuggestions(any(), anyInt())).thenReturn(new ArrayList<>());
        assertTrue(service.getReportSuggestions(query).isEmpty());
    }

    @Test
    public void testGetReportSuggestionsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(service.getReportSuggestions(query).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetTopicSuggestions() {
        List<String> topics = new ArrayList<>();
        topics.add(topic.getTitle());
        when(searchGateway.getTopicSuggestions(any(), anyInt())).thenReturn(topics);
        assertEquals(topics, service.getTopicSuggestions(query));
    }

    @Test
    public void testGetTopicSuggestionsNoTopics() {
        when(searchGateway.getTopicSuggestions(any(), anyInt())).thenReturn(new ArrayList<>());
        assertTrue(service.getTopicSuggestions(query).isEmpty());
    }

    @Test
    public void testGetTopicSuggestionsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(service.getTopicSuggestions(query).isEmpty());
        verify(feedbackEvent).fire(any());
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

    @Test
    public void testGetUserResults() {
        List<User> users = new ArrayList<>();
        users.add(user);
        when(searchGateway.getUserResults(any(), any(), anyBoolean(), anyBoolean())).thenReturn(users);
        assertEquals(users, service.getUserResults(query, null, true, true));
    }

    @Test
    public void testGetUserResultsNoUsers() {
        when(searchGateway.getUserResults(any(), any(), anyBoolean(), anyBoolean())).thenReturn(new ArrayList<>());
        assertTrue(service.getUserResults(query, null, true, true).isEmpty());
    }

    @Test
    public void testGetUserResultsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(service.getUserResults(query, null, true, true).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetTopicResults() {
        List<Topic> topics = new ArrayList<>();
        topics.add(topic);
        when(searchGateway.getTopicResults(any(), any())).thenReturn(topics);
        assertEquals(topics, service.getTopicResults(query, null));
    }

    @Test
    public void testGetTopicResultsNoTopics() {
        when(searchGateway.getTopicResults(any(), any())).thenReturn(new ArrayList<>());
        assertTrue(service.getTopicResults(query, null).isEmpty());
    }

    @Test
    public void testGetTopicResultsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(service.getTopicResults(query, null).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetReportResults() throws Exception {
        List<Report> reports = new ArrayList<>();
        reports.add(report);
        when(searchGateway.getReportResults(any(), any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any(), any())).thenReturn(reports);
        assertEquals(reports, service.getReportResults(query, null, null, null, true, false, false,
                null, Map.of(), Map.of()));
    }

    @Test
    public void testGetReportResultsNoReports() throws Exception {
        when(searchGateway.getReportResults(any(), any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any(), any())).thenReturn(new ArrayList<>());
        assertTrue(service.getReportResults(query, null, null, null, true, false, false,
                null, Map.of(), Map.of()).isEmpty());
    }

    @Test
    public void testGetReportResultsNotFoundException() throws Exception {
        when(searchGateway.getReportResults(any(), any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any(), any())).thenThrow(NotFoundException.class);
        assertTrue(service.getReportResults(query, null, null, null, true, false, false,
                null, Map.of(), Map.of()).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetReportResultsTransactionException() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(service.getReportResults(query, null, null, null, true, false, false,
                null, Map.of(), Map.of()).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetNumberOfReportResults() throws Exception {
        when(searchGateway.getNumberOfReportResults(any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any(), any())).thenReturn(26);
        assertEquals(26, service.getNumberOfReportResults(query, null, null, true, false, false,
                null, Map.of(), Map.of()));
    }

    @Test
    public void testGetNumberOfReportResultsNoReports() throws Exception {
        when(searchGateway.getNumberOfReportResults(any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any(), any())).thenReturn(0);
        assertEquals(0, service.getNumberOfReportResults(query, null, null, true, false, false,
                null, Map.of(), Map.of()));
    }

    @Test
    public void testGetNumberOfReportResultsNotFoundException() throws Exception {
        when(searchGateway.getNumberOfReportResults(any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any(), any())).thenThrow(NotFoundException.class);
        assertEquals(0, service.getNumberOfReportResults(query, null, null, true, false, false,
                null, Map.of(), Map.of()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetNumberOfReportResultsTransactionException() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, service.getNumberOfReportResults(query, null, null, true, false, false,
                null, Map.of(), Map.of()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetNumberOfUserResults() {
        when(searchGateway.getNumberOfUserResults(any(), anyBoolean(), anyBoolean())).thenReturn(42);
        assertEquals(42, service.getNumberOfUserResults(query, true, true));
    }

    @Test
    public void testGetNumberOfUserResultsNoUsers() {
        when(searchGateway.getNumberOfUserResults(any(), anyBoolean(), anyBoolean())).thenReturn(0);
        assertEquals(0, service.getNumberOfUserResults(query, true, true));
    }

    @Test
    public void testGetNumberOfUserResultsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, service.getNumberOfUserResults(query, true, true));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetNumberOfTopicResults() {
        when(searchGateway.getNumberOfTopicResults(any())).thenReturn(42);
        assertEquals(42, service.getNumberOfTopicResults(query));
    }

    @Test
    public void testGetNumberOfTopicResultsNoTopics() {
        when(searchGateway.getNumberOfTopicResults(any())).thenReturn(0);
        assertEquals(0, service.getNumberOfTopicResults(query));
    }

    @Test
    public void testGetNumberOfTopicResultsTransactionException() throws TransactionException {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, service.getNumberOfTopicResults(query));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetVotingWeightFromPosts() {
        doReturn("0,1,10,100").when(config).getVotingWeightDefinition();
        assertEquals(3, service.getVotingWeightFromPosts(42));
    }

    @Test
    public void testGetVotingWeightFromPostsWhenTooManyPosts() {
        doReturn("0,1,10,100").when(config).getVotingWeightDefinition();
        assertEquals(4, service.getVotingWeightFromPosts(420));
    }

    @Test
    public void testGetVotingWeightFromPostsWhenNoPosts() {
        assertEquals(1, service.getVotingWeightFromPosts(0));
    }

    @Test
    public void testGetVotingWeightFromPostsWhenSomePostsNoSettings() {
        doReturn(" ").when(config).getVotingWeightDefinition();
        assertEquals(0, service.getVotingWeightFromPosts(42));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetVotingWeightFromPostsWhenSomePostsInvalidSettings() {
        doReturn(",").when(config).getVotingWeightDefinition();
        assertEquals(0, service.getVotingWeightFromPosts(42));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetVotingWeightFromPostsWhenSomePostsSettingsWithNonInt() {
        doReturn("a,2").when(config).getVotingWeightDefinition();
        assertEquals(0, service.getVotingWeightFromPosts(42));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetVotingWeightFromPostsWhenSomePostsSettingsWithoutZero() {
        doReturn("1,2").when(config).getVotingWeightDefinition();
        assertEquals(0, service.getVotingWeightFromPosts(42));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetFulltextResults() throws Exception {
        List<Report> reports = new ArrayList<>();
        reports.add(report);
        when(searchGateway.getFulltextResults(any(), any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any(), any())).thenReturn(reports);
        assertEquals(reports, service.getFulltextResults(query, null, null, null, true, false, false,
                null, Map.of(), Map.of()));
    }

    @Test
    public void testGetFulltextResultsNoReports() throws Exception {
        when(searchGateway.getFulltextResults(any(), any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any(), any())).thenReturn(new ArrayList<>());
        assertTrue(service.getFulltextResults(query, null, null, null, true, false, false,
                null, Map.of(), Map.of()).isEmpty());
    }

    @Test
    public void testGetFulltextResultsNotFoundException() throws Exception {
        when(searchGateway.getFulltextResults(any(), any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any(), any())).thenThrow(NotFoundException.class);
        assertTrue(service.getFulltextResults(query, null, null, null, true, false, false,
                null, Map.of(), Map.of()).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetFulltextResultsTransactionException() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertTrue(service.getFulltextResults(query, null, null, null, true, false, false,
                null, Map.of(), Map.of()).isEmpty());
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testNumberOfGetFulltextResults() throws Exception {
        when(searchGateway.getNumberOfFulltextResults(any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any(), any())).thenReturn(42);
        assertEquals(42, service.getNumberOfFulltextResults(query, null, null, true, false, false,
                null, Map.of(), Map.of()));
    }

    @Test
    public void testGetNumberOfFulltextResultsNoReports() throws Exception {
        when(searchGateway.getNumberOfFulltextResults(any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any(), any())).thenReturn(0);
        assertEquals(0, service.getNumberOfFulltextResults(query, null, null, true, false, false,
                null, Map.of(), Map.of()));
    }

    @Test
    public void testGetNumberOfFulltextResultsNotFoundException() throws Exception {
        when(searchGateway.getNumberOfFulltextResults(any(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean(),
                any(), any(), any())).thenThrow(NotFoundException.class);
        assertEquals(0, service.getNumberOfFulltextResults(query, null, null, true, false, false,
                null, Map.of(), Map.of()));
        verify(feedbackEvent).fire(any());
    }

    @Test
    public void testGetNumberOfFulltextResultsTransactionException() throws Exception {
        doThrow(TransactionException.class).when(tx).commit();
        assertEquals(0, service.getNumberOfFulltextResults(query, null, null, true, false, false,
                null, Map.of(), Map.of()));
        verify(feedbackEvent).fire(any());
    }

}