package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Pagination(
        String sortField,
        Object after,
        SortOrder order,
        Integer limit
) {
    public Pagination withAfter(Object after) {
        return new Pagination(sortField, after, order, limit);
    }
}
