package tech.bugger.persistence.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.ZoneId;
import tech.bugger.global.transfer.Token;
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
    public Token createToken(final Token token) throws NotFoundException {
        if (token.getUser() == null) {
            throw new IllegalArgumentException("User may not be null!");
        } else if (token.getUser().getId() == null) {
            throw new IllegalArgumentException("User ID may not be null!");
        } else if (token.getValue() == null) {
            throw new IllegalArgumentException("Token value may not be null!");
        } else if (token.getType() == null) {
            throw new IllegalArgumentException("Token type may not be null!");
        }

        Token ret;

        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM \"user\" WHERE id = ?")) {
            ResultSet rs = new StatementParametrizer(stmt)
                    .integer(token.getUser().getId())
                    .toStatement().executeQuery();

            if (!rs.next()) {
                throw new NotFoundException("User doesn't exist.");
            }
        } catch (SQLException e) {
            log.error("Couldn't search for existing users.", e);
            throw new StoreException(e);
        }

        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO token (value, type, verifies) "
                + "VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {

            new StatementParametrizer(stmt)
                    .string(token.getValue())
                    .object(token.getType(), Types.OTHER)
                    .integer(token.getUser().getId())
                    .toStatement().executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                ret = getTokenByValue(rs.getString("value"));
            } else {
                log.error("Couldn't read new token data.");
                throw new StoreException("Couldn't read new token data.");
            }
        } catch (SQLException e) {
            log.error("Couldn't insert token into database.", e);
            throw new StoreException(e);
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token getTokenByValue(final String value) throws NotFoundException {
        Token token;

        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM token t "
                + "JOIN \"user\" u on t.verifies = u.id WHERE t.value = ?")) {
            ResultSet rs = new StatementParametrizer(stmt).string(value).toStatement().executeQuery();
            if (rs.next()) {
                token = new Token(rs.getString("value"), Token.Type.valueOf(rs.getString("type")),
                        rs.getTimestamp("timestamp").toLocalDateTime().atZone(ZoneId.systemDefault()),
                        UserDBGateway.getUserFromResultSet(rs));
            } else {
                log.error("Searched token by value could not be found.");
                throw new NotFoundException("Searched token by value could not be found!");
            }
        } catch (SQLException e) {
            log.error("Couldn't search the token by value due to a database error.", e);
            throw new StoreException(e);
        }

        return token;
    }

    /**
     * {@inheritDoc}
     */
    public void cleanUp(final int expirationAge) {
    }

}
