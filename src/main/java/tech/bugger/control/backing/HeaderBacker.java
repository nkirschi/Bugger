package tech.bugger.control.backing;

import tech.bugger.business.internal.ApplicationSettings;
import tech.bugger.business.internal.UserSession;
import tech.bugger.business.service.SearchService;
import tech.bugger.global.transfer.Report;
import tech.bugger.global.transfer.Topic;
import tech.bugger.global.transfer.User;
import tech.bugger.global.util.Log;

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
    private static final Log log = Log.forClass(HeaderBacker.class);
    private User user;
    private String searchQuery;
    private List<Topic> topicSuggestions;
    private List<Report> reportSuggestions;
    private List<User> userSuggestions;

    @Inject
    private transient SearchService searchService;

    @Inject
    private UserSession session;

    @Inject
    private ApplicationSettings applicationSettings;

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
    public void setUser(User user) {
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
    public void setSearchQuery(String searchQuery) {
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
    public void setTopicSuggestions(List<Topic> topicSuggestions) {
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
    public void setReportSuggestions(List<Report> reportSuggestions) {
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
    public void setUserSuggestions(List<User> userSuggestions) {
        this.userSuggestions = userSuggestions;
    }
}
