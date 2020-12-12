package tech.bugger.business.service;

import tech.bugger.business.util.Feedback;
import tech.bugger.global.transfer.Configuration;
import tech.bugger.global.transfer.Organization;
import tech.bugger.global.util.Log;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

/**
 * Service providing methods related to general settings of the application. A {@code Feedback} event is fired, if
 * unexpected circumstances occur.
 */
@Dependent
public class SettingsService {
    private static final Log log = Log.forClass(SettingsService.class);

    @Inject
    @Any
    Event<Feedback> feedback;

    /**
     * Updates the organization settings in the data storage.
     *
     * @param organization The new organization settings.
     */
    public void updateOrganization(Organization organization) {
    }

    /**
     * Updates the configuration in the data storage.
     *
     * @param configuration The new configuration.
     */
    public void updateConfiguration(Configuration configuration) {

    }
}
