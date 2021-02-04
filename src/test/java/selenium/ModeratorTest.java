package selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static selenium.Constants.*;

@ExtendWith(SeleniumExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModeratorTest {


    private WebDriver driver;
    private String baseURL;
    private WebDriverWait wait;

    private String originalID = "";

    @BeforeEach
    public void setUp(WebDriver driver, String baseURL) {
        this.driver = driver;
        this.baseURL = baseURL;
        wait = new WebDriverWait(driver, 5);
    }

    @Test
    public void T220_notifications() {
        driver.get(baseURL);

        // Log in as moderator.
        driver.findElement(By.id("l-login")).click();
        driver.findElement(By.id("f-login:it-username")).click();
        driver.findElement(By.id("f-login:it-username")).sendKeys(ALF_USERNAME);
        driver.findElement(By.id("f-login:it-password")).sendKeys(ALF_PASSWORD);
        driver.findElement(By.id("f-login:cb-login")).click();

        // Check inbox for expected notifications.
        List<WebElement> notificationButtons = driver.findElements(By.cssSelector("[id*=cb-notification-button]"));
        List<WebElement> notificationReports = driver.findElements(By.cssSelector("[id*=l-notification-report]"));

        assertEquals(EXPECTED_INBOX_SIZE, notificationButtons.size());
        assertEquals(NEW_POST_NOTIFICATION_BUTTON, notificationButtons.get(0).getAttribute("value"));
        assertTrue(notificationReports.get(0).getText().endsWith(REPORT_NO_TRANSLATION));
        assertEquals(NEW_REPORT_NOTIFICATION_BUTTON, notificationButtons.get(3).getAttribute("value"));
        assertTrue(notificationReports.get(3).getText().endsWith(REPORT_NO_TRANSLATION));
    }

    @Test
    public void T230_deletePost() {
        driver.findElement(By.cssSelector("#p-notifications input[value=\"" + NEW_POST_NOTIFICATION_BUTTON + "\"]"))
                .click();
        driver.findElement(By.cssSelector("table .mb-3:nth-child(2) input[type=\"submit\"]")).click();
        driver.findElement(By.id("f-delete-post:cb-delete-post")).click();

        // Only one post should be left.
        assertEquals(EXPECTED_POST_NUM, driver.findElements(By.cssSelector("td > .mb-3")).size());
    }

    @Test
    public void T240_overwriteRelevance() {
        WebElement overwriteInput = driver.findElement(By.id("f-vote:i-overwrite-relevance-value"));
        overwriteInput.clear();
        overwriteInput.sendKeys(String.valueOf(OVERWRITING_RELEVANCE));
        driver.findElement(By.id("f-vote:cb-overwrite-relevance")).click();

        assertEquals(String.valueOf(OVERWRITING_RELEVANCE), driver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T250_upvote() {
        driver.findElement(By.id("f-vote:cb-upvote")).click();
        assertEquals(String.valueOf(OVERWRITING_RELEVANCE), driver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T260_undoOverwrite() {
        driver.findElement(By.id("f-vote:i-overwrite-relevance-value")).clear();
        driver.findElement(By.name("f-vote:cb-overwrite-relevance")).click();
        assertEquals(String.valueOf(CALCULATED_RELEVANCE), driver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T270_reportSearchSuggestions() {
        // Search for reports and check for expected suggestions.
        driver.findElement(By.id("f-search-header:it-search")).sendKeys(REPORT_SEARCH_QUERY);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "#f-search-header\\:p-search-suggestions .search")));

        List<String> suggestions = getSearchSuggestions();
        assertTrue(suggestions.size() == 2
                && suggestions.contains(REPORT_NO_TRANSLATION)
                && suggestions.contains(REPORT_NO_NAME));
    }

    @Test
    public void T280_reportSearch() {
        // Search and save search results.
        driver.findElement(By.id("f-search-header:cb-search")).click();
        List<String> resultTitles = getSearchResultTitles();
        originalID = driver.findElement(By.linkText(REPORT_NO_TRANSLATION))
                .findElement(By.xpath("./../../td[1]/a[1]"))
                .getText().substring(1);

        // Search again with additional filters.
        driver.findElement(By.id("f-search:s-show-hint-reports")).click();
        driver.findElement(By.id("f-search:s-show-feature-reports")).click();
        driver.findElement(By.id("f-search:cb-search-large")).click();
        List<String> resultTitlesFiltered = getSearchResultTitles();

        // Check if search results are what we expected.
        assertTrue(resultTitles.size() == 2
                && resultTitles.contains(REPORT_NO_TRANSLATION)
                && resultTitles.contains(REPORT_NO_NAME));
        assertEquals(Arrays.asList(REPORT_NO_NAME), resultTitlesFiltered);
    }

    @Test
    public void T290_markDuplicate() {
        driver.findElement(By.linkText(REPORT_NO_NAME)).click();
        driver.findElement(By.name("f-report:cb-mark-duplicate")).click();
        WebElement originalIDInput = driver.findElement(By.name("f-duplicate:it-duplicate"));
        originalIDInput.clear();
        originalIDInput.sendKeys(originalID);
        driver.findElement(By.name("f-duplicate:cb-duplicate")).click();

        assertEquals(driver.findElement(By.id("ot-status1")).getText(), CLOSED_AT);
        assertEquals(driver.findElement(By.id("l-duplicate")).getText(), "#" + originalID);
    }

    @Test
    public void T300_topicSearchSuggestions() {
        driver.findElement(By.id("f-search-header:it-search")).sendKeys(TOPIC_GUI);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "#f-search-header\\:p-search-suggestions .search")));
        assertEquals(Arrays.asList(TOPIC_GUI), getSearchSuggestions());

        driver.findElement(By.cssSelector("#f-search-header\\:p-search-suggestions .search")).click();
        driver.findElement(By.cssSelector("#p-tab-topic-content tbody td:first-child a")).click();
        assertTrue(driver.findElement(By.id("title")).getText().endsWith(TOPIC_GUI));
    }

    @Test
    public void T310_banUser() {
        driver.findElement(By.id("f-banned-status:cb-image-ban")).click();
        driver.findElement(By.id("f-ban:it-username-ban")).sendKeys(BEA_USERNAME);
        driver.findElement(By.id("f-ban:cb-ban")).click();

        List<String> bannedUsers = driver
                .findElements(By.cssSelector("#p-banned tbody a"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
        assertEquals(Arrays.asList(USERNAME_PREFIX + BEA_USERNAME), bannedUsers);
    }

    private List<String> getSearchSuggestions() {
        return driver
                .findElements(By.cssSelector("#f-search-header\\:p-search-suggestions > a"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    private List<String> getSearchResultTitles() {
        return driver
                .findElements(By.cssSelector("#p-tab-report-content td:nth-child(2) a"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

}
