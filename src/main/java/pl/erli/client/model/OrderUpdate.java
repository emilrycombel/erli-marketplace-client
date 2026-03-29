package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderUpdate(
        Object deliveryTracking,
        String externalOrderId
) {}
