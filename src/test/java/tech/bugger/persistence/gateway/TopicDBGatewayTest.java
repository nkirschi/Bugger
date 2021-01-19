package tech.bugger.persistence.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
class TopicDBGatewayTest {

    private TopicDBGateway topicGateway;
    private UserDBGateway userGateway;
    private Connection connection;
    private int numberOfTopics;
    private Selection selection;
    private List<Topic> topics;
    private User user;
    private Topic topic1;
    private Topic topic2;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        topicGateway = new TopicDBGateway(connection);
        userGateway = new UserDBGateway(connection);
        user = new User(null, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        topic1 = new Topic(null, "topic1", "description");
        topic2 = new Topic(null, "topic2", "description");
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    private void validSelection() {
        selection = new Selection(3, 0, Selection.PageSize.NORMAL, "id", true);
    }

    private void addTopics() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DO\n" +
                    "$$\n" +
                    "BEGIN\n" +
                    "FOR i IN 1.." + numberOfTopics + " LOOP\n" +
                    "    INSERT INTO topic (title, description) VALUES\n" +
                    "        (CONCAT('testtopic', CURRVAL('topic_id_seq')), CONCAT('Description for testtopic', CURRVAL('topic_id_seq')));\n" +
                    "END LOOP;\n" +
                    "END;\n" +
                    "$$\n" +
                    ";\n");
        }
    }

    private void expectedTopics(int number) {
        topics = new ArrayList<>(number);
        for (int i = 1; i <= number; i++) {
            Topic topic = new Topic(i, "testtopic" + i, "Description for testtopic" + i);
            topics.add(topic);
        }
    }

    private Topic makeTestTopic(int id) {
        return new Topic(id, "testtopic" + id, "Description for testtopic" + id);
    }

    @Test
    public void testCountTopicsWhenThereAreSome() throws Exception {
        numberOfTopics = 5;
        addTopics();
        assertEquals(numberOfTopics, topicGateway.countTopics());
    }

    @Test
    public void testCountTopicsWhenThereAreNone() {
        assertEquals(0, topicGateway.countTopics());
    }

    @Test
    public void testCountTopicsWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new TopicDBGateway(connectionSpy).countTopics());
    }

    @Test
    public void testCountTopicsWhenSNAFU() throws Exception {
        ResultSet resultSetMock = mock(ResultSet.class);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(false).when(resultSetMock).next();
        doReturn(resultSetMock).when(stmtMock).executeQuery();
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any());
        assertThrows(InternalError.class, () -> new TopicDBGateway(connectionSpy).countTopics());
    }

    @Test
    public void testSelectTopicsWhenSelectionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> topicGateway.selectTopics(null));
    }

    @Test
    public void testSelectTopicsWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        validSelection();
        assertThrows(StoreException.class, () -> new TopicDBGateway(connectionSpy).selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenSelectionIsBlank() {
        validSelection();
        selection.setSortedBy("");
        assertThrows(IllegalArgumentException.class, () -> topicGateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenThereAreSome() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        expectedTopics(5);
        assertEquals(topics, topicGateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenSortedByIDDescending() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setAscending(false);
        selection.setSortedBy("id");
        expectedTopics(5);
        Collections.reverse(topics);
        assertEquals(topics, topicGateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenThereAreNone() {
        validSelection();
        assertTrue(topicGateway.selectTopics(selection).isEmpty());
    }

    @Test
    public void testSelectTopicsWhenCurrentPageIsInvalid() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setCurrentPage(-1);
        assertThrows(StoreException.class, () -> topicGateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenSortedByIsInvalid() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setSortedBy("hüttenkäse");
        assertThrows(StoreException.class, () -> topicGateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenTotalSizeIsTooBig() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setTotalSize(300);
        expectedTopics(5);
        assertEquals(topics, topicGateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenTotalSizeIsNegative() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setTotalSize(-11);
        expectedTopics(5);
        assertEquals(topics, topicGateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenTotalSizeIsTooSmall() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setTotalSize(3);
        expectedTopics(5);
        assertEquals(topics, topicGateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenThereAreTooMany() throws Exception {
        numberOfTopics = 50;
        addTopics();
        validSelection();
        expectedTopics(20);
        assertEquals(topics, topicGateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenThereAreTooManySortedByTitleDescending() throws Exception {
        numberOfTopics = 50;
        addTopics();
        validSelection();
        selection.setSortedBy("title");
        selection.setAscending(false);
        topics = new ArrayList<>(20);
        for (int i = 9; i >= 6; i--) {
            topics.add(makeTestTopic(i));
        }
        topics.add(makeTestTopic(50));
        topics.add(makeTestTopic(5));
        for (int i = 49; i >= 40; i--) {
            topics.add(makeTestTopic(i));
        }
        topics.add(makeTestTopic(4));
        for (int i = 39; i >= 37; i--) {
            topics.add(makeTestTopic(i));
        }
        assertEquals(topics, topicGateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenSortedByLastActivityWithNoActivity() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setSortedBy("last_activity");
        expectedTopics(5);
        assertEquals(topics, topicGateway.selectTopics(selection));
    }

    @Test
    public void testSelectTopicsWhenSortedByLastActivityWithSomeActivity() throws Exception {
        numberOfTopics = 5;
        addTopics();
        validSelection();
        selection.setSortedBy("last_activity");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO report (title, type, severity, topic) VALUES ('Hello', 'BUG', 'MINOR', 5)");
        }
        expectedTopics(4);
        topics.add(0, makeTestTopic(5));
        assertEquals(topics, topicGateway.selectTopics(selection));
    }

    @Test
    public void testDetermineLastActivityWhenTopicIsNull() {
        assertThrows(IllegalArgumentException.class, () -> topicGateway.determineLastActivity(null));
    }

    @Test
    public void testDetermineLastActivityWhenTopicIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> topicGateway.determineLastActivity(new Topic()));
    }

    @Test
    public void testDetermineLastActivityWhenNoActivity() throws Exception {
        numberOfTopics = 1;
        addTopics();
        Topic topic = makeTestTopic(1);
        assertNull(topicGateway.determineLastActivity(topic));
    }

    @Test
    public void testDetermineLastActivityWhenThereWasSomeActivity() throws Exception {
        numberOfTopics = 1;
        addTopics();
        Topic topic = makeTestTopic(1);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO report (title, type, severity, topic) VALUES ('Hello', 'BUG', 'MINOR', 1)");
        }
        assertNotNull(topicGateway.determineLastActivity(topic));
    }

    @Test
    public void testDetermineLastActivityWhenTopicNotFound() {
        Topic topic = makeTestTopic(42);
        assertThrows(NotFoundException.class, () -> topicGateway.determineLastActivity(topic));
    }

    @Test
    public void testGetNumberOfTopicsWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new TopicDBGateway(connectionSpy).countTopics());
    }

    @Test
    public void testPromoteModerator() throws NotFoundException {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        topicGateway.promoteModerator(topic1, user);
        assertTrue(userGateway.isModerator(user, topic1));
    }

    @Test
    public void testPromoteModeratorNoUser() {
        topicGateway.createTopic(topic1);
        user.setId(10);
        assertThrows(NotFoundException.class,
                () -> topicGateway.promoteModerator(topic1, user)
        );
    }

    @Test
    public void testPromoteModeratorSQLException() throws SQLException {
        topic1.setId(2);
        user.setId(3);
        Connection connectionSpy = spy(connection);
        SQLException mockException = mock(SQLException.class);
        doThrow(mockException).when(connectionSpy).prepareStatement(any());
        when(mockException.getSQLState()).thenReturn("");
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).promoteModerator(topic1, user)
        );
    }

    @Test
    public void testPromoteModeratorNoTopic() {
        userGateway.createUser(user);
        topic1.setId(1);
        assertThrows(NotFoundException.class,
                () -> topicGateway.promoteModerator(topic1, user)
        );
    }

    @Test
    public void testPromoteModeratorUserIdNull() {
        topic1.setId(1);
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.promoteModerator(topic1, user)
        );
    }

    @Test
    public void testPromoteModeratorTopicIdNull() {
        user.setId(1);
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.promoteModerator(topic1, user)
        );
    }

    @Test
    public void testDemoteModerator() throws NotFoundException {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        topicGateway.promoteModerator(topic1, user);
        topicGateway.demoteModerator(topic1, user);
        assertFalse(userGateway.isModerator(user, topic1));
    }

    @Test
    public void testDemoteModeratorNotFound() {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        assertThrows(NotFoundException.class,
                () -> topicGateway.demoteModerator(topic1, user)
        );
    }

    @Test
    public void testDemoteModeratorTopicIdNull() {
        userGateway.createUser(user);
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.demoteModerator(topic1, user)
        );
    }

    @Test
    public void testDemoteModeratorUserIdNull() {
        topicGateway.createTopic(topic1);
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.demoteModerator(topic1, user)
        );
    }

    @Test
    public void testDemoteModeratorSQLException() throws SQLException {
        topic1.setId(10);
        user.setId(10);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).demoteModerator(topic1, user)
        );
    }

    @Test
    public void testCountModerators() throws NotFoundException {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        topicGateway.promoteModerator(topic1, user);
        user.setUsername("testUser2");
        user.setEmailAddress("user@user.com");
        userGateway.createUser(user);
        topicGateway.promoteModerator(topic1, user);
        assertEquals(2, topicGateway.countModerators(topic1));
    }

    @Test
    public void testCountModeratorsNotFound() {
        topicGateway.createTopic(topic1);
        assertEquals(0, topicGateway.countModerators(topic1));
    }

    @Test
    public void testCountModeratorsSQLException() throws SQLException {
        topicGateway.createTopic(topic1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).countModerators(topic1)
        );
    }

    @Test
    public void testCountModeratorsTopicIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.countModerators(topic1)
        );
    }

    @Test
    public void testCountModeratorsNoModerators() throws SQLException {
        topic1.setId(1);
        ResultSet resultSetMock = mock(ResultSet.class);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(false).when(resultSetMock).next();
        doReturn(resultSetMock).when(stmtMock).executeQuery();
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any());
        assertEquals(0, new TopicDBGateway(connectionSpy).countModerators(topic1));
    }

    @Test
    public void testGetModeratedTopics() throws NotFoundException {
        List<Topic> topics = new ArrayList<>();
        topics.add(topic1);
        topics.add(topic2);
        topicGateway.createTopic(topic1);
        topicGateway.createTopic(topic2);
        userGateway.createUser(user);
        topicGateway.promoteModerator(topic1, user);
        topicGateway.promoteModerator(topic2, user);
        Selection selection = new Selection(2, 0, Selection.PageSize.SMALL, "title", true);
        assertEquals(topics, topicGateway.getModeratedTopics(user, selection));
    }

    @Test
    public void testGetModeratedTopicsSelectionNull() {
        user.setId(1);
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.getModeratedTopics(user, null)
        );
    }

    @Test
    public void testGetModeratedTopicsUserIdNull() {
        Selection selection = new Selection(2, 0, Selection.PageSize.SMALL, "title", true);
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.getModeratedTopics(user, selection)
        );
    }

    @Test
    public void testGetModeratedTopicsSelectionBlank() {
        user.setId(1);
        Selection selection = new Selection(2, 0, Selection.PageSize.SMALL, "", true);
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.getModeratedTopics(user, selection)
        );
    }

    @Test
    public void testGetModeratedTopicsSQLException() throws SQLException {
        user.setId(1);
        Selection selection = new Selection(2, 0, Selection.PageSize.SMALL, "title", true);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).getModeratedTopics(user, selection)
        );
    }

    @Test
    public void testBanUser() throws NotFoundException {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        topicGateway.banUser(topic1, user);
        assertTrue(userGateway.isBanned(user, topic1));
    }

    @Test
    public void testBanUserForeignKeyViolationTopic() {
        topic1.setId(1);
        userGateway.createUser(user);
        assertThrows(NotFoundException.class,
                () -> topicGateway.banUser(topic1, user)
        );
    }

    @Test
    public void testBanUserForeignKeyViolationUser() {
        user.setId(5);
        topicGateway.createTopic(topic1);
        assertThrows(NotFoundException.class,
                () -> topicGateway.banUser(topic1, user)
        );
    }

    @Test
    public void testBanUserUserIdNull() {
        topic1.setId(1);
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.banUser(topic1, user)
        );
    }

    @Test
    public void testBanUserTopicIdNull() {
        user.setId(1);
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.banUser(topic1, user)
        );
    }

    @Test
    public void testBanUserSQLException() throws SQLException {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        Connection connectionSpy = spy(connection);
        SQLException mockException = mock(SQLException.class);
        doThrow(mockException).when(connectionSpy).prepareStatement(any());
        when(mockException.getSQLState()).thenReturn("");
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).banUser(topic1, user)
        );
    }

    @Test
    public void testUnbanUser() throws NotFoundException {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        topicGateway.banUser(topic1, user);
        topicGateway.unbanUser(topic1, user);
        assertFalse(userGateway.isBanned(user, topic1));
    }

    @Test
    public void testUnbanUserNotFound() {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        assertThrows(NotFoundException.class,
                () -> topicGateway.unbanUser(topic1, user)
        );
    }

    @Test
    public void testUnbanUserUserIdNull() {
        topicGateway.createTopic(topic1);
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.unbanUser(topic1, user)
        );
    }

    @Test
    public void testUnbanUserTopicIdNull() {
        userGateway.createUser(user);
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.unbanUser(topic1, user)
        );
    }

    @Test
    public void testUnbanUserSQLException() throws SQLException {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).unbanUser(topic1, user)
        );
    }

    @Test
    public void testCountBannedUsers() throws NotFoundException {
        topicGateway.createTopic(topic1);
        userGateway.createUser(user);
        topicGateway.banUser(topic1, user);
        user.setUsername("Helgi");
        user.setEmailAddress("helgi@test.de");
        userGateway.createUser(user);
        topicGateway.banUser(topic1, user);
        assertEquals(2, topicGateway.countBannedUsers(topic1));
    }

    @Test
    public void testCountBannedUsersTopicIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.countBannedUsers(topic1)
        );
    }

    @Test
    public void testCountBannedUsersSQLException() throws SQLException {
        topicGateway.createTopic(topic1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).countBannedUsers(topic1)
        );
    }

    @Test
    public void testCountBannedUsersResultSetNull() throws SQLException {
        topic1.setId(1);
        ResultSet resultSetMock = mock(ResultSet.class);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(false).when(resultSetMock).next();
        doReturn(resultSetMock).when(stmtMock).executeQuery();
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any());
        assertEquals(0, new TopicDBGateway(connectionSpy).countBannedUsers(topic1));
    }

}