package tech.bugger.persistence.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(DBExtension.class)
@ExtendWith(LogExtension.class)
public class UserDBGatewayTest {

    private UserDBGateway gateway;

    private Connection connection;

    private User user;

    @BeforeEach
    public void setUp() throws Exception {
        connection = DBExtension.getConnection();
        gateway = new UserDBGateway(connection);

        user = new User(null, "testuser", "0123456789abcdef", "0123456789abcdef", "SHA3-512", "test@test.de", "Test", "User", new Lazy<>(new byte[]{1, 2, 3, 4}), new byte[]{1}, "# I am a test user.",
                Language.GERMAN, User.ProfileVisibility.MINIMAL, null, 3, false);
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
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

}
package tech.bugger.persistence.gateway;

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
import tech.bugger.persistence.util.StatementParametrizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
public class UserDBGatewayTest {

    private UserDBGateway gateway;
    private Connection conn;
    private User user;
    private User admin1;
    private User admin2;

    @BeforeEach
    public void setup() throws Exception {
        conn = DBExtension.getConnection();
        gateway = new UserDBGateway(conn);
        user = new User(12345, "Helgi", "v3ry_s3cur3", "salt", "algorithm", "helga@web.de", "Helga", "Brötchen", new Lazy<>(new byte[0]),
                null, "Hallo, ich bin die Helgi | Perfect | He/They/Her | vergeben | Abo =|= endorsement",
                Language.GERMAN, User.ProfileVisibility.MINIMAL, ZonedDateTime.now(), null, false);
        admin1 = new User(67890, "Helgo", "v3ry_s3cur3", "salt", "algorithm", "helgo@admin.de", "Helgo", "Brötchen", null,
                null, "Ich bin der Administrator hier!", Language.ENGLISH, User.ProfileVisibility.MINIMAL,
                ZonedDateTime.now(), null, true);
        admin2 = new User(11111, "Der Admin", "v3ry_s3cur3", "salt", "algorithm", "admin@admin.de", "Admin", "Admin", null,
                null, "Ich war zuerst da!", Language.ENGLISH, User.ProfileVisibility.MINIMAL,
                ZonedDateTime.now(), null, true);
        createUser(user);
        createUser(admin1);
        createUser(admin2);
    }

    @AfterEach
    public void teardown() throws SQLException {
        deleteUser(user);
        deleteUser(admin1);
        deleteUser(admin2);
        conn.close();
    }

    private void createUser(User user) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO \"user\" "
                        + "(username, password_hash, password_salt, hashing_algorithm, email_address, first_name, "
                        + "last_name, avatar, avatar_thumbnail, biography, preferred_language, profile_visibility, "
                        + "forced_voting_weight, is_admin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            Lazy<byte[]> avatar = user.getAvatar();

            new StatementParametrizer(stmt)
                    .string(user.getUsername())
                    .string(user.getPasswordHash())
                    .string(user.getPasswordSalt())
                    .string(user.getHashingAlgorithm())
                    .string(user.getEmailAddress())
                    .string(user.getFirstName())
                    .string(user.getLastName())
                    .bytes(avatar != null ? avatar.get() : new byte[0])
                    .bytes(user.getAvatarThumbnail())
                    .string(user.getBiography())
                    .string(user.getPreferredLanguage().name())
                    .object(user.getProfileVisibility(), Types.OTHER)
                    .object(user.getForcedVotingWeight(), Types.INTEGER)
                    .bool(user.isAdministrator())
                    .toStatement().executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt("id"));
                    user.setRegistrationDate(rs.getTimestamp("registered_at").toLocalDateTime()
                            .atZone(ZoneId.systemDefault()));
                }
            }
        }
    }

    private void deleteUser(User user) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM \"user\" WHERE id = ?;")) {
            new StatementParametrizer(stmt)
                    .integer(user.getId())
                    .toStatement()
                    .executeUpdate();
        }
    }

    private void deleteAllUsers() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM \"user\";");
        }
    }

    @Test
    public void testGetNumberOfAdmins() throws NotFoundException {
        //Two inserted admins plus default admin.
        assertEquals(3, gateway.getNumberOfAdmins());
    }

    @Test
    public void testGetNumberOfAdminsNoAdmins() throws SQLException {
        deleteAllUsers();
        assertEquals(0, gateway.getNumberOfAdmins());
    }

    @Test
    public void testGetNumberOfAdminsDatabaseError() throws SQLException {
        Connection connSpy = spy(conn);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new UserDBGateway(connSpy).getNumberOfAdmins()
        );
    }

    @Test
    public void testGetUserByID() throws NotFoundException {
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
        Connection connSpy = spy(conn);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new UserDBGateway(connSpy).getUserByID(user.getId())
        );
    }

    @Test
    public void testUpdateUser() throws NotFoundException {
        user.setLastName("Helgi");
        user.setEmailAddress("helgi@web.de");
        gateway.updateUser(user);
        User helga = gateway.getUserByID(user.getId());
        assertEquals(user, helga);
    }

    @Test
    public void testUpdateUserNotFound() {
        user.setId(2222);
        assertThrows(NotFoundException.class,
                () -> gateway.updateUser(user)
        );
    }

    @Test
    public void testUpdateUserInternalError() {
        assertThrows(InternalError.class,
                () -> gateway.updateUser(admin1)
        );
    }

    @Test
    public void testUpdateUserDatabaseError() throws SQLException {
        Connection connSpy = spy(conn);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new UserDBGateway(connSpy).updateUser(user)
        );
    }

    @Test
    public void testGetNumberOfPosts() throws NotFoundException {
        assertEquals(0, gateway.getNumberOfPosts(user));
    }

    @Test
    public void testGetNumberOfPostNoEntries() {
        user.setId(2222);
        assertEquals(0, gateway.getNumberOfPosts(user));
    }

    @Test
    public void testGetNumberOfPostsDatabaseError() throws SQLException {
        Connection connSpy = spy(conn);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new UserDBGateway(connSpy).getNumberOfPosts(user)
        );
    }
}
