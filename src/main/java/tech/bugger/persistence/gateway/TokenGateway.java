package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;
import tech.bugger.persistence.exception.NotFoundException;

/**
 * A token gateway allows to query and modify a persistent storage of verification tokens.
 */
public interface TokenGateway {

    /**
     * Generates and stores a token for a user action.
     *
     * @param user The user to generate a token for.
     * @param type The type of user action the token should verify.
     * @return The newly generated token.
     * @throws NotFoundException The user could not be found.
     */
    Token generateToken(User user, Token.Type type) throws NotFoundException;

    /**
     * Checks whether a token exists and is still valid.
     *
     * @param token The token whose validity is to check.
     * @return Whether {@code token} exists and is valid.
     */
    boolean isValid(String token);

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
