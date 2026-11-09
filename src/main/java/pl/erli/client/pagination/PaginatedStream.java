package pl.erli.client.pagination;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class PaginatedStream {

    private static final Logger log = LogManager.getLogger(PaginatedStream.class);

    private PaginatedStream() {}

    public record Page<T>(List<T> items, Object nextCursor) {}

    public static <T> Stream<T> create(Function<Object, Page<T>> pageFetcher,
                                        int pageSize, int prefetchCount) {
        var queue = new LinkedBlockingQueue<List<T>>(prefetchCount + 1);
        var sentinel = List.<T>of();
        var error = new AtomicReference<Throwable>();

        Thread producer = Thread.ofVirtual().name("erli-paginator").start(() -> {
            try {
                Object cursor = null;
                while (!Thread.currentThread().isInterrupted()) {
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
                error.set(e);
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
                            Throwable t = error.get();
                            if (t != null) {
                                throw t instanceof RuntimeException re ? re : new RuntimeException(t);
                            }
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
                    throw new NoSuchElementException();
                }
                return currentPage.next();
            }
        };

        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false)
                .onClose(() -> producer.interrupt());
    }
}
