package tech.bugger;

import java.util.Collections;
import java.util.Enumeration;
import java.util.ResourceBundle;

// Mockito cannot mock resource bundles :(
public class ResourceBundleMocker {
    public static ResourceBundle mock(String valueToAlwaysReturn) {
        return new ResourceBundle() {
            @Override
            @SuppressWarnings("NullableProblems")
            protected Object handleGetObject(String key) {
                return valueToAlwaysReturn;
            }

            @Override
            @SuppressWarnings("NullableProblems")
            public Enumeration<String> getKeys() {
                return Collections.emptyEnumeration();
            }
        };
    }
}
