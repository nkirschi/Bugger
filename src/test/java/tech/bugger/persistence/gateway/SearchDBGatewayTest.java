package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private final String query = "test";
    private final int limit = 5;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        searchGateway = new SearchDBGateway(connection);
        userGateway = new UserDBGateway(connection);
        topicGateway = new TopicDBGateway(connection);

        user1 = new User(null, "testuser1", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User", new Lazy<>(new byte[]{1, 2, 3, 4}), new byte[]{1}, "# I am a test user.",
                Language.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        user2 = new User(null, "testuser2", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.com", "Test", "User", new Lazy<>(new byte[]{1, 2, 3, 4}), new byte[]{1}, "# I am a test user.",
                Language.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        admin = new User(null, "testadmin", "v3ry_s3cur3", "salt", "algorithm", "admin@admin.de", "Helgo", "Brötchen", new Lazy<>(new byte[]{1, 2, 3, 4}),
                new byte[]{1}, "Ich bin der Administrator hier!", Language.ENGLISH, User.ProfileVisibility.MINIMAL,
                ZonedDateTime.now(), null, true);
        topic = new Topic(null, "title", "description");
    }

    @Test
    public void testGetUserBanSuggestions() throws NotFoundException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        List<String> suggestions = searchGateway.getUserBanSuggestions(query, limit, topic);
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
        List<String> suggestions = searchGateway.getUserBanSuggestions(query, limit, topic);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    public void testGetUserBanSuggestionsNone() throws NotFoundException {
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        List<String> suggestions = searchGateway.getUserBanSuggestions(query, limit, topic);
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testGetUserBanSuggestionsQueryNull() {
        assertThrows(IllegalArgumentException.class,
                () -> searchGateway.getUserBanSuggestions(null, limit, topic)
        );
    }

    @Test
    public void testGetUserBanSuggestionsQueryBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> searchGateway.getUserBanSuggestions("", limit, topic)
        );
    }

    @Test
    public void testGetUserBanSuggestionsTopicIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> searchGateway.getUserBanSuggestions(query, limit, topic)
        );
    }

    @Test
    public void testGetUserBanSuggestionsLimitNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> searchGateway.getUserBanSuggestions(query, -1, topic)
        );
    }

    @Test
    public void testGetUserBanSuggestionsSQLException() throws SQLException {
        topic.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SearchDBGateway(connectionSpy).getUserBanSuggestions(query, limit, topic)
        );
    }

    @Test
    public void testGetUserUnbanSuggestions() throws NotFoundException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        topicGateway.banUser(topic, user1);
        List<String> suggestions = searchGateway.getUserUnbanSuggestions(query, limit, topic);
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
        List<String> suggestions = searchGateway.getUserUnbanSuggestions(query, limit, topic);
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testGetUserUnbanSuggestionsSQLException() throws SQLException {
        topic.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SearchDBGateway(connectionSpy).getUserUnbanSuggestions(query, limit, topic)
        );
    }

    @Test
    public void testGetUserModSuggestions() throws NotFoundException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        topicGateway.promoteModerator(topic, user2);
        List<String> suggestions = searchGateway.getUserModSuggestions(query, limit, topic);
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
        List<String> suggestions = searchGateway.getUserModSuggestions(query, limit, topic);
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testGetUserModSuggestionsSQLException() throws SQLException {
        topic.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SearchDBGateway(connectionSpy).getUserModSuggestions(query, limit, topic)
        );
    }

    @Test
    public void testGetUserUnmodSuggestions() throws NotFoundException {
        userGateway.createUser(user1);
        userGateway.createUser(user2);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        topicGateway.promoteModerator(topic, user1);
        List<String> suggestions = searchGateway.getUserUnmodSuggestions(query, limit, topic);
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
        List<String> suggestions = searchGateway.getUserUnmodSuggestions(query, limit, topic);
        assertEquals(0, suggestions.size());
    }

    @Test
    public void testGetUserUnmodSuggestionsSQLException() throws SQLException {
        topic.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new SearchDBGateway(connectionSpy).getUserUnmodSuggestions(query, limit, topic)
        );
    }

}
