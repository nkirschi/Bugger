package selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static selenium.Constants.*;

@ExtendWith(SeleniumExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@Order(1)
public class AdministratorTest {

    private WebDriver driver;
    private String baseURL;

    private final String testID;

    public AdministratorTest() {
        this.testID = "";
    }

    public AdministratorTest(final String testID) {
        this.testID = testID;
    }

    @BeforeEach
    public void setUp(WebDriver driver, String baseURL) {
        this.driver = driver;
        this.baseURL = baseURL;
    }

    @Test
    public void T010_login() {
        driver.get(baseURL);
        driver.findElement(By.id("l-login")).click();
        driver.findElement(By.id("f-login:it-username")).click();
        driver.findElement(By.id("f-login:it-username")).sendKeys(ADMIN_USERNAME);
        driver.findElement(By.id("f-login:it-password")).sendKeys(ADMIN_PASSWORD);
        driver.findElement(By.id("f-login:cb-login")).click();

        Select languageDropdown = new Select(driver.findElement(By.id("f-language:s-language")));
        assertAll(
                () -> assertEquals("en", languageDropdown.getFirstSelectedOption().getAttribute("value")),
                () -> assertDoesNotThrow(() -> driver.findElement(By.id("p-avatar-thumbnail")))
        );
    }

    @Test
    public void T015_administration() {
        new Actions(driver).moveToElement(driver.findElement(By.id("p-avatar-thumbnail"))).perform();
        driver.findElement(By.id("l-configuration")).click();

        assertTrue(driver.getTitle().contains(ADMIN_TITLE));
    }

    @Test
    public void T020_create_user() {
        driver.findElement(By.id("l-create")).click();
        driver.findElement(By.id("f-profile-edit:it-username")).sendKeys(ALF_USERNAME + testID);
        driver.findElement(By.id("f-profile-edit:it-first-name")).sendKeys(ALF_FIRST_NAME);
        driver.findElement(By.id("f-profile-edit:it-last-name")).sendKeys(ALF_LAST_NAME);
        driver.findElement(By.id("f-profile-edit:it-email")).sendKeys(ALF_EMAIL_USER + testID + EMAIL_HOST);
        driver.findElement(By.id("f-profile-edit:i-password-new")).sendKeys(ALF_PASSWORD);
        driver.findElement(By.id("f-profile-edit:i-password-new-repeat")).sendKeys(ALF_PASSWORD);
        driver.findElement(By.id("f-profile-edit:cb-apply")).click();

        driver.findElement(By.id("f-change-user:i-password-change")).sendKeys(ADMIN_PASSWORD);
        driver.findElement(By.id("f-change-user:cb-really-change")).click();

        assertAll(
                () -> assertTrue(driver.getTitle().contains(ALF_PROFILE_TITLE + testID)),
                () -> assertEquals(ALF_USERNAME + testID, driver.findElement(By.id("f-profile:ot-username")).getText()),
                () -> assertEquals(ALF_FIRST_NAME, driver.findElement(By.id("f-profile:ot-first-name")).getText()),
                () -> assertEquals(ALF_LAST_NAME, driver.findElement(By.id("f-profile:ot-last-name")).getText()),
                () -> assertEquals(ALF_EMAIL_USER + testID + EMAIL_HOST,
                        driver.findElement(By.id("f-profile:ot-email")).getText())
        );
    }

    @Test
    public void T030_change_voting_weight() {
        driver.findElement(By.id("f-profile:l-edit")).click();

        driver.findElement(By.id("f-profile-edit:it-overwrite-vote")).sendKeys(ALF_VOTING_WEIGHT);
        driver.findElement(By.id("f-profile-edit:cb-apply")).click();

        driver.findElement(By.id("f-change-user:i-password-change")).sendKeys(ADMIN_PASSWORD);
        driver.findElement(By.id("f-change-user:cb-really-change")).click();

        assertAll(
                () -> assertTrue(driver.getTitle().contains(ALF_PROFILE_TITLE + testID)),
                () -> assertEquals(ALF_VOTING_WEIGHT, driver.findElement(By.id("f-profile:ot-weight")).getText()),
                () -> assertDoesNotThrow(() -> driver.findElement(By.className("alert-success")))
        );
    }

    @Test
    public void T040_create_topic() {
        driver.findElement(By.id("l-logo")).click();
        driver.findElement(By.id("l-create")).click();
        driver.findElement(By.id("f-topic-edit:it-title")).sendKeys(TOPIC_FEEDBACK + testID);
        driver.findElement(By.id("f-topic-edit:it-description")).sendKeys(TOPIC_FEEDBACK_DESCRIPTION);
        driver.findElement(By.id("f-topic-edit:cb-save")).click();

        assertAll(
                () -> assertTrue(driver.getTitle().contains(TOPIC_FEEDBACK + testID)),
                () -> assertEquals(TOPIC_FEEDBACK_DESCRIPTION,
                                   driver.findElement(By.id("f-topic:ot-description")).getText())
        );
    }

    @Test
    public void T050_add_moderator() {
        driver.findElement(By.id("f-moderator-status:cb-image-promote")).click();
        driver.findElement(By.id("f-promote-mod:it-username")).sendKeys(ALF_USERNAME + testID);
        driver.findElement(By.id("f-promote-mod:cb-promote")).click();

        assertDoesNotThrow(() -> driver.findElement(By.linkText(ALF_LINK_TEXT + testID)));
    }

    @Test
    public void T060_create_topic_with_same_title() {
        driver.findElement(By.id("l-logo")).click();
        driver.findElement(By.id("l-create")).click();
        driver.findElement(By.id("f-topic-edit:it-title")).sendKeys(TOPIC_FEEDBACK + testID);
        driver.findElement(By.id("f-topic-edit:it-description")).sendKeys(TOPIC_FEEDBACK_DESCRIPTION);
        driver.findElement(By.id("f-topic-edit:cb-save")).click();

        assertDoesNotThrow(() -> driver.findElement(By.className("alert-danger")));
    }

    @Test
    public void T070_change_topic_title() {
        driver.findElement(By.id("f-topic-edit:it-title")).clear();
        driver.findElement(By.id("f-topic-edit:it-title")).sendKeys(TOPIC_GUI + testID);
        driver.findElement(By.id("f-topic-edit:cb-save")).click();
        driver.findElement(By.id("f-moderator-status:cb-image-promote")).click();
        driver.findElement(By.id("f-promote-mod:it-username")).sendKeys(ALF_USERNAME + testID);
        driver.findElement(By.id("f-promote-mod:cb-promote")).click();

        assertAll(
                () -> assertTrue(driver.findElement(By.id("title")).getText().contains(TOPIC_GUI + testID)),
                () -> assertEquals(TOPIC_FEEDBACK_DESCRIPTION,
                                   driver.findElement(By.id("f-topic:ot-description")).getText()),
                () -> assertDoesNotThrow(() -> driver.findElement(By.linkText(ALF_LINK_TEXT + testID)))
        );
    }

    @Test
    public void T080_create_report_with_invalid_attachment() {
        driver.findElement(By.id("f-topic:l-create-report")).click();
        new Select(driver.findElement(By.id("f-create-report:s-type"))).selectByValue(TYPE_HINT_OPTION);
        driver.findElement(By.id("f-create-report:it-title")).sendKeys(REPORT_NO_TRANSLATION);
        driver.findElement(By.id("f-create-report:it-post-content")).sendKeys(POST_NO_TRANSLATION);

        String file = absolutePathOf(EVIL_FILE);
        driver.findElement(By.id("f-create-report:it-attachment")).sendKeys(file);
        driver.findElement(By.id("f-create-report:cb-add-attachment")).click();

        assertDoesNotThrow(() -> driver.findElement(By.className("alert-danger")));
    }

    @Test
    public void T090_create_report_with_valid_attachment() {
        String file = absolutePathOf(FRIENDLY_FILE);
        driver.findElement(By.id("f-create-report:it-attachment")).sendKeys(file);
        driver.findElement(By.id("f-create-report:cb-create")).click();

        assertAll(
                () -> assertTrue(driver.findElement(By.id("title")).getText().contains(REPORT_NO_TRANSLATION)),
                () -> assertEquals(TYPE_HINT_TEXT, driver.findElement(By.id("ot-type")).getText()),
                () -> assertEquals(SEVERITY_MINOR_TEXT, driver.findElement(By.id("ot-severity")).getText())
        );
    }

    @Test
    public void T100_try_demote_admin() {
        new Actions(driver).moveToElement(driver.findElement(By.id("p-avatar-thumbnail"))).perform();
        driver.findElement(By.id("l-profile")).click();

        driver.findElement(By.id("f-profile:cb-rem-admin")).click();
        driver.findElement(By.id("f-change-status:i-user-password")).sendKeys(ADMIN_PASSWORD);
        driver.findElement(By.id("f-change-status:cb-change-status")).click();

        assertDoesNotThrow(() -> driver.findElement(By.className("alert-danger")));
    }

    private String absolutePathOf(String path) {
        try {
            return Path.of(ClassLoader.getSystemResource(path).toURI()).toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            throw new AssertionError("Could not determine absolute path of " + path, e);
        }
    }

}
