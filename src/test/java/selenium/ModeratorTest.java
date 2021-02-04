package selenium;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SeleniumExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModeratorTest {

    private static final String MODERATOR_USERNAME = "AlfDerBenutzer";
    private static final String MODERATOR_PASSWORD = "Welten-Mysterium1";

    private static final String OVERWRITING_RELEVANCE = "42";
    private static final String CALCULATED_RELEVANCE = "6";
    private static final int EXPECTED_INBOX_SIZE = 4;
    private static final int EXPECTED_POST_NUM = 1;

    private static final String REPORT_SEARCH_QUERY = "Button";
    private static final String ORIGINAL_TITLE = "Button Übersetzung fehlt";
    private static final String DUPLICATE_TITLE = "Button hat keinen Namen";
    private static final String TOPIC_TITLE = "Reversi: Grafikoberfläche";
    private static final String USER_TO_BAN = "BeaDieBiene";

    private static final String USERNAME_PREFIX = "@";
    private static final String CLOSED_AT = "closed at";
    private static final String NEW_POST_NOTIFICATION_BUTTON = "New post";
    private static final String NEW_REPORT_NOTIFICATION_BUTTON = "New report";

    private WebDriver webDriver;
    private WebDriverWait wait;

    private String originalID = "";

    @BeforeAll
    public void setUp() {
        webDriver = SeleniumExtension.getDriver();
        webDriver.get(SeleniumExtension.getBaseURL());
        wait = new WebDriverWait(webDriver, 5);
    }

    @Test
    public void T220_notifications() {
        // Log in as moderator.
        webDriver.findElement(By.id("l-login")).click();
        webDriver.findElement(By.id("f-login:it-username")).click();
        webDriver.findElement(By.id("f-login:it-username")).sendKeys(MODERATOR_USERNAME);
        webDriver.findElement(By.id("f-login:it-password")).sendKeys(MODERATOR_PASSWORD);
        webDriver.findElement(By.id("f-login:cb-login")).click();

        // Check inbox for expected notifications.
        List<WebElement> notificationButtons = webDriver.findElements(By.cssSelector("[id*=cb-notification-button]"));
        List<WebElement> notificationReports = webDriver.findElements(By.cssSelector("[id*=cb-notification-report]"));
        assertEquals(EXPECTED_INBOX_SIZE, notificationButtons.size());
        assertEquals(NEW_POST_NOTIFICATION_BUTTON, notificationButtons.get(0).getAttribute("value"));
        assertTrue(notificationReports.get(0).getText().endsWith(ORIGINAL_TITLE));
        assertEquals(NEW_REPORT_NOTIFICATION_BUTTON, notificationButtons.get(3).getAttribute("value"));
        assertTrue(notificationReports.get(3).getText().endsWith(ORIGINAL_TITLE));
    }

    @Test
    public void T230_deletePost() {
        webDriver.findElement(By.cssSelector("#p-notifications input[value=\"" + NEW_POST_NOTIFICATION_BUTTON + "\"]"))
                .click();
        SeleniumExtension
                .scrollTo(webDriver.findElement(By.cssSelector("table .mb-3:nth-child(2) input[type=\"submit\"]")))
                .click();
        webDriver.findElement(By.id("f-delete-post:cb-delete-post")).click();

        // Only one post should be left.
        assertEquals(EXPECTED_POST_NUM, webDriver.findElements(By.cssSelector("td > .mb-3")).size());
    }

    @Test
    public void T240_overwriteRelevance() {
        WebElement overwriteInput = webDriver.findElement(By.id("f-vote:i-overwrite-relevance-value"));
        overwriteInput.clear();
        overwriteInput.sendKeys(OVERWRITING_RELEVANCE);
        webDriver.findElement(By.id("f-vote:cb-overwrite-relevance")).click();

        assertEquals(OVERWRITING_RELEVANCE, webDriver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T250_upvote() {
        webDriver.findElement(By.id("f-vote:cb-upvote")).click();
        assertEquals(OVERWRITING_RELEVANCE, webDriver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T260_undoOverwrite() {
        webDriver.findElement(By.id("f-vote:i-overwrite-relevance-value")).clear();
        webDriver.findElement(By.name("f-vote:cb-overwrite-relevance")).click();
        assertEquals(CALCULATED_RELEVANCE, webDriver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T270_reportSearchSuggestions() {
        // Search for reports and check for expected suggestions.
        webDriver.findElement(By.id("f-search-header:it-search")).sendKeys(REPORT_SEARCH_QUERY);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "#f-search-header\\:p-search-suggestions .search")));

        List<String> suggestions = getSearchSuggestions();
        assertTrue(suggestions.size() == 2
                && suggestions.contains(ORIGINAL_TITLE)
                && suggestions.contains(DUPLICATE_TITLE));
    }

    @Test
    public void T280_reportSearch() {
        // Search and save search results.
        webDriver.findElement(By.id("f-search-header:cb-search")).click();
        List<String> resultTitles = getSearchResultTitles();
        originalID = webDriver.findElement(By.linkText(ORIGINAL_TITLE))
                .findElement(By.xpath("./../../td[1]/a[1]"))
                .getText().substring(1);

        // Search again with additional filters.
        webDriver.findElement(By.id("f-search:s-show-hint-reports")).click();
        webDriver.findElement(By.id("f-search:s-show-feature-reports")).click();
        webDriver.findElement(By.id("f-search:cb-search-large")).click();
        List<String> resultTitlesFiltered = getSearchResultTitles();

        // Check if search results are what we expected.
        assertTrue(resultTitles.size() == 2
                && resultTitles.contains(ORIGINAL_TITLE)
                && resultTitles.contains(DUPLICATE_TITLE));
        assertEquals(Arrays.asList(DUPLICATE_TITLE), resultTitlesFiltered);
    }

    @Test
    public void T290_markDuplicate() {
        webDriver.findElement(By.linkText(DUPLICATE_TITLE)).click();
        webDriver.findElement(By.name("f-report:cb-mark-duplicate")).click();
        WebElement originalIDInput = webDriver.findElement(By.name("f-duplicate:it-duplicate"));
        originalIDInput.clear();
        originalIDInput.sendKeys(originalID);
        webDriver.findElement(By.name("f-duplicate:cb-duplicate")).click();

        assertEquals(webDriver.findElement(By.id("ot-status1")).getText(), CLOSED_AT);
        assertEquals(webDriver.findElement(By.id("l-duplicate")).getText(), "#" + originalID);
    }

    @Test
    public void T300_topicSearchSuggestions() {
        webDriver.findElement(By.id("f-search-header:it-search")).sendKeys(TOPIC_TITLE);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "#f-search-header\\:p-search-suggestions .search")));
        assertEquals(Arrays.asList(TOPIC_TITLE), getSearchSuggestions());

        webDriver.findElement(By.cssSelector("#f-search-header\\:p-search-suggestions .search")).click();
        webDriver.findElement(By.cssSelector("#p-tab-topic-content tbody td:first-child a")).click();
        assertTrue(webDriver.findElement(By.id("title")).getText().endsWith(TOPIC_TITLE));
    }

    @Test
    public void T310_banUser() {
        webDriver.findElement(By.id("f-banned-status:cb-image-ban")).click();
        webDriver.findElement(By.id("f-ban:it-username-ban")).sendKeys(USER_TO_BAN);
        webDriver.findElement(By.id("f-ban:cb-ban")).click();

        List<String> bannedUsers = webDriver
                .findElements(By.cssSelector("#p-banned tbody a"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(USERNAME_PREFIX + USER_TO_BAN), bannedUsers);
    }

    private List<String> getSearchSuggestions() {
        return webDriver
                .findElements(By.cssSelector("#f-search-header\\:p-search-suggestions > a"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    private List<String> getSearchResultTitles() {
        return webDriver
                .findElements(By.cssSelector("#p-tab-report-content td:nth-child(2) a"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

}
