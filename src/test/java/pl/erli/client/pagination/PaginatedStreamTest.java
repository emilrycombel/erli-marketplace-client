package pl.erli.client.pagination;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PaginatedStreamTest {

    @Test
    void shouldStreamAllItemsAcrossPages() {
        Stream<String> stream = PaginatedStream.create(cursor -> {
            int offset = cursor == null ? 0 : (int) cursor;
            List<String> items = new ArrayList<>();
            for (int i = offset; i < Math.min(offset + 3, 10); i++) {
                items.add("item-" + i);
            }
            int nextOffset = offset + 3;
            return new PaginatedStream.Page<>(items, nextOffset < 10 ? nextOffset : null);
        }, 2);

        List<String> result = stream.toList();

        assertEquals(10, result.size());
        assertEquals("item-0", result.getFirst());
        assertEquals("item-9", result.getLast());
    }

    @Test
    void shouldHandleEmptyFirstPage() {
        Stream<String> stream = PaginatedStream.create(
                cursor -> new PaginatedStream.Page<>(List.of(), null), 2);

        assertTrue(stream.toList().isEmpty());
    }

    @Test
    void shouldHandleSinglePage() {
        Stream<String> stream = PaginatedStream.create(
                cursor -> new PaginatedStream.Page<>(List.of("only-item"), null), 2);

        assertEquals(List.of("only-item"), stream.toList());
    }

    @Test
    void shouldFollowCursorPastShortPage() {
        Stream<String> stream = PaginatedStream.create(cursor -> {
            if (cursor == null) return new PaginatedStream.Page<>(List.of("a"), 1);
            if ((int) cursor == 1) return new PaginatedStream.Page<>(List.of("b", "c"), 2);
            return new PaginatedStream.Page<>(List.of(), null);
        }, 2);

        assertEquals(List.of("a", "b", "c"), stream.toList());
    }

    @Test
    void shouldNotFetchUntilTraversed() throws InterruptedException {
        AtomicInteger fetchCount = new AtomicInteger();

        try (Stream<String> stream = PaginatedStream.create(cursor -> {
            fetchCount.incrementAndGet();
            return new PaginatedStream.Page<>(List.of("x"), null);
        }, 2)) {
            Thread.sleep(100);
            assertEquals(0, fetchCount.get());
        }
    }

    @Test
    void shouldKeepAtMostPrefetchCountPagesAhead() {
        AtomicInteger fetchCount = new AtomicInteger();

        Stream<String> stream = PaginatedStream.create(cursor -> {
            fetchCount.incrementAndGet();
            int offset = cursor == null ? 0 : (int) cursor;
            return new PaginatedStream.Page<>(List.of("item-" + offset), offset + 1);
        }, 2);

        assertEquals(3, stream.limit(3).toList().size());
        // 3 consumed + 2 buffered + 1 fetched and waiting for room
        assertTrue(fetchCount.get() <= 6, "Fetched too many pages: " + fetchCount.get());
        stream.close();
    }

    @Test
    void shouldPropagateExceptionFromFetcher() {
        Stream<String> stream = PaginatedStream.create(cursor -> {
            if (cursor == null) {
                return new PaginatedStream.Page<>(List.of("first"), 1);
            }
            throw new RuntimeException("API failure");
        }, 2);

        RuntimeException ex = assertThrows(RuntimeException.class, stream::toList);
        assertEquals("API failure", ex.getMessage());
    }

    @Test
    void shouldStopProducerOnStreamClose() throws InterruptedException {
        AtomicInteger fetchCount = new AtomicInteger();

        try (Stream<String> stream = PaginatedStream.create(cursor -> {
            fetchCount.incrementAndGet();
            int offset = cursor == null ? 0 : (int) cursor;
            return new PaginatedStream.Page<>(List.of("item-" + offset), offset + 1);
        }, 2)) {
            assertEquals(2, stream.limit(2).toList().size());
        }

        Thread.sleep(100); // a fetch already in flight at close may still finish
        int afterClose = fetchCount.get();
        Thread.sleep(200);
        assertEquals(afterClose, fetchCount.get(), "Producer kept fetching after close");
    }

    @Test
    void closingFromAnotherThreadReleasesWaitingConsumer() throws Exception {
        Stream<String> stream = PaginatedStream.create(cursor -> {
            if (cursor == null) {
                return new PaginatedStream.Page<>(List.of("first"), 1);
            }
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return new PaginatedStream.Page<>(List.of(), null);
        }, 2);

        CompletableFuture<List<String>> consumer = CompletableFuture.supplyAsync(stream::toList);
        Thread.sleep(200);
        stream.close();

        assertEquals(List.of("first"), consumer.get(2, TimeUnit.SECONDS));
    }
}
