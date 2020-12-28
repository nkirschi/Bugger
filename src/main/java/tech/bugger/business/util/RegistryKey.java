package tech.bugger.business.util;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifier annotation specifying a registry key.
 */
@Qualifier
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface RegistryKey {

    /**
     * The registry key.
     *
     * @return The registry key.
     */
    @Nonbinding String value() default "";

}
