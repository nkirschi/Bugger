package tech.bugger.global.util;

import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.LogExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LogExtension.class)
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

    @Test
    public void testToStringAbsent() {
        assertEquals("Lazy{absent}", lazyString.toString());
    }

    @Test
    public void testToStringPresent() {
        assertEquals("Lazy{value=eager}", eagerString.toString());
    }

    @Test
    public void testToStringCustomAbsent() {
        assertEquals("Lazy{absent}", lazyString.toString(String::toUpperCase));
    }

    @Test
    public void testToStringCustomPresent() {
        assertEquals("Lazy{value=EAGER}", eagerString.toString(String::toUpperCase));
    }

}
