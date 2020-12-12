package tech.bugger.business.util;

import javax.enterprise.inject.Produces;
import java.util.ResourceBundle;

/**
 * CDI producer for convenient access to resource bundles.
 */
public class BundleProducer {
    /**
     * Returns the resource bundle with help texts.
     *
     * @return The help resource bundle.
     */
    @Produces
    @Bundle("help")
    public ResourceBundle getHelp() {
        return ResourceBundle.getBundle("tech.bugger.i18n.help");
    }

    /**
     * Returns the resource bundle with UI labels.
     *
     * @return The label resource bundle.
     */
    @Produces
    @Bundle("labels")
    public ResourceBundle getLabels() {
        return ResourceBundle.getBundle("tech.bugger.i18n.labels");
    }

    /**
     * Returns the resource bundle with feedback messages.
     *
     * @return The message resource bundle.
     */
    @Produces
    @Bundle("messages")
    public ResourceBundle getMessages() {
        return ResourceBundle.getBundle("tech.bugger.i18n.messages");
    }
}
