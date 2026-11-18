package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderItem(
        Integer id,
        String externalId,
        Integer quantity,
        Double weight,
        Integer unitPrice,
        Integer unitPriceBeforeRebate,
        String name,
        String slug,
        String ean,
        String sku,
        String taxRate
) {}
