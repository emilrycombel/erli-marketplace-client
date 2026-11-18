package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Category(
        Integer id,
        String name,
        Boolean leaf,
        List<BreadcrumbEntry> breadcrumb
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BreadcrumbEntry(Integer id, String name) {}
}
