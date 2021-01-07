package tech.bugger.global.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.LogExtension;
import tech.bugger.global.transfer.Selection;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LogExtension.class)
public class PagitableTest {

    private Selection selection;

    @BeforeEach
    public void setup() {
        selection = new Selection(50, 0, Selection.PageSize.NORMAL, "id", true);
    }

    @Test
    public void testConstructorAccess() throws NoSuchMethodException {
        Constructor<Pagitable> constructor = Pagitable.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Throwable e = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
    }

    @Test
    public void testGetItemOffsetFirstPage() {
        selection.setCurrentPage(0);
        assertEquals(0, Pagitable.getItemOffset(selection));
    }

    @Test
    public void testGetItemOffsetSomeOtherPage() {
        selection.setCurrentPage(2);
        assertEquals(40, Pagitable.getItemOffset(selection));
    }

    @Test
    public void testGetItemLimitNormal() {
        Selection.PageSize size = Selection.PageSize.NORMAL;
        selection.setPageSize(size);
        assertEquals(size.getSize(), Pagitable.getItemLimit(selection));
    }

    @Test
    public void testGetItemLimitHuge() {
        Selection.PageSize size = Selection.PageSize.HUGE;
        selection.setPageSize(size);
        assertEquals(size.getSize(), Pagitable.getItemLimit(selection));
    }

}