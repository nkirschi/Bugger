package tech.bugger.control.backing;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.service.SettingsService;
import tech.bugger.business.util.Feedback;
import tech.bugger.business.util.RegistryKey;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Backing Bean for the admin page.
 */
@RequestScoped
@Named
public class AdminBacker {

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(AdminBacker.class);

    /**
     * The application settings cache.
     */
    private final ApplicationSettings applicationSettings;

    /**
     * The settings service providing logic.
     */
    private final SettingsService settingsService;

    /**
     * Temporary configuration being filled with user input.
     */
    private Configuration configuration;

    /**
     * Temporary organization being filled with user input.
     */
    private Organization organization;

    /**
     * Reference to the current {@link ExternalContext}.
     */
    private final ExternalContext ectx;

    /**
     * Feedback Event for user feedback.
     */
    private final Event<Feedback> feedbackEvent;

    /**
     * Resource bundle for feedback messages.
     */
    private final ResourceBundle messagesBundle;

    /**
     * Constructs a new admin page backing bean with the necessary dependencies.
     * @param applicationSettings The application settings cache.
     * @param settingsService     The settings service to use.
     * @param ectx                The current {@link ExternalContext} of the application.
     * @param feedbackEvent      The feedback event to use for user feedback.
     * @param messagesBundle     The resource bundle for feedback messages.
     */
    @Inject
    public AdminBacker(final ApplicationSettings applicationSettings,
                       final SettingsService settingsService,
                       final ExternalContext ectx, final Event<Feedback> feedbackEvent,
                       @RegistryKey("messages") final ResourceBundle messagesBundle) {
        this.applicationSettings = applicationSettings;
        this.settingsService = settingsService;
        this.ectx = ectx;
        this.feedbackEvent = feedbackEvent;
        this.messagesBundle = messagesBundle;
    }

    /**
     * Initializes temporary data holders for the application configuration and organization data.
     */
    @PostConstruct
    void init() {
        configuration = new Configuration(applicationSettings.getConfiguration());
        organization = new Organization(applicationSettings.getOrganization());
    }

    /**
     * Converts the uploaded logo into a {@code byte[]} and puts it into the temporary configuration.
     * <p>
     * This method is only called when the uploaded file actually changes.
     *
     * @param vce The event fired upon change in uploaded file.
     */
    public void uploadLogo(final ValueChangeEvent vce) {
        Part upload = (Part) vce.getNewValue();
        try {
            byte[] logo = settingsService.readFile(upload.getInputStream());
            if (logo != null) {
                organization.setLogo(logo);
            } else {
                feedbackEvent.fire(new Feedback(messagesBundle.getString("logo_upload_error"), Feedback.Type.ERROR));
            }
        } catch (IOException e) {
            log.error("Could not fetch input stream from uploaded logo.", e);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("logo_upload_error"), Feedback.Type.ERROR));
        }
    }

    /**
     * Removes the current logo of the organization.
     *
     * @param vce The event fired upon change on the selection.
     */
    public void removeLogo(final ValueChangeEvent vce) {
        if ((boolean) vce.getNewValue()) {
            organization.setLogo(new byte[0]);
        }
    }

    /**
     * Determines the available themes for skinning the application.
     *
     * @return The filenames of the available themes.
     */
    public List<String> getAvailableThemes() {
        List<String> files = settingsService.discoverFiles(ectx.getRealPath("/resources/design/themes"));
        if (files != null) {
            List<String> themes = files.stream().filter(f -> f.endsWith(".css")).collect(Collectors.toList());
            if (themes.isEmpty()) {
                themes.add(organization.getTheme()); // defensive: at least current theme for displaying
            }
            return themes;
        } else {
            feedbackEvent.fire(new Feedback(messagesBundle.getString("themes_discovery_error"), Feedback.Type.WARNING));
            return Collections.singletonList(organization.getTheme());
        }
    }

    /**
     * Saves and applies the changes made to the application configuration.
     */
    public void saveConfiguration() {
        if (settingsService.updateConfiguration(configuration)) {
            applicationSettings.setConfiguration(configuration);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("changes_successfully_saved"),
                                            Feedback.Type.INFO));
        } else {
            feedbackEvent.fire(new Feedback(messagesBundle.getString("update_failure"), Feedback.Type.ERROR));
        }
    }

    /**
     * Saves and applies the changes made to the organization data.
     */
    public void saveOrganization() {
        if (settingsService.updateOrganization(organization)) {
            applicationSettings.setOrganization(organization);
            feedbackEvent.fire(new Feedback(messagesBundle.getString("changes_successfully_saved"),
                                            Feedback.Type.INFO));
        } else {
            feedbackEvent.fire(new Feedback(messagesBundle.getString("update_failure"), Feedback.Type.ERROR));
        }
    }

    /**
     * Returns the temporary application configuration.
     *
     * @return The current configuration.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the temporary application configuration.
     *
     * @param configuration The configuration to set.
     */
    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns the temporary organization data.
     *
     * @return The current organization data.
     */
    public Organization getOrganization() {
        return organization;
    }

    /**
     * Sets the temporary organization data.
     *
     * @param organization The organization to set.
     */
    public void setOrganization(final Organization organization) {
        this.organization = organization;
    }

}
