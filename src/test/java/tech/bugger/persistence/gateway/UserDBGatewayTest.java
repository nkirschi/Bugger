package tech.bugger.persistence.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.DBExtension;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Language;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Lazy;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(DBExtension.class)
@ExtendWith(LogExtension.class)
public class UserDBGatewayTest {

    private UserDBGateway gateway;

    private Connection connection;

    private User user;
    private User admin;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        gateway = new UserDBGateway(connection);

        user = new User(2, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User", new Lazy<>(new byte[]{1, 2, 3, 4}), new byte[]{1}, "# I am a test user.",
                Language.GERMAN, User.ProfileVisibility.MINIMAL, null, null, false);
        admin = new User(3, "Helgo", "v3ry_s3cur3", "salt", "algorithm", "helgo@admin.de", "Helgo", "Brötchen", new Lazy<>(new byte[]{1, 2, 3, 4}),
                new byte[]{1}, "Ich bin der Administrator hier!", Language.ENGLISH, User.ProfileVisibility.MINIMAL,
                ZonedDateTime.now(), null, true);
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
        gateway.createUser(user);

        User copyFromDatabase = gateway.getUserByID(user.getId());
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
        assertThrows(IllegalArgumentException.class, () -> gateway.createUser(user));
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
        assertThrows(NotFoundException.class, () -> gateway.getUserByID(42));
    }

    @Test
    public void testGetUserByIdWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).getUserByID(1));
    }

    @Test
    public void testUpdateUser() throws Exception {
        gateway.createUser(user);
        user.setLastName("Heinrich");
        gateway.updateUser(user);

        User copyFromDatabase = gateway.getUserByID(user.getId());
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
        assertThrows(NotFoundException.class, () -> gateway.updateUser(user));
    }

    @Test
    public void testUpdateUserNullId() {
        user.setId(null);
        assertThrows(IllegalArgumentException.class, () -> gateway.updateUser(user));
    }

    @Test
    public void testUpdateUserAbsentAvatar() {
        user.setId(1);
        user.setAvatar(new Lazy<>(() -> null));
        assertThrows(IllegalArgumentException.class, () -> gateway.updateUser(user));
    }

    @Test
    public void testGetUserByEmailNotFound() {
        assertThrows(NotFoundException.class, () -> gateway.getUserByEmail("t@t.tk"));
    }

    @Test
    public void testGetUserByEmailFound() throws Exception {
        gateway.createUser(user);
        assertEquals(user, gateway.getUserByEmail("test@test.de"));
    }

    @Test
    public void testGetUserByEmailWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).getUserByEmail("t@t.tk"));
    }

    @Test
    public void testGetUserByUsernameNotFound() {
        assertThrows(NotFoundException.class, () -> gateway.getUserByUsername("testuser"));
    }

    @Test
    public void testGetUserByUsernameFound() throws Exception {
        gateway.createUser(user);
        assertEquals(user, gateway.getUserByUsername("testuser"));
    }

    @Test
    public void testGetUserByUsernameWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).getUserByUsername("testuser"));
    }

    @Test
    public void testGetNumberOfAdmins() throws NotFoundException {
        gateway.createUser(user);
        gateway.createUser(admin);
        //One inserted admin plus default admin.
        assertEquals(2, gateway.getNumberOfAdmins());
    }

    @Test
    public void testGetNumberOfAdminsNoAdmins() throws SQLException {
        deleteAllUsers();
        assertEquals(0, gateway.getNumberOfAdmins());
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
        gateway.createUser(user);
        User helga = gateway.getUserByID(user.getId());
        assertEquals(user, helga);
    }

    @Test
    public void testGetUserByIDNotFound() {
        assertThrows(NotFoundException.class,
                () -> gateway.getUserByID(2222)
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
        gateway.createUser(user);
        assertEquals(0, gateway.getNumberOfPosts(user));
    }

    @Test
    public void testGetNumberOfPostNoEntries() {
        assertThrows(NotFoundException.class,
                () -> gateway.getNumberOfPosts(user)
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
        gateway.createUser(user);
        gateway.deleteUser(user);
        assertThrows(NotFoundException.class,
                () -> gateway.getUserByID(user.getId())
        );
    }

    @Test
    public void testDeleteUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> gateway.deleteUser(user)
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

}
