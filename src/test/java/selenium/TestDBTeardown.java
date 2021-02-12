package selenium;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Order(Integer.MAX_VALUE)
@Tag("system")
public class TestDBTeardown {

    @Test
    public void run() {
        DBCleaner.cleanup();
    }

}