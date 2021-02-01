package selenium;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import tech.bugger.LogExtension;

import java.util.HashMap;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DriverExtension.class)
@ExtendWith(LogExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HuettenkaeseTest {

    private WebDriver webDriver;
    private static String baseURL;
    private static JavascriptExecutor js;
    private static HashMap<String, Object> vars;
    private String title = "Hüttenkäse";
    private static final String DESCRIPTION = "Hüttenkäse rocks!!!";
    private int parameter;
    private static int count = 0;

    public void run() {
        new DriverExtension();
        DriverExtension.setDriverType(DriverExtension.getDriverType());
        parameter = count++;
        title += parameter;
        setUp();
        test1_login();
        System.out.println("login");
        test2_createTopic();
        System.out.println(title);
        test3_deleteTopic();
        System.out.println(DESCRIPTION);
        test4_logout();
        System.out.println("logout");
        webDriver.close();
    }

    @BeforeAll
    public void setUp() {
        baseURL = DriverExtension.getBaseURL();
        webDriver = DriverExtension.getDriver();
        js = (JavascriptExecutor) webDriver;
        vars = new HashMap<>();
        webDriver.get(baseURL);
    }

    @AfterAll
    public void tearDown() {
        webDriver.close();
    }

    @Test
    public void test1_login() {
        webDriver.findElement(By.id("l-login")).click();
        webDriver.findElement(By.id("f-login:it-username")).click();
        webDriver.findElement(By.id("f-login:it-username")).sendKeys("admin");
        webDriver.findElement(By.id("f-login:it-password")).sendKeys("BuggerFahrenMachtSpass42");
        webDriver.findElement(By.id("f-login:cb-login")).click();
        assertTrue(isElementPresentById(By.id("p-profile-menu-toggle")));
    }

    @Test
    public void test2_createTopic() {
        webDriver.findElement(By.id("l-create")).click();
        webDriver.findElement(By.id("f-topic-edit:it-title")).click();
        webDriver.findElement(By.id("f-topic-edit:it-title")).sendKeys(title);
        webDriver.findElement(By.id("f-topic-edit:it-description")).sendKeys(DESCRIPTION);
        webDriver.findElement(By.id("f-topic-edit:cb-save")).click();

        assertAll(
                () -> assertTrue(webDriver.findElement(By.id("title")).getText().contains(title)),
                () -> assertEquals(DESCRIPTION, webDriver.findElement(By.id("f-topic:ot-description")).getText())
        );
    }

    @Test
    public void test3_deleteTopic() {
        webDriver.findElement(By.id("f-topic:cb-delete")).click();
        webDriver.findElement(By.id("f-delTopic:cb-delTopic")).click();

        assertTrue(isElementPresentById(By.id("l-create")));
    }

    @Test
    public void test4_logout() {
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
