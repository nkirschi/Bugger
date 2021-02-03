package selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SeleniumExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class RegistrationTest {

    private String baseURL;
    private WebDriver driver;

    private static final String USERNAME = "BeaDieBiene";
    private static final String EMAIL_HOST = "restmail.net";

    @BeforeEach
    public void setUp() {
        baseURL = SeleniumExtension.getBaseURL();
        driver = SeleniumExtension.getDriver();
    }

    @Test
    public void T110_insecure_direct_object_access() {
        driver.get(baseURL + "admin");

        assertTrue(driver.getTitle().toLowerCase().contains("not found"));
    }

    @Test
    public void T120_register_with_validation_errors() {
        driver.get(baseURL + "register"); // TODO: no header on error page :(
        driver.findElement(By.id("f-register:it-username")).sendKeys("AlfDerBenutzer");
        driver.findElement(By.id("f-register:it-first-name")).sendKeys("Bea");
        driver.findElement(By.id("f-register:it-last-name")).sendKeys("Baum");
        driver.findElement(By.id("f-register:cb-register")).click();

        assertDoesNotThrow(() -> driver.findElement(By.cssSelector(".alert-danger")));
    }

    @Test
    public void T130_register_successfully() {
        driver.findElement(By.id("f-register:it-username")).clear();
        driver.findElement(By.id("f-register:it-username")).sendKeys(USERNAME);
        driver.findElement(By.id("f-register:it-email")).sendKeys(USERNAME + "@" + EMAIL_HOST);
        RESTMail.clearEmails(USERNAME.toLowerCase());
        driver.findElement(By.id("f-register:cb-register")).click();

        assertDoesNotThrow(() -> driver.findElement(By.cssSelector(".alert-success")));
    }

    @Test
    public void T135_set_password_with_validation_errors() {
        Wait<String> wait = new FluentWait<>(USERNAME.toLowerCase())
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(AssertionError.class);
        driver.get(wait.until(RESTMail::findLatestURL));
        RESTMail.clearEmails(USERNAME.toLowerCase());
        driver.findElement(By.id("f-password:it-password")).sendKeys("EineKuriositaet");
        driver.findElement(By.id("f-password:it-repeat")).sendKeys("EineKuriosität");
        driver.findElement(By.id("f-password:cb-submit")).click();

        assertDoesNotThrow(() -> driver.findElement(By.cssSelector(".alert-danger")));
    }

    @Test
    public void T140_set_password_successfully() {
        driver.findElement(By.id("f-password:it-password")).sendKeys("EineKuri0sitaet!");
        driver.findElement(By.id("f-password:it-repeat")).sendKeys("EineKuri0sitaet!");
        driver.findElement(By.id("f-password:cb-submit")).click();

        assertAll(
                () -> assertDoesNotThrow(() -> driver.findElement(By.cssSelector(".alert-success"))),
                () -> assertDoesNotThrow(() -> driver.findElement(By.id("p-profile-menu-toggle")))
        );
    }

    @Test
    public void T150_change_profile() {
        new Actions(driver).moveToElement(driver.findElement(By.id("p-avatar-thumbnail"))).perform();
        driver.findElement(By.id("l-profile")).click();
        driver.findElement(By.id("f-profile:l-edit")).click();

        SeleniumExtension.scrollTo(driver.findElement(By.id("f-profile-edit:it-last-name"))).sendKeys("Blume");
        SeleniumExtension.scrollTo(driver.findElement(By.id("f-profile-edit:cb-apply"))).click();
        SeleniumExtension.scrollTo(driver.findElement(By.id("f-change-user:i-password-change")))
                         .sendKeys("EineKuri0sitaet!");
        SeleniumExtension.scrollTo(driver.findElement(By.id("f-change-user:cb-really-change"))).click();

        assertDoesNotThrow(() -> driver.findElement(By.className("alert-success")));
    }

    @Test
    public void T160_browse_content() {
        driver.findElement(By.id("l-logo")).click();
        assertTrue(driver.getTitle().toLowerCase().contains("home"));
        driver.findElement(By.linkText("Reversi: Lob und Tadel")).click();
        assertTrue(driver.getTitle().contains("Reversi: Lob und Tadel"));
        driver.findElement(By.id("f-topic:cb-subscribe")).click();
        // TODO assert unsubscribe text
    }

    @Test
    public void T170_help() {
        // TODO stuff missing
        driver.findElement(By.id("f-help:cb-help")).click();
        assertDoesNotThrow(() -> driver.findElement(By.id("p-help")));
        driver.findElement(By.id("f-close-help:cb-close-help")).click();
    }

    @Test
    public void T180_create_report() {
        driver.findElement(By.id("f-topic:l-create-report")).click();
        driver.findElement(By.id("f-create-report:it-title")).sendKeys("Button hat keinen Namen");
        driver.findElement(By.id("f-create-report:it-post-content")).sendKeys(
                "Wenn ich auf englische Sprache stelle, hat der \"Zug rückgängig machen\""
                        + " Button keine Beschreibung mehr.");
        new Select(driver.findElement(By.id("f-create-report:s-type"))).selectByValue("BUG");
        new Select(driver.findElement(By.id("f-create-report:s-severity"))).selectByValue("MINOR");
        SeleniumExtension.scrollTo(driver.findElement(By.id("f-create-report:cb-create"))).click();

        assertAll(
                () -> assertDoesNotThrow(() -> driver.findElement(By.cssSelector(".alert-success"))),
                () -> assertDoesNotThrow(() -> driver.findElement(By.id("f-report:cb-unsubscribe")))
        );
    }

    @Test
    public void T190_edit_report() {
        driver.findElement(By.id("f-report:cb-edit-report")).click();
        driver.findElement(By.id("f-report-edit:s-topic")).click();
        new Select(driver.findElement(By.id("f-report-edit:s-topic"))).selectByVisibleText("Reversi: Grafikoberfläche");
        driver.findElement(By.id("f-report-edit:cb-submit")).click();
        driver.findElement(By.id("f-confirm-dialog:cb-save-changes")).click();

        assertTrue(driver.findElement(By.id("ot-topic")).getText().contains("Reversi: Grafikoberfläche"));
    }

    @Test
    public void T200_change_report_and_vote() {
        driver.findElement(By.id("l-topic")).click();
        driver.findElement(By.linkText("Button Übersetzung fehlt")).click();
        driver.findElement(By.name("f-vote:cb-upvote")).click();

        assertEquals("1", driver.findElement(By.id("ot-relevance")).getText());
    }

    @Test
    public void T210_create_post() {
        driver.findElement(By.id("f-report:cb-add-post")).click();
        driver.findElement(By.id("f-edit-post:it-content")).sendKeys(
                "Oh, das wisst ihr ja schon, das habe ich eben nochmal geschrieben.");
        driver.findElement(By.name("f-edit-post:j_idt72")).click();

        // TODO fish element
        /*assertEquals("@BeaDieBiene",
                     driver.findElement(By.id("j_idt150:f-table:j_idt189:1:l-post-modifier")).getText());*/
    }

}
