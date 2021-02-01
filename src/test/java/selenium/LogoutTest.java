package selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import tech.bugger.LogExtension;

import java.util.HashMap;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DriverExtension.class)
@ExtendWith(LogExtension.class)
public class LogoutTest {

    private WebDriver webDriver;
    private static String baseURL;
    private JavascriptExecutor js;
    private HashMap<String, Object> vars;

    public void run() {
        new DriverExtension();
        DriverExtension.setDriverType(DriverExtension.getDriverType());
        setUp();
        logoutTest();
        webDriver.close();
    }

    @BeforeEach
    public void setUp() {
        baseURL = DriverExtension.getBaseURL();
        webDriver = DriverExtension.getDriver();
        js = (JavascriptExecutor) webDriver;
        vars = new HashMap<>();
    }

    @AfterEach
    public void tearDown() {
        webDriver.close();
    }

    @Test
    public void logoutTest() {
        webDriver.get(baseURL);
        webDriver.findElement(By.id("l-login")).click();
        webDriver.findElement(By.id("f-login:it-username")).click();
        webDriver.findElement(By.id("f-login:it-username")).sendKeys("admin");
        webDriver.findElement(By.id("f-login:it-password")).sendKeys("BuggerFahrenMachtSpass42");
        webDriver.findElement(By.id("f-login:cb-login")).click();
        {
            WebElement element = webDriver.findElement(By.id("p-profile-menu-toggle"));
            Actions builder = new Actions(webDriver);
            builder.moveToElement(element).perform();
        }
        webDriver.findElement(By.id("f-logout:cb-logout")).click();

        assertTrue(isElementPresentById(By.id("l-login")));
    }

    private boolean isElementPresentById(By by) {
        try {
            webDriver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}
