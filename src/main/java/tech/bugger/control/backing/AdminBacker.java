package tech.bugger.control.backing;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.service.SettingsService;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.List;

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
     * Temporary configuration until submit.
     */
    private Configuration configuration;

    /**
     * Temporary organization until submit.
     */
    private Organization organization;

    /**
     * Reference to the current {@link ExternalContext}.
     */
    private final ExternalContext ectx;

    /**
     * Constructs a new admin page backing bean with the necessary dependencies.
     *
     * @param applicationSettings The application settings cache.
     * @param settingsService     The settings service to use.
     * @param ectx                The current {@link ExternalContext} of the application.
     */
    @Inject
    public AdminBacker(final ApplicationSettings applicationSettings, final SettingsService settingsService,
                       final ExternalContext ectx) {
        this.applicationSettings = applicationSettings;
        this.settingsService = settingsService;
        this.ectx = ectx;
    }

    /**
     * Initializes temporary data holders for the application configuration and organization data.
     */
    @PostConstruct
    public void init() {
        configuration = new Configuration(applicationSettings.getConfiguration());
        organization = new Organization(applicationSettings.getOrganization());
    }

    /**
     * Redirects to the place where to browse users.
     *
     * @return The direction.
     */
    public String browseUsers() {
        // TODO redirect to search page on users tab with no filters
        return null;
    }

    /**
     * Redirects to the place where to create a new user.
     *
     * @return The direction.
     */
    public String createUser() {
        // TODO redirect to edit-user.xhtml with parameters
        return null;
    }

    /**
     * Converts the uploaded logo into a {@code byte[]} and puts it into the temporary configuration.
     *
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
            }
        } catch (IOException e) {
            log.error("Could not fetch input stream from uploaded logo.", e);
        }
    }

    /**
     * Removes the current logo of the organization.
     *
     * @param vce The event fired up change on the selection.
     */
    public void removeLogo(final ValueChangeEvent vce) {
        log.debug("removeLogo: " + (boolean) vce.getNewValue());
        if ((boolean) vce.getNewValue()) {
            organization.setLogo(new byte[0]);
        }
    }

    /**
     * Saves and applies the changes made to the application configuration.
     */
    public void saveConfiguration() {
        if (settingsService.updateConfiguration(configuration)) {
            applicationSettings.setConfiguration(configuration);
        }
    }

    /**
     * Saves and applies the changes made to the organization data.
     */
    public void saveOrganization() {
        if (settingsService.updateOrganization(organization)) {
            applicationSettings.setOrganization(organization);
        }
    }

    /**
     * Determines the available themes for skinning the application.
     *
     * @return The filenames of the available themes.
     */
    public List<String> getAvailableThemes() {
        List<String> themes = settingsService.discoverFiles(ectx.getRealPath("/resources/design/themes"));
        if (themes.isEmpty()) {
            themes.add(organization.getTheme()); // at least current theme for displaying
        }
        return themes;
    }

    /**
     * Returns the temporary application configuration until submit.
     *
     * @return The current configuration.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the temporary application configuration until submit.
     *
     * @param configuration The configuration to set.
     */
    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns the temporary organization data until submit.
     *
     * @return The current organization data.
     */
    public Organization getOrganization() {
        return organization;
    }

    /**
     * Sets the temporary organization data until submit.
     *
     * @param organization The organization to set.
     */
    public void setOrganization(final Organization organization) {
        this.organization = organization;
    }

}
