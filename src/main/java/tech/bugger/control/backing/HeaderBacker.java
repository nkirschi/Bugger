package tech.bugger.control.backing;
import tech.bugger.business.internal.UserSession;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Backing Bean for the header.
 */
@SessionScoped
@Named
public class HeaderBacker implements Serializable {
    @Serial
    private static final long serialVersionUID = 7342292657804667855L;

    /**
     * The {@link Log} instance associated with this class for logging purposes.
     */
    private static final Log log = Log.forClass(HeaderBacker.class);

    /**
     * The user behind the current UserSession.
     */
    private User user;

    /**
     * The current search Query.
     */
    private String searchQuery;

    /**
     * List of suggested Topics for the current search.
     */
    private List<Topic> topicSuggestions;

    /**
     * List of suggested Reports for the current search.
     */
    private List<Report> reportSuggestions;

    /**
     * List of suggested Users for the current search.
     */
    private List<User> userSuggestions;

    /**
     * The current UserSession.
     */
    @Inject
    private UserSession session;

    /**
     * {@code true} if the Menu should be displayed, {@code false} otherwise.
     */
    private boolean displayMenu;

    /**
     * Constructs a new header backing bean with the necessary dependencies.
     */
    @Inject
    public HeaderBacker() { }

    @PostConstruct
    private void init() {
        user = session.getUser();
        closeMenu();
    }

    /**
     * Takes the user to the search page with the current {@code searchQuery} already typed in.
     *
     * @return The location to redirect to.
     */
    public String search() {
        return null;
    }

    /**
     * Logs out the user. This also invalidates the user session.
     *
     * @return The location to redirect to.
     */
    public String logout() {
        return null;
    }

    /**
     * @return The user.
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user The user to set.
     */
    public void setUser(final User user) {
        this.user = user;
    }

    /**
     * @return The searchQuery.
     */
    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * @param searchQuery The searchQuery to set.
     */
    public void setSearchQuery(final String searchQuery) {
        this.searchQuery = searchQuery;
    }

    /**
     * @return The topicSuggestions.
     */
    public List<Topic> getTopicSuggestions() {
        return topicSuggestions;
    }

    /**
     * @param topicSuggestions The topicSuggestions to set.
     */
    public void setTopicSuggestions(final List<Topic> topicSuggestions) {
        this.topicSuggestions = topicSuggestions;
    }

    /**
     * @return The reportSuggestions.
     */
    public List<Report> getReportSuggestions() {
        return reportSuggestions;
    }

    /**
     * @param reportSuggestions The reportSuggestions to set.
     */
    public void setReportSuggestions(final List<Report> reportSuggestions) {
        this.reportSuggestions = reportSuggestions;
    }

    /**
     * @return The userSuggestions.
     */
    public List<User> getUserSuggestions() {
        return userSuggestions;
    }

    /**
     * @param userSuggestions The userSuggestions to set.
     */
    public void setUserSuggestions(final List<User> userSuggestions) {
        this.userSuggestions = userSuggestions;
    }


    /**
     * Activates/Deactivates the menu.
     *
     * @return {@code Null} to reload the page.
     */
    final public String toggleMenu() {
        if (displayMenu) {
            closeMenu();
        } else {
            openMenu();
        }
        return null;
    }

    /**
     * @return {@code true} if the Menu should be displayed, {@code false} otherwise.
     */
    final public boolean isDisplayMenu() {
        return displayMenu;
    }

    private void closeMenu() {
        displayMenu = false;
    }

    private void openMenu() {
        displayMenu = true;
    }
}
