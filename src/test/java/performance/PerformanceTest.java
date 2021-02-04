package performance;

import org.junit.jupiter.api.Test;
import selenium.AdministratorTest;
import selenium.ModeratorTest;
import selenium.SeleniumExtension;
import selenium.TestDBCleaner;
import selenium.UserTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class PerformanceTest {

    private static final int NUM_PARALLEL_EXECUTIONS = 1;

    @Test
    public void run() {
        TestDBCleaner cleaner = new TestDBCleaner();
        cleaner.setup();

        System.out.println("Running " + NUM_PARALLEL_EXECUTIONS + " test suites in parallel.");

        CountDownLatch latch = new CountDownLatch(NUM_PARALLEL_EXECUTIONS);
        List<Thread> threads = new ArrayList<>();
        int numDigits = Math.max(1, (int) Math.floor(Math.log10(NUM_PARALLEL_EXECUTIONS)));

        for (int i = 0; i < NUM_PARALLEL_EXECUTIONS; i++) {
            final String testID = String.format("%0" + numDigits + "d", i);
            Thread thread = new Thread(() -> {
                latch.countDown();
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Running test suite with ID " + testID);
                SeleniumExtension extension = new SeleniumExtension();

                extension.beforeAll(null);
                try {
                    AdministratorTest administratorTest = new AdministratorTest();
                    administratorTest.setTestID(testID);
                    administratorTest.setUp(extension.getDriver(), extension.getWaiter(), extension.getBaseURL());
                    System.out.println(testID + ": B010");
                    administratorTest.T010_login();
                    System.out.println(testID + ": T010");
                    administratorTest.T015_administration();
                    System.out.println(testID + ": T015");
                    administratorTest.T020_create_user();
                    System.out.println(testID + ": T020");
                    administratorTest.T030_change_voting_weight();
                    System.out.println(testID + ": T030");
                    administratorTest.T040_create_topic();
                    System.out.println(testID + ": T040");
                    administratorTest.T050_add_moderator();
                    System.out.println(testID + ": T050");
                    administratorTest.T060_create_topic_with_same_title();
                    System.out.println(testID + ": T060");
                    administratorTest.T070_change_topic_title();
                    System.out.println(testID + ": T070");
                    administratorTest.T080_create_report_with_invalid_attachment();
                    System.out.println(testID + ": T080");
                    administratorTest.T090_create_report_with_valid_attachment();
                    System.out.println(testID + ": T090");
                    administratorTest.T100_try_demote_admin();
                    System.out.println(testID + ": T100");
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    extension.afterAll(null);
                }

                extension.beforeAll(null);
                try {
                    UserTest userTest = new UserTest();
                    userTest.setTestID(testID);
                    userTest.setUp(extension.getDriver(), extension.getBaseURL());
                    System.out.println(testID + ": B020");
                    userTest.T110_insecure_direct_object_access();
                    System.out.println(testID + ": T110");
                    userTest.T120_register_with_validation_errors();
                    System.out.println(testID + ": T120");
                    userTest.T130_register_successfully();
                    System.out.println(testID + ": T130");
                    userTest.T135_set_password_with_validation_errors();
                    System.out.println(testID + ": T135");
                    userTest.T140_set_password_successfully();
                    System.out.println(testID + ": T140");
                    userTest.T150_change_profile();
                    System.out.println(testID + ": T150");
                    userTest.T160_browse_content();
                    System.out.println(testID + ": T160");
                    userTest.T170_help_popup();
                    System.out.println(testID + ": T170");
                    userTest.T180_create_report();
                    System.out.println(testID + ": T180");
                    userTest.T190_edit_report();
                    System.out.println(testID + ": T190");
                    userTest.T200_change_report_and_vote();
                    System.out.println(testID + ": T200");
                    userTest.T210_create_post();
                    System.out.println(testID + ": T210");
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    extension.afterAll(null);
                }

                extension.beforeAll(null);
                try {
                    ModeratorTest moderatorTest = new ModeratorTest();
                    moderatorTest.setTestID(testID);
                    moderatorTest.setUp(extension.getDriver(), extension.getWaiter(), extension.getBaseURL());
                    System.out.println(testID + ": B030");
                    moderatorTest.T220_discover_notifications();
                    System.out.println(testID + ": T220");
                    moderatorTest.T230_delete_post();
                    System.out.println(testID + ": T230");
                    moderatorTest.T240_overwrite_relevance();
                    System.out.println(testID + ": T240");
                    moderatorTest.T250_upvote();
                    System.out.println(testID + ": T250");
                    moderatorTest.T260_undo_overwrite();
                    System.out.println(testID + ": T260");
                    moderatorTest.T270_search_report_suggestions();
                    System.out.println(testID + ": T270");
                    moderatorTest.T280_search_report();
                    System.out.println(testID + ": T280");
                    moderatorTest.T290_mark_duplicate();
                    System.out.println(testID + ": T290");
                    moderatorTest.T300_search_topic_suggestions();
                    System.out.println(testID + ": T300");
                    moderatorTest.T310_ban_user();
                    System.out.println(testID + ": T310");
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    extension.afterAll(null);
                }

                System.out.println("Finished test suite with ID " + testID);
            });
            threads.add(thread);
            thread.start();
        }

        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        cleaner.setup();
        TimeCounter.close();
    }
}
