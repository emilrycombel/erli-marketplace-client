package pl.er.shoper.client.pagination;

import org.junit.jupiter.api.Test;
import pl.er.shoper.client.model.PagedResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PagedStreamTest {

    @Test
    void streamReturnsAllItems() {
        var items = PagedStream.<String>create(page -> switch (page) {
            case 1 -> new PagedResponse<>(4, 2, 1, List.of("a", "b"));
            case 2 -> new PagedResponse<>(4, 2, 2, List.of("c", "d"));
            default -> throw new IllegalArgumentException("Unexpected page: " + page);
        }).toList();

        assertEquals(List.of("a", "b", "c", "d"), items);
    }

    @Test
    void streamSupportsFiltering() {
        var items = PagedStream.<Integer>create(page -> switch (page) {
            case 1 -> new PagedResponse<>(6, 2, 1, List.of(1, 2, 3));
            case 2 -> new PagedResponse<>(6, 2, 2, List.of(4, 5, 6));
            default -> throw new IllegalArgumentException("Unexpected page: " + page);
        }).filter(n -> n % 2 == 0).toList();

        assertEquals(List.of(2, 4, 6), items);
    }

    @Test
    void streamSupportsMapping() {
        var items = PagedStream.<String>create(page ->
                new PagedResponse<>(2, 1, 1, List.of("hello", "world"))
        ).map(String::toUpperCase).toList();

        assertEquals(List.of("HELLO", "WORLD"), items);
    }

    @Test
    void streamHandlesEmptyResult() {
        var items = PagedStream.<String>create(page ->
                new PagedResponse<>(0, 0, 1, List.of())
        ).toList();

        assertEquals(List.of(), items);
    }
}
