package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShopResponse(
        Integer id,
        String name,
        Boolean active,
        String externalMatchingPolicy
) {}
