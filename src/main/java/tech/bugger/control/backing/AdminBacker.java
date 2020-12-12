package tech.bugger.control.backing;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.ProfileService;
import tech.bugger.business.service.SettingsService;
import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.Part;
import java.util.List;

/**
 * Backing Bean for the admin page.
 */
@RequestScoped
@Named
public class AdminBacker {

    private static final Log log = Log.forClass(AdminBacker.class);

    private User user;
    private Part tempLogo;
    private Organization organization;
    private Configuration configuration;
    private List<String> availableThemes;

    @Inject
    private ApplicationSettings applicationSettings;

    @Inject
    private SettingsService settingsService;

    @Inject
    private ProfileService profileService;

    @Inject
    private UserSession session;

    @Inject
    private FacesContext fctx;

    /**
     * Initializes the admin page. Checks if the user is allowed to access the page. If not, acts as if the page did not
     * exist. Puts the names of the available CSS files into {@code availableThemes}.
     */
    @PostConstruct
    public void init() {

    }

    /**
     * Creates a FacesMessage to display if an event is fired in one of the injected services.
     *
     * @param feedback The feedback with details on what to display.
     */
    public void displayFeedback(@Observes @Any Feedback feedback) {

    }

    /**
     * Creates a new user.
     */
    public void createUser() {

    }

    /**
     * Converts the uploaded logo in {@code tempLogo} into a {@code byte[]} and puts it into {@code organization}.
     */
    public void uploadLogo() {

    }

    /**
     * Saves and applies the changes made.
     */
    public void saveSettings() {

    }

    /**
     * {@code user} holds data for a new user an administrator wants to create.
     *
     * @return The user.
     */
    public User getUser() {
        return user;
    }

    /**
     * {@code user} holds data for a new user an administrator wants to create.
     *
     * @param user The user to set.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * {@code tempLogo} temporarily holds an uploaded logo. This needs to be converted to something actually usable.
     *
     * @return The tempLogo.
     */
    public Part getTempLogo() {
        return tempLogo;
    }

    /**
     * {@code tempLogo} temporarily holds an uploaded logo. This needs to be converted to something actually usable.
     *
     * @param tempLogo The tempLogo to set.
     */
    public void setTempLogo(Part tempLogo) {
        this.tempLogo = tempLogo;
    }

    /**
     * {@code organization} holds details for modifying things like the appearance of the front end without those
     * options being already effective.
     *
     * @return The organization.
     */
    public Organization getOrganization() {
        return organization;
    }

    /**
     * {@code organization} holds details for modifying things like the appearance of the front end without those
     * options being already effective.
     *
     * @param organization The organization to set.
     */
    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * {@code configuration} holds information about system-wide moderation options without those options already being
     * in effect.
     *
     * @return The configuration.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * {@code configuration} holds information about system-wide moderation options without those options already being
     * in effect.
     *
     * @param configuration The configuration to set.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * {@code availableThemes} holds information about which themes can be selected.
     *
     * @return The availableThemes.
     */
    public List<String> getAvailableThemes() {
        return availableThemes;
    }

    /**
     * {@code availableThemes} holds information about which themes can be selected.
     *
     * @param availableThemes The availableThemes to set.
     */
    public void setAvailableThemes(List<String> availableThemes) {
        this.availableThemes = availableThemes;
    }

}
