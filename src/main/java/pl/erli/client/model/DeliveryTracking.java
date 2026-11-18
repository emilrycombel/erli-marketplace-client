package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeliveryTracking(
        String status,
        String vendor,
        String trackingNumber,
        String trackingUrl
) {}
