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
import static selenium.Constants.*;

@ExtendWith(SeleniumExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@Order(2)
public class UserTest {

    private String baseURL;
    private WebDriver driver;

    @BeforeEach
    public void setUp(WebDriver driver, String baseURL) {
        this.driver = driver;
        this.baseURL = baseURL;
    }

    @Test
    public void T110_insecure_direct_object_access() {
        driver.get(baseURL + ADMIN_PAGE);
        assertTrue(driver.getTitle().contains(NOT_FOUND_TITLE));
    }

    @Test
    public void T120_register_with_validation_errors() {
        driver.get(baseURL + REGISTER_PAGE); // TODO: no header on error page :(
        driver.findElement(By.id("f-register:it-username")).sendKeys(ALF_USERNAME);
        driver.findElement(By.id("f-register:it-first-name")).sendKeys(BEA_FIRST_NAME);
        driver.findElement(By.id("f-register:it-last-name")).sendKeys(BEA_LAST_NAME);
        driver.findElement(By.id("f-register:cb-register")).click();

        assertAll(
                () -> assertDoesNotThrow(() -> driver.findElement(By.id("f-register:m-username"))),
                () -> assertDoesNotThrow(() -> driver.findElement(By.className("alert-danger")))
        );
    }

    @Test
    public void T130_register_successfully() {
        RESTMail.clearEmails(EMAIL_USER);
        driver.findElement(By.id("f-register:it-username")).clear();
        driver.findElement(By.id("f-register:it-username")).sendKeys(BEA_USERNAME);
        driver.findElement(By.id("f-register:it-email")).sendKeys(EMAIL_USER + EMAIL_HOST);
        driver.findElement(By.id("f-register:cb-register")).click();

        assertDoesNotThrow(() -> driver.findElement(By.className("alert-success")));
    }

    @Test
    public void T135_set_password_with_validation_errors() {
        String confirmationURL = new FluentWait<>(EMAIL_USER)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(AssertionError.class)
                .until(RESTMail::findLatestURL);
        RESTMail.clearEmails(EMAIL_USER);

        driver.get(confirmationURL);
        driver.findElement(By.id("f-password:it-password")).sendKeys(BEA_INSUFFICIENT_PASSWORD);
        driver.findElement(By.id("f-password:it-repeat")).sendKeys(BEA_UNMATCHING_PASSWORD);
        driver.findElement(By.id("f-password:cb-submit")).click();

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
        driver.findElement(By.id("f-password:cb-submit")).click();

        assertAll(
                () -> assertDoesNotThrow(() -> driver.findElement(By.className("alert-success"))),
                () -> assertDoesNotThrow(() -> driver.findElement(By.id("p-profile-menu-toggle")))
        );
    }

    @Test
    public void T150_change_profile() {
        new Actions(driver).moveToElement(driver.findElement(By.id("p-avatar-thumbnail"))).perform();
        driver.findElement(By.id("l-profile")).click();
        driver.findElement(By.id("f-profile:l-edit")).click();

        driver.findElement(By.id("f-profile-edit:it-last-name")).sendKeys(BEA_NEW_LAST_NAME);
        driver.findElement(By.id("f-profile-edit:cb-apply")).click();
        driver.findElement(By.id("f-change-user:i-password-change")).sendKeys(BEA_PASSWORD);
        driver.findElement(By.id("f-change-user:cb-really-change")).click();

        assertDoesNotThrow(() -> driver.findElement(By.className("alert-success")));
    }

    @Test
    public void T160_browse_content() {
        driver.findElement(By.id("l-logo")).click();
        assertTrue(driver.getTitle().contains(HOME_TITLE));
        driver.findElement(By.linkText(TOPIC_FEEDBACK)).click();
        assertTrue(driver.getTitle().contains(TOPIC_FEEDBACK));
        driver.findElement(By.id("f-topic:cb-subscribe")).click();

        assertDoesNotThrow(() -> driver.findElement(By.id("f-topic:cb-unsubscribe")));
    }

    @Test
    public void T170_help_popup() {
        driver.findElement(By.id("f-help:cb-help")).click();
        assertTrue(driver.findElement(By.id("ot-help")).getText().contains(HELP_TOPIC_TITLE));

        driver.findElement(By.id("f-close-help:cb-close-help")).click();
        assertThrows(NoSuchElementException.class, () -> driver.findElement(By.id("p-help")));
    }

    @Test
    public void T180_create_report() {
        driver.findElement(By.id("f-topic:l-create-report")).click();
        driver.findElement(By.id("f-create-report:it-title")).sendKeys(REPORT_NO_NAME);
        driver.findElement(By.id("f-create-report:it-post-content")).sendKeys(POST_NO_NAME);
        new Select(driver.findElement(By.id("f-create-report:s-type"))).selectByValue(TYPE_BUG_OPTION);
        new Select(driver.findElement(By.id("f-create-report:s-severity"))).selectByValue(SEVERITY_MINOR_OPTION);
        driver.findElement(By.id("f-create-report:cb-create")).click();

        assertAll(
                () -> assertDoesNotThrow(() -> driver.findElement(By.className("alert-success"))),
                () -> assertDoesNotThrow(() -> driver.findElement(By.id("f-report:cb-unsubscribe")))
        );
    }

    @Test
    public void T190_edit_report() {
        driver.findElement(By.id("f-report:cb-edit-report")).click();
        driver.findElement(By.id("f-report-edit:s-topic")).click();
        new Select(driver.findElement(By.id("f-report-edit:s-topic"))).selectByVisibleText(TOPIC_GUI);
        driver.findElement(By.id("f-report-edit:cb-submit")).click();
        driver.findElement(By.id("f-confirm-dialog:cb-save-changes")).click();

        assertAll(
                () -> assertDoesNotThrow(() -> driver.findElement(By.className("alert-success"))),
                () -> assertTrue(driver.findElement(By.id("ot-topic")).getText().contains(TOPIC_GUI))
        );
    }

    @Test
    public void T200_change_report_and_vote() {
        driver.findElement(By.id("l-topic")).click();
        driver.findElement(By.linkText(REPORT_NO_TRANSLATION)).click();
        driver.findElement(By.name("f-vote:cb-upvote")).click();

        assertEquals(REPORT_NO_TRANSLATION_RELEVANCE, driver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T210_create_post() {
        driver.findElement(By.id("f-report:cb-add-post")).click();
        driver.findElement(By.id("f-edit-post:it-content")).sendKeys(POST_SPAM);
        driver.findElement(By.name("f-edit-post:cb-submit")).click();

        List<WebElement> elements = driver.findElements(By.cssSelector("[id*=l-post-modifier]"));
        assertEquals(BEA_LINK_TEXT, elements.get(elements.size() - 1).getText());
    }

}
