package selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static performance.TimeCounter.startTime;
import static performance.TimeCounter.stopTime;
import static selenium.Constants.*;

@ExtendWith(SeleniumExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@Order(2)
public class UserTest {

    private String baseURL;
    private WebDriver driver;

    private String testID;

    public UserTest() {
        this.testID = "";
    }

    @BeforeEach
    public void setUp(WebDriver driver, String baseURL) {
        this.driver = driver;
        this.baseURL = baseURL;
    }

    @Test
    public void T110_insecure_direct_object_access() {
        startTime(testID);
        driver.get(baseURL + ADMIN_PAGE);
        stopTime(testID, "T110 admin");
        assertTrue(driver.getTitle().contains(NOT_FOUND_TITLE));
    }

    @Test
    public void T120_register_with_validation_errors() {
        startTime(testID);
        driver.findElement(By.id("l-go-home")).click();
        stopTime(testID, "T120 home");
        startTime(testID);
        driver.findElement(By.id("b-register")).click();
        stopTime(testID, "T120 register");
        driver.findElement(By.id("f-register:it-username")).sendKeys(ALF_USERNAME + testID);
        driver.findElement(By.id("f-register:it-first-name")).sendKeys(BEA_FIRST_NAME);
        driver.findElement(By.id("f-register:it-last-name")).sendKeys(BEA_LAST_NAME);
        startTime(testID);
        driver.findElement(By.id("f-register:cb-register")).click();
        stopTime(testID, "T120 register2");

        assertAll(
                () -> assertDoesNotThrow(() -> driver.findElement(By.id("f-register:m-username"))),
                () -> assertDoesNotThrow(() -> driver.findElement(By.className("alert-danger")))
        );
    }

    @Test
    public void T130_register_successfully() {
        RESTMail.clearEmails(BEA_EMAIL_USER + testID + EMAIL_HOST);
        driver.findElement(By.id("f-register:it-username")).clear();
        driver.findElement(By.id("f-register:it-username")).sendKeys(BEA_USERNAME + testID);
        driver.findElement(By.id("f-register:it-email")).sendKeys(BEA_EMAIL_USER + testID + EMAIL_HOST);
        startTime(testID);
        driver.findElement(By.id("f-register:cb-register")).click();
        stopTime(testID, "T130 home");

        assertDoesNotThrow(() -> driver.findElement(By.className("alert-success")));
    }

    @Test
    public void T135_set_password_with_validation_errors() {
        String confirmationURL = new FluentWait<>(BEA_EMAIL_USER + testID + EMAIL_HOST)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(AssertionError.class)
                .until(RESTMail::findLatestURL);
        RESTMail.clearEmails(BEA_EMAIL_USER + testID + EMAIL_HOST);

        startTime(testID);
        driver.get(confirmationURL);
        stopTime(testID, "T135 password-set");
        driver.findElement(By.id("f-password:it-password")).sendKeys(BEA_INSUFFICIENT_PASSWORD);
        driver.findElement(By.id("f-password:it-repeat")).sendKeys(BEA_UNMATCHING_PASSWORD);
        startTime(testID);
        driver.findElement(By.id("f-password:cb-submit")).click();
        stopTime(testID, "T135 password-set2");

        assertAll(
                () -> assertDoesNotThrow(() -> driver.findElement(By.id("f-password:m-password"))),
                () -> assertDoesNotThrow(() -> driver.findElement(By.id("f-password:m-repeat"))),
                () -> assertDoesNotThrow(() -> driver.findElement(By.className("alert-danger")))
        );
    }

    @Test
    public void T140_set_password_successfully() {
        driver.findElement(By.id("f-password:it-password")).sendKeys(BEA_PASSWORD);
        driver.findElement(By.id("f-password:it-repeat")).sendKeys(BEA_PASSWORD);
        startTime(testID);
        driver.findElement(By.id("f-password:cb-submit")).click();
        stopTime(testID, "T140 home");

        assertAll(
                () -> assertDoesNotThrow(() -> driver.findElement(By.className("alert-success"))),
                () -> assertDoesNotThrow(() -> driver.findElement(By.id("p-profile-menu-toggle")))
        );
    }

    @Test
    public void T150_change_profile() {
        new Actions(driver).moveToElement(driver.findElement(By.id("p-avatar-thumbnail"))).perform();
        startTime(testID);
        driver.findElement(By.id("l-profile")).click();
        stopTime(testID, "T150 profile");
        startTime(testID);
        driver.findElement(By.id("f-profile:l-edit")).click();
        stopTime(testID, "T150 profile-edit");

        driver.findElement(By.id("f-profile-edit:it-last-name")).sendKeys(BEA_NEW_LAST_NAME);
        driver.findElement(By.id("f-profile-edit:cb-apply")).click();
        driver.findElement(By.id("f-change-user:i-password-change")).sendKeys(BEA_PASSWORD);
        startTime(testID);
        driver.findElement(By.id("f-change-user:cb-really-change")).click();
        stopTime(testID, "T150 profile2");

        assertDoesNotThrow(() -> driver.findElement(By.className("alert-success")));
    }

    @Test
    public void T160_browse_content() {
        startTime(testID);
        driver.findElement(By.id("l-logo")).click();
        stopTime(testID, "T160 home");
        assertTrue(driver.getTitle().contains(HOME_TITLE));
        startTime(testID);
        driver.findElement(By.linkText(TOPIC_FEEDBACK + testID)).click();
        stopTime(testID, "T160 topic");
        assertTrue(driver.getTitle().contains(TOPIC_FEEDBACK + testID));
        startTime(testID);
        driver.findElement(By.id("f-topic:cb-subscribe")).click();
        stopTime(testID, "T160 topic2");

        assertDoesNotThrow(() -> driver.findElement(By.id("f-topic:cb-unsubscribe")));
    }

    @Test
    public void T170_help_popup() {
        startTime(testID);
        driver.findElement(By.id("f-help:cb-help")).click();
        stopTime(testID, "T170 topic");
        assertTrue(driver.findElement(By.id("ot-help")).getText().contains(HELP_TOPIC_TITLE));

        startTime(testID);
        driver.findElement(By.id("f-close-help:cb-close-help")).click();
        stopTime(testID, "T170 topic2");
        assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("p-help")));
    }

    @Test
    public void T180_create_report() {
        startTime(testID);
        driver.findElement(By.id("f-topic:l-create-report")).click();
        stopTime(testID, "T180 report-create");
        driver.findElement(By.id("f-create-report:it-title")).sendKeys(testID + REPORT_NO_NAME);
        driver.findElement(By.id("f-create-report:it-post-content")).sendKeys(POST_NO_NAME);
        new Select(driver.findElement(By.id("f-create-report:s-type"))).selectByValue(TYPE_BUG_OPTION);
        new Select(driver.findElement(By.id("f-create-report:s-severity"))).selectByValue(SEVERITY_MINOR_OPTION);
        startTime(testID);
        driver.findElement(By.id("f-create-report:cb-create")).click();
        stopTime(testID, "T180 report");

        assertAll(
                () -> assertDoesNotThrow(() -> driver.findElement(By.className("alert-success"))),
                () -> assertDoesNotThrow(() -> driver.findElement(By.id("f-report:cb-unsubscribe")))
        );
    }

    @Test
    public void T190_edit_report() {
        startTime(testID);
        driver.findElement(By.id("f-report:cb-edit-report")).click();
        stopTime(testID, "T190 report-edit");
        startTime(testID);
        driver.findElement(By.id("f-report-edit:s-topic")).click();
        stopTime(testID, "T190 report-edit2");
        new Select(driver.findElement(By.id("f-report-edit:s-topic"))).selectByVisibleText(TOPIC_GUI + testID);
        driver.findElement(By.id("f-report-edit:cb-submit")).click();
        startTime(testID);
        driver.findElement(By.id("f-confirm-dialog:cb-save-changes")).click();
        stopTime(testID, "T190 report");

        assertAll(
                () -> assertDoesNotThrow(() -> driver.findElement(By.className("alert-success"))),
                () -> assertTrue(driver.findElement(By.id("ot-topic")).getText().contains(TOPIC_GUI + testID))
        );
    }

    @Test
    public void T200_change_report_and_vote() {
        startTime(testID);
        driver.findElement(By.id("l-topic")).click();
        stopTime(testID, "T200 topic");
        startTime(testID);
        driver.findElement(By.linkText(testID + REPORT_NO_TRANSLATION)).click();
        stopTime(testID, "T200 report");
        startTime(testID);
        driver.findElement(By.name("f-vote:cb-upvote")).click();
        stopTime(testID, "T200 report2");

        assertEquals(REPORT_NO_TRANSLATION_RELEVANCE, driver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T210_create_post() {
        startTime(testID);
        driver.findElement(By.id("f-report:cb-add-post")).click();
        stopTime(testID, "T210 post-edit");
        driver.findElement(By.id("f-edit-post:it-content")).sendKeys(POST_SPAM);
        startTime(testID);
        driver.findElement(By.name("f-edit-post:cb-submit")).click();
        stopTime(testID, "T210 report");

        List<WebElement> elements = driver.findElements(By.cssSelector("[id*=l-post-modifier]"));
        assertEquals(BEA_LINK_TEXT + testID, elements.get(elements.size() - 1).getText());
    }

    @Test
    public void T215_logout() {
        new Actions(driver).moveToElement(driver.findElement(By.id("p-avatar-thumbnail"))).perform();
        startTime(testID);
        driver.findElement(By.id("f-logout:cb-logout")).click();
        stopTime(testID, "T215 home");

        assertDoesNotThrow(() -> driver.findElement(By.id("l-login")));
    }

    public void setTestID(String testID) {
        this.testID = testID;
    }

}
