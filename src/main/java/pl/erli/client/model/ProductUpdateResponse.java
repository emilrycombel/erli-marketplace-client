package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductUpdateResponse(
        List<String> updatedFields
) {}
