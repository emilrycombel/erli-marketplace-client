package pl.erli.client.pagination;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PaginatedStreamTest {

    @Test
    void shouldStreamAllItemsAcrossPages() {
        AtomicInteger pageCount = new AtomicInteger(0);

        Stream<String> stream = PaginatedStream.create(cursor -> {
            int page = pageCount.incrementAndGet();
            int offset = cursor == null ? 0 : (int) cursor;
            if (offset >= 10) {
                return new PaginatedStream.Page<>(List.of(), null);
            }
            List<String> items = new ArrayList<>();
            for (int i = offset; i < Math.min(offset + 3, 10); i++) {
                items.add("item-" + i);
            }
            int nextOffset = offset + 3;
            return new PaginatedStream.Page<>(items, nextOffset < 10 ? nextOffset : null);
        }, 3, 2);

        List<String> result = stream.toList();

        assertEquals(10, result.size());
        assertEquals("item-0", result.getFirst());
        assertEquals("item-9", result.getLast());
        assertTrue(pageCount.get() >= 4); // at least 4 pages: 3+3+3+1
    }

    @Test
    void shouldHandleEmptyFirstPage() {
        Stream<String> stream = PaginatedStream.create(
                cursor -> new PaginatedStream.Page<>(List.of(), null),
                10, 2
        );

        List<String> result = stream.toList();
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleSinglePage() {
        Stream<String> stream = PaginatedStream.create(cursor -> {
            if (cursor != null) {
                return new PaginatedStream.Page<>(List.of(), null);
            }
            return new PaginatedStream.Page<>(List.of("only-item"), null);
        }, 10, 2);

        List<String> result = stream.toList();
        assertEquals(1, result.size());
        assertEquals("only-item", result.getFirst());
    }

    @Test
    void shouldSupportLazyEvaluation() {
        AtomicInteger fetchCount = new AtomicInteger(0);

        Stream<String> stream = PaginatedStream.create(cursor -> {
            int page = fetchCount.incrementAndGet();
            int offset = cursor == null ? 0 : (int) cursor;
            List<String> items = List.of("item-" + offset);
            return new PaginatedStream.Page<>(items, offset + 1);
        }, 1, 2);

        // Only take 3 items - should not fetch all pages
        List<String> result = stream.limit(3).toList();

        assertEquals(3, result.size());
        // With prefetch of 2, it may fetch a few more but not hundreds
        assertTrue(fetchCount.get() <= 6, "Should not fetch too many pages, got: " + fetchCount.get());
    }
}
