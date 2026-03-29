package pl.er.shoper.client.pagination;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.er.shoper.client.model.PagedResponse;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class PrefetchingPageIterator<T> implements Iterator<T>, AutoCloseable {

    private static final Logger log = LogManager.getLogger(PrefetchingPageIterator.class);
    private static final int PREFETCH_AHEAD = 2;

    private final Function<Integer, PagedResponse<T>> pageFetcher;
    private final ExecutorService executor;
    private final Deque<T> buffer = new ArrayDeque<>();
    private final Deque<CompletableFuture<PagedResponse<T>>> pendingPages = new ArrayDeque<>();

    private int totalPages;
    private int nextPageToFetch = 2;
    private boolean closed = false;

    public PrefetchingPageIterator(Function<Integer, PagedResponse<T>> pageFetcher) {
        this.pageFetcher = pageFetcher;
        this.executor = Executors.newSingleThreadExecutor(r -> {
            var thread = new Thread(r, "shoper-prefetch");
            thread.setDaemon(true);
            return thread;
        });
        // Fetch page 1 synchronously to learn total pages
        var firstPage = pageFetcher.apply(1);
        totalPages = firstPage.pages();
        if (firstPage.list() != null) {
            buffer.addAll(firstPage.list());
        }
        log.debug("Loaded page 1/{}, {} items in buffer", totalPages, buffer.size());
        schedulePrefetch();
    }

    private void schedulePrefetch() {
        while (pendingPages.size() < PREFETCH_AHEAD && nextPageToFetch <= totalPages) {
            int pageToFetch = nextPageToFetch++;
            log.debug("Scheduling prefetch for page {}", pageToFetch);
            pendingPages.addLast(CompletableFuture.supplyAsync(() -> pageFetcher.apply(pageToFetch), executor));
        }
    }

    private void drainNextPage() {
        if (pendingPages.isEmpty()) {
            return;
        }
        var future = pendingPages.pollFirst();
        try {
            var response = future.join();
            if (response.list() != null) {
                buffer.addAll(response.list());
            }
            log.debug("Loaded page {}/{}, {} items in buffer", response.page(), totalPages, buffer.size());
            schedulePrefetch();
        } catch (Exception e) {
            log.error("Failed to fetch page", e);
            throw new RuntimeException("Failed to fetch page from Shoper API", e);
        }
    }

    @Override
    public boolean hasNext() {
        if (!buffer.isEmpty()) {
            return true;
        }
        if (!pendingPages.isEmpty()) {
            drainNextPage();
        }
        return !buffer.isEmpty();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        var item = buffer.pollFirst();
        if (buffer.isEmpty() && !pendingPages.isEmpty()) {
            drainNextPage();
        }
        return item;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            executor.shutdownNow();
        }
    }
}
