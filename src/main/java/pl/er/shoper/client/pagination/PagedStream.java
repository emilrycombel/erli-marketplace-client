package pl.er.shoper.client.pagination;

import pl.er.shoper.client.model.PagedResponse;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class PagedStream {

    private PagedStream() {
    }

    public static <T> Stream<T> create(Function<Integer, PagedResponse<T>> pageFetcher) {
        var iterator = new PrefetchingPageIterator<>(pageFetcher);
        var spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL);
        return StreamSupport.stream(spliterator, false)
                .onClose(iterator::close);
    }
}
