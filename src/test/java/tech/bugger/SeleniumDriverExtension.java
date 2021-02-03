package tech.bugger;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import tech.bugger.persistence.util.PropertiesReader;

public class SeleniumDriverExtension implements BeforeAllCallback {

    private static String driverType;
    private static String driverPath;
    private static String baseURL;
    private static String os;
    private static WebDriver webDriver;
    private static PropertiesReader propertiesReader;

    public SeleniumDriverExtension() {
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
        if (driverType.equals("firefox")) {
            return new FirefoxDriver();
        } else {
            throw new IllegalArgumentException("The configured driver type is not supported!");
        }
    }

    public static String getBaseURL () {
        if (baseURL == null || baseURL.trim().isBlank()) {
            throw new IllegalArgumentException("The configured url must not be null or blank!");
        } else {
            return baseURL;
        }
    }

    public static String getDriverType () {
        return driverType;
    }

}
