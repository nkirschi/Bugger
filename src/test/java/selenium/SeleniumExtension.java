package selenium;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import tech.bugger.persistence.util.PropertiesReader;

import java.util.concurrent.TimeUnit;

public class SeleniumExtension implements BeforeAllCallback, AfterAllCallback {

    private static String driverType;
    private static String driverPath;
    private static String baseURL;
    private static String os;
    private static WebDriver webDriver;
    private static PropertiesReader propertiesReader;

    public SeleniumExtension() {
        try {
            propertiesReader = new PropertiesReader(ClassLoader.getSystemResourceAsStream("selenium.properties"));
            driverType = propertiesReader.getString("driver.type");
            driverPath = propertiesReader.getString("driver.path");
            baseURL = propertiesReader.getString("url");
            os = propertiesReader.getString("os");
        } catch (Exception e) {
            throw new InternalError("Could not load properties for selenium tests.", e);
        }

    }

    @Override
    public void beforeAll(ExtensionContext context) {
        setDriverType(driverType);
        if (driverType.equals("firefox")) {
            webDriver = new FirefoxDriver();
            webDriver.manage().window().maximize();
            webDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS); // time to make screenshots ;)
        } else {
            throw new IllegalArgumentException("The configured driver type is not supported!");
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        webDriver.quit();
    }

    public static void setDriverType(String type) {
        if (type.equals("firefox")) {
            switch (os) {
            case "Windows" -> driverPath += "geckodriver.exe";
            case "Linux" -> driverPath += "geckodriver";
            case "Mac" -> driverPath += "geckodriverMac";
            default -> throw new IllegalArgumentException("The configured OS is invalid!");
            }
            System.setProperty("webdriver.gecko.driver", driverPath);
        } else {
            throw new IllegalArgumentException("The configured driver type is not supported!");
        }
    }

    public static WebDriver getDriver() {
        return webDriver;
    }

    public static String getBaseURL() {
        if (baseURL == null || baseURL.trim().isBlank()) {
            throw new IllegalArgumentException("The configured url must not be null or blank!");
        } else {
            return baseURL;
        }
    }

}
