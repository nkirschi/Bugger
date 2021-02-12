package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.DuplicateException;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
public class TopicDBGatewayTest {

    private TopicDBGateway topicGateway;
    private UserDBGateway userGateway;
    private SubscriptionGateway subscriptionGateway;
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
        subscriptionGateway = new SubscriptionDBGateway(connection);
        user = new User(null, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test",
                "User",
                new byte[]{1, 2, 3, 4}, new byte[]{1}, "# I am a test user.",
                Locale.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        topic1 = new Topic(null, "topic1", "description");
        topic2 = new Topic(null, "topic2", "description");
        selection = new Selection(2, 0, Selection.PageSize.SMALL, "id", true);
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
                    "        (CONCAT('testtopic', CURRVAL('topic_id_seq')), CONCAT('Description for "
                    + "testtopic', CURRVAL('topic_id_seq')));\n" +
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
    public void testDetermineLastActivityDatabaseError() throws SQLException {
        topic1.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new TopicDBGateway(connectionSpy).determineLastActivity(topic1));
    }

    @Test
    public void testGetNumberOfTopicsWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new TopicDBGateway(connectionSpy).countTopics());
    }

    @Test
    public void testPromoteModerator() throws NotFoundException, DuplicateException {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        topicGateway.promoteModerator(topic1, user);
        assertTrue(userGateway.isModerator(user, topic1));
    }

    @Test
    public void testPromoteModeratorNoUser() throws DuplicateException {
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
    public void testDemoteModerator() throws NotFoundException, DuplicateException {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        topicGateway.promoteModerator(topic1, user);
        topicGateway.demoteModerator(topic1, user);
        assertFalse(userGateway.isModerator(user, topic1));
    }

    @Test
    public void testDemoteModeratorNotFound() throws DuplicateException {
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
    public void testDemoteModeratorUserIdNull() throws DuplicateException {
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
    public void testCountModerators() throws NotFoundException, DuplicateException {
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
    public void testCountModeratorsNotFound() throws DuplicateException {
        topicGateway.createTopic(topic1);
        assertEquals(0, topicGateway.countModerators(topic1));
    }

    @Test
    public void testCountModeratorsSQLException() throws SQLException, DuplicateException {
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
    public void testGetModeratedTopics() throws NotFoundException, DuplicateException {
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
    public void testBanUser() throws NotFoundException, DuplicateException {
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
    public void testBanUserForeignKeyViolationUser() throws DuplicateException {
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
    public void testBanUserSQLException() throws SQLException, DuplicateException {
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
    public void testUnbanUser() throws NotFoundException, DuplicateException {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        topicGateway.banUser(topic1, user);
        topicGateway.unbanUser(topic1, user);
        assertFalse(userGateway.isBanned(user, topic1));
    }

    @Test
    public void testUnbanUserNotFound() throws DuplicateException {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        assertThrows(NotFoundException.class,
                () -> topicGateway.unbanUser(topic1, user)
        );
    }

    @Test
    public void testUnbanUserUserIdNull() throws DuplicateException {
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
    public void testUnbanUserSQLException() throws SQLException, DuplicateException {
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).unbanUser(topic1, user)
        );
    }

    @Test
    public void testCountBannedUsers() throws NotFoundException, DuplicateException {
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
    public void testCountBannedUsersSQLException() throws SQLException, DuplicateException {
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

    @Test
    public void testCreateTopic() throws NotFoundException, DuplicateException {
        topicGateway.createTopic(topic1);
        Topic topic = topicGateway.findTopic(topic1.getId());
        assertAll(
                () -> assertEquals(topic1.getId(), topic.getId()),
                () -> assertEquals(topic1.getTitle(), topic.getTitle()),
                () -> assertEquals(topic1.getDescription(), topic.getDescription())
        );
    }

    @Test
    public void testCreateTopicNoKeysGenerated() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any(), anyInt());
        doReturn(rs).when(stmt).getGeneratedKeys();
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).createTopic(topic1)
        );
    }

    @Test
    public void testCreateTopicStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        SQLException mockException = mock(SQLException.class);
        doThrow(mockException).when(connectionSpy).prepareStatement(any(), anyInt());
        when(mockException.getSQLState()).thenReturn("");
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).createTopic(topic1)
        );
    }

    @Test
    public void testCountSubscribers() throws NotFoundException, DuplicateException {
        User admin = userGateway.getUserByID(1);
        userGateway.createUser(user);
        topicGateway.createTopic(topic1);
        subscriptionGateway.subscribe(topic1, admin);
        subscriptionGateway.subscribe(topic1, user);
        assertEquals(2, topicGateway.countSubscribers(topic1));
    }

    @Test
    public void testCountSubscribersNone() throws DuplicateException {
        topicGateway.createTopic(topic1);
        assertEquals(0, topicGateway.countSubscribers(topic1));
    }

    @Test
    public void testCountSubscribersNoResultSet() throws SQLException {
        topic1.setId(1);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        assertEquals(0, new TopicDBGateway(connectionSpy).countSubscribers(topic1));
    }

    @Test
    public void testCountSubscribersStoreException() throws SQLException {
        topic1.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).countSubscribers(topic1)
        );
    }

    @Test
    public void testCountSubscribersTopicNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.countSubscribers(null)
        );
    }

    @Test
    public void testCountSubscribersTopicIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.countSubscribers(topic1)
        );
    }

    @Test
    public void testDiscoverTopics() throws DuplicateException {
        topicGateway.createTopic(topic1);
        topicGateway.createTopic(topic2);
        List<String> topicTitles = topicGateway.discoverTopics().stream().map(Topic::getTitle)
                .collect(Collectors.toList());
        assertAll(
                () -> assertEquals(2, topicTitles.size()),
                () -> assertTrue(topicTitles.contains(topic1.getTitle())),
                () -> assertTrue(topicTitles.contains(topic2.getTitle()))
        );
    }

    @Test
    public void testDiscoverTopicsNoTopics() {
        assertTrue(topicGateway.discoverTopics().isEmpty());
    }

    @Test
    public void testDiscoverTopicsNoResults() throws SQLException {
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        assertTrue(new TopicDBGateway(connectionSpy).discoverTopics().isEmpty());
    }

    @Test
    public void testDiscoverTopicsStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).discoverTopics()
        );
    }

    @Test
    public void testSelectSubscribedTopics() throws NotFoundException, DuplicateException {
        topicGateway.createTopic(topic1);
        topicGateway.createTopic(topic2);
        userGateway.createUser(user);
        subscriptionGateway.subscribe(topic1, user);
        List<Topic> subscribedTopics = topicGateway.selectSubscribedTopics(user, selection);
        assertAll(
                () -> assertEquals(1, subscribedTopics.size()),
                () -> assertTrue(subscribedTopics.contains(topic1)),
                () -> assertFalse(subscribedTopics.contains(topic2))
        );
    }

    @Test
    public void testSelectSubscribedTopicsSelectionDesc() throws NotFoundException, DuplicateException {
        selection.setAscending(false);
        topicGateway.createTopic(topic1);
        topicGateway.createTopic(topic2);
        userGateway.createUser(user);
        subscriptionGateway.subscribe(topic1, user);
        subscriptionGateway.subscribe(topic2, user);
        List<Topic> subscribedTopics = topicGateway.selectSubscribedTopics(user, selection);
        assertAll(
                () -> assertEquals(2, subscribedTopics.size()),
                () -> assertTrue(subscribedTopics.contains(topic1)),
                () -> assertTrue(subscribedTopics.contains(topic2))
        );
    }

    @Test
    public void testSelectSubscribedTopicsNoSubscriptions() throws DuplicateException {
        topicGateway.createTopic(topic1);
        userGateway.createUser(user);
        assertTrue(topicGateway.selectSubscribedTopics(user, selection).isEmpty());
    }

    @Test
    public void testSelectSubscribedTopicsStoreException() throws SQLException {
        user.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).selectSubscribedTopics(user, selection)
        );
    }

    @Test
    public void testSelectSubscribedTopicsSelectionNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.selectSubscribedTopics(user, null)
        );
    }

    @Test
    public void testSelectSubscribedTopicsSelectionSortedByBlank() {
        selection.setSortedBy("");
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.selectSubscribedTopics(user, selection)
        );
    }

    @Test
    public void testSelectSubscribedTopicsUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.selectSubscribedTopics(null, selection)
        );
    }

    @Test
    public void testSelectSubscribedTopicsUserIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.selectSubscribedTopics(user, selection)
        );
    }

    @Test
    public void testCountSubscribedTopics() throws NotFoundException, DuplicateException {
        topicGateway.createTopic(topic1);
        topicGateway.createTopic(topic2);
        userGateway.createUser(user);
        subscriptionGateway.subscribe(topic1, user);
        subscriptionGateway.subscribe(topic2, user);
        assertEquals(2, topicGateway.countSubscribedTopics(user));
    }

    @Test
    public void testCountSubscribedTopicsZero() throws DuplicateException {
        topicGateway.createTopic(topic1);
        topicGateway.createTopic(topic2);
        userGateway.createUser(user);
        assertEquals(0, topicGateway.countSubscribedTopics(user));
    }

    @Test
    public void testCountSubscribedTopicsNoResult() throws SQLException {
        user.setId(1);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        assertThrows(InternalError.class,
                () -> new TopicDBGateway(connectionSpy).countSubscribedTopics(user)
        );
    }

    @Test
    public void testCountSubscribedTopicsStoreException() throws SQLException {
        user.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).countSubscribedTopics(user)
        );
    }

    @Test
    public void testCountSubscribedTopicsUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.countSubscribedTopics(null)
        );
    }

    @Test
    public void testCountSubscribedTopicsUserIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> topicGateway.countSubscribedTopics(user)
        );
    }

    @Test
    public void testDeleteTopic() throws DuplicateException {
        topicGateway.createTopic(topic1);
        topicGateway.deleteTopic(topic1);
        assertThrows(NotFoundException.class,
                () -> topicGateway.findTopic(topic1.getId())
        );
    }

    @Test
    public void testDeleteTopicStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).deleteTopic(topic1)
        );
    }

    @Test
    public void testUpdateTopic() throws NotFoundException, DuplicateException {
        String newTitle = "new title";
        String newDescription = "new description";
        topicGateway.createTopic(topic1);
        topic1.setTitle(newTitle);
        topic1.setDescription(newDescription);
        topicGateway.updateTopic(topic1);
        Topic topic = topicGateway.findTopic(topic1.getId());
        assertAll(
                () -> assertEquals(topic1.getTitle(), topic.getTitle()),
                () -> assertEquals(topic1.getDescription(), topic.getDescription())
        );
    }

    @Test
    public void testUpdateTopicNotFound() {
        topic1.setId(1);
        assertThrows(NotFoundException.class,
                () -> topicGateway.updateTopic(topic1)
        );
    }

    @Test
    public void testUpdateTopicStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).updateTopic(topic1)
        );
    }

    @Test
    public void testFindTopic() throws NotFoundException, DuplicateException {
        topicGateway.createTopic(topic1);
        Topic topic = topicGateway.findTopic(topic1.getId());
        assertAll(
                () -> assertEquals(topic1.getId(), topic.getId()),
                () -> assertEquals(topic1.getTitle(), topic.getTitle()),
                () -> assertEquals(topic1.getDescription(), topic.getDescription())
        );
    }

    @Test
    public void testFindTopicNotFound() {
        topic1.setId(1);
        assertThrows(NotFoundException.class,
                () -> topicGateway.findTopic(topic1.getId())
        );
    }

    @Test
    public void testFindTopicStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).findTopic(1)
        );
    }

    @Test
    public void testCountReports() throws NotFoundException {
        DBExtension.insertMinimalTestData();
        Topic topic = topicGateway.findTopic(1);
        assertEquals(3, topicGateway.countReports(topic, true, true));
    }

    @Test
    public void testCountReportsOnlyOpenReports() throws NotFoundException {
        DBExtension.insertMinimalTestData();
        Topic topic = topicGateway.findTopic(1);
        assertEquals(1, topicGateway.countReports(topic, true, false));
    }

    @Test
    public void testCountReportsOnlyClosedReports() throws NotFoundException {
        DBExtension.insertMinimalTestData();
        Topic topic = topicGateway.findTopic(1);
        assertEquals(2, topicGateway.countReports(topic, false, true));
    }

    @Test
    public void testCountReportsNotOpenOrClosed() throws NotFoundException {
        DBExtension.insertMinimalTestData();
        Topic topic = topicGateway.findTopic(1);
        assertEquals(0, topicGateway.countReports(topic, false, false));
    }

    @Test
    public void testCountReportsNoResult() throws SQLException, NotFoundException {
        DBExtension.insertMinimalTestData();
        Topic topic = topicGateway.findTopic(1);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        Connection connectionSpy = spy(connection);
        doReturn(stmt).when(connectionSpy).prepareStatement(any());
        doReturn(rs).when(stmt).executeQuery();
        assertEquals(0, new TopicDBGateway(connectionSpy).countReports(topic, true, true));
    }

    @Test
    public void testCountReportsStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new TopicDBGateway(connectionSpy).countReports(topic1, true, true)
        );
    }

}