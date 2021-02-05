package selenium;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(Integer.MIN_VALUE)
public class TestDBSetup {

    @Test
    public void setup() {
        DBCleaner.cleanup();
    }

}