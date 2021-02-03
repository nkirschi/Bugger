package tech.bugger.system;

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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import tech.bugger.LogExtension;
import tech.bugger.SeleniumDriverExtension;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SeleniumDriverExtension.class)
@ExtendWith(LogExtension.class)
@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AdministratorTest {

    private WebDriver webDriver;
    private static String baseURL;
    private JavascriptExecutor js;
    private HashMap<String, Object> vars;
    private String reversiFeedback = "Reversi: Lob und Tadel";
    private String reversiGraphics = "Reversi: Grafikoberfläche";
    private static final String DESCRIPTION = "Hier können Sie Feedback jeglicher Art zur Version 1.0 abgeben.";
    private static final String ADMINISTRATION = "Administration";
    private static final String ADMIN = "admin";
    private static final String ADMIN_PASSWORD = "BuggerFahrenMachtSpass42";
    private static final String VOTING_WEIGHT = "5";
    private static final String NO_TRANSLATION = "Button Übersetzung fehlt";
    private static final String MISSING = "Für \"Zug rückgängig machen\" fehlt die englische Übersetzung.";
    private String alf = "AlfDerBenutzer";
    private String alfFirstName = "Alf";
    private String alfLastName = "Albrecht";
    private String alfEmailPrefix = "alfxtreme";
    private String alfEmailSuffix = "@gmail.com";
    private String alfPassword = "Welten-Mysterium1";
    private String profilePage = "Profile of @AlfDerBenutzer";
    private String linkText = "";
    private String reversiFeedbackURL = "";
    private String reversiGraphicsURL = "";
    private WebDriverWait wait;

    @BeforeAll
    public void setUp() {
        baseURL = SeleniumDriverExtension.getBaseURL();
        webDriver = SeleniumDriverExtension.getDriver();
        linkText = "@" + alf;
        js = (JavascriptExecutor) webDriver;
        vars = new HashMap<>();
        webDriver.get(baseURL);
        wait = new WebDriverWait(webDriver, 5);
    }

    @AfterAll
    public void tearDown() {
        deleteReversiFeedback();
        deleteAlf();
        deleteReversiGraphics();
        webDriver.quit();
    }

    @Test
    public void T010_login() {
        webDriver.get(baseURL);
        webDriver.findElement(By.id("l-login")).click();
        webDriver.findElement(By.id("f-login:it-username")).click();
        webDriver.findElement(By.id("f-login:it-username")).sendKeys(ADMIN);
        webDriver.findElement(By.id("f-login:it-password")).sendKeys(ADMIN_PASSWORD);
        webDriver.findElement(By.id("f-login:cb-login")).click();

        Select select = new Select(webDriver.findElement(By.id("f-language:s-language")));

        assertAll(
                () -> assertTrue(isElementPresentBy(By.id("p-avatar-thumbnail"))),
                () -> assertEquals("English", select.getFirstSelectedOption().getText())
        );
    }

    @Test
    public void T015_administration() {
        WebElement element = webDriver.findElement(By.id("p-avatar-thumbnail"));
        Actions builder = new Actions(webDriver);
        builder.moveToElement(element).perform();

        webDriver.findElement(By.id("l-configuration")).click();

        assertEquals(ADMINISTRATION, webDriver.findElement(By.id("title")).getText());
    }

    @Test
    public void T020_createUser() {
        webDriver.findElement(By.id("l-create")).click();
        webDriver.findElement(By.id("f-profile-edit:it-username")).sendKeys(alf);
        webDriver.findElement(By.id("f-profile-edit:it-first-name")).sendKeys(alfFirstName);
        webDriver.findElement(By.id("f-profile-edit:it-last-name")).sendKeys(alfLastName);
        webDriver.findElement(By.id("f-profile-edit:it-email")).sendKeys(alfEmailPrefix + alfEmailSuffix);
        webDriver.findElement(By.id("f-profile-edit:i-password-new")).sendKeys(alfPassword);
        webDriver.findElement(By.id("f-profile-edit:i-password-new-repeat")).sendKeys(alfPassword);

        // Turns out that scrolling in selenium is terrible, especially if you have a footer that causes Selenium to
        // constantly throw ElementClickInterceptedExceptions. Forcing selenium to scroll elements at the bottom of a
        // page properly into view is a very fun activity you should not miss. Sadly, the code seems to turn out ugly.
        WebElement element = webDriver.findElement(By.id("f-profile-edit:cb-apply"));
        js.executeScript("arguments[0].scrollIntoView();", element);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-profile-edit:ia-bio")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-profile-edit:ol-bio-hint")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-profile-edit:it-overwrite-vote")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-profile-edit:cb-apply")));

        webDriver.findElement(By.id("f-profile-edit:ia-bio")).click();
        webDriver.findElement(By.id("f-profile-edit:ol-bio-hint")).click();
        webDriver.findElement(By.id("f-profile-edit:it-overwrite-vote")).click();
        webDriver.findElement(By.id("f-profile-edit:cb-apply")).click();
        webDriver.findElement(By.id("f-change-user:i-password-change")).sendKeys(ADMIN_PASSWORD);
        webDriver.findElement(By.id("f-change-user:cb-really-change")).click();

        assertAll(
                () -> assertEquals(profilePage, webDriver.findElement(By.id("title")).getText()),
                () -> assertEquals(alf, webDriver.findElement(By.id("f-profile:ot-username")).getText()),
                () -> assertEquals(alfFirstName, webDriver.findElement(By.id("f-profile:ot-first-name")).getText()),
                () -> assertEquals(alfLastName, webDriver.findElement(By.id("f-profile:ot-last-name")).getText()),
                () -> assertEquals(alfEmailPrefix + alfEmailSuffix,
                        webDriver.findElement(By.id("f-profile:ot-email")).getText())
        );
    }

    @Test
    public void T030_changeVotingWeight() {
        webDriver.findElement(By.id("f-profile:l-edit")).click();

        WebElement element = webDriver.findElement(By.id("f-profile-edit:cb-apply"));
        js.executeScript("arguments[0].scrollIntoView();", element);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-profile-edit:ia-bio")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-profile-edit:ol-bio-hint")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-profile-edit:it-overwrite-vote")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-profile-edit:cb-apply")));

        webDriver.findElement(By.id("f-profile-edit:ia-bio")).click();
        webDriver.findElement(By.id("f-profile-edit:ol-bio-hint")).click();
        webDriver.findElement(By.id("f-profile-edit:it-overwrite-vote")).click();

        webDriver.findElement(By.id("f-profile-edit:it-overwrite-vote")).sendKeys(VOTING_WEIGHT);
        webDriver.findElement(By.id("f-profile-edit:cb-apply")).click();
        webDriver.findElement(By.id("f-change-user:i-password-change")).sendKeys(ADMIN_PASSWORD);
        webDriver.findElement(By.id("f-change-user:cb-really-change")).click();

        assertAll(
                () -> assertEquals(profilePage, webDriver.findElement(By.id("title")).getText()),
                () -> assertEquals(VOTING_WEIGHT, webDriver.findElement(By.id("f-profile:ot-weight")).getText()),
                () -> assertTrue(isElementPresentBy(By.className("alert-success")))
        );
    }

    @Test
    public void T040_createTopic() {
        webDriver.findElement(By.id("ot-comp-name")).click();
        webDriver.findElement(By.id("l-create")).click();
        webDriver.findElement(By.id("f-topic-edit:it-title")).sendKeys(reversiFeedback);
        webDriver.findElement(By.id("f-topic-edit:it-description")).sendKeys(DESCRIPTION);
        webDriver.findElement(By.id("f-topic-edit:cb-save")).click();

        reversiFeedbackURL = webDriver.getCurrentUrl();

        assertAll(
                () -> assertTrue(webDriver.findElement(By.id("title")).getText().contains(reversiFeedback)),
                () -> assertEquals(DESCRIPTION, webDriver.findElement(By.id("f-topic:ot-description")).getText())
        );
    }

    @Test
    public void T050_addModerator() {
        webDriver.findElement(By.id("f-moderator-status:cb-image-promote")).click();
        webDriver.findElement(By.id("f-promote-mod:it-username")).sendKeys(alf);
        webDriver.findElement(By.id("f-promote-mod:cb-promote")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(linkText)));

        assertTrue(isElementPresentBy(By.linkText(linkText)));
    }

    @Test
    public void T060_createTopicSameTitle() {
        webDriver.findElement(By.id("ot-comp-name")).click();
        webDriver.findElement(By.id("l-create")).click();
        webDriver.findElement(By.id("f-topic-edit:it-title")).sendKeys(reversiFeedback);
        webDriver.findElement(By.id("f-topic-edit:it-description")).sendKeys(DESCRIPTION);
        webDriver.findElement(By.id("f-topic-edit:cb-save")).click();

        assertTrue(isElementPresentBy(By.className("alert-danger")));
    }

    @Test
    public void T070_changeTitle() {
        webDriver.findElement(By.id("f-topic-edit:it-title")).clear();
        webDriver.findElement(By.id("f-topic-edit:it-title")).sendKeys(reversiGraphics);
        webDriver.findElement(By.id("f-topic-edit:cb-save")).click();
        webDriver.findElement(By.id("f-moderator-status:cb-image-promote")).click();
        webDriver.findElement(By.id("f-promote-mod:it-username")).sendKeys(alf);
        webDriver.findElement(By.id("f-promote-mod:cb-promote")).click();

        reversiGraphicsURL = webDriver.getCurrentUrl();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(linkText)));

        assertAll(
                () -> assertTrue(webDriver.findElement(By.id("title")).getText().contains(reversiGraphics)),
                () -> assertEquals(DESCRIPTION, webDriver.findElement(By.id("f-topic:ot-description")).getText()),
                () -> assertTrue(isElementPresentBy(By.linkText(linkText)))
        );
    }

    @Test
    public void T080_createReport() throws URISyntaxException {
        webDriver.findElement(By.id("f-topic:l-create-report")).click();
        {
            WebElement dropdown = webDriver.findElement(By.id("f-create-report:s-type"));
            dropdown.findElement(By.xpath("//option[. = 'Hint']")).click();
        }
        webDriver.findElement(By.id("f-create-report:it-title")).sendKeys(NO_TRANSLATION);
        webDriver.findElement(By.id("f-create-report:it-post-content")).sendKeys(MISSING);

        WebElement element = webDriver.findElement(By.id("f-create-report:cb-create"));
        js.executeScript("arguments[0].scrollIntoView();", element);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-create-report:it-attachment")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-create-report:cb-add-attachment")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-create-report:cb-create")));

        // Needed as Windows need backslashes to locate file.
        String file = Paths.get(getClass().getClassLoader().getResource("images/boeseDatei.exe").toURI()).toFile().getPath();
        webDriver.findElement(By.id("f-create-report:it-attachment"));
        webDriver.findElement(By.id("f-create-report:it-attachment")).sendKeys(file);
        webDriver.findElement(By.className("justify-content-between"));
        webDriver.findElement(By.id("f-create-report:cb-add-attachment")).click();

        assertTrue(isElementPresentBy(By.className("alert-danger")));
    }

    @Test
    public void T090_changePicture() throws InterruptedException, URISyntaxException {
        // Needed as Windows need backslashes to locate file.
        String file = Paths.get(getClass().getClassLoader().getResource("images/bugger.png").toURI()).toFile().getPath();

        // I cannot get the method used above to work here, without the test being super flaky.
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        Thread.sleep(1000);

        webDriver.findElement(By.id("f-create-report:it-attachment"));
        webDriver.findElement(By.id("f-create-report:it-attachment")).sendKeys(file);
        webDriver.findElement(By.id("f-create-report:cb-create")).click();

        assertAll(
                () -> assertTrue(webDriver.findElement(By.id("title")).getText().contains(NO_TRANSLATION)),
                () -> assertEquals("Hint", webDriver.findElement(By.id("ot-type")).getText()),
                () -> assertEquals("Minor", webDriver.findElement(By.id("ot-severity")).getText())
        );
    }

    @Test
    public void T100_demoteAdmin() {
        webDriver.get(baseURL + "profile?u=admin");
        webDriver.findElement(By.id("f-profile:cb-rem-admin")).click();
        webDriver.findElement(By.id("f-change-status:i-user-password")).click();
        webDriver.findElement(By.id("f-change-status:i-user-password")).sendKeys(ADMIN_PASSWORD);
        webDriver.findElement(By.id("f-change-status:cb-change-status")).click();

        assertTrue(isElementPresentBy(By.className("alert-danger")));
    }

    private boolean isElementPresentBy(By by) {
        try {
            webDriver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    //Methods for cleanup until the database is cleaned.
    private void deleteAlf() {
        webDriver.get(baseURL + "profile-edit?e=" + alf);

        WebElement element = webDriver.findElement(By.id("f-profile-edit:cb-delete"));
        js.executeScript("arguments[0].scrollIntoView();", element);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-profile-edit:ia-bio")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-profile-edit:ol-bio-hint")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-profile-edit:it-overwrite-vote")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("f-profile-edit:cb-apply")));

        webDriver.findElement(By.id("f-profile-edit:ia-bio")).click();
        webDriver.findElement(By.id("f-profile-edit:ol-bio-hint")).click();
        webDriver.findElement(By.id("f-profile-edit:it-overwrite-vote")).click();

        webDriver.findElement(By.id("f-profile-edit:cb-delete")).click();
        webDriver.findElement(By.id("f-del-user:i-password-del")).sendKeys(ADMIN_PASSWORD);
        webDriver.findElement(By.id("f-del-user:cb-really-del")).click();
    }

    private void deleteReversiFeedback() {
        webDriver.get(reversiFeedbackURL);
        webDriver.findElement(By.id("f-topic:cb-delete")).click();
        webDriver.findElement(By.id("f-delTopic:cb-delTopic")).click();
    }

    private void deleteReversiGraphics() {
        webDriver.get(reversiGraphicsURL);
        webDriver.findElement(By.id("f-topic:cb-delete")).click();
        webDriver.findElement(By.id("f-delTopic:cb-delTopic")).click();
    }

}
