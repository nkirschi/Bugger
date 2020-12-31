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
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@ExtendWith(LogExtension.class)
@ExtendWith(DBExtension.class)
public class UserDBGatewayTest {

    private UserDBGateway gateway;
    private Connection conn;
    private User user;

    @BeforeEach
    public void setup() throws Exception {
        conn = DBExtension.getConnection();
        gateway = new UserDBGateway(conn);
        user = new User(12345, "Helgi", "v3ry_s3cur3", "salt", "algorithm", "helga@web.de", "Helga", "Brötchen", new Lazy<>(new byte[0]),
                null, "Hallo, ich bin die Helgi | Perfect | He/They/Her | vergeben | Abo =|= endorsement",
                Language.GERMAN, User.ProfileVisibility.MINIMAL, ZonedDateTime.now(), null, false);
        createUser(user);
    }

    @AfterEach
    public void teardown() throws SQLException {
        deleteUser(user);
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

    @Test
    public void testGetUserByUsername() throws NotFoundException {
        User helga = gateway.getUserByUsername(user.getUsername());
        assertEquals(user, helga);
    }

    @Test
    public void testGetUserByUsernameNotFound() {
        assertThrows(NotFoundException.class,
                () -> gateway.getUserByUsername("helga")
        );
    }

    @Test
    public void testGetUserByUsernameDatabaseError() throws SQLException {
        Connection connSpy = spy(conn);
        doThrow(SQLException.class).when(connSpy).prepareStatement(any());
        assertThrows(StoreException.class,
                () -> new UserDBGateway(connSpy).getUserByUsername(user.getUsername())
        );
    }
}
