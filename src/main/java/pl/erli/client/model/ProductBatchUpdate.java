package pl.erli.client.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductBatchUpdate(
        String externalId,
        String name,
        Integer price,
        Integer stock,
        String status,
        Object description,
        String ean,
        String sku,
        List<ProductImage> images,
        DispatchTime dispatchTime,
        Map<String, Boolean> frozen
) {}
