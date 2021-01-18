package tech.bugger.persistence.gateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Authorship;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Selection;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.DuplicateException;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.SelfReferenceException;
import tech.bugger.persistence.exception.StoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

@ExtendWith(DBExtension.class)
@ExtendWith(LogExtension.class)
public class UserDBGatewayTest {

    private UserGateway userGateway;
    private TopicGateway topicGateway;
    private SubscriptionGateway subscriptionGateway;
    private ReportGateway reportGateway;

    private Connection connection;

    private User user;
    private User admin;
    private Topic topic;
    private Report report;
    private Selection selection;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        userGateway = new UserDBGateway(connection);
        topicGateway = new TopicDBGateway(connection);
        subscriptionGateway = new SubscriptionDBGateway(connection);
        reportGateway = new ReportDBGateway(connection, userGateway);

        user = new User(2, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test",
                        "User", new Lazy<>(new byte[]{1, 2, 3, 4}), new byte[]{1}, "# I am a test user.",
                        Language.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        admin = new User(3, "Helgo", "v3ry_s3cur3", "salt", "algorithm", "helgo@admin.de", "Helgo", "Brötchen",
                         new Lazy<>(new byte[]{1, 2, 3, 4}),
                         new byte[]{1}, "Ich bin der Administrator hier!", Language.ENGLISH,
                         User.ProfileVisibility.MINIMAL,
                         ZonedDateTime.now(), null, true);
        topic = new Topic(null, "title", "description");
        report = new Report(null, "Some title", Report.Type.BUG, Report.Severity.RELEVANT, "", mock(Authorship.class),
                mock(ZonedDateTime.class), null, null, false, null);
        selection = new Selection(2, 0, Selection.PageSize.NORMAL, "id", true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    private void deleteAllUsers() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM \"user\";");
        }
    }

    @Test
    public void testCreateUserAndGetUser() throws Exception {
        userGateway.createUser(user);

        User copyFromDatabase = userGateway.getUserByID(user.getId());
        assertAll(() -> assertNotNull(user.getId()),
                  () -> assertEquals(user.getId(), copyFromDatabase.getId()),
                  () -> assertEquals(user.getUsername(), copyFromDatabase.getUsername()),
                  () -> assertEquals(user.getPasswordHash(), copyFromDatabase.getPasswordHash()),
                  () -> assertEquals(user.getPasswordSalt(), copyFromDatabase.getPasswordSalt()),
                  () -> assertEquals(user.getHashingAlgorithm(), copyFromDatabase.getHashingAlgorithm()),
                  () -> assertEquals(user.getEmailAddress(), copyFromDatabase.getEmailAddress()),
                  () -> assertEquals(user.getFirstName(), copyFromDatabase.getFirstName()),
                  () -> assertEquals(user.getLastName(), copyFromDatabase.getLastName()),
                  () -> assertArrayEquals(user.getAvatarThumbnail(), copyFromDatabase.getAvatarThumbnail()),
                  () -> assertEquals(user.getBiography(), copyFromDatabase.getBiography()),
                  () -> assertEquals(user.getPreferredLanguage(), copyFromDatabase.getPreferredLanguage()),
                  () -> assertEquals(user.getProfileVisibility(), copyFromDatabase.getProfileVisibility()),
                  () -> assertEquals(user.getForcedVotingWeight(), copyFromDatabase.getForcedVotingWeight()),
                  () -> assertEquals(user.isAdministrator(), copyFromDatabase.isAdministrator()));
    }

    @Test
    public void testCreateUserWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any(), anyInt());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).createUser(new User(user)));
    }

    @Test
    public void testCreateUserAbsentAvatar() {
        user.setAvatar(new Lazy<>(() -> null));
        assertThrows(IllegalArgumentException.class, () -> userGateway.createUser(user));
    }

    @Test
    public void testCreateUserNotAdded() throws Exception {
        ResultSet resultSetMock = mock(ResultSet.class);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(false).when(resultSetMock).next();
        doReturn(resultSetMock).when(stmtMock).getGeneratedKeys();
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any(), anyInt());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).createUser(user));
        reset(connectionSpy, stmtMock);
    }

    @Test
    public void testGetUserByIdNotFound() {
        assertThrows(NotFoundException.class, () -> userGateway.getUserByID(42));
    }

    @Test
    public void testGetUserByIdWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).getUserByID(1));
    }

    @Test
    public void testUpdateUser() throws Exception {
        userGateway.createUser(user);
        user.setLastName("Heinrich");
        userGateway.updateUser(user);

        User copyFromDatabase = userGateway.getUserByID(user.getId());
        assertAll(() -> assertEquals(user.getId(), copyFromDatabase.getId()),
                  () -> assertEquals(user.getUsername(), copyFromDatabase.getUsername()),
                  () -> assertEquals(user.getPasswordHash(), copyFromDatabase.getPasswordHash()),
                  () -> assertEquals(user.getPasswordSalt(), copyFromDatabase.getPasswordSalt()),
                  () -> assertEquals(user.getHashingAlgorithm(), copyFromDatabase.getHashingAlgorithm()),
                  () -> assertEquals(user.getEmailAddress(), copyFromDatabase.getEmailAddress()),
                  () -> assertEquals(user.getFirstName(), copyFromDatabase.getFirstName()),
                  () -> assertEquals(user.getLastName(), copyFromDatabase.getLastName()),
                  () -> assertArrayEquals(user.getAvatarThumbnail(), copyFromDatabase.getAvatarThumbnail()),
                  () -> assertEquals(user.getBiography(), copyFromDatabase.getBiography()),
                  () -> assertEquals(user.getRegistrationDate(), copyFromDatabase.getRegistrationDate()),
                  () -> assertEquals(user.getPreferredLanguage(), copyFromDatabase.getPreferredLanguage()),
                  () -> assertEquals(user.getProfileVisibility(), copyFromDatabase.getProfileVisibility()),
                  () -> assertEquals(user.getForcedVotingWeight(), copyFromDatabase.getForcedVotingWeight()),
                  () -> assertEquals(user.isAdministrator(), copyFromDatabase.isAdministrator()));
    }

    @Test
    public void testUpdateUserWhenDatabaseError() throws Exception {
        user.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any(), anyInt());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).updateUser(user));
    }

    @Test
    public void testUpdateUserNotExists() {
        user.setId(42);
        assertThrows(NotFoundException.class, () -> userGateway.updateUser(user));
    }

    @Test
    public void testUpdateUserNullId() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class, () -> userGateway.updateUser(user));
    }

    @Test
    public void testUpdateUserAbsentAvatar() {
        user.setId(1);
        user.setAvatar(new Lazy<>(() -> null));
        assertThrows(IllegalArgumentException.class, () -> userGateway.updateUser(user));
    }

    @Test
    public void testGetUserByEmailNotFound() {
        assertThrows(NotFoundException.class, () -> userGateway.getUserByEmail("t@t.tk"));
    }

    @Test
    public void testGetUserByEmailFound() throws Exception {
        userGateway.createUser(user);
        assertEquals(user, userGateway.getUserByEmail("test@test.de"));
    }

    @Test
    public void testGetUserByEmailWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).getUserByEmail("t@t.tk"));
    }

    @Test
    public void testGetUserByUsernameNotFound() {
        assertThrows(NotFoundException.class, () -> userGateway.getUserByUsername("testuser"));
    }

    @Test
    public void testGetUserByUsernameFound() throws Exception {
        userGateway.createUser(user);
        assertEquals(user, userGateway.getUserByUsername("testuser"));
    }

    @Test
    public void testGetUserByUsernameWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).getUserByUsername("testuser"));
    }

    @Test
    public void testGetNumberOfAdmins() {
        userGateway.createUser(user);
        userGateway.createUser(admin);
        //One inserted admin plus default admin.
        assertEquals(2, userGateway.getNumberOfAdmins());
    }

    @Test
    public void testGetNumberOfAdminsNoAdmins() throws SQLException {
        deleteAllUsers();
        assertEquals(0, userGateway.getNumberOfAdmins());
    }

    @Test
    public void testGetNumberOfAdminsEmptyResultSet() throws SQLException {
        ResultSet resultSetMock = mock(ResultSet.class);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(false).when(resultSetMock).next();
        doReturn(resultSetMock).when(stmtMock).executeQuery();
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any());
        assertEquals(0, new UserDBGateway(connectionSpy).getNumberOfAdmins());
        reset(connectionSpy, stmtMock);
    }

    @Test
    public void testGetNumberOfAdminsDatabaseError() throws SQLException {
        Connection connSpy = spy(connection);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                     () -> new UserDBGateway(connSpy).getNumberOfAdmins()
        );
    }

    @Test
    public void testGetUserByID() throws NotFoundException {
        userGateway.createUser(user);
        User helga = userGateway.getUserByID(user.getId());
        assertEquals(user, helga);
    }

    @Test
    public void testGetUserByIDNotFound() {
        assertThrows(NotFoundException.class,
                     () -> userGateway.getUserByID(2222)
        );
    }

    @Test
    public void testGetUserByIDDatabaseError() throws SQLException {
        Connection connSpy = spy(connection);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                     () -> new UserDBGateway(connSpy).getUserByID(user.getId())
        );
    }

    @Test
    public void testGetNumberOfPosts() throws NotFoundException {
        userGateway.createUser(user);
        assertEquals(0, userGateway.getNumberOfPosts(user));
    }

    @Test
    public void testGetNumberOfPostNoEntries() {
        assertThrows(NotFoundException.class,
                     () -> userGateway.getNumberOfPosts(user)
        );
    }

    @Test
    public void testGetNumberOfPostsDatabaseError() throws SQLException {
        Connection connSpy = spy(connection);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                     () -> new UserDBGateway(connSpy).getNumberOfPosts(user)
        );
    }

    @Test
    public void testDeleteUser() throws NotFoundException {
        userGateway.createUser(user);
        userGateway.deleteUser(user);
        assertThrows(NotFoundException.class,
                     () -> userGateway.getUserByID(user.getId())
        );
    }

    @Test
    public void testDeleteUserNotFound() {
        assertThrows(NotFoundException.class,
                     () -> userGateway.deleteUser(user)
        );
    }

    @Test
    public void testDeleteUserDatabaseError() throws SQLException {
        Connection connSpy = spy(connection);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                     () -> new UserDBGateway(connSpy).deleteUser(user)
        );
    }

    @Test
    public void testIsModerator() throws NotFoundException {
        topicGateway.createTopic(topic);
        userGateway.createUser(user);
        topicGateway.promoteModerator(topic, user);
        assertTrue(userGateway.isModerator(user, topic));
    }

    @Test
    public void testIsModeratorTopicIdNull() {
        assertThrows(IllegalArgumentException.class,
                     () -> userGateway.isModerator(user, topic)
        );
    }

    @Test
    public void testIsModeratorUserIdNull() {
        user.setId(null);
        topic.setId(1);
        assertThrows(IllegalArgumentException.class,
                     () -> userGateway.isModerator(user, topic)
        );
    }

    @Test
    public void testIsModeratorSQLException() throws SQLException {
        user.setId(1);
        topic.setId(1);
        Connection connSpy = spy(connection);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                     () -> new UserDBGateway(connSpy).isModerator(user, topic)
        );
    }

    @Test
    public void testGetSelectedModerators() throws NotFoundException {
        topicGateway.createTopic(topic);
        userGateway.createUser(user);
        userGateway.createUser(admin);
        topicGateway.promoteModerator(topic, user);
        topicGateway.promoteModerator(topic, admin);
        List<User> users = userGateway.getSelectedModerators(topic, selection);
        assertAll(
                () -> assertTrue(users.contains(user)),
                () -> assertTrue(users.contains(admin))
        );
    }

    @Test
    public void testGetSelectedModeratorsNoEntries() throws NotFoundException {
        topicGateway.createTopic(topic);
        assertThrows(NotFoundException.class,
                     () -> userGateway.getSelectedModerators(topic, selection)
        );
    }

    @Test
    public void testGetSelectedModeratorsTopicIdNull() {
        assertThrows(IllegalArgumentException.class,
                     () -> userGateway.getSelectedModerators(topic, selection)
        );
    }

    @Test
    public void testGetSelectedModeratorsSelectionNull() {
        topic.setId(1);
        assertThrows(IllegalArgumentException.class,
                     () -> userGateway.getSelectedModerators(topic, null)
        );
    }

    @Test
    public void testGetSelectedModeratorsNotSorted() {
        selection.setSortedBy("");
        topic.setId(1);
        assertThrows(IllegalArgumentException.class,
                     () -> userGateway.getSelectedModerators(topic, selection)
        );
    }

    @Test
    public void testGetSelectedModeratorsSQLException() throws SQLException {
        topic.setId(1);
        Connection connSpy = spy(connection);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                     () -> new UserDBGateway(connSpy).getSelectedModerators(topic, selection)
        );
    }

    @Test
    public void testGetNumberOfModeratedTopics() throws NotFoundException {
        userGateway.createUser(user);
        topicGateway.createTopic(topic);
        topicGateway.promoteModerator(topic, user);
        assertEquals(1, userGateway.getNumberOfModeratedTopics(user));
    }

    @Test
    public void testGetNumberOfModeratedTopicsNone() {
        userGateway.createUser(user);
        assertEquals(0, userGateway.getNumberOfModeratedTopics(user));
    }

    @Test
    public void testGetNumberOfModeratorsUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                     () -> userGateway.getNumberOfModeratedTopics(user)
        );
    }

    @Test
    public void testGetNumberOfModeratorsSQLException() throws SQLException {
        Connection connSpy = spy(connection);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                     () -> new UserDBGateway(connSpy).getNumberOfModeratedTopics(user)
        );
    }

    @Test
    public void testGetNumberOfModeratorsNoResult() throws SQLException {
        ResultSet resultSetMock = mock(ResultSet.class);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(false).when(resultSetMock).next();
        doReturn(resultSetMock).when(stmtMock).executeQuery();
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any());
        assertEquals(0, new UserDBGateway(connectionSpy).getNumberOfModeratedTopics(user));
        reset(connectionSpy, stmtMock);
    }

    @Test
    public void testGetSelectedBannedUsers() throws NotFoundException {
        topicGateway.createTopic(topic);
        userGateway.createUser(user);
        userGateway.createUser(admin);
        topicGateway.banUser(topic, user);
        topicGateway.banUser(topic, admin);
        List<User> users = userGateway.getSelectedBannedUsers(topic, selection);
        assertAll(
                () -> assertTrue(users.contains(user)),
                () -> assertTrue(users.contains(admin))
        );
    }

    @Test
    public void testGetSelectedBannedUsersNoneBanned() throws NotFoundException {
        topicGateway.createTopic(topic);
        assertEquals(0, userGateway.getSelectedBannedUsers(topic, selection).size());
    }

    @Test
    public void testGetSelectedBannedUsersSQLException() throws SQLException {
        topic.setId(1);
        Connection connSpy = spy(connection);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                     () -> new UserDBGateway(connSpy).getSelectedBannedUsers(topic, selection)
        );
    }

    @Test
    public void testIsBanned() throws NotFoundException {
        topicGateway.createTopic(topic);
        userGateway.createUser(user);
        topicGateway.banUser(topic, user);
        assertTrue(userGateway.isBanned(user, topic));
    }

    @Test
    public void testIsBannedNotBanned() throws NotFoundException {
        topicGateway.createTopic(topic);
        userGateway.createUser(user);
        assertFalse(userGateway.isBanned(user, topic));
    }

    @Test
    public void testIsBannedUserIdNull() {
        user.setId(null);
        topic.setId(1);
        assertThrows(IllegalArgumentException.class,
                     () -> userGateway.isBanned(user, topic)
        );
    }

    @Test
    public void testIsBannedTopicIdNull() {
        assertThrows(IllegalArgumentException.class,
                     () -> userGateway.isBanned(user, topic)
        );
    }

    @Test
    public void testIsBannedSQLException() throws SQLException {
        topic.setId(1);
        Connection connSpy = spy(connection);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                     () -> new UserDBGateway(connSpy).isBanned(user, topic)
        );
    }

    @Test
    public void testCleanExpiredRegistrationsWhenSuccess() {
        DBExtension.insertMinimalTestData();
        assertDoesNotThrow(() -> userGateway.getUserByUsername("pending"));
        assertDoesNotThrow(() -> userGateway.getUserByUsername("corpse"));
        userGateway.cleanExpiredRegistrations();
        assertDoesNotThrow(() -> userGateway.getUserByUsername("pending"));
        assertThrows(NotFoundException.class, () -> userGateway.getUserByUsername("corpse"));
    }

    @Test
    public void testCleanExpiredRegistrationsWhenError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).cleanExpiredRegistrations());
    }

    @Test
    public void testGetSubscribersOfUser() throws DuplicateException, NotFoundException, SelfReferenceException {
        userGateway.createUser(user);
        userGateway.createUser(admin);
        subscriptionGateway.subscribe(user, admin);
        List<User> subscribers = userGateway.getSubscribersOf(user);
        assertAll(
                () -> assertEquals(1, subscribers.size()),
                () -> assertTrue(subscribers.contains(admin))
        );
    }

    @Test
    public void testGetSubscribersOfUserUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userGateway.getSubscribersOf((User) null)
        );
    }

    @Test
    public void testGetSubscribersOfUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> userGateway.getSubscribersOf(user)
        );
    }

    @Test
    public void testGetSubscribersOfUserStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).getSubscribersOf(user));
    }

    @Test
    public void testGetSubscribersOfUserNoSubscribers() {
        userGateway.createUser(user);
        assertTrue(userGateway.getSubscribersOf(user).isEmpty());
    }

    @Test
    public void testGetSubscribersOfReport() throws DuplicateException, NotFoundException {
        userGateway.createUser(user);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        report.setTopicID(topic.getId());
        reportGateway.create(report);
        subscriptionGateway.subscribe(report, user);
        subscriptionGateway.subscribe(report, admin);
        List<User> subscribers = userGateway.getSubscribersOf(report);
        assertAll(
                () -> assertEquals(2, subscribers.size()),
                () -> assertTrue(subscribers.contains(admin)),
                () -> assertTrue(subscribers.contains(user))
        );
    }

    @Test
    public void testGetSubscribersOfReportReportNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userGateway.getSubscribersOf((Report) null)
        );
    }

    @Test
    public void testGetSubscribersOfReportIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userGateway.getSubscribersOf(report)
        );
    }

    @Test
    public void testGetSubscribersOfReportStoreException() throws SQLException, NotFoundException {
        topicGateway.createTopic(topic);
        report.setTopicID(topic.getId());
        reportGateway.create(report);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).getSubscribersOf(report));
    }

    @Test
    public void testGetSubscribersOfReportNoSubscribers() throws NotFoundException {
        topicGateway.createTopic(topic);
        report.setTopicID(topic.getId());
        reportGateway.create(report);
        assertTrue(userGateway.getSubscribersOf(report).isEmpty());
    }

    @Test
    public void testGetSubscribersOfTopic() throws DuplicateException, NotFoundException {
        userGateway.createUser(user);
        userGateway.createUser(admin);
        topicGateway.createTopic(topic);
        subscriptionGateway.subscribe(topic, user);
        subscriptionGateway.subscribe(topic, admin);
        List<User> subscribers = userGateway.getSubscribersOf(topic);
        assertAll(
                () -> assertEquals(2, subscribers.size()),
                () -> assertTrue(subscribers.contains(admin)),
                () -> assertTrue(subscribers.contains(user))
        );
    }

    @Test
    public void testGetSubscribersOfTopicTopicNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userGateway.getSubscribersOf((Topic) null)
        );
    }

    @Test
    public void testGetSubscribersOfTopicIdNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userGateway.getSubscribersOf(topic)
        );
    }

    @Test
    public void testGetSubscribersOfTopicStoreException() throws SQLException, NotFoundException {
        topicGateway.createTopic(topic);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).getSubscribersOf(topic));
    }

    @Test
    public void testGetSubscribersOfTopicNoSubscribers() throws NotFoundException {
        topicGateway.createTopic(topic);
        assertTrue(userGateway.getSubscribersOf(topic).isEmpty());
    }

    @Test
    public void testSelectSubscribedUsers() throws DuplicateException, NotFoundException, SelfReferenceException {
        userGateway.createUser(user);
        userGateway.createUser(admin);
        subscriptionGateway.subscribe(admin, user);
        selection.setTotalSize(1);
        List<User> subscribers = userGateway.selectSubscribedUsers(user, selection);
        assertAll(
                () -> assertEquals(1, subscribers.size()),
                () -> assertTrue(subscribers.contains(admin))
        );
    }

    @Test
    public void testSelectSubscribedUsersSelectionNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userGateway.selectSubscribedUsers(user, null)
        );
    }

    @Test
    public void testSelectSubscribedUsersSelectionBlank() {
        selection.setSortedBy("");
        assertThrows(IllegalArgumentException.class,
                () -> userGateway.selectSubscribedUsers(user, selection)
        );
    }

    @Test
    public void testSelectSubscribedUsersUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userGateway.selectSubscribedUsers(null, selection)
        );
    }

    @Test
    public void testSelectSubscribedUsersUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> userGateway.selectSubscribedUsers(user, selection)
        );
    }

    @Test
    public void testSelectSubscribedUsersStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new UserDBGateway(connectionSpy).selectSubscribedUsers(user, selection)
        );
    }

    @Test
    public void testSelectSubscribedUsersNoSubscribers() {
        userGateway.createUser(user);
        assertTrue(userGateway.selectSubscribedUsers(user, selection).isEmpty());
    }

    @Test
    public void testCountSubscribedUsers() throws DuplicateException, NotFoundException, SelfReferenceException {
        userGateway.createUser(user);
        userGateway.createUser(admin);
        subscriptionGateway.subscribe(admin, user);
        assertEquals(1, userGateway.countSubscribedUsers(user));
    }

    @Test
    public void testCountSubscribedUsersUserNull() {
        assertThrows(IllegalArgumentException.class,
                () -> userGateway.countSubscribedUsers(null)
        );
    }

    @Test
    public void testCountSubscribedUsersUserIdNull() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class,
                () -> userGateway.countSubscribedUsers(user)
        );
    }

    @Test
    public void testCountSubscribedUsersStoreException() throws SQLException {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new UserDBGateway(connectionSpy).countSubscribedUsers(user)
        );
    }

    @Test
    public void testCountSubscribedUsersResultSet() throws SQLException {
        userGateway.createUser(user);
        ResultSet resultSetMock = mock(ResultSet.class);
        PreparedStatement stmtMock = mock(PreparedStatement.class);
        Connection connectionSpy = spy(connection);
        doReturn(false).when(resultSetMock).next();
        doReturn(resultSetMock).when(stmtMock).executeQuery();
        doReturn(stmtMock).when(connectionSpy).prepareStatement(any());
        assertThrows(InternalError.class,
                () -> new UserDBGateway(connectionSpy).countSubscribedUsers(user)
        );
    }

}
