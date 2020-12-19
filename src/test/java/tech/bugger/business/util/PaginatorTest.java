package tech.bugger.business.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaginatorTest {

    private List<Integer> testData;
    private Paginator<Integer> paginator;
    private Paginator<Integer> paginatorEmpty;

    @BeforeEach
    void init() {
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
    void testInitialPage() {
        assertEquals(1, paginator.getCurrentPage());
    }

    @Test
    void testInitialPageIsFirst() {
        assertTrue(paginator.isFirstPage());
    }

    @Test
    void testInitialFirstPage() {
        assertEquals(1, paginator.iterator().next());
    }

    @Test
    void testFirstPage() {
        paginator.setCurrentPage(3);
        paginator.firstPage();
        assertEquals(1, paginator.iterator().next());
    }

    @Test
    void testNextPage() {
        paginator.setCurrentPage(1);
        paginator.nextPage();
        assertEquals(21, paginator.iterator().next());
    }

    @Test
    void testPrevPage() {
        paginator.setCurrentPage(2);
        paginator.prevPage();
        assertEquals(1, paginator.iterator().next());
    }

    @Test
    void testLastPage() {
        paginator.setCurrentPage(1);
        paginator.lastPage();
        assertEquals(41, paginator.iterator().next());
    }

    @Test
    void testIsFirstPageWhileFirstPage() {
        paginator.firstPage();
        assertTrue(paginator.isFirstPage());
    }

    @Test
    void testIsFirstPageWhileNotFirstPage() {
        paginator.setCurrentPage(2);
        assertFalse(paginator.isFirstPage());
    }

    @Test
    void testIsLastPageWhileLastPage() {
        paginator.lastPage();
        assertTrue(paginator.isLastPage());
    }

    @Test
    void testIsLastPageWhileNotLastPage() {
        paginator.firstPage();
        assertFalse(paginator.isLastPage());
    }

    @Test
    void testPageSizeValues() {
        assertArrayEquals(new int[]{10, 20, 50, 100}, paginator.pageSizeValues());
    }

    @Test
    void testIsEmptyWhileFull() {
        assertFalse(paginator.isEmpty());
    }

    @Test
    void testIsEmptyWhileEmpty() {
        paginator.setWrappedData(new ArrayList<>());
        assertTrue(paginatorEmpty.isEmpty());
    }

    @Test
    void testUpdateReset() {
        paginator.setCurrentPage(5);
        paginator.updateReset();
        assertEquals(1, paginator.iterator().next());
    }

    @Test
    void testSortByAscending() {
        List<Integer> sorted = new ArrayList<>();
        paginator.forEach(sorted::add);
        Collections.sort(sorted);
        assertIterableEquals(sorted, paginator);
    }

    @Test
    void testSortByDescending() {
        paginator.sortBy("id");
        List<Integer> sorted = new ArrayList<>();
        paginator.forEach(sorted::add);
        sorted.sort(Collections.reverseOrder());
        assertIterableEquals(sorted, paginator);
    }

    @Test
    void testSortByAscendingByToggle() {
        paginator.sortBy("id");
        paginator.sortBy("id");
        List<Integer> sorted = new ArrayList<>();
        paginator.forEach(sorted::add);
        Collections.sort(sorted);
        assertIterableEquals(sorted, paginator);
    }

    @Test
    void testSortByAscendingBySwitch() {
        paginator.sortBy("otherKey");
        paginator.sortBy("id");
        List<Integer> sorted = new ArrayList<>();
        paginator.forEach(sorted::add);
        Collections.sort(sorted);
        assertIterableEquals(sorted, paginator);
    }

    @Test
    void testOtherPageSize() {
        paginator.getSelection().setPageSize(50);
        assertTrue(paginator.isLastPage());
    }

}
