package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import java.sql.Connection;

/**
 * Token gateway that gives access to verification tokens stored in a database.
 */
public class TokenDBGateway implements TokenGateway {

    private static final Log log = Log.forClass(TokenDBGateway.class);

    private Connection conn;

    /**
     * Constructs a new token gateway with the given database connection.
     *
     * @param conn The database connection to use for the gateway.
     */
    public TokenDBGateway(Connection conn) {
        this.conn = conn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Token generateToken(User user, Token.Type type) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(String token) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void cleanUp(int expirationAge) {
        // TODO Auto-generated method stub
    }
}
