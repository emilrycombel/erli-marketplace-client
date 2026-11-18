package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductSearchRequest(
        Pagination pagination,
        Filter filter,
        List<String> fields
) {}
