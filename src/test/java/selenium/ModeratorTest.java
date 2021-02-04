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
        driver.get(baseURL);

        // Log in as moderator.
        driver.findElement(By.id("l-login")).click();
        driver.findElement(By.id("f-login:it-username")).sendKeys(ALF_USERNAME + testID);
        driver.findElement(By.id("f-login:it-password")).sendKeys(ALF_PASSWORD);
        driver.findElement(By.id("f-login:cb-login")).click();

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
        driver.findElement(By.cssSelector("[id*=cb-notification-button][value='" + NEW_POST_NOTIFICATION_BUTTON + "']"))
                .click();
        driver.findElements(By.cssSelector("[id*=cb-delete-post-dialog")).get(1).click();
        driver.findElement(By.id("f-delete-post:cb-delete-post")).click();

        assertEquals(EXPECTED_POST_NUM, driver.findElements(By.cssSelector("[id^=post]")).size());
    }

    @Test
    public void T240_overwrite_relevance() {
        driver.findElement(By.id("f-vote:i-overwrite-relevance-value")).clear();
        driver.findElement(By.id("f-vote:i-overwrite-relevance-value")).sendKeys(String.valueOf(OVERWRITING_RELEVANCE));
        driver.findElement(By.id("f-vote:cb-overwrite-relevance")).click();

        assertEquals(String.valueOf(OVERWRITING_RELEVANCE), driver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T250_upvote() {
        driver.findElement(By.id("f-vote:cb-upvote")).click();
        assertEquals(String.valueOf(OVERWRITING_RELEVANCE), driver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T260_undo_overwrite() {
        driver.findElement(By.id("f-vote:i-overwrite-relevance-value")).clear();
        driver.findElement(By.name("f-vote:cb-overwrite-relevance")).click();
        assertEquals(String.valueOf(CALCULATED_RELEVANCE), driver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T270_search_report_suggestions() {
        // Search for reports and check for expected suggestions.
        driver.findElement(By.id("f-search-header:it-search")).sendKeys(testID + REPORT_SEARCH_QUERY);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "#f-search-header\\:p-search-suggestions .search")));

        List<String> suggestions = getSearchSuggestions();
        assertTrue(suggestions.size() == 2
                && suggestions.contains(testID + REPORT_NO_TRANSLATION)
                && suggestions.contains(testID + REPORT_NO_NAME));
    }

    @Test
    public void T280_search_report() {
        // Search and save search results.
        driver.findElement(By.id("f-search-header:cb-search")).click();
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
        driver.findElement(By.id("f-search:cb-search-large")).click();
        List<String> resultTitlesFiltered = getSearchResultTitles();

        // Check if search results are what we expected.
        assertAll(() -> assertTrue(resultTitles.contains(testID + REPORT_NO_TRANSLATION)),
                () -> assertTrue(resultTitles.contains(testID + REPORT_NO_NAME)),
                () -> assertEquals(Collections.singletonList(testID + REPORT_NO_NAME), resultTitlesFiltered));
    }

    @Test
    public void T290_mark_duplicate() {
        driver.findElement(By.linkText(testID + REPORT_NO_NAME)).click();
        driver.findElement(By.id("f-report:cb-mark-duplicate")).click();

        String originalID = GLOBAL_VARS.get("originalID" + testID);
        driver.findElement(By.id("f-duplicate:it-duplicate")).clear();
        driver.findElement(By.id("f-duplicate:it-duplicate")).sendKeys(originalID);
        driver.findElement(By.id("f-duplicate:cb-duplicate")).click();

        assertAll(
                () -> assertEquals(CLOSED_AT, driver.findElement(By.id("ot-status1")).getText()),
                () -> assertEquals(driver.findElement(By.id("l-duplicate")).getText(), "#" + originalID)
        );
    }

    @Test
    public void T300_search_topic_suggestions() {
        driver.findElement(By.id("f-search-header:it-search")).sendKeys(TOPIC_GUI + testID);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                "#f-search-header\\:p-search-suggestions .search")));
        assertEquals(Collections.singletonList(TOPIC_GUI + testID), getSearchSuggestions());

        driver.findElement(By.cssSelector("#f-search-header\\:p-search-suggestions .search")).click();
        driver.findElement(By.linkText(TOPIC_GUI + testID)).click();
        assertTrue(driver.getTitle().contains(TOPIC_GUI + testID));
    }

    @Test
    public void T310_ban_user() {
        driver.findElement(By.id("f-banned-status:cb-image-ban")).click();
        driver.findElement(By.id("f-ban:it-username-ban")).sendKeys(BEA_USERNAME + testID);
        driver.findElement(By.id("f-ban:cb-ban")).click();

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
