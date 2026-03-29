package pl.er.shoper.client.model.offer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record OfferPriceUpdate(
        @JsonProperty("price") BigDecimal price
) {
}
