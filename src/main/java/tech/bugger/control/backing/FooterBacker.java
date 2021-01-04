package tech.bugger.control.backing;

import tech.bugger.business.internal.UserSession;
import tech.bugger.global.transfer.Language;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;

/**
 * Backing Bean for the footer.
 */
@SessionScoped
@Named
public class FooterBacker implements Serializable {

    @Serial
    private static final long serialVersionUID = 4849101262721339096L;

    /**
     * The current Language.
     */
    private Language language;

    /**
     * The current UserSession.
     */
    @Inject
    private UserSession session;

    /**
     * Changes language. The change is effective for the whole session.
     */
    public void changeLanguage() {

    }

    /**
     * @return The language.
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * @param language The language to set.
     */
    public void setLanguage(final Language language) {
        this.language = language;
    }

}
