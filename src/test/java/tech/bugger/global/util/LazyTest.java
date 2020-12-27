package tech.bugger.global.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LazyTest {
    private Lazy<String> lazyString;
    private Lazy<String> eagerString;

    @BeforeEach
    public void init() {
        lazyString = new Lazy<>(() -> "lazy");
        eagerString = new Lazy<>("eager");
    }

    @Test
    public void testLazyConstructorWhenNull() {
        assertThrows(IllegalArgumentException.class, () -> new Lazy<>((Supplier<String>) null));
    }

    @Test
    public void testEagerConstructorWhenNull() {
        assertThrows(IllegalArgumentException.class, () -> new Lazy<>((String) null));
    }

    @Test
    public void testIsPresentWhenNotPresent() {
        assertFalse(lazyString.isPresent());
    }

    @Test
    public void testIsPresentWhenPresent() {
        lazyString.get();
        assertTrue(lazyString.isPresent());
    }

    @Test
    public void testIsPresentOnEager() {
        assertTrue(eagerString.isPresent());
    }

    @Test
    public void testGetOnLazy() {
        assertEquals("lazy", lazyString.get());
    }

    @Test
    public void testGetOnEager() {
        assertEquals("eager", eagerString.get());
    }
}
