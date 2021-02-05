package selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static performance.TimeCounter.startTime;
import static performance.TimeCounter.stopTime;
import static selenium.Constants.*;

@ExtendWith(SeleniumExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@Order(3)
public class ModeratorTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseURL;

    private String testID;

    public ModeratorTest() {
        this.testID = "";
    }

    @BeforeEach
    public void setUp(WebDriver driver, WebDriverWait wait, String baseURL) {
        this.driver = driver;
        this.wait = wait;
        this.baseURL = baseURL;
    }

    @Test
    public void T220_discover_notifications() {
        startTime(testID);
        driver.get(baseURL);
        stopTime(testID, "T220 home");

        // Log in as moderator.
        startTime(testID);
        driver.findElement(By.id("l-login")).click();
        stopTime(testID, "T220 login");
        driver.findElement(By.id("f-login:it-username")).sendKeys(ALF_USERNAME + testID);
        driver.findElement(By.id("f-login:it-password")).sendKeys(ALF_PASSWORD);
        startTime(testID);
        driver.findElement(By.id("f-login:cb-login")).click();
        stopTime(testID, "T220 home2");

        // Check inbox for expected notifications.
        List<WebElement> notificationButtons = driver.findElements(By.cssSelector("[id*=cb-notification-button]"));
        List<WebElement> notificationReports = driver.findElements(By.cssSelector("[id*=l-notification-report]"));

        assertEquals(EXPECTED_INBOX_SIZE, notificationButtons.size());
        assertEquals(NEW_POST_NOTIFICATION_BUTTON, notificationButtons.get(0).getAttribute("value"));
        assertTrue(notificationReports.get(0).getText().endsWith(testID + REPORT_NO_TRANSLATION));
        assertEquals(NEW_REPORT_NOTIFICATION_BUTTON, notificationButtons.get(3).getAttribute("value"));
        assertTrue(notificationReports.get(3).getText().endsWith(testID + REPORT_NO_TRANSLATION));
    }

    @Test
    public void T230_delete_post() {
        startTime(testID);
        driver.findElement(By.cssSelector("[id*=cb-notification-button][value='" + NEW_POST_NOTIFICATION_BUTTON + "']"))
                .click();
        stopTime(testID, "T230 report");
        startTime(testID);
        driver.findElements(By.cssSelector("[id*=cb-delete-post-dialog]")).get(1).click();
        driver.findElement(By.id("f-delete-post:cb-delete-post")).click();
        stopTime(testID, "T230 report2");

        assertEquals(EXPECTED_POST_NUM, driver.findElements(By.cssSelector("[id^=post]")).size());
    }

    @Test
    public void T240_overwrite_relevance() {
        driver.findElement(By.id("f-vote:i-overwrite-relevance-value")).clear();
        driver.findElement(By.id("f-vote:i-overwrite-relevance-value")).sendKeys(String.valueOf(OVERWRITING_RELEVANCE));
        startTime(testID);
        driver.findElement(By.id("f-vote:cb-overwrite-relevance")).click();
        stopTime(testID, "T240 report");

        assertEquals(String.valueOf(OVERWRITING_RELEVANCE), driver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T250_upvote() {
        startTime(testID);
        driver.findElement(By.id("f-vote:cb-upvote")).click();
        stopTime(testID, "T250 report");

        assertEquals(String.valueOf(OVERWRITING_RELEVANCE), driver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T260_undo_overwrite() {
        driver.findElement(By.id("f-vote:i-overwrite-relevance-value")).clear();
        startTime(testID);
        driver.findElement(By.name("f-vote:cb-overwrite-relevance")).click();
        stopTime(testID, "T260 report");

        assertEquals(String.valueOf(CALCULATED_RELEVANCE), driver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T270_search_report_suggestions() {
        // Search for reports and check for expected suggestions.
        driver.findElement(By.id("f-search-header:it-search")).sendKeys(testID + REPORT_SEARCH_QUERY);
        startTime(testID);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "#f-search-header\\:p-search-suggestions .search")));
        stopTime(testID, "T270 suggestions");

        List<String> suggestions = getSearchSuggestions();
        assertTrue(suggestions.size() == 2
                && suggestions.contains(testID + REPORT_NO_TRANSLATION)
                && suggestions.contains(testID + REPORT_NO_NAME));
    }

    @Test
    public void T280_search_report() {
        // Search and save search results.
        startTime(testID);
        driver.findElement(By.id("f-search-header:cb-search")).click();
        stopTime(testID, "T280 search");
        List<String> resultTitles = getSearchResultTitles();
        String originalID = driver
                .findElements(By.cssSelector("#p-tab-report-content td:nth-child(1) a"))
                .get(resultTitles.indexOf(testID + REPORT_NO_TRANSLATION))
                .getText()
                .substring(1);
        GLOBAL_VARS.put("originalID" + testID, originalID);

        // Search again with additional filters.
        driver.findElement(By.id("f-search:s-show-hint-reports")).click();
        driver.findElement(By.id("f-search:s-show-feature-reports")).click();
        startTime(testID);
        driver.findElement(By.id("f-search:cb-search-large")).click();
        stopTime(testID, "T280 search2");
        List<String> resultTitlesFiltered = getSearchResultTitles();

        // Check if search results are what we expected.
        assertAll(() -> assertTrue(resultTitles.contains(testID + REPORT_NO_TRANSLATION)),
                () -> assertTrue(resultTitles.contains(testID + REPORT_NO_NAME)),
                () -> assertEquals(Collections.singletonList(testID + REPORT_NO_NAME), resultTitlesFiltered));
    }

    @Test
    public void T290_mark_duplicate() {
        startTime(testID);
        driver.findElement(By.linkText(testID + REPORT_NO_NAME)).click();
        stopTime(testID, "T290 report");
        startTime(testID);
        driver.findElement(By.id("f-report:cb-mark-duplicate")).click();
        stopTime(testID, "T290 report2");

        String originalID = GLOBAL_VARS.get("originalID" + testID);
        driver.findElement(By.id("f-duplicate:it-duplicate")).clear();
        driver.findElement(By.id("f-duplicate:it-duplicate")).sendKeys(originalID);
        startTime(testID);
        driver.findElement(By.id("f-duplicate:cb-duplicate")).click();
        stopTime(testID, "T290 report3");

        assertAll(
                () -> assertEquals(CLOSED_AT, driver.findElement(By.id("ot-status1")).getText()),
                () -> assertEquals(driver.findElement(By.id("l-duplicate")).getText(), "#" + originalID)
        );
    }

    @Test
    public void T300_search_topic_suggestions() {
        driver.findElement(By.id("f-search-header:it-search")).sendKeys(TOPIC_GUI + testID);
        startTime(testID);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "#f-search-header\\:p-search-suggestions .search")));
        stopTime(testID, "T300 suggestions");
        assertEquals(Collections.singletonList(TOPIC_GUI + testID), getSearchSuggestions());

        startTime(testID);
        driver.findElement(By.cssSelector("#f-search-header\\:p-search-suggestions .search")).click();
        stopTime(testID, "T300 search");
        startTime(testID);
        driver.findElement(By.linkText(TOPIC_GUI + testID)).click();
        stopTime(testID, "T300 topic");

        assertTrue(driver.getTitle().contains(TOPIC_GUI + testID));
    }

    @Test
    public void T310_ban_user() {
        startTime(testID);
        driver.findElement(By.id("f-banned-status:cb-image-ban")).click();
        stopTime(testID, "T310 topic");
        driver.findElement(By.id("f-ban:it-username-ban")).sendKeys(BEA_USERNAME + testID);
        startTime(testID);
        driver.findElement(By.id("f-ban:cb-ban")).click();
        stopTime(testID, "T310 topic2");

        assertEquals(Collections.singletonList(USERNAME_PREFIX + BEA_USERNAME + testID), getBannedUsers());
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

    private List<String> getBannedUsers() {
        return driver
                .findElements(By.cssSelector("#p-banned tbody a"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public void setTestID(String testID) {
        this.testID = testID;
    }

}
