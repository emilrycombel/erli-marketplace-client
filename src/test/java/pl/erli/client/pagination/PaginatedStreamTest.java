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
        assertTrue(pageCount.get() >= 4);
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

        List<String> result = stream.limit(3).toList();

        assertEquals(3, result.size());
        assertTrue(fetchCount.get() <= 6, "Should not fetch too many pages, got: " + fetchCount.get());
    }

    @Test
    void shouldPropagateExceptionFromFetcher() {
        Stream<String> stream = PaginatedStream.create(cursor -> {
            if (cursor == null) {
                return new PaginatedStream.Page<>(List.of("first"), 1);
            }
            throw new RuntimeException("API failure");
        }, 1, 2);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> stream.toList());
        assertEquals("API failure", ex.getMessage());
    }

    @Test
    void shouldStopProducerOnStreamClose() throws InterruptedException {
        AtomicInteger fetchCount = new AtomicInteger(0);

        try (Stream<String> stream = PaginatedStream.create(cursor -> {
            fetchCount.incrementAndGet();
            int offset = cursor == null ? 0 : (int) cursor;
            return new PaginatedStream.Page<>(List.of("item-" + offset), offset + 1);
        }, 1, 2)) {
            stream.limit(2).toList();
        }

        int countAtClose = fetchCount.get();
        Thread.sleep(200);
        assertEquals(countAtClose, fetchCount.get(), "Producer should stop after stream close");
    }
}
