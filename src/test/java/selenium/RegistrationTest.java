package selenium;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.FluentWait;
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

}
