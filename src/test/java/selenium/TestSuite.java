package selenium;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectClasses({TestDBCleaner.class, AdministratorTest.class, RegistrationTest.class, TestDBCleaner.class})
@SuiteDisplayName("Bugger System Test")
public class TestSuite {

}