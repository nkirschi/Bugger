package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

/**
 * Service providing methods related to authentication. A {@code Feedback} event is fired, if unexpected circumstances
 * occur.
 */
@Dependent
public class AuthenticationService {

    private static final Log log = Log.forClass(AuthenticationService.class);

    @Inject
    @Any
    Event<Feedback> feedback;

    /**
     * Authenticates a user, e.g. when logging in.
     *
     * @param username The username.
     * @param password The password.
     * @return The user with all their data.
     */
    public User authenticate(String username, String password) {
        return null;
    }

    /**
     * Registers a new user.
     *
     * @param user The user to be registered.
     */
    public void register(User user) {
    }

    public void setPassword(User user, String password, String token) {

    }

    /**
     * If a user forgot their password and provides their username and email address, an email is sent with instructions
     * to set a new password.
     *
     * @param user The user who forgot their password.
     */
    public void forgotPassword(User user) {
    }

    /**
     * Checks if a token is still valid.
     *
     * @param token The unique String identifying the token.
     * @return {@code true} if the token is valid, {@code false} otherwise.
     */
    public boolean isValid(String token) {
        return false;
    }
}
