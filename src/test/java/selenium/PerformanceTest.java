package selenium;

import org.junit.jupiter.api.Test;

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

        for (int i = 0; i < NUM_PARALLEL_EXECUTIONS; i++) {
            final String testID = String.valueOf(i);
            Thread thread = new Thread(() -> {
                latch.countDown();
                System.out.println("Latch countdown.");
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Running test suite with ID " + testID);
                SeleniumExtension extension = new SeleniumExtension();
                extension.beforeAll(null);

                try {
                    AdministratorTest administratorTest = new AdministratorTest(testID);
                    administratorTest.setUp(extension.getDriver(), extension.getBaseURL());
                    administratorTest.T010_login();
                    administratorTest.T015_administration();
                    administratorTest.T020_create_user();
                    administratorTest.T030_change_voting_weight();
                    administratorTest.T040_create_topic();
                    administratorTest.T050_add_moderator();
                    administratorTest.T060_create_topic_with_same_title();
                    administratorTest.T070_change_topic_title();
                    administratorTest.T080_create_report_with_invalid_attachment();
                    administratorTest.T090_create_report_with_valid_attachment();
                    administratorTest.T100_try_demote_admin();

                    UserTest userTest = new UserTest(testID);
                    userTest.setUp(extension.getDriver(), extension.getBaseURL());
                    userTest.T110_insecure_direct_object_access();
                    userTest.T120_register_with_validation_errors();
                    userTest.T130_register_successfully();
                    userTest.T135_set_password_with_validation_errors();
                    userTest.T140_set_password_successfully();
                    userTest.T150_change_profile();
                    userTest.T160_browse_content();
                    userTest.T170_help_popup();
                    userTest.T180_create_report();
                    userTest.T190_edit_report();
                    userTest.T200_change_report_and_vote();
                    userTest.T210_create_post();

                    ModeratorTest moderatorTest = new ModeratorTest(testID);
                    moderatorTest.setUp(extension.getDriver(), extension.getWaiter(), extension.getBaseURL());
                    moderatorTest.T220_discover_notifications();
                    moderatorTest.T230_delete_post();
                    moderatorTest.T240_overwrite_relevance();
                    moderatorTest.T250_upvote();
                    moderatorTest.T260_undo_overwrite();
                    moderatorTest.T270_search_report_suggestions();
                    moderatorTest.T280_search_report();
                    moderatorTest.T290_mark_duplicate();
                    moderatorTest.T300_search_topic_suggestions();
                    moderatorTest.T310_ban_user();
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
    }
}
