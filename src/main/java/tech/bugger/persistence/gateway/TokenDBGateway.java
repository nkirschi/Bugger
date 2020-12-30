package tech.bugger.persistence.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import tech.bugger.business.util.Hasher;
import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.StoreException;
import tech.bugger.persistence.util.StatementParametrizer;

/**
 * Token gateway that gives access to verification tokens stored in a database.
 */
public class TokenDBGateway implements TokenGateway {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(TokenDBGateway.class);

    /**
     * The length of a generated token.
     */
    private static final int TOKEN_LENGTH = 32;

    /**
     * Database connection used by this gateway.
     */
    private final Connection conn;

    /**
     * Constructs a new token gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public TokenDBGateway(final Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token generateToken(final User user, final Token.Type type) throws NotFoundException {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID may not be null!");
        }

        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"user\" WHERE id = ?")) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new NotFoundException("User doesn't exist.");
            }
        } catch (SQLException e) {
            log.error("Couldn't search for existing users.", e);
            throw new StoreException(e);
        }

        String token;
        do {
            token = Hasher.generateRandomBytes(TOKEN_LENGTH);
        } while (isValid(token));

        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO token (value, type, verifies) "
                + "VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {

            new StatementParametrizer(stmt)
                    .string(token)
                    .object(type, Types.OTHER)
                    .integer(user.getId())
                    .toStatement().executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                return new Token(token, type, timestamp.atZone(ZoneId.systemDefault()), user);
            }
        } catch (SQLException e) {
            log.error("Couldn't insert token into database.", e);
            throw new StoreException(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(final String token) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM token WHERE value = ?")) {
            stmt.setString(1, token);

            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            log.error("Couldn't verify the token's validity due to a database error.", e);
            throw new StoreException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUserIdForToken(final String token) throws NotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM token WHERE value = ?")) {
            stmt.setString(1, token);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("verifies");
            } else {
                log.error("No associated user found.");
                throw new NotFoundException("Associated user couldn't be found!");
            }
        } catch (SQLException e) {
            log.error("Couldn't verify the token's validity due to a database error.", e);
            throw new StoreException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void cleanUp(final int expirationAge) {
    }

}
