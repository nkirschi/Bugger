package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Hasher;
import tech.bugger.business.util.PriorityExecutor;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.util.Mailer;
import tech.bugger.persistence.util.PropertiesReader;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.ResourceBundle;

/**
 * Service providing methods related to authentication. A {@code Feedback} event is fired, if unexpected circumstances
 * occur.
 */
@Dependent
public class AuthenticationService {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(AuthenticationService.class);

    /**
     * Maximum amount of times an email should be tried to resend.
     */
    private static final int MAX_EMAIL_TRIES = 3;

    /**
     * Salt length to use when hashing passwords.
     */
    private static final int SALT_LENGTH = 16;

    /**
     * Transaction manager used for creating transactions.
     */
    private final TransactionManager transactionManager;

    /**
     * Feedback Event for user feedback.
     */
    private final Event<Feedback> feedback;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messages;

    /**
     * The {@link PriorityExecutor} instance to use when sending e-mails.
     */
    private final PriorityExecutor priorityExecutor;

    /**
     * The {@link Mailer} instance to use when sending e-mails.
     */
    private final Mailer mailer;

    /**
     * The {@link PropertiesReader} instance to use when reading the current configuration.
     */
    private final PropertiesReader configReader;

    /**
     * Constructs a new authentication service with the given dependencies.
     *
     * @param transactionManager The transaction manager to use for creating transactions.
     * @param feedback           The feedback event to use for user feedback.
     * @param messages           The resource bundle for feedback messages.
     * @param priorityExecutor   The priority executor to use when sending e-mails.
     * @param mailer             The mailer to use.
     * @param configReader       The configuration reader to use.
     */
    @Inject
    public AuthenticationService(final TransactionManager transactionManager, final Event<Feedback> feedback,
                                 @RegistryKey("messages") final ResourceBundle messages,
                                 @RegistryKey("mails") final PriorityExecutor priorityExecutor,
                                 @RegistryKey("main") final Mailer mailer,
                                 @RegistryKey("config") final PropertiesReader configReader) {
        this.transactionManager = transactionManager;
        this.feedback = feedback;
        this.messages = messages;
        this.priorityExecutor = priorityExecutor;
        this.mailer = mailer;
        this.configReader = configReader;
    }

    /**
     * Authenticates a user, e.g. when logging in.
     *
     * @param username The username.
     * @param password The password.
     * @return The user with all their data.
     */
    public User authenticate(final String username, final String password) {
        //TODO add message with key wrong_credentials
        User user = null;
        try (Transaction tx = transactionManager.begin()) {
            user = tx.newUserGateway().getUserByUsername(username);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("The user with username " + username + "could not be found.", e);
            feedback.fire(new Feedback(messages.getString("wrong_credentials"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error while loading user with username " + username, e);
            feedback.fire(new Feedback(messages.getString("data_access_error"), Feedback.Type.ERROR));
        }
        if (user == null) {
            return null;
        }
        if (!(user.getPasswordHash().equals(Hasher.hash(password, user.getPasswordSalt(),
                user.getHashingAlgorithm())))) {
            log.warning("The supplied password of the user with username " + username + " did not match the actual"
                    + " password");
            feedback.fire(new Feedback(messages.getString("wrong_credentials"), Feedback.Type.ERROR));
            return null;
        }
        return user;
    }

    /**
     * Registers a new user.
     *
     * @param user The user to be registered.
     */
    public void register(final User user) {
    }

    public void setPassword(final User user, final String password, final String token) {

    }

    /**
     * If a user forgot their password and provides their username and email address, an email is sent with instructions
     * to set a new password.
     *
     * @param user The user who forgot their password.
     */
    public void forgotPassword(final User user) {
    }

    /**
     * Checks if a token is still valid.
     *
     * @param token The unique String identifying the token.
     * @return {@code true} if the token is valid, {@code false} otherwise.
     */
    public boolean isValid(final String token) {
        return false;
    }

}
