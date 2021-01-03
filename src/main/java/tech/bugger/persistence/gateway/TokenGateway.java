package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Token;
import tech.bugger.persistence.exception.NotFoundException;

/**
 * A token gateway allows to query and modify a persistent storage of verification tokens.
 */
public interface TokenGateway {

    /**
     * Stores a given {@link Token} for a user action (at least a value, type, and user ID is required).
     *
     * @param token The {@link Token} to store.
     * @return The newly stored {@link Token} with valid additional metadata (at least a timestamp is generated).
     * @throws NotFoundException The user could not be found.
     */
    Token createToken(Token token) throws NotFoundException;

    /**
     * Returns the complete {@link Token} DTO for the given value.
     *
     * @param value The token value to find the associated DTO for.
     * @return The complete {@link Token}.
     * @throws NotFoundException The token value could not be found.
     */
    Token getTokenByValue(String value) throws NotFoundException;

    /**
     * Deletes expired verification tokens and unverified users that lack a valid verification token.
     *
     * @param expirationAge The maximum number of seconds a verification token is to be considered valid.
     */
    void cleanUp(int expirationAge);

}
