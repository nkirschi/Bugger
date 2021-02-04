package tech.bugger.persistence.gateway;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.DuplicateException;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@ExtendWith(DBExtension.class)
@ExtendWith(LogExtension.class)
public class SearchDBGatewayTest {

    private SearchGateway searchGateway;
    private UserGateway userGateway;
    private ReportGateway reportGateway;
    private TopicGateway topicGateway;

    private Connection connection;

    private Selection selection;
    private User user1;
    private User user2;
    private Topic topic1;
    private Topic topic2;
    private Report report1;
    private Report report2;
    private User admin;
    private static final String QUERY = "test";
    private static final String QUERY1 = "1";
    private static final String QUERY2 = "2";
    private static final int LIMIT = 5;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        userGateway = new UserDBGateway(connection);
        searchGateway = new SearchDBGateway(connection);
        topicGateway = new TopicDBGateway(connection);
        reportGateway = new ReportDBGateway(connection, userGateway);
        selection = new Selection(0,0, Selection.PageSize.SMALL, "id", true);

        user1 = new User(null, "testuser1", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test"
                , "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        user2 = new User(null, "testuser2", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.com",
                "Test", "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.FULL, null, null, false);
        topic1 = new Topic(1, "topic1", "description");
        topic2 = new Topic(2, "topic2", "description");
        report1 = new Report(1, "report1", Report.Type.BUG, Report.Severity.MINOR, "",
                new Authorship(null, null, null, null), null,
                null, 100, true, 1, null);
        report2 = new Report(2, "report2", Report.Type.BUG, Report.Severity.MINOR, "",
                new Authorship(null, null, null, null), null,
                null, null, false, 2, null);
        admin = new User(null, "testadmin", "v3ry_s3cur3", "salt", "algorithm", "admin@admin.de", "Helgo", "Brötchen",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "Ich bin der Administrator hier!", Locale.ENGLISH,
                User.ProfileVisibility.MINIMAL, OffsetDateTime.now(), null, true);

    }

    @Test
    public void testGetUserBanSuggestions() throws NotFoundException, DuplicateException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic1);
        List<String> suggestions = searchGateway.getUserBanSuggestions(QUERY, LIMIT, topic1);
        assertAll(
                () -> assertTrue(suggestions.contains(user1.getUsername())),
                () -> assertTrue(suggestions.contains(user2.getUsername())),
                () -> assertFalse(suggestions.contains(admin.getUsername()))
        );
    }

    @Test
    public void testGetUserBanSuggestionsWithModeratorsAndBanned() throws NotFoundException, DuplicateException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic1);
        topicGateway.promoteModerator(topic1, user1);
        topicGateway.banUser(topic1, user2);
        List<String> suggestions = searchGateway.getUserBanSuggestions(QUERY, LIMIT, topic1);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    public void testGetUserBanSuggestionsNone() throws NotFoundException, DuplicateException {
        userGateway.createUser(admin);
        topicGateway.createTopic(topic1);
        List<String> suggestions = searchGateway.getUserBanSuggestions(QUERY, LIMIT, topic1);
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testGetUserBanSuggestionsQueryNull() {
        assertThrows(IllegalArgumentException.class,
                () -> searchGateway.getUserBanSuggestions(null, LIMIT, topic1)
        );
    }

    @Test
    public void testGetUserBanSuggestionsQueryBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> searchGateway.getUserBanSuggestions("", LIMIT, topic1)
        );
    }

    @Test
    public void testGetUserBanSuggestionsTopicIdNull() {
        topic1.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> searchGateway.getUserBanSuggestions(QUERY, LIMIT, topic1)
        );
    }

    @Test
    public void testGetUserBanSuggestionsLimitNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> searchGateway.getUserBanSuggestions(QUERY, -1, topic1)
        );
    }

    @Test
    public void testGetUserBanSuggestionsSQLException() throws SQLException {
        topic1.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SearchDBGateway(connectionSpy).getUserBanSuggestions(QUERY, LIMIT, topic1)
        );
    }

    @Test
    public void testGetUserUnbanSuggestions() throws NotFoundException, DuplicateException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic1);
        topicGateway.banUser(topic1, user1);
        List<String> suggestions = searchGateway.getUserUnbanSuggestions(QUERY, LIMIT, topic1);
        assertAll(
                () -> assertTrue(suggestions.contains(user1.getUsername())),
                () -> assertFalse(suggestions.contains(user2.getUsername())),
                () -> assertFalse(suggestions.contains(admin.getUsername()))
        );
    }

    @Test
    public void testGetUserUnbanSuggestionsNone() throws NotFoundException, DuplicateException {
        userGateway.createUser(user1);
        topicGateway.createTopic(topic1);
        List<String> suggestions = searchGateway.getUserUnbanSuggestions(QUERY, LIMIT, topic1);
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testGetUserUnbanSuggestionsSQLException() throws SQLException {
        topic1.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SearchDBGateway(connectionSpy).getUserUnbanSuggestions(QUERY, LIMIT, topic1)
        );
    }

    @Test
    public void testGetUserModSuggestions() throws NotFoundException, DuplicateException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic1);
        topicGateway.promoteModerator(topic1, user2);
        List<String> suggestions = searchGateway.getUserModSuggestions(QUERY, LIMIT, topic1);
        assertAll(
                () -> assertTrue(suggestions.contains(user1.getUsername())),
                () -> assertFalse(suggestions.contains(user2.getUsername())),
                () -> assertFalse(suggestions.contains(admin.getUsername()))
        );
    }

    @Test
    public void testGetUserModSuggestionsNone() throws NotFoundException, DuplicateException {
        userGateway.createUser(admin);
        topicGateway.createTopic(topic1);
        List<String> suggestions = searchGateway.getUserModSuggestions(QUERY, LIMIT, topic1);
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testGetUserModSuggestionsSQLException() throws SQLException {
        topic1.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SearchDBGateway(connectionSpy).getUserModSuggestions(QUERY, LIMIT, topic1)
        );
    }

    @Test
    public void testGetUserUnmodSuggestions() throws NotFoundException, DuplicateException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic1);
        topicGateway.promoteModerator(topic1, user1);
        List<String> suggestions = searchGateway.getUserUnmodSuggestions(QUERY, LIMIT, topic1);
        assertAll(
                () -> assertTrue(suggestions.contains(user1.getUsername())),
                () -> assertFalse(suggestions.contains(user2.getUsername())),
                () -> assertFalse(suggestions.contains(admin.getUsername()))
        );
    }

    @Test
    public void testGetUserUnmodSuggestionsNone() throws NotFoundException, DuplicateException {
        userGateway.createUser(admin);
        topicGateway.createTopic(topic1);
        List<String> suggestions = searchGateway.getUserUnmodSuggestions(QUERY, LIMIT, topic1);
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testGetUserUnmodSuggestionsSQLException() throws SQLException {
        topic1.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SearchDBGateway(connectionSpy).getUserUnmodSuggestions(QUERY, LIMIT, topic1)
        );
    }

    @Test
    public void testGetUserSuggestions() {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        List<String> suggestions = searchGateway.getUserSuggestions(QUERY2, LIMIT);
        assertAll(
                () -> assertFalse(suggestions.contains(user1.getUsername())),
                () -> assertTrue(suggestions.contains(user2.getUsername()))
        );
    }

    @Test
    public void testGetUserSuggestionSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        SearchGateway spyedSearchGateway = new SearchDBGateway(connectionSpy);
        assertThrows(StoreException.class, () -> spyedSearchGateway.getUserSuggestions(QUERY2, LIMIT));
    }

    @Test
    public void testGetTopicSuggestionSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        SearchGateway spyedSearchGateway = new SearchDBGateway(connectionSpy);
        assertThrows(StoreException.class, () -> spyedSearchGateway.getTopicSuggestions(QUERY2, LIMIT));
    }

    @Test
    public void testGetReportSuggestionSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        SearchGateway spyedSearchGateway = new SearchDBGateway(connectionSpy);
        assertThrows(StoreException.class, () -> spyedSearchGateway.getReportSuggestions(QUERY2, LIMIT));
    }

    @Test
    public void testGetReportSuggestions() throws NotFoundException, DuplicateException {
        topicGateway.createTopic(topic1);
        topicGateway.createTopic(topic2);
        reportGateway.create(report1);
        reportGateway.create(report2);

        List<String> suggestions = searchGateway.getReportSuggestions(QUERY2, LIMIT);
        assertAll(
                () -> assertFalse(suggestions.contains(report1.getTitle())),
                () -> assertTrue(suggestions.contains(report2.getTitle()))
        );
    }

    @Test
    public void testGetTopicSuggestions() throws NotFoundException, DuplicateException {
        topicGateway.createTopic(topic1);
        topicGateway.createTopic(topic2);
        List<String> suggestions = searchGateway.getTopicSuggestions(QUERY2, LIMIT);
        assertAll(
                () -> assertFalse(suggestions.contains(topic1.getTitle())),
                () -> assertTrue(suggestions.contains(topic2.getTitle()))
        );
    }

    @Test
    public void testGetUserResults() {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        selection.setSortedBy("username");
        List<User> result = searchGateway.getUserResults(QUERY2, selection, true, true);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetUserResultsSelectionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> searchGateway.getUserResults(QUERY2, null, true, true));
    }

    @Test
    public void testGetUserResultsQueryIsNull() {
        assertThrows(IllegalArgumentException.class, () -> searchGateway.getUserResults(null, selection, true, true));
    }

    @Test
    public void testGetUserResultsSortByIsNull() {
        selection.setSortedBy(null);
        assertThrows(IllegalArgumentException.class, () -> searchGateway.getUserResults(QUERY2, selection, true, true));
    }

    @Test
    public void testGetUserResultsFilterAll() {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        selection.setSortedBy("username");
        List<User> result = searchGateway.getUserResults(QUERY2, selection, false, false);
        assertEquals(0, result.size());  }

    @Test
    public void testGetUserResultsShowNonAdmins() {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        selection.setSortedBy("username");
        List<User> result = searchGateway.getUserResults(QUERY1, selection, false, true);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetUserResultsSortByRelevance() {
        selection.setSortedBy("relevance");
        assertThrows(IllegalArgumentException.class, () -> searchGateway.getUserResults(QUERY2, selection, true, true));
    }

    @Test
    public void testGetUserResultsSQLException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        SearchGateway spyedSearchGateway = new SearchDBGateway(connectionSpy);
        assertThrows(StoreException.class, () -> spyedSearchGateway.getUserResults(QUERY2, selection, true, true));
    }

    @Test
    public void testValidateSuggestionParamsQueryIsNull() {
        assertThrows(IllegalArgumentException.class, () -> searchGateway.getReportSuggestions(null, LIMIT));
    }

    @Test
    public void testValidateSuggestionParamsQueryIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> searchGateway.getReportSuggestions("", LIMIT));
    }

    @Test
    public void testValidateSuggestionParamsLimitIsZero() {
        assertThrows(IllegalArgumentException.class, () -> searchGateway.getReportSuggestions(null, 0));
    }

    @Test
    public void testGetReportResults() throws NotFoundException, DuplicateException {
        Map<Report.Type, Boolean> typeHashMap = new HashMap<>();
        typeHashMap.put(Report.Type.BUG, true);
        typeHashMap.put(Report.Type.FEATURE, true);
        typeHashMap.put(Report.Type.HINT, true);
        Map<Report.Severity, Boolean> severityHashMap = new HashMap<>();
        severityHashMap.put(Report.Severity.MINOR, true);
        severityHashMap.put(Report.Severity.RELEVANT, true);
        severityHashMap.put(Report.Severity.SEVERE, true);
        topicGateway.createTopic(topic1);
        topicGateway.createTopic(topic2);
        reportGateway.create(report1);
        reportGateway.create(report2);
        selection.setSortedBy("title");
        List<Report> result = searchGateway.getReportResults(QUERY2, selection, null, null, true, true, true, null, typeHashMap, severityHashMap);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetReportResultsSelectionIsNull() {
        Map<Report.Type, Boolean> typeHashMap = new HashMap<>();
        typeHashMap.put(Report.Type.BUG, true);
        typeHashMap.put(Report.Type.FEATURE, true);
        typeHashMap.put(Report.Type.HINT, true);
        Map<Report.Severity, Boolean> severityHashMap = new HashMap<>();
        severityHashMap.put(Report.Severity.MINOR, true);
        severityHashMap.put(Report.Severity.RELEVANT, true);
        severityHashMap.put(Report.Severity.SEVERE, true);
        assertThrows(IllegalArgumentException.class, () -> searchGateway.getReportResults(QUERY2, null, null, null, true, true, true, null, typeHashMap, severityHashMap));
    }

    @Test
    public void testGetReportResultsQueryIsNull() {
        Map<Report.Type, Boolean> typeHashMap = new HashMap<>();
        typeHashMap.put(Report.Type.BUG, true);
        typeHashMap.put(Report.Type.FEATURE, true);
        typeHashMap.put(Report.Type.HINT, true);
        Map<Report.Severity, Boolean> severityHashMap = new HashMap<>();
        severityHashMap.put(Report.Severity.MINOR, true);
        severityHashMap.put(Report.Severity.RELEVANT, true);
        severityHashMap.put(Report.Severity.SEVERE, true);
        assertThrows(IllegalArgumentException.class, () -> searchGateway.getReportResults(null , selection, null, null, true, true, true, null, typeHashMap, severityHashMap));
    }

    @Test
    public void testGetReportResultsSortByIsNull() {
        selection.setSortedBy(null);
        Map<Report.Type, Boolean> typeHashMap = new HashMap<>();
        typeHashMap.put(Report.Type.BUG, true);
        typeHashMap.put(Report.Type.FEATURE, true);
        typeHashMap.put(Report.Type.HINT, true);
        Map<Report.Severity, Boolean> severityHashMap = new HashMap<>();
        severityHashMap.put(Report.Severity.MINOR, true);
        severityHashMap.put(Report.Severity.RELEVANT, true);
        severityHashMap.put(Report.Severity.SEVERE, true);
        assertThrows(IllegalArgumentException.class, () -> searchGateway.getReportResults(QUERY2, selection, null, null, true, true, true, null, typeHashMap, severityHashMap));
    }

    @Test
    public void testGetReportResultsTypeFilter() throws NotFoundException {
        Map<Report.Type, Boolean> typeHashMap = new HashMap<>();
        typeHashMap.put(Report.Type.BUG, false);
        typeHashMap.put(Report.Type.FEATURE, false);
        typeHashMap.put(Report.Type.HINT, false);
        Map<Report.Severity, Boolean> severityHashMap = new HashMap<>();
        severityHashMap.put(Report.Severity.MINOR, true);
        severityHashMap.put(Report.Severity.RELEVANT, true);
        severityHashMap.put(Report.Severity.SEVERE, true);
        List<Report> result = searchGateway.getReportResults(QUERY2, selection, null, null, true, true, true, null, typeHashMap, severityHashMap);
        assertEquals(0, result.size());    }

    @Test
    public void testGetReportResultsSeverityFilter() throws NotFoundException {
        Map<Report.Type, Boolean> typeHashMap = new HashMap<>();
        typeHashMap.put(Report.Type.BUG, true);
        typeHashMap.put(Report.Type.FEATURE, true);
        typeHashMap.put(Report.Type.HINT, true);
        Map<Report.Severity, Boolean> severityHashMap = new HashMap<>();
        severityHashMap.put(Report.Severity.MINOR, false);
        severityHashMap.put(Report.Severity.RELEVANT, false);
        severityHashMap.put(Report.Severity.SEVERE, false);
        List<Report> result = searchGateway.getReportResults(QUERY2, selection, null, null, true, true, true, null, typeHashMap, severityHashMap);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetTopicResults() throws NotFoundException, DuplicateException {
        topicGateway.createTopic(topic1);
        topicGateway.createTopic(topic2);
        selection.setSortedBy("title");
        List<Topic> result = searchGateway.getTopicResults(QUERY2, selection);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetTopicResultsSelectionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> searchGateway.getTopicResults(QUERY2, null));
    }

    @Test
    public void testGetTopicResultsQueryIsNull() {
        assertThrows(IllegalArgumentException.class, () -> searchGateway.getTopicResults(null , selection));
    }

    @Test
    public void testGetTopicResultsSortByIsNull() {
        selection.setSortedBy(null);
        assertThrows(IllegalArgumentException.class, () -> searchGateway.getTopicResults(QUERY2, selection));
    }

    @Test
    public void getNumberOfUserResults() {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        selection.setSortedBy("username");
        assertEquals(1, searchGateway.getNumberOfUserResults(QUERY2, true, true));
    }

    @Test
    public void getNumberOfReportResults() throws NotFoundException, DuplicateException {
        Map<Report.Type, Boolean> typeHashMap = new HashMap<>();
        typeHashMap.put(Report.Type.BUG, true);
        typeHashMap.put(Report.Type.FEATURE, true);
        typeHashMap.put(Report.Type.HINT, true);
        Map<Report.Severity, Boolean> severityHashMap = new HashMap<>();
        severityHashMap.put(Report.Severity.MINOR, true);
        severityHashMap.put(Report.Severity.RELEVANT, true);
        severityHashMap.put(Report.Severity.SEVERE, true);
        topicGateway.createTopic(topic1);
        topicGateway.createTopic(topic2);
        reportGateway.create(report1);
        reportGateway.create(report2);
        assertEquals(1, searchGateway.getNumberOfReportResults(QUERY2, null, null, true, true, true, null, typeHashMap, severityHashMap));
    }

    @Test
    public void getNumberOfTopicResults() throws NotFoundException, DuplicateException {
        topicGateway.createTopic(topic1);
        topicGateway.createTopic(topic2);
        selection.setSortedBy("title");
        assertEquals(1, searchGateway.getNumberOfTopicResults(QUERY2));
    }

}
