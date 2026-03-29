package pl.er.shoper.client.pagination;

import org.junit.jupiter.api.Test;
import pl.er.shoper.client.model.PagedResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PrefetchingPageIteratorTest {

    @Test
    void iteratesOverAllPages() {
        try (var iterator = new PrefetchingPageIterator<String>(page -> switch (page) {
            case 1 -> new PagedResponse<>(6, 3, 1, List.of("a", "b"));
            case 2 -> new PagedResponse<>(6, 3, 2, List.of("c", "d"));
            case 3 -> new PagedResponse<>(6, 3, 3, List.of("e", "f"));
            default -> throw new IllegalArgumentException("Unexpected page: " + page);
        })) {
            var items = new ArrayList<String>();
            while (iterator.hasNext()) {
                items.add(iterator.next());
            }
            assertEquals(List.of("a", "b", "c", "d", "e", "f"), items);
        }
    }

    @Test
    void handlesSinglePage() {
        try (var iterator = new PrefetchingPageIterator<String>(page ->
                new PagedResponse<>(2, 1, 1, List.of("x", "y")))) {
            var items = new ArrayList<String>();
            while (iterator.hasNext()) {
                items.add(iterator.next());
            }
            assertEquals(List.of("x", "y"), items);
        }
    }

    @Test
    void handlesEmptyResponse() {
        try (var iterator = new PrefetchingPageIterator<String>(page ->
                new PagedResponse<>(0, 0, 1, List.of()))) {
            assertFalse(iterator.hasNext());
        }
    }

    @Test
    void prefetchesTwoPagesAhead() {
        var fetchedPages = new ArrayList<Integer>();

        try (var iterator = new PrefetchingPageIterator<String>(page -> {
            synchronized (fetchedPages) {
                fetchedPages.add(page);
            }
            return switch (page) {
                case 1 -> new PagedResponse<>(5, 5, 1, List.of("a"));
                case 2 -> new PagedResponse<>(5, 5, 2, List.of("b"));
                case 3 -> new PagedResponse<>(5, 5, 3, List.of("c"));
                case 4 -> new PagedResponse<>(5, 5, 4, List.of("d"));
                case 5 -> new PagedResponse<>(5, 5, 5, List.of("e"));
                default -> throw new IllegalArgumentException("Unexpected page: " + page);
            };
        })) {
            // Page 1 fetched synchronously, then pages 2 and 3 scheduled for prefetch
            assertTrue(fetchedPages.contains(1));

            // Consume first item to trigger draining
            assertEquals("a", iterator.next());

            // Allow async prefetch to complete
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            synchronized (fetchedPages) {
                assertTrue(fetchedPages.contains(2), "Page 2 should have been prefetched");
                assertTrue(fetchedPages.contains(3), "Page 3 should have been prefetched");
            }
        }
    }
}
