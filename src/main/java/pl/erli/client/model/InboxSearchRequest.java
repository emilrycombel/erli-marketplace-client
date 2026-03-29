package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InboxSearchRequest(
        List<String> types
) {}
