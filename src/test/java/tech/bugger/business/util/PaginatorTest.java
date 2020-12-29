package tech.bugger.business.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tech.bugger.LogExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(LogExtension.class)
public class PaginatorTest {

    private List<Integer> testData;
    private Paginator<Integer> paginator;
    private Paginator<Integer> paginatorEmpty;

    @BeforeEach
    public void init() {
        testData = IntStream.range(1, 50).boxed().collect(Collectors.toList());

        paginator = new Paginator<>("id", 20) {
            @Override
            protected Iterable<Integer> fetch() {
                Comparator<Integer> comp = Integer::compareTo;
                if (!getSelection().isAscending()) {
                    comp = comp.reversed();
                }
                testData.sort(comp);

                return testData.subList(getSelection().getCurrentPage() * getSelection().getPageSize(),
                        Math.min((getSelection().getCurrentPage() + 1) * getSelection().getPageSize(),
                                testData.size()));
            }

            @Override
            protected int totalSize() {
                return testData.size();
            }
        };

        paginatorEmpty = new Paginator<>("id", 20) {
            private final List<Integer> EMPTY_LIST = new ArrayList<>();

            @Override
            protected Iterable<Integer> fetch() {
                return EMPTY_LIST;
            }

            @Override
            protected int totalSize() {
                return 0;
            }
        };
    }

    @Test
    public void testInitialPage() {
        assertEquals(1, paginator.getCurrentPage());
    }

    @Test
    public void testInitialPageIsFirst() {
        assertTrue(paginator.isFirstPage());
    }

    @Test
    public void testInitialFirstPage() {
        assertEquals(1, paginator.iterator().next());
    }

    @Test
    public void testFirstPage() {
        paginator.setCurrentPage(3);
        paginator.firstPage();
        assertEquals(1, paginator.iterator().next());
    }

    @Test
    public void testNextPage() {
        paginator.setCurrentPage(1);
        paginator.nextPage();
        assertEquals(21, paginator.iterator().next());
    }

    @Test
    public void testInvalidPrevPage() {
        paginator.setCurrentPage(1);
        assertThrows(IllegalStateException.class, () -> paginator.prevPage());
    }

    @Test
    public void testPrevPage() {
        paginator.setCurrentPage(2);
        paginator.prevPage();
        assertEquals(1, paginator.iterator().next());
    }

    @Test
    public void testInvalidLastPage() {
        paginator.setCurrentPage(paginator.determineLastPageIndex());
        assertThrows(IllegalStateException.class, () -> paginator.nextPage());
    }

    @Test
    public void testLastPage() {
        paginator.setCurrentPage(1);
        paginator.lastPage();
        assertEquals(41, paginator.iterator().next());
    }

    @Test
    public void testIsFirstPageWhileFirstPage() {
        paginator.firstPage();
        assertTrue(paginator.isFirstPage());
    }

    @Test
    public void testIsFirstPageWhileNotFirstPage() {
        paginator.setCurrentPage(2);
        assertFalse(paginator.isFirstPage());
    }

    @Test
    public void testIsLastPageWhileLastPage() {
        paginator.lastPage();
        assertTrue(paginator.isLastPage());
    }

    @Test
    public void testIsLastPageWhileNotLastPage() {
        paginator.firstPage();
        assertFalse(paginator.isLastPage());
    }

    @Test
    public void testPageSizeValues() {
        assertArrayEquals(new int[]{10, 20, 50, 100}, paginator.pageSizeValues());
    }

    @Test
    public void testIsEmptyWhileFull() {
        assertFalse(paginator.isEmpty());
    }

    @Test
    public void testIsEmptyWhileEmpty() {
        paginator.setWrappedData(new ArrayList<>());
        assertTrue(paginatorEmpty.isEmpty());
    }

    @Test
    public void testUpdateReset() {
        paginator.setCurrentPage(2);
        paginator.updateReset();
        assertEquals(1, paginator.iterator().next());
    }

    @Test
    public void testSortByAscending() {
        List<Integer> sorted = new ArrayList<>();
        paginator.forEach(sorted::add);
        Collections.sort(sorted);
        assertIterableEquals(sorted, paginator);
    }

    @Test
    public void testSortByDescending() {
        paginator.sortBy("id");
        List<Integer> sorted = new ArrayList<>();
        paginator.forEach(sorted::add);
        sorted.sort(Collections.reverseOrder());
        assertIterableEquals(sorted, paginator);
    }

    @Test
    public void testSortByAscendingByToggle() {
        paginator.sortBy("id");
        paginator.sortBy("id");
        List<Integer> sorted = new ArrayList<>();
        paginator.forEach(sorted::add);
        Collections.sort(sorted);
        assertIterableEquals(sorted, paginator);
    }

    @Test
    public void testSortByAscendingBySwitch() {
        paginator.sortBy("otherKey");
        paginator.sortBy("id");
        List<Integer> sorted = new ArrayList<>();
        paginator.forEach(sorted::add);
        Collections.sort(sorted);
        assertIterableEquals(sorted, paginator);
    }

    @Test
    public void testOtherPageSize() {
        paginator.getSelection().setPageSize(50);
        assertTrue(paginator.isLastPage());
    }

    @Test
    public void testInvalidPage1() {
        assertThrows(IllegalArgumentException.class, () -> paginator.setCurrentPage(0));
    }

    @Test
    public void testInvalidPage2() {
        assertThrows(IllegalArgumentException.class, () -> paginator.setCurrentPage(5));
    }

}
