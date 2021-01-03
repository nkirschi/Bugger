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

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
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