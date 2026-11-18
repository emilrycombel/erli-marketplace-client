package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderDelivery(
        String name,
        String typeId,
        Integer price,
        Integer cancelled,
        Boolean cod,
        String sourceMarket,
        String targetMarket,
        Map<String, Object> pickupPlace
) {}
