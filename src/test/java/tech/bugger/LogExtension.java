package tech.bugger;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import tech.bugger.global.util.Log;

public class LogExtension implements TestInstancePostProcessor {
    @Override
    public void postProcessTestInstance(Object instance, ExtensionContext context) throws Exception {
        Log.init(ClassLoader.getSystemResourceAsStream("logging.properties"));
    }
}
