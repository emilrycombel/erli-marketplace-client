package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ShopResponse(
        Integer id,
        String name,
        Boolean active,
        Map<String, String> company,
        String externalMatchingPolicy
) {}
