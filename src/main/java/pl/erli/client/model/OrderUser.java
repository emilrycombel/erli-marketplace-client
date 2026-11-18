package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderUser(
        String email,
        DeliveryAddress deliveryAddress,
        Map<String, Object> invoiceAddress
) {}
