package tech.bugger.business.util;

import tech.bugger.business.internal.UserSession;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ResourceBundle;

/**
 * CDI producer for convenient access to resource bundles.
 */
public class BundleProducer {
    private final UserSession userSession;

    @Inject
    public BundleProducer(UserSession userSession) {
        this.userSession = userSession;
    }

    /**
     * Returns the resource bundle with help texts.
     *
     * @return The help resource bundle.
     */
    @Produces
    @Bundle("help")
    public ResourceBundle getHelp() {
        return ResourceBundle.getBundle("tech.bugger.i18n.help", userSession.getLocale());

    }

    /**
     * Returns the resource bundle with UI labels.
     *
     * @return The label resource bundle.
     */
    @Produces
    @Bundle("labels")
    public ResourceBundle getLabels() {
        return ResourceBundle.getBundle("tech.bugger.i18n.labels", userSession.getLocale());
    }

    /**
     * Returns the resource bundle with feedback messages.
     *
     * @return The message resource bundle.
     */
    @Produces
    @Bundle("messages")
    public ResourceBundle getMessages() {
        return ResourceBundle.getBundle("tech.bugger.i18n.messages", userSession.getLocale());
    }
}
