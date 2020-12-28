package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;
import tech.bugger.global.util.Log;
import tech.bugger.persistence.exception.NotFoundException;
import tech.bugger.persistence.exception.TransactionException;
import tech.bugger.persistence.util.Transaction;
import tech.bugger.persistence.util.TransactionManager;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.inject.Inject;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for application settings.
 */
@Dependent
public class SettingsService {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(SettingsService.class);

    /**
     * Transaction manager used for creating transactions.
     */
    private TransactionManager transactionManager;

    /**
     * Feedback Event for user feedback.
     */
    private Event<Feedback> feedbackEvent;

    /**
     * Resource bundle for feedback messages.
     */
    private ResourceBundle messagesBundle;

    /**
     * Reference to the current {@link ExternalContext}.
     */
    private ExternalContext ectx;

    /**
     * Constructs a new settings service with the given dependencies.
     *
     * @param transactionManager The transaction manager to use for creating transactions.
     * @param feedbackEvent      The feedback event to use for user feedback.
     * @param messagesBundle     The resource bundle for feedback messages.
     * @param ectx               The current {@link ExternalContext} of the application.
     */
    @Inject
    public SettingsService(final TransactionManager transactionManager, final Event<Feedback> feedbackEvent,
                           final @RegistryKey("messages") ResourceBundle messagesBundle, final ExternalContext ectx) {
        this.transactionManager = transactionManager;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
        this.ectx = ectx;
    }

    /**
     * Loads the current application configuration.
     *
     * @return The loaded {@link Configuration}.
     */
    public Configuration loadConfiguration() {
        Configuration configuration = null;
        try (Transaction tx = transactionManager.begin()) {
            configuration = tx.newSettingsGateway().getConfiguration();
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Application configuration not found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error when loading application configuration.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return configuration;
    }

    /**
     * Loads the current organization data.
     *
     * @return The loaded {@link Organization}.
     */
    public Organization loadOrganization() {
        Organization organization = null;
        try (Transaction tx = transactionManager.begin()) {
            organization = tx.newSettingsGateway().getOrganization();
            tx.commit();
        } catch (NotFoundException e) {
            log.error("Organization data not found.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("not_found_error"), Feedback.Type.ERROR));
        } catch (TransactionException e) {
            log.error("Error when loading organization data.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("data_access_error"), Feedback.Type.ERROR));
        }
        return organization;
    }

    /**
     * Updates the application configuration in the data storage.
     *
     * @param configuration The new {@link Configuration}.
     * @return {@code true} iff the update succeeded.
     */
    public boolean updateConfiguration(final Configuration configuration) {
        try (Transaction tx = transactionManager.begin()) {
            tx.newSettingsGateway().setConfiguration(configuration);
            tx.commit();
            return true;
        } catch (TransactionException e) {
            feedbackEvent.fire(new Feedback(messagesBundle.getString("update_failure"), Feedback.Type.ERROR));
            return false;
        }
    }

    /**
     * Updates the organization data in the data storage.
     *
     * @param organization The new {@link Organization}.
     * @return {@code true} iff the update succeeded.
     */
    public boolean updateOrganization(final Organization organization) {
        try (Transaction tx = transactionManager.begin()) {
            tx.newSettingsGateway().setOrganization(organization);
            tx.commit();
            return true;
        } catch (TransactionException e) {
            feedbackEvent.fire(new Feedback(messagesBundle.getString("update_failure"), Feedback.Type.ERROR));
            return false;
        }
    }

    /**
     * Discovers the available themes in the corresponding directory.
     *
     * @return The filenames of the available themes.
     */
    public List<String> discoverThemes() {
        List<String> themes = Collections.emptyList();
        try (Stream<Path> files = Files.list(Paths.get(ectx.getRealPath("/resources/design/themes")))) {
            themes = files.filter(Files::isRegularFile)
                          .map(p -> p.getFileName().toString())
                          .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Could not discover available themes on file system.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("themes_discovery_error"), Feedback.Type.WARNING));
        }
        return themes;
    }

    /**
     * Converts the given logo to the internal format for further use.
     *
     * @param upload The uploaded logo in {@link Part} format}.
     * @return The converted logo as {@code byte[]} array.
     */
    public byte[] convertLogo(final Part upload) {
        try {
            return upload.getInputStream().readAllBytes();
        } catch (IOException e) {
            log.warning("Could not read uploaded logo.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("logo_conversion_error"), Feedback.Type.ERROR));
            return null;
        }
    }

}
