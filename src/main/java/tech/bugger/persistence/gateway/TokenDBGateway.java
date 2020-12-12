package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

/**
 * Token gateway that gives access to verification tokens stored in a database.
 */
public class TokenDBGateway implements TokenGateway {

    private static final Log log = Log.forClass(TokenDBGateway.class);

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
