package tech.bugger.global.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConstantsTest {

    @Test
    public void testHandlerConstructorAccess() throws NoSuchMethodException {
        Constructor<Constants> constructor = Constants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Throwable e = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
    }

}