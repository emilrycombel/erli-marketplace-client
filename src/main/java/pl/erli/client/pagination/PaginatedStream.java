package pl.erli.client.pagination;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Creates a lazy {@link Stream} over paginated API results with configurable prefetch.
 * Pages are loaded on demand with {@code prefetchCount} pages always fetched ahead
 * using virtual threads.
 */
public final class PaginatedStream {

    private static final Logger log = LogManager.getLogger(PaginatedStream.class);

    private PaginatedStream() {}

    /**
     * A page of results returned by the page fetcher.
     *
     * @param items the items on this page
     * @param nextCursor the cursor for fetching the next page, or null if this is the last page
     * @param <T> the item type
     */
    public record Page<T>(List<T> items, Object nextCursor) {}

    /**
     * Creates a lazy stream that fetches pages on demand with prefetch.
     *
     * @param pageFetcher function that takes a cursor (null for first page) and returns a Page
     * @param pageSize the page size (used to detect last page)
     * @param prefetchCount how many pages to fetch ahead (default 2)
     * @param <T> the item type
     * @return a Stream of all items across all pages
     */
    public static <T> Stream<T> create(Function<Object, Page<T>> pageFetcher,
                                        int pageSize, int prefetchCount) {
        var queue = new LinkedBlockingQueue<List<T>>(prefetchCount + 1);
        var sentinel = List.<T>of();

        Thread.ofVirtual().name("erli-paginator").start(() -> {
            try {
                Object cursor = null;
                while (true) {
                    Page<T> page = pageFetcher.apply(cursor);
                    List<T> items = page.items();
                    if (items == null || items.isEmpty()) {
                        break;
                    }
                    queue.put(items);
                    log.debug("Prefetched page with {} items, cursor={}", items.size(), page.nextCursor());
                    if (items.size() < pageSize || page.nextCursor() == null) {
                        break;
                    }
                    cursor = page.nextCursor();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Error during page prefetch", e);
            } finally {
                try {
                    queue.put(sentinel);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Iterator<T> iterator = new Iterator<>() {
            private Iterator<T> currentPage = null;
            private boolean done = false;

            @Override
            public boolean hasNext() {
                while (!done && (currentPage == null || !currentPage.hasNext())) {
                    try {
                        List<T> page = queue.take();
                        if (page.isEmpty()) {
                            done = true;
                            return false;
                        }
                        currentPage = page.iterator();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        done = true;
                        return false;
                    }
                }
                return !done && currentPage.hasNext();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                return currentPage.next();
            }
        };

        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }
}
