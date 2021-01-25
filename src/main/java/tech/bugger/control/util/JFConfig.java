package tech.bugger.control.util;

import java.net.MalformedURLException;
import java.net.URL;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.annotation.FacesConfig;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Configuration Utility Class.
 */
@FacesConfig(version = FacesConfig.Version.JSF_2_3) // enables JF 2.3 specific features
@ApplicationScoped
public final class JFConfig {

    /**
     * Prevents instantiation of this configuration utility class.
     */
    private JFConfig() {
        throw new UnsupportedOperationException(); // for reflection abusers
    }

    /**
     * Retrieves the current application path using the current {@link ExternalContext}.
     *
     * @return The current application path.
     */
    public static String getApplicationPath() {
        return getApplicationPath(FacesContext.getCurrentInstance().getExternalContext());
    }

    /**
     * Retrieves the current application path using the given {@link ExternalContext}.
     *
     * @param ectx The {@link ExternalContext} to use.
     * @return The current application path.
     */
    public static String getApplicationPath(final ExternalContext ectx) {
        URL currentUrl;
        try {
            currentUrl = new URL(((HttpServletRequest) ectx.getRequest()).getRequestURL().toString());
        } catch (MalformedURLException e) {
            throw new InternalError("URL is invalid.", e);
        }

        return String.format("%s://%s%s", currentUrl.getProtocol(), currentUrl.getAuthority(),
                ectx.getApplicationContextPath());
    }

}
