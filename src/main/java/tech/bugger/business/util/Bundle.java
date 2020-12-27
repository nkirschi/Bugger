package tech.bugger.business.util;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifier annotation specifying a certain resource bundle by name.
 */
@Qualifier
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface Bundle {
    /**
     * The name of the resource bundle being referenced.
     *
     * @return The resource bundle name.
     */
    String value();
}
