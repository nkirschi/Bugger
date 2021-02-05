package selenium;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import tech.bugger.persistence.util.PropertiesReader;

import java.util.concurrent.TimeUnit;

public class SeleniumExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private final String driverType;
    private final String driverDir;
    private final String driverOS;
    private final String baseURL;
    private final boolean headless;

    private WebDriver driver;

    private WebDriverWait waiter;

    public SeleniumExtension() {
        try {
            PropertiesReader conf = new PropertiesReader(ClassLoader.getSystemResourceAsStream("selenium.properties"));
            driverType = conf.getString("driver.type");
            driverDir = conf.getString("driver.path");
            baseURL = conf.getString("url");
            driverOS = conf.getString("os");
            headless = conf.getBoolean("headless");
            registerDriver(driverType);
        } catch (Exception e) {
            throw new InternalError("Could not load properties for selenium tests.", e);
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        if (driverType.equals("firefox")) {
            FirefoxOptions options = new FirefoxOptions();
            options.addPreference("intl.accept_languages", "en-US");
            options.setHeadless(headless);
            driver = new FirefoxDriver(options);
            driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
            waiter = new WebDriverWait(driver, 5);
        } else {
            throw new IllegalArgumentException("The configured driver type is not supported!");
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        driver.quit();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return WebDriver.class.equals(type) || WebDriverWait.class.equals(type) || String.class.equals(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        if (WebDriver.class.equals(type)) {
            return driver;
        } else if (WebDriverWait.class.equals(type)) {
            return waiter;
        } else if (String.class.equals(type)) {
            return baseURL;
        } else {
            throw new ParameterResolutionException("Unsupported parameter type " + type);
        }
    }

    private void registerDriver(String type) {
        if (type.equals("firefox")) {
            System.setProperty("webdriver.gecko.driver", switch (driverOS) {
                case "Windows" -> driverDir + "geckodriver.exe";
                case "Linux" -> driverDir + "geckodriver";
                case "Mac" -> driverDir + "geckodriverMac";
                default -> throw new IllegalArgumentException("The configured OS is invalid!");
            });
        } else {
            throw new IllegalArgumentException("The configured driver type is not supported!");
        }
    }

    public String getBaseURL() {
        return baseURL;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public WebDriverWait getWaiter() {
        return waiter;
    }

}
