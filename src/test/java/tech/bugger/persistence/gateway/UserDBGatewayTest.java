package tech.bugger.persistence.gateway;

import java.sql.Connection;
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
        User copy = new User(user);
        gateway.createUser(copy);

        User copyFromDatabase = gateway.getUserByID(copy.getId());
        assertAll(() -> assertNotNull(copy.getId()),
                () -> assertEquals(copy.getId(), copyFromDatabase.getId()),
                () -> assertEquals(copy.getUsername(), copyFromDatabase.getUsername()),
                () -> assertEquals(copy.getPasswordHash(), copyFromDatabase.getPasswordHash()),
                () -> assertEquals(copy.getPasswordSalt(), copyFromDatabase.getPasswordSalt()),
                () -> assertEquals(copy.getHashingAlgorithm(), copyFromDatabase.getHashingAlgorithm()),
                () -> assertEquals(copy.getEmailAddress(), copyFromDatabase.getEmailAddress()),
                () -> assertEquals(copy.getFirstName(), copyFromDatabase.getFirstName()),
                () -> assertEquals(copy.getLastName(), copyFromDatabase.getLastName()),
                () -> assertArrayEquals(copy.getAvatarThumbnail(), copyFromDatabase.getAvatarThumbnail()),
                () -> assertEquals(copy.getBiography(), copyFromDatabase.getBiography()),
                () -> assertEquals(copy.getPreferredLanguage(), copyFromDatabase.getPreferredLanguage()),
                () -> assertEquals(copy.getProfileVisibility(), copyFromDatabase.getProfileVisibility()),
                () -> assertEquals(copy.getForcedVotingWeight(), copyFromDatabase.getForcedVotingWeight()),
                () -> assertEquals(copy.isAdministrator(), copyFromDatabase.isAdministrator()));
    }

    @Test
    public void testCreateUserWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any(), anyInt());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).createUser(new User(user)));
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
        User copy = new User(user);
        gateway.createUser(copy);
        copy.setLastName("Heinrich");
        gateway.updateUser(copy);

        User copyFromDatabase = gateway.getUserByID(copy.getId());
        assertAll(() -> assertEquals(copy.getId(), copyFromDatabase.getId()),
                () -> assertEquals(copy.getUsername(), copyFromDatabase.getUsername()),
                () -> assertEquals(copy.getPasswordHash(), copyFromDatabase.getPasswordHash()),
                () -> assertEquals(copy.getPasswordSalt(), copyFromDatabase.getPasswordSalt()),
                () -> assertEquals(copy.getHashingAlgorithm(), copyFromDatabase.getHashingAlgorithm()),
                () -> assertEquals(copy.getEmailAddress(), copyFromDatabase.getEmailAddress()),
                () -> assertEquals(copy.getFirstName(), copyFromDatabase.getFirstName()),
                () -> assertEquals(copy.getLastName(), copyFromDatabase.getLastName()),
                () -> assertArrayEquals(copy.getAvatarThumbnail(), copyFromDatabase.getAvatarThumbnail()),
                () -> assertEquals(copy.getBiography(), copyFromDatabase.getBiography()),
                () -> assertEquals(copy.getRegistrationDate(), copyFromDatabase.getRegistrationDate()),
                () -> assertEquals(copy.getPreferredLanguage(), copyFromDatabase.getPreferredLanguage()),
                () -> assertEquals(copy.getProfileVisibility(), copyFromDatabase.getProfileVisibility()),
                () -> assertEquals(copy.getForcedVotingWeight(), copyFromDatabase.getForcedVotingWeight()),
                () -> assertEquals(copy.isAdministrator(), copyFromDatabase.isAdministrator()));
    }

    @Test
    public void testUpdateUserWhenDatabaseError() throws Exception {
        User copy = new User(user);
        copy.setId(1);
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any(), anyInt());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).updateUser(copy));
    }

    @Test
    public void testUpdateUserNotExists() {
        User copy = new User(user);
        copy.setId(42);
        assertThrows(NotFoundException.class, () -> gateway.updateUser(copy));
    }

    @Test
    public void testUpdateUserNullId() {
        User copy = new User(user);
        assertThrows(IllegalArgumentException.class, () -> gateway.updateUser(copy));
    }

    @Test
    public void testIsEmailAssignedNo() {
        assertFalse(() -> gateway.isEmailAssigned("t@t.tk"));
    }

    @Test
    public void testIsEmailAssignedYes() {
        User copy = new User(user);
        gateway.createUser(copy);
        assertTrue(() -> gateway.isEmailAssigned("test@test.de"));
    }

    @Test
    public void testIsEmailAssignedWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).isEmailAssigned("t@t.tk"));
    }

    @Test
    public void testIsUsernameAssignedNo() {
        assertFalse(() -> gateway.isUsernameAssigned("testuser"));
    }

    @Test
    public void testIsUsernameAssignedYes() {
        User copy = new User(user);
        gateway.createUser(copy);
        assertTrue(() -> gateway.isUsernameAssigned("testuser"));
    }

    @Test
    public void testIsUsernameAssignedWhenDatabaseError() throws Exception {
        Connection connectionSpy = spy(connection);
        doThrow(SQLException.class).when(connectionSpy).prepareStatement(any());
        assertThrows(StoreException.class, () -> new UserDBGateway(connectionSpy).isUsernameAssigned("testuser"));
    }

}