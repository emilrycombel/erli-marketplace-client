package pl.erli.client.pagination;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class PaginatedStream {

    private static final Logger log = LogManager.getLogger(PaginatedStream.class);
    private static final long CLOSE_CHECK_MS = 100;

    private PaginatedStream() {}

    public record Page<T>(List<T> items, Object nextCursor) {}

    /**
     * The fetcher receives {@code null} for the first page and the previous page's
     * {@code nextCursor} afterwards. The stream ends on an empty page or a null cursor.
     */
    public static <T> Stream<T> create(Function<Object, Page<T>> pageFetcher, int prefetchCount) {
        var pages = new PrefetchingIterator<>(pageFetcher, Math.max(1, prefetchCount));
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(pages, Spliterator.ORDERED), false)
                .onClose(pages::close);
    }

    private static final class PrefetchingIterator<T> implements Iterator<T> {

        private static final List<?> END = List.of();

        private final Function<Object, Page<T>> pageFetcher;
        private final LinkedBlockingQueue<List<T>> queue;
        private final AtomicReference<Throwable> error = new AtomicReference<>();
        private volatile boolean closed;
        private Thread producer;
        private Iterator<T> current = Collections.emptyIterator();
        private boolean done;

        PrefetchingIterator(Function<Object, Page<T>> pageFetcher, int capacity) {
            this.pageFetcher = pageFetcher;
            this.queue = new LinkedBlockingQueue<>(capacity);
        }

        @Override
        public boolean hasNext() {
            if (producer == null) {
                startProducer();
            }
            while (!done && !current.hasNext()) {
                List<T> page = nextPage();
                if (page == null || page == END) {
                    done = true;
                    rethrowProducerError();
                } else {
                    current = page.iterator();
                }
            }
            return !done;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return current.next();
        }

        void close() {
            closed = true;
            Thread running;
            synchronized (this) {
                running = producer;
            }
            if (running != null) {
                running.interrupt();
            }
            queue.clear();
        }

        private synchronized void startProducer() {
            if (producer == null && !closed) {
                producer = Thread.ofVirtual().name("erli-paginator").start(this::produce);
            }
        }

        private List<T> nextPage() {
            try {
                while (!closed) {
                    List<T> page = queue.poll(CLOSE_CHECK_MS, TimeUnit.MILLISECONDS);
                    if (page != null) {
                        return page;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private void produce() {
            try {
                Object cursor = null;
                while (!closed) {
                    Page<T> page = pageFetcher.apply(cursor);
                    List<T> items = page.items();
                    if (items == null || items.isEmpty()) {
                        break;
                    }
                    queue.put(items);
                    log.debug("Prefetched page with {} items, next cursor={}", items.size(), page.nextCursor());
                    if (page.nextCursor() == null) {
                        break;
                    }
                    cursor = page.nextCursor();
                }
                if (!closed) {
                    queue.put((List<T>) END);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Throwable t) {
                if (!closed) {
                    log.error("Error while fetching page", t);
                    error.set(t);
                    try {
                        queue.put((List<T>) END);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        private void rethrowProducerError() {
            Throwable t = error.get();
            if (t == null || closed) {
                return;
            }
            if (t instanceof RuntimeException re) {
                throw re;
            }
            if (t instanceof Error err) {
                throw err;
            }
            throw new IllegalStateException(t);
        }
    }
}
