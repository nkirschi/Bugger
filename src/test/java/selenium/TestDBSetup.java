package selenium;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Order(Integer.MIN_VALUE)
@Tag("system")
public class TestDBSetup {

    @Test
    public void setup() {
        DBCleaner.cleanup();
    }

}