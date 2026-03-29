package pl.er.shoper.client.model;

import java.util.List;

public record PagedResponse<T>(
        int count,
        int pages,
        int page,
        List<T> list
) {
}
