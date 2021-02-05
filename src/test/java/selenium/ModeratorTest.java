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

import static performance.TimeCounter.startTime;
import static performance.TimeCounter.stopTime;

import static org.junit.jupiter.api.Assertions.*;
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

        // Log in as moderator.
        driver.findElement(By.id("l-login")).click();
        stopTime(testID, "T220 moderator home");
        startTime(testID);
        driver.findElement(By.id("f-login:it-username")).sendKeys(ALF_USERNAME + testID);
        stopTime(testID, "T220 moderator login");
        startTime(testID);
        driver.findElement(By.id("f-login:it-password")).sendKeys(ALF_PASSWORD);
        driver.findElement(By.id("f-login:cb-login")).click();

        // Check inbox for expected notifications.
        List<WebElement> notificationButtons = driver.findElements(By.cssSelector("[id*=cb-notification-button]"));
        List<WebElement> notificationReports = driver.findElements(By.cssSelector("[id*=l-notification-report]"));
        stopTime(testID, "T220 hom2");

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
        stopTime(testID, "T230 load post");
        startTime(testID);
        driver.findElements(By.cssSelector("[id*=cb-delete-post-dialog]")).get(1).click();
        driver.findElement(By.id("f-delete-post:cb-delete-post")).click();

        assertEquals(EXPECTED_POST_NUM, driver.findElements(By.cssSelector("[id^=post]")).size());
        stopTime(testID, "T230 delete post");
    }

    @Test
    public void T240_overwrite_relevance() {
        startTime(testID);
        driver.findElement(By.id("f-vote:i-overwrite-relevance-value")).clear();
        driver.findElement(By.id("f-vote:i-overwrite-relevance-value")).sendKeys(String.valueOf(OVERWRITING_RELEVANCE));
        driver.findElement(By.id("f-vote:cb-overwrite-relevance")).click();

        assertEquals(String.valueOf(OVERWRITING_RELEVANCE), driver.findElement(By.id("ot-relevance")).getText());
        stopTime(testID, "T240 overwrite releveance");
    }

    @Test
    public void T250_upvote() {
        startTime(testID);
        driver.findElement(By.id("f-vote:cb-upvote")).click();
        assertEquals(String.valueOf(OVERWRITING_RELEVANCE), driver.findElement(By.id("ot-relevance")).getText());
        stopTime(testID, "T250 upvote");
    }

    @Test
    public void T260_undo_overwrite() {
        startTime(testID);
        driver.findElement(By.id("f-vote:i-overwrite-relevance-value")).clear();
        driver.findElement(By.name("f-vote:cb-overwrite-relevance")).click();
        assertEquals(String.valueOf(CALCULATED_RELEVANCE), driver.findElement(By.id("ot-relevance")).getText());
        stopTime(testID, "T260 undo overwrite");
    }

    @Test
    public void T270_search_report_suggestions() {
        startTime(testID);
        // Search for reports and check for expected suggestions.
        driver.findElement(By.id("f-search-header:it-search")).sendKeys(testID + REPORT_SEARCH_QUERY);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "#f-search-header\\:p-search-suggestions .search")));

        List<String> suggestions = getSearchSuggestions();
        assertTrue(suggestions.size() == 2
                && suggestions.contains(testID + REPORT_NO_TRANSLATION)
                && suggestions.contains(testID + REPORT_NO_NAME));
        stopTime(testID, "T270 search report suggestions");
    }

    @Test
    public void T280_search_report() {
        startTime(testID);
        // Search and save search results.
        driver.findElement(By.id("f-search-header:cb-search")).click();
        List<String> resultTitles = getSearchResultTitles();
        stopTime(testID, "T280 search results for report");
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
        List<String> resultTitlesFiltered = getSearchResultTitles();
        stopTime(testID, "T280 filter results");

        // Check if search results are what we expected.
        assertAll(() -> assertTrue(resultTitles.contains(testID + REPORT_NO_TRANSLATION)),
                () -> assertTrue(resultTitles.contains(testID + REPORT_NO_NAME)),
                () -> assertEquals(Collections.singletonList(testID + REPORT_NO_NAME), resultTitlesFiltered));
    }

    @Test
    public void T290_mark_duplicate() {
        startTime(testID);
        driver.findElement(By.linkText(testID + REPORT_NO_NAME)).click();
        driver.findElement(By.id("f-report:cb-mark-duplicate")).click();
        stopTime(testID, "T290 find duplicate button");

        String originalID = GLOBAL_VARS.get("originalID" + testID);
        driver.findElement(By.id("f-duplicate:it-duplicate")).clear();
        driver.findElement(By.id("f-duplicate:it-duplicate")).sendKeys(originalID);
        startTime(testID);
        driver.findElement(By.id("f-duplicate:cb-duplicate")).click();

        assertAll(
                () -> assertEquals(CLOSED_AT, driver.findElement(By.id("ot-status1")).getText()),
                () -> assertEquals(driver.findElement(By.id("l-duplicate")).getText(), "#" + originalID)
        );
        stopTime(testID, "T290 mark duplicate");
    }

    @Test
    public void T300_search_topic_suggestions() {
        startTime(testID);
        driver.findElement(By.id("f-search-header:it-search")).sendKeys(TOPIC_GUI + testID);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "#f-search-header\\:p-search-suggestions .search")));
        assertEquals(Collections.singletonList(TOPIC_GUI + testID), getSearchSuggestions());
        stopTime(testID, "T300 search topic suggestions");

        startTime(testID);
        driver.findElement(By.cssSelector("#f-search-header\\:p-search-suggestions .search")).click();
        driver.findElement(By.linkText(TOPIC_GUI + testID)).click();
        assertTrue(driver.getTitle().contains(TOPIC_GUI + testID));
        stopTime(testID, "T300 navigate to topic");
    }

    @Test
    public void T310_ban_user() {
        startTime(testID);
        driver.findElement(By.id("f-banned-status:cb-image-ban")).click();
        driver.findElement(By.id("f-ban:it-username-ban")).sendKeys(BEA_USERNAME + testID);
        stopTime(testID, "T310 ban user overlay");
        startTime(testID);
        driver.findElement(By.id("f-ban:cb-ban")).click();

        assertEquals(Collections.singletonList(USERNAME_PREFIX + BEA_USERNAME + testID), getBannedUsers());
        stopTime(testID, "T310 ban user");
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
