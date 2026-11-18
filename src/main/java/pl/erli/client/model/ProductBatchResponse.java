package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductBatchResponse(
        String externalId,
        Integer status,
        Result result,
        ErrorDetail error
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(List<String> updatedFields) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorDetail(String name, String message, String failureType,
                               String polishMessage, Map<String, Object> payload) {}
}
