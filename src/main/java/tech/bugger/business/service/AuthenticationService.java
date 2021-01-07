package tech.bugger.business.service;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.Hasher;
import tech.bugger.business.util.PriorityExecutor;
import tech.bugger.business.util.PriorityTask;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Token;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.util.Mail;
import tech.bugger.persistence.util.MailBuilder;
import tech.bugger.persistence.util.Mailer;
import tech.bugger.persistence.util.PropertiesReader;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

/**
 * Service for user authentication. A {@link Feedback} {@link Event} is fired, if unexpected circumstances occur.
 */
@Dependent
public class AuthenticationService {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(AuthenticationService.class);

    /**
     * The length of a generated token.
     */
    private static final int TOKEN_LENGTH = 32;

    /**
     * Maximum amount of times an email should be tried to resend.
     */
    private static final int MAX_EMAIL_TRIES = 3;

    /**
     * Transaction manager used for creating transactions.
     */
    private final TransactionManager transactionManager;

    /**
     * Feedback Event for user feedback.
     */
    private final Event<Feedback> feedbackEvent;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Resource bundle for interaction messages.
     */
    private final ResourceBundle interactionsBundle;

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
     * @param feedbackEvent      The feedback event to use for user feedback.
     * @param messagesBundle     The resource bundle for feedback messages.
     * @param interactionsBundle The resource bundle for interaction messages.
     * @param priorityExecutor   The priority executor to use when sending e-mails.
     * @param mailer             The mailer to use.
     * @param configReader       The configuration reader to use.
     */
    @Inject
    public AuthenticationService(final TransactionManager transactionManager, final Event<Feedback> feedbackEvent,
                                 @RegistryKey("messages") final ResourceBundle messagesBundle,
                                 @RegistryKey("interactions") final ResourceBundle interactionsBundle,
                                 @RegistryKey("mails") final PriorityExecutor priorityExecutor,
                                 @RegistryKey("main") final Mailer mailer,
                                 @RegistryKey("config") final PropertiesReader configReader) {
        this.transactionManager = transactionManager;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
        this.interactionsBundle = interactionsBundle;
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
        User user = findUser(username);

        if ((user != null) && (user.getPasswordHash().equals(Hasher.hash(password, user.getPasswordSalt(),
                user.getHashingAlgorithm())))) {
            String configAlgo = configReader.getString("HASH_ALGO");

            if (!user.getHashingAlgorithm().equals(configAlgo)) {
                try {
                    user = updateHashingAlgorithm(user, configAlgo, password);
                } catch (NotFoundException e) {
                    log.error("The user with username " + username + " could not be found in the database.");
                    throw new tech.bugger.business.exception.NotFoundException("The user with username " + username
                            + " could not be found in the database.");
                }
            }

            return user;
        } else {
            feedbackEvent.fire(new Feedback(messagesBundle.getString("authentication_service.wrong_credentials"),
                    Feedback.Type.ERROR));
        }

        return null;
    }

    /**
     * Loads the user with the given username from the database.
     *
     * @param username The username.
     * @return The user with all their data.
     */
    private User findUser(final String username) {
        User user = null;

        try (Transaction tx = transactionManager.begin()) {
            user = tx.newUserGateway().getUserByUsername(username);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("The user with username " + username + " could not be found.", e);
        } catch (TransactionException e) {
            log.error("Error while loading user with username " + username, e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return user;
    }

    /**
     * Updates the given user's hashing algorithm to the current one specified in the configuration.
     *
     * @param user The user.
     * @param hashingAlgo The new hashing algorithm.
     * @param password The user's password.
     * @return The user.
     */
    private User updateHashingAlgorithm(final User user, final String hashingAlgo, final String password)
            throws NotFoundException {
        User updateUser = new User(user);
        updateUser.setPasswordHash(Hasher.hash(password, user.getPasswordSalt(), hashingAlgo));
        updateUser.setHashingAlgorithm(hashingAlgo);

        try (Transaction tx = transactionManager.begin()) {
            tx.newUserGateway().updateUser(user);
            tx.commit();
            return updateUser;
        } catch (TransactionException e) {
            log.error("Error while updating the hashing algorithm of the user with username "
                    + user.getUsername(), e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"),
                    Feedback.Type.ERROR));
        }

        return user;
    }

    /**
     * Generates a token value for verification of a user action.
     *
     * @return The newly generated token value.
     */
    public String generateToken() {
        String value;

        do {
            value = Hasher.generateRandomBytes(TOKEN_LENGTH);
        } while (isValid(value));

        return value;
    }

    /**
     * Registers a new user by generating a {@link Token} and sending a confirmation email to the new user.
     *
     * @param user The user to be registered.
     * @param path The current deployment path of this web application.
     * @return Whether the action was successful or not.
     */
    public boolean register(final User user, final String path) {
        Token token = null;

        try (Transaction tx = transactionManager.begin()) {
            Token toInsert = new Token(generateToken(), Token.Type.REGISTER, null, user);
            token = tx.newTokenGateway().createToken(toInsert);
            tx.commit();
        } catch (NotFoundException e) {
            log.error("The user couldn't be found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Token could not be generated.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        if (token == null) {
            return false;
        }

        String link = path + "/password-set?token=" + token.getValue();
        Mail mail = new MailBuilder()
                .to(user.getEmailAddress())
                .subject(interactionsBundle.getString("email_register_subject"))
                .content(new MessageFormat(interactionsBundle.getString("email_register_content"))
                        .format(new String[]{token.getUser().getFirstName(), token.getUser().getLastName(), link}))
                .envelop();
        sendMail(mail);

        return true;
    }

    /**
     * Sets the password for the given user using the given token in the process.
     *
     * @param user     The {@link User} whose password should be set.
     * @param password The password to set.
     * @param token    The used authentication token.
     * @return Whether the action was successful or not.
     */
    public boolean setPassword(final User user, final String password, final String token) {
        if (!isValid(token)) {
            feedbackEvent.fire(new Feedback(messagesBundle.getString("token_invalid"), Feedback.Type.INFO));
            return false;
        }

        String salt = Hasher.generateRandomBytes(configReader.getInt("SALT_LENGTH"));
        String algorithm = configReader.getString("HASH_ALGO");
        String hashed = Hasher.hash(password, salt, algorithm);

        user.setPasswordSalt(salt);
        user.setHashingAlgorithm(algorithm);
        user.setPasswordHash(hashed);

        try (Transaction tx = transactionManager.begin()) {
            tx.newUserGateway().updateUser(user);
            tx.commit();
            return true;
        } catch (NotFoundException e) {
            log.error("The user couldn't be found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("User could not be updated.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return false;
    }

    /**
     * Returns the complete {@link Token} DTO for the given value.
     *
     * @param value The token value to find the associated DTO for.
     * @return The complete {@link Token} or {@code null} if the given {@code value} is invalid.
     */
    public Token findToken(final String value) {
        Token token = null;

        try (Transaction tx = transactionManager.begin()) {
            token = tx.newTokenGateway().findToken(value);
            tx.commit();
        } catch (NotFoundException e) {
            log.debug("Token by value could not be found.");
        } catch (TransactionException e) {
            log.error("Token by value could not be fetched.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }

        return token;
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
        return findToken(token) != null;
    }

    /**
     * Tries to send the given {@link Mail}.
     *
     * @param mail The e-mail to send.
     */
    private void sendMail(final Mail mail) {
        priorityExecutor.enqueue(new PriorityTask(PriorityTask.Priority.HIGH, () -> {
            int tries = 1;
            log.debug("Sending e-mail " + mail + ".");
            while (!mailer.send(mail) && tries++ <= MAX_EMAIL_TRIES) {
                log.warning("Trying to send e-mail again. Try #" + tries + '.');
            }
            if (tries > MAX_EMAIL_TRIES) {
                log.error("Couldn't send e-mail for more than " + MAX_EMAIL_TRIES + " times! Please investigate!");
            }
        }));
    }

}
