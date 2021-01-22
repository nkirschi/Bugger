package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@ExtendWith(DBExtension.class)
@ExtendWith(LogExtension.class)
public class SearchDBGatewayTest {

    private SearchGateway searchGateway;
    private UserGateway userGateway;
    private TopicGateway topicGateway;

    private Connection connection;

    private User user1;
    private User user2;
    private User admin;
    private Topic topic;
    private static final String QUERY = "test";
    private static final String QUERY2 = "2";
    private static final int LIMIT = 5;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        userGateway = new UserDBGateway(connection);
        searchGateway = new SearchDBGateway(connection, userGateway);
        topicGateway = new TopicDBGateway(connection);

        user1 = new User(null, "testuser1", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test"
                , "User",
                         new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                         Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        user2 = new User(null, "testuser2", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.com",
                         "Test", "User",
                         new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                         Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        admin = new User(null, "testadmin", "v3ry_s3cur3", "salt", "algorithm", "admin@admin.de", "Helgo", "Brötchen",
                         new byte[]{1, 2, 3, 4}, new byte[]{1}, "Ich bin der Administrator hier!", Locale.ENGLISH,
                         User.ProfileVisibility.MINIMAL, OffsetDateTime.now(), null, true);
        topic = new Topic(null, "title", "description");
    }

    @Test
    public void testGetUserBanSuggestions() throws NotFoundException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        List<String> suggestions = searchGateway.getUserBanSuggestions(QUERY, LIMIT, topic);
        assertAll(
                () -> assertTrue(suggestions.contains(user1.getUsername())),
                () -> assertTrue(suggestions.contains(user2.getUsername())),
                () -> assertFalse(suggestions.contains(admin.getUsername()))
        );
    }

    @Test
    public void testGetUserBanSuggestionsWithModeratorsAndBanned() throws NotFoundException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        topicGateway.promoteModerator(topic, user1);
        topicGateway.banUser(topic, user2);
        List<String> suggestions = searchGateway.getUserBanSuggestions(QUERY, LIMIT, topic);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    public void testGetUserBanSuggestionsNone() throws NotFoundException {
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        List<String> suggestions = searchGateway.getUserBanSuggestions(QUERY, LIMIT, topic);
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testGetUserBanSuggestionsQueryNull() {
        assertThrows(IllegalArgumentException.class,
                     () -> searchGateway.getUserBanSuggestions(null, LIMIT, topic)
        );
    }

    @Test
    public void testGetUserBanSuggestionsQueryBlank() {
        assertThrows(IllegalArgumentException.class,
                     () -> searchGateway.getUserBanSuggestions("", LIMIT, topic)
        );
    }

    @Test
    public void testGetUserBanSuggestionsTopicIdNull() {
        assertThrows(IllegalArgumentException.class,
                     () -> searchGateway.getUserBanSuggestions(QUERY, LIMIT, topic)
        );
    }

    @Test
    public void testGetUserBanSuggestionsLimitNegative() {
        assertThrows(IllegalArgumentException.class,
                     () -> searchGateway.getUserBanSuggestions(QUERY, -1, topic)
        );
    }

    @Test
    public void testGetUserBanSuggestionsSQLException() throws SQLException {
        topic.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                     () -> new SearchDBGateway(connectionSpy, userGateway).getUserBanSuggestions(QUERY, LIMIT, topic)
        );
    }

    @Test
    public void testGetUserUnbanSuggestions() throws NotFoundException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        topicGateway.banUser(topic, user1);
        List<String> suggestions = searchGateway.getUserUnbanSuggestions(QUERY, LIMIT, topic);
        assertAll(
                () -> assertTrue(suggestions.contains(user1.getUsername())),
                () -> assertFalse(suggestions.contains(user2.getUsername())),
                () -> assertFalse(suggestions.contains(admin.getUsername()))
        );
    }

    @Test
    public void testGetUserUnbanSuggestionsNone() throws NotFoundException {
        userGateway.createUser(user1);
        topicGateway.createTopic(topic);
        List<String> suggestions = searchGateway.getUserUnbanSuggestions(QUERY, LIMIT, topic);
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testGetUserUnbanSuggestionsSQLException() throws SQLException {
        topic.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                     () -> new SearchDBGateway(connectionSpy, userGateway).getUserUnbanSuggestions(QUERY, LIMIT, topic)
        );
    }

    @Test
    public void testGetUserModSuggestions() throws NotFoundException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        topicGateway.promoteModerator(topic, user2);
        List<String> suggestions = searchGateway.getUserModSuggestions(QUERY, LIMIT, topic);
        assertAll(
                () -> assertTrue(suggestions.contains(user1.getUsername())),
                () -> assertFalse(suggestions.contains(user2.getUsername())),
                () -> assertFalse(suggestions.contains(admin.getUsername()))
        );
    }

    @Test
    public void testGetUserModSuggestionsNone() throws NotFoundException {
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        List<String> suggestions = searchGateway.getUserModSuggestions(QUERY, LIMIT, topic);
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testGetUserModSuggestionsSQLException() throws SQLException {
        topic.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                     () -> new SearchDBGateway(connectionSpy, userGateway).getUserModSuggestions(QUERY, LIMIT, topic)
        );
    }

    @Test
    public void testGetUserUnmodSuggestions() throws NotFoundException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        topicGateway.promoteModerator(topic, user1);
        List<String> suggestions = searchGateway.getUserUnmodSuggestions(QUERY, LIMIT, topic);
        assertAll(
                () -> assertTrue(suggestions.contains(user1.getUsername())),
                () -> assertFalse(suggestions.contains(user2.getUsername())),
                () -> assertFalse(suggestions.contains(admin.getUsername()))
        );
    }

    @Test
    public void testGetUserUnmodSuggestionsNone() throws NotFoundException {
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        List<String> suggestions = searchGateway.getUserUnmodSuggestions(QUERY, LIMIT, topic);
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testGetUserUnmodSuggestionsSQLException() throws SQLException {
        topic.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                     () -> new SearchDBGateway(connectionSpy, userGateway).getUserUnmodSuggestions(QUERY, LIMIT, topic)
        );
    }

    @Test
    public void testGetUserSuggestions() throws NotFoundException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        List<String> suggestions = searchGateway.getUserSuggestions(QUERY2, LIMIT);
        assertAll(
                () -> assertFalse(suggestions.contains(user1.getUsername())),
                () -> assertTrue(suggestions.contains(user2.getUsername()))
        );
    }

}
