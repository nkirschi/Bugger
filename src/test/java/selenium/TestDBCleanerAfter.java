package selenium;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(Integer.MAX_VALUE)
public class TestDBCleanerAfter {

    @Test
    public void run() {
        TestDBCleaner.cleanup();
    }

}